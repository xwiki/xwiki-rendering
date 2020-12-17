/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.test.integration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.jmock.XWikiComponentInitializer;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 * <p>Usage Example</p>
 * <pre>
 * <code>
 * {@literal @}RunWith(RenderingTestSuite.class)
 * public class IntegrationTests
 * {
 * }
 * </code>
 * </pre>
 * <p>It's also possible to get access to the underlying Component Manager used, for example in order to register
 * Mock implementations of components. For example:</p>
 * <pre>
 * <code>
 * {@literal @}RunWith(RenderingTestSuite.class)
 * {@literal @}AllComponents
 * public class IntegrationTests
 * {
 *     {@literal @}RenderingTestSuite.Initialized
 *     public void initialize(MockitoComponentManager componentManager)
 *     {
 *         // Init mocks here for example
 *     }
 * }
 * </code>
 * </pre>
 *
 * @version $Id$
 * @since 3.0RC1
 */
public class RenderingTestSuite extends Suite
{
    private static final TestDataGenerator GENERATOR = new TestDataGenerator();

    private static final String DEFAULT_PATTERN = ".*\\.test";

    private final Object klassInstance;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Initialized
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Scope
    {
        /**
         * @return the classpath prefix to search in
         */
        String value() default "";

        /**
         * @return the regex pattern to filter *.test files to execute
         */
        String pattern() default DEFAULT_PATTERN;
    }

    private class TestClassRunnerForParameters extends
        BlockJUnit4ClassRunner
    {
        private final int parameterSetNumber;

        private final MockitoComponentManager mockitoComponentManager = new MockitoComponentManager();

        private final XWikiComponentInitializer componentInitializer = new XWikiComponentInitializer();

        private final List<Object[]> parameterList;

        TestClassRunnerForParameters(Class<?> type,
            List<Object[]> parameterList, int i) throws InitializationError
        {
            super(type);
            this.parameterList = parameterList;
            this.parameterSetNumber = i;
        }

        @Override
        public Object createTest() throws Exception
        {
            return getTestClass().getOnlyConstructor().newInstance(
                computeParams());
        }

        private Object[] computeParams() throws Exception
        {
            // Add the Component Manager as the last parameter in order to pass it to the Test constructor
            // Remove the first parameter which is the test name and that is not needed in RenderingTest
            Object[] originalObjects = this.parameterList.get(this.parameterSetNumber);
            Object[] newObjects = new Object[originalObjects.length];
            System.arraycopy(originalObjects, 1, newObjects, 0, originalObjects.length - 1);
            newObjects[originalObjects.length - 1] = getComponentManager();
            return newObjects;
        }

        @Override
        protected String getName()
        {
            return (String) this.parameterList.get(this.parameterSetNumber)[0];
        }

        @Override
        protected String testName(final FrameworkMethod method)
        {
            return getName();
        }

        @Override
        protected void validateConstructor(List<Throwable> errors)
        {
            validateOnlyOneConstructor(errors);
        }

        @Override
        protected Statement classBlock(RunNotifier notifier)
        {
            return childrenInvoker(notifier);
        }

        /**
         * Initialize the Component Manager and call all methods annotated with {@link Initialized} in the suite,
         * before each test is executed, to ensure test isolation.
         */
        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier)
        {
            initializeComponentManager(notifier);

            // Check all methods for a ComponentManager annotation and call the found ones.
            try {
                for (Method klassMethod : RenderingTestSuite.this.klassInstance.getClass().getMethods()) {
                    Initialized componentManagerAnnotation = klassMethod.getAnnotation(Initialized.class);
                    if (componentManagerAnnotation != null) {
                        // Call it!
                        klassMethod.invoke(RenderingTestSuite.this.klassInstance, getComponentManager());
                    }
                }
            } catch (Exception e) {
                notifier.fireTestFailure(new Failure(getDescription(),
                    new RuntimeException("Failed to call Component Manager initialization method", e)));
            }

            try {
                super.runChild(method, notifier);
            } finally {
                shutdownComponentManager(notifier);
            }
        }

        private void initializeComponentManager(RunNotifier notifier)
        {
            try {
                if (isLegacyMode()) {
                    this.componentInitializer.initializeConfigurationSource();
                    this.componentInitializer.initializeExecution();
                } else {
                    this.mockitoComponentManager.initializeTest(RenderingTestSuite.this.klassInstance);
                    this.mockitoComponentManager.registerMemoryConfigurationSource();
                }
            } catch (Exception e) {
                notifier.fireTestFailure(new Failure(getDescription(),
                    new RuntimeException("Failed to initialize Component Manager", e)));
            }

        }

        private void shutdownComponentManager(RunNotifier notifier)
        {
            try {
                if (isLegacyMode()) {
                    this.componentInitializer.shutdown();
                } else {
                    this.mockitoComponentManager.shutdownTest();
                }
            } catch (Exception e) {
                notifier.fireTestFailure(new Failure(getDescription(),
                    new RuntimeException("Failed to shutdown Component Manager", e)));
            }
        }

        private ComponentManager getComponentManager() throws Exception
        {
            if (isLegacyMode()) {
                return this.componentInitializer.getComponentManager();
            } else {
                return this.mockitoComponentManager;
            }
        }

        private boolean isLegacyMode()
        {
            boolean isLegacyMode = true;
            for (Method klassMethod : RenderingTestSuite.this.klassInstance.getClass().getMethods()) {
                Initialized componentManagerAnnotation = klassMethod.getAnnotation(Initialized.class);
                if (componentManagerAnnotation != null) {
                    if (MockitoComponentManager.class.isAssignableFrom(klassMethod.getParameterTypes()[0])) {
                        isLegacyMode = false;
                    }
                    break;
                }
            }
            // If the class is using either @AllComponents or @ComponentList then consider we're not in legacy.
            if (isLegacyMode
                && (RenderingTestSuite.this.klassInstance.getClass().getAnnotation(AllComponents.class) != null
                || RenderingTestSuite.this.klassInstance.getClass().getAnnotation(ComponentList.class) != null))
            {
                isLegacyMode = false;
            }

            return isLegacyMode;
        }
    }

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public RenderingTestSuite(Class<?> klass) throws Throwable
    {
        super(klass, Collections.<Runner>emptyList());

        try {
            this.klassInstance = klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct instance of [" + klass.getName() + "]", e);
        }

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = klass.getAnnotation(Scope.class);
        String packagePrefix = "";
        String pattern = DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }
        List<Object[]> parametersList = (List<Object[]>) GENERATOR.generateData(packagePrefix, pattern);

        for (int i = 0; i < parametersList.size(); i++) {
            this.runners.add(new TestClassRunnerForParameters(RenderingTest.class, parametersList, i));
        }
    }

    @Override
    protected List<Runner> getChildren()
    {
        return this.runners;
    }

    /**
     * {@inheritDoc}
     *
     * We override this method so that the JUnit results are not displayed in a test hierarchy with a single test
     * result for each node (as it would be otherwise since RenderingTest has a single test method).
     */
    @Override
    public Description getDescription()
    {
        return Description.createSuiteDescription(getTestClass().getJavaClass());
    }
}

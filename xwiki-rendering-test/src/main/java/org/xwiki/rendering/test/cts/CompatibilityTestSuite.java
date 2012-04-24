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
package org.xwiki.rendering.test.cts;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.xwiki.test.XWikiComponentInitializer;

/**
 * Run all tests found in resources files located in the classpath. The algorithm is the following:
 * <ul>
 *   <li>Look for *.xdom resources in the classpath</li>
 *   <li>For each N.xdom resource found try to find a N.input resource. If found, parse the input in the syntax passed
 *       to this suite and verify it generates what's in the N.xdom resource. If no N.input is found, raise a warning
 *       in the logs.</li>
 *   <li>For each N.xdom resource found try to find a N.output resource. If found, render the N.xdom content
 *       using the syntax passed to this suite and verify it matches N.output. If no N.output is found, raise a warning
 *       in the logs.</li>
 *   <li>If a N.config file exists, use its content to customize the process. For example if it mentions to run Macro
 *       transformations, execute it after parsing the N.input file.</li>
 * </ul>
 *
 * <p>Usage Example</p>
 * {@code
 * @RunWith(CompatibilityTestSuite.class)
 * @Syntax("xwiki/2.0")
 * @Scope("simple")
 * public class IntegrationTests
 * {
 * }
 * }
 * <p>It's also possible to get access to the underlying Component Manager used, for example in order to register
 * Mock implementations of components. For example:</p>
 * {@code
 * @RunWith(CompatibilityTestSuite.class)
 * @Syntax("xwiki/2.0")
 * @Scope("simple")
 * public class IntegrationTests
 * {
 *     @Initialized
 *     public void initialize(ComponentManager componentManager)
 *     {
 *         // Init mocks here for example
 *     }
 * }
 * }
 *
 * @version $Id$
 * @since 4.1M1
 */
public class CompatibilityTestSuite extends Suite
{
    private static final TestDataGenerator GENERATOR = new TestDataGenerator();

    private final Object klassInstance;

    private class TestClassRunnerForParameters extends
        BlockJUnit4ClassRunner
    {
        private final XWikiComponentInitializer componentInitializer = new XWikiComponentInitializer();

        private final String testPrefix;

        private final TestData testData;

        private final String syntaxId;

        TestClassRunnerForParameters(Class<?> type, String syntaxId, String testPrefix, TestData testData)
            throws InitializationError
        {
            super(type);
            this.testPrefix = testPrefix;
            this.syntaxId = syntaxId;
            this.testData = testData;
        }

        @Override
        public Object createTest() throws Exception
        {
            return getTestClass().getOnlyConstructor().newInstance(
                this.testPrefix, this.syntaxId, this.testData, this.componentInitializer.getComponentManager());
        }

        @Override
        protected String getName()
        {
            return this.testPrefix + " [" + this.syntaxId + "]";
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
            try {
                this.componentInitializer.initializeConfigurationSource();
                this.componentInitializer.initializeExecution();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Component Manager", e);
            }

            // Check all methods for a ComponentManager annotation and call the found ones.
            try {
                for (Method klassMethod : klassInstance.getClass().getMethods()) {
                    Initialized componentManagerAnnotation = klassMethod.getAnnotation(Initialized.class);
                    if (componentManagerAnnotation != null) {
                        // Call it!
                        klassMethod.invoke(klassInstance, this.componentInitializer.getComponentManager());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to call Component Manager initialization method", e);
            }

            try {
                super.runChild(method, notifier);
            } finally {
                try {
                    this.componentInitializer.shutdown();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to shutdown Component Manager", e);
                }
            }
        }
    }

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public CompatibilityTestSuite(Class<?> klass) throws Throwable
    {
        super(RenderingTest.class, Collections.<Runner>emptyList());

        try {
            this.klassInstance = klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct instance of [" + klass.getName() + "]", e);
        }

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = klass.getAnnotation(Scope.class);
        String packagePrefix = "";
        String pattern = Scope.DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }

        // Get the specified Syntax from the Syntax annotation
        Syntax syntaxAnnotation = klass.getAnnotation(Syntax.class);
        if (syntaxAnnotation == null) {
            throw new RuntimeException("You must specify a Syntax using the @Syntax annotation");
        }
        String syntaxId = syntaxAnnotation.value();

        for (Map.Entry<String, TestData> entry :
            GENERATOR.generateTestData(syntaxId, packagePrefix, pattern).entrySet())
        {
            this.runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(),
                syntaxId, entry.getKey(), entry.getValue()));
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

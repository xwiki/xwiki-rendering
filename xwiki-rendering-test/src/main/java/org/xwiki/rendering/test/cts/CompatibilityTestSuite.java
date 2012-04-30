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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.internal.builders.IgnoredClassRunner;
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
 * Run all tests found in resources files located in the classpath, for a given Syntax.
 *
 * The algorithm is the following:
 * <ul>
 *   <li>Look for {@code cts/[scope]} resources in the classpath where {@code [scope]} represents the value of the
 *       {@code &#064;Scope} annotation prefixed by {@code cts\\.}. By default if no Scope annotation is defined,
 *       {@code .*\\.xml} is used, leading to a total regex of {@code cts\\..*\\.xml}. This is the regex that's used
 *       to look for resources in the classpath. For example the following test file would match:
 *       {@code cts/simple/bold/bold1.inout.xml}. We call these {@code CTS} resources.</li>
 *   <li>For each {@code CTS} resource found look for equivalent test input and output files for the tested Syntax.
 *       For example if we have {@code cts/simple/bold/bold1.inout.xml} then if the Syntax is {@code xwiki/2.0} look
 *       for {@code xwiki20/simple/bold/bold1.[in|out|inout].txt} test files. We call them {@code SYN} resources.
 *   </li>
 *   <li>For each {@code SYN IN} resource, parse it with the corresponding Syntax parser and render the generated XDOM
 *       with the CTS Renderer, and compare the results with the {@code CTS OUT} resource. Note that if no
 *       {@code SYN IN} resource is found generate a warning in the test logs.</li>
 *   <li>For each {@code SYN OUT} resource, parse the {@code CTS IN} resource with the CTS Syntax parser and render the
 *       generated XDOM with the Syntax Renderer, and compare the results with the {@code SYN OUT} resource.
 *       Note that if no {@code SYN OUT} resource is found generate a warning in the test logs.</li>
 * </ul>
 *
 * <p>
 * Usage Example
 * </p>
 * <pre><code>
 * &#064;RunWith(CompatibilityTestSuite.class)
 * &#064;Syntax("xwiki/2.0")
 * &#064;Scope("simple")
 * public class IntegrationTests
 * {
 * }
 * </code></pre>
 * <p>
 * It's also possible to get access to the underlying Component Manager used, for example in order to register
 * Mock implementations of components. For example:
 * </p>
 * <pre><code>
 * &#064;RunWith(CompatibilityTestSuite.class)
 * &#064;Syntax("xwiki/2.0")
 * &#064;Scope("simple")
 * public class IntegrationTests
 * {
 *     &#064;Initialized
 *     public void initialize(ComponentManager componentManager)
 *     {
 *         // Init mocks here for example
 *     }
 * }
 * </code></pre>
 *
 * @version $Id$
 * @since 4.1M1
 */
public class CompatibilityTestSuite extends Suite
{
    /**
     * Used to locate and parse Test Data.
     */
    private static final TestDataGenerator GENERATOR = new TestDataGenerator();

    /**
     * The Test instance (The Test instance is the class on which this Compatibility Test Suite is used).
     */
    private final Object testInstance;

    /**
     * Represents a Test Runner for a single Rendering Test to execute.
     */
    private class RenderingTestClassRunner extends BlockJUnit4ClassRunner
    {
        /**
         * Used to pass the Component Manager to the Rendering Test instance executing.
         */
        private final XWikiComponentInitializer componentInitializer = new XWikiComponentInitializer();

        /**
         * @see #RenderingTestClassRunner(Class, TestData)
         */
        private final TestData testData;

        /**
         * @param testClass the {@link RenderingTest} class
         * @param testData the Test Data, passed to the Rendering Test instance executing
         * @throws InitializationError if the {@link RenderingTest} isn't a valid JUnit Test class
         */
        RenderingTestClassRunner(Class<?> testClass, TestData testData) throws InitializationError
        {
            super(testClass);
            this.testData = testData;
        }

        @Override
        public Object createTest() throws Exception
        {
            return getTestClass().getOnlyConstructor().newInstance(
                this.testData, this.componentInitializer.getComponentManager());
        }

        @Override
        protected String getName()
        {
            return computeTestName(this.testData);
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
         * {@inheritDoc}
         *
         * <p>
         * Initialize the Component Manager and call all methods annotated with {@link Initialized} in the suite,
         * before each test is executed, to ensure test isolation.
         * </p>
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
                for (Method klassMethod : testInstance.getClass().getMethods()) {
                    Initialized componentManagerAnnotation = klassMethod.getAnnotation(Initialized.class);
                    if (componentManagerAnnotation != null) {
                        // Call it!
                        klassMethod.invoke(testInstance, this.componentInitializer.getComponentManager());
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

    /**
     * Used to ignore tests for which there is CTS data but no Syntax test data.
     */
    private class IgnoredRenderingTestClassRunner extends IgnoredClassRunner
    {
        /**
         * @see #IgnoredRenderingTestClassRunner(Class, TestData)
         */
        private final TestData testData;

        /**
         * @param testClass the {@link RenderingTest} class
         * @param testData the Test Data, passed to the Rendering Test instance executing
         */
        public IgnoredRenderingTestClassRunner(Class<?> testClass, TestData testData)
        {
            super(testClass);
            this.testData = testData;
        }

        @Override
        public Description getDescription()
        {
            return Description.createTestDescription(getTestClass().getJavaClass(), computeTestName(this.testData));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * We have one Test Runner per Syntax Test to execute, so that each test is reported individually and also to
     * provide test isolation.
     * </p>
     */
    private final List<Runner> runners = new ArrayList<Runner>();

    /**
     * Only called reflectively. Do not use programmatically.
     *
     * @param klass the test instance class on which this Test Suite is applied
     * @throws IOException if we fail to locate or load test data
     * @throws InitializationError if the {@link RenderingTest} isn't a valid JUnit Test class
     */
    public CompatibilityTestSuite(Class<?> klass) throws IOException, InitializationError
    {
        super(RenderingTest.class, Collections.<Runner>emptyList());

        try {
            this.testInstance = klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to construct instance of [%s]", klass.getName()), e);
        }

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = klass.getAnnotation(Scope.class);
        String packagePrefix = "";
        String pattern = Scope.DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = "cts." + scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }

        // Get the specified Syntax from the Syntax annotation
        Syntax syntaxAnnotation = klass.getAnnotation(Syntax.class);
        if (syntaxAnnotation == null) {
            throw new RuntimeException("You must specify a Syntax using the @Syntax annotation");
        }
        String syntaxId = syntaxAnnotation.value();

        for (TestData testData : GENERATOR.generateTestData(syntaxId, packagePrefix, pattern))
        {
            if (testData.syntaxData != null) {
                this.runners.add(new RenderingTestClassRunner(getTestClass().getJavaClass(), testData));
            } else {
                this.runners.add(new IgnoredRenderingTestClassRunner(getTestClass().getJavaClass(), testData));
            }
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
     * <p>
     * We override this method so that the JUnit results are not displayed in a test hierarchy with a single test
     * result for each node (as it would be otherwise since RenderingTest has a single test method).
     * </p>
     */
    @Override
    public Description getDescription()
    {
        return Description.createSuiteDescription(getTestClass().getJavaClass());
    }

    /**
     * Compute a test name based on the Test Data.
     *
     * @param testData the data from which to compute the test name
     * @return the computed test name (eg "cts/simple/bold/bold1(IN) [xwiki/2.0]")
     */
    private String computeTestName(TestData testData)
    {
        return String.format("%s(%s) [%s]", testData.prefix, testData.isSyntaxInputTest ? "IN" : "OUT",
            testData.syntaxId);
    }
}

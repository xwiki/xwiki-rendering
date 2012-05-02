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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.reflections.util.ClasspathHelper;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
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
    private static final TestDataParser PARSER = new TestDataParser();

    /**
     * The Test instance (The Test instance is the class on which this Compatibility Test Suite is used).
     */
    private final Object testInstance;

    /**
     * Used to find if there are Parser or Renderers for a given Syntax.
     */
    private final ComponentManager componentManager;

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
     * @throws Exception if we fail to locate or load test data, if the {@link RenderingTest} isn't a valid JUnit Test
     *         class or if we cannot locate the Component Manager
     */
    public CompatibilityTestSuite(Class<?> klass) throws Exception
    {
        super(RenderingTest.class, Collections.<Runner>emptyList());

        try {
            this.testInstance = klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to construct instance of [%s]", klass.getName()), e);
        }

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = klass.getAnnotation(Scope.class);
        String packagePrefix = "cts.";
        String pattern = Scope.DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = packagePrefix + scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }

        // Get the specified Syntax from the Syntax annotation
        Syntax syntaxAnnotation = klass.getAnnotation(Syntax.class);
        if (syntaxAnnotation == null) {
            throw new RuntimeException("You must specify a Syntax using the @Syntax annotation");
        }
        String syntaxId = syntaxAnnotation.value();

        // Initialize the Component Manager
        this.componentManager = new XWikiComponentInitializer().getComponentManager();

        // Note: We use the Reflections framework to find all ClassLoader URLs that contain the "cst" package.
        List<TestData> testDatas =
            PARSER.parseTestData(syntaxId, packagePrefix, pattern, ClasspathHelper.forPackage("cts"));

        for (TestData testData : testDatas) {
            if (!testData.isIgnored()) {
                if (testData.syntaxData != null) {
                    this.runners.add(new RenderingTestClassRunner(
                        this.testInstance, getTestClass().getJavaClass(), testData));
                } else {
                    if (ignoreTest(testData)) {
                        this.runners.add(new IgnoredRenderingTestClassRunner(getTestClass().getJavaClass(), testData));
                    }
                }
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
     * We ignore a test if there's no Parser or Renderer for that Syntax.
     *
     * @param testData the test data used to decide if the test is ignored or not
     * @return if the test should be ignored or false otherwise
     */
    private boolean ignoreTest(TestData testData)
    {
        boolean ignoreTest = false;
        if ((testData.isSyntaxInputTest && hasParserForSyntax(testData.syntaxId))
            || (!testData.isSyntaxInputTest && hasRendererForSyntax(testData.syntaxId)))
        {
            ignoreTest = true;
        }
        return ignoreTest;
    }

    /**
     * @param syntaxId the syntax for which to verify if there's a Parser
     * @return true if a Parser exists for the passed syntax, false otherwise
     */
    private boolean hasParserForSyntax(String syntaxId)
    {
        boolean hasParser = true;
        try {
            this.componentManager.getInstance(Parser.class, syntaxId);
        } catch (ComponentLookupException e) {
            hasParser = false;
        }
        return hasParser;
    }

    /**
     * @param syntaxId the syntax for which to verify if there's a Renderer
     * @return true if a Renderer exists for the passed syntax, false otherwise
     */
    private boolean hasRendererForSyntax(String syntaxId)
    {
        boolean hasRenderer = true;
        try {
            this.componentManager.getInstance(BlockRenderer.class, syntaxId);
        } catch (ComponentLookupException e) {
            hasRenderer = false;
        }
        return hasRenderer;
    }
}

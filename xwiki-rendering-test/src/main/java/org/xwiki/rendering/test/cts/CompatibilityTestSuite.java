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

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.test.jmock.XWikiComponentInitializer;

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
     * We have one Test Runner per Syntax Test to execute, so that each test is reported individually and also to
     * provide test isolation.
     */
    private final List<Runner> runners = new ArrayList<>();

    /**
     * Only called reflectively. Do not use programmatically.
     *
     * @param klass the test instance class on which this Test Suite is applied
     * @throws Exception if we fail to locate or load test data, if the {@link RenderingTest} isn't a valid JUnit Test
     *         class or if we cannot locate the Component Manager
     */
    public CompatibilityTestSuite(Class<?> klass) throws Exception
    {
        super(RenderingTest.class, Collections.emptyList());

        try {
            this.testInstance = klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to construct instance of [%s]", klass.getName()), e);
        }

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = klass.getAnnotation(Scope.class);
        String packageFilter = "";
        String pattern = Scope.DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packageFilter = scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }

        // Get the specified Syntax from the Syntax annotation
        Syntax syntaxAnnotation = klass.getAnnotation(Syntax.class);
        if (syntaxAnnotation == null) {
            throw new RuntimeException("You must specify a Syntax using the @Syntax annotation");
        }
        String syntaxId = syntaxAnnotation.value();
        String metadataSyntaxId = syntaxAnnotation.metadata();
        if (StringUtils.isEmpty(metadataSyntaxId)) {
            metadataSyntaxId = syntaxId;
        }

        // Initialize the Component Manager
        this.componentManager = new XWikiComponentInitializer().getComponentManager();

        // Note: We use the Reflections framework to find all ClassLoader URLs that contain the "cts" package.
        List<TestData> testDatas = PARSER.parseTestData(syntaxId, "cts", packageFilter, pattern);

        for (TestData testData : testDatas) {
            // The following cases can happen:
            // - There's no syntax test for the CTS test and there's no Parser/Renderer for that syntax: we don't add
            //   the test at all
            // - The test is configured to be not applicable: we don't add the test at all
            // - The test is configured as not working: we ignore it in JUnit with a cause message in the test
            //   description
            // - There's no syntax test for the CTS test but there's a Parser/Renderer for that syntax: we ignore it in
            //   JUnit with a cause message in the test description
            if (isApplicable(testData)) {
                if (testData.syntaxData != null && !testData.isFailingTest()) {
                    this.runners.add(new RenderingTestClassRunner(
                        this.testInstance, getTestClass().getJavaClass(), testData, metadataSyntaxId));
                } else {
                    this.runners.add(new IgnoredRenderingTestClassRunner(getTestClass().getJavaClass(), testData));
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
     * Verify if a test is applicable (ie it should be executed, even as ignored). A test is applicable if:
     * <ul>
     *   <li>it's not marked as not applicable</li>
     *   <li>it has a Syntax test</li>
     *   <li>it doesn't have a Syntax test but there's a Parser or Renderer for the Syntax</li>
     * </ul>
     *
     * @param testData the test data used to decide if the test is applicable or not
     * @return if the test should be executed or false otherwise
     */
    private boolean isApplicable(TestData testData)
    {
        boolean isApplicable;
        if (testData.isNotApplicable()) {
            isApplicable = false;
        } else {
            if (hasParserOrRenderer(testData)) {
                isApplicable = true;
            } else {
                isApplicable = false;
            }
        }
        return isApplicable;
    }

    /**
     * @param testData the test data used to decide if the test has a Parser or Renderer for it
     * @return true if there's a Parser or Renderer for the passed test data, false otherwise
     */
    private boolean hasParserOrRenderer(TestData testData)
    {
        return (testData.isSyntaxInputTest && hasParserForSyntax(testData.syntaxId))
            || (!testData.isSyntaxInputTest && hasRendererForSyntax(testData.syntaxId));
    }

    /**
     * @param syntaxId the syntax for which to verify if there's a Parser
     * @return true if a Parser exists for the passed syntax, false otherwise
     */
    private boolean hasParserForSyntax(String syntaxId)
    {
        return this.componentManager.hasComponent(Parser.class, syntaxId);
    }

    /**
     * @param syntaxId the syntax for which to verify if there's a Renderer
     * @return true if a Renderer exists for the passed syntax, false otherwise
     */
    private boolean hasRendererForSyntax(String syntaxId)
    {
        return this.componentManager.hasComponent(BlockRenderer.class, syntaxId);
    }
}

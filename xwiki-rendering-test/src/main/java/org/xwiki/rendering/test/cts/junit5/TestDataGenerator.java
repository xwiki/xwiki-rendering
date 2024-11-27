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
package org.xwiki.rendering.test.cts.junit5;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.test.cts.Scope;
import org.xwiki.rendering.test.cts.Syntax;
import org.xwiki.rendering.test.cts.TestData;
import org.xwiki.rendering.test.cts.TestDataParser;

/**
 * Generate all the tests that should be executed, in the form of a list of {@link TestData} elements.
 *
 * @version $Id$
 * @since 17.0.0RC1
 */
public class TestDataGenerator
{
    private ComponentManager componentManager;

    /**
     * @param componentManager the component manager to use to find if there's a parser or renderer for a given syntax
     */
    public TestDataGenerator(ComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    /**
     * @param syntaxId see {@link Syntax#value()}
     * @param packageFilter see {@link Scope#value()}
     * @param pattern see {@link Scope#pattern()}
     * @return the tests that should be executed, in the form of a list of {@link TestData} element.
     * @throws Exception if an error happens during the generation of test data
     */
    public List<TestData> generate(String syntaxId, String packageFilter, String pattern) throws Exception
    {
        // Note: We use the Reflections framework to find all ClassLoader URLs that contain the "cts" package.
        TestDataParser parser = new TestDataParser();
        List<TestData> testDataList = parser.parseTestData(syntaxId, "cts", packageFilter, pattern);

        List<TestData> filteredTestDataList = new ArrayList<>();
        for (TestData testData : testDataList) {
            // The following cases can happen:
            // - There's no syntax test for the CTS test and there's no Parser/Renderer for that syntax: we don't add
            //   the test at all
            // - The test is configured to be not applicable: we don't add the test at all
            // - The test is configured as not working: we ignore it in JUnit with a cause message in the test
            //   description. Note that this is implemented in RenderingTest since there's no way in JUnit5 to say that
            //   a dynamic test should be ignored. You need to execute it and then run an Assumption to abort it.
            //   See https://github.com/junit-team/junit5/issues/1439
            // - There's no syntax test for the CTS test but there's a Parser/Renderer for that syntax: we ignore it in
            //   JUnit with a cause message in the test description
            if (isApplicable(testData)) {
                filteredTestDataList.add(testData);
            }
        }

        return filteredTestDataList;
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

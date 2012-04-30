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

import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.ComparisonFailure;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * A generic JUnit Test used by {@link CompatibilityTestSuite} to run a single CTS test.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class RenderingTest
{
    /**
     * The Syntax id corresponding to the syntax in which the CTS tests are written in.
     */
    private static final String CTS_SYNTAX_ID = org.xwiki.rendering.syntax.Syntax.XDOMXML_CURRENT.toIdString();

    /**
     * @see RenderingTest
     */
    private TestData testData;

    /**
     * @see RenderingTest
     */
    private ComponentManager componentManager;

    /**
     * @param testData the data for a single test
     * @param componentManager see {@link #getComponentManager()}
     */
    public RenderingTest(TestData testData, ComponentManager componentManager)
    {
        this.testData = testData;
        this.componentManager = componentManager;
    }

    /**
     * Executes a single test.
     *
     * @throws Exception if an error happened during the test
     */
    @Test
    public void execute() throws Exception
    {
        if (this.testData.isSyntaxInputTest) {
            executeInputTest();
        } else {
            executeOutputTest();
        }
    }

    /**
     * Executes the test as an input test. This means:
     * <ul>
     *   <li>Parse the Syntax input</li>
     *   <li>Render the generated XDOM using the CTS Renderer</li>
     *   <li>Compare result with the CTS Output</li>
     * </ul>
     *
     * @throws Exception if an error happens, for example if a Parser or Renderer cannot be found
     */
    private void executeInputTest() throws Exception
    {
        executeTest(this.testData.syntaxData, this.testData.syntaxId, this.testData.ctsData, CTS_SYNTAX_ID);
    }

    /**
     * Executes the test as an output test. This means:
     * <ul>
     *   <li>Parse the CTS input</li>
     *   <li>Render the generated XDOM using the Syntax Renderer</li>
     *   <li>Compare result with the Syntax Output</li>
     * </ul>
     *
     * @throws Exception if an error happens, for example if a Parser or Renderer cannot be found
     */
    private void executeOutputTest() throws Exception
    {
        executeTest(this.testData.ctsData, CTS_SYNTAX_ID, this.testData.syntaxData, this.testData.syntaxId);
    }

    /**
     * Executes a test in a generic manner.
     *
     * @param inputData the input data to parse
     * @param inputSyntaxId the syntax in which the input data is written in
     * @param expectedOutputData the output data to compare to
     * @param outputSyntaxId the syntax in which the output data is written in
     * @throws Exception if an error happens, for example if a Parser or Renderer cannot be found
     */
    private void executeTest(String inputData, String inputSyntaxId, String expectedOutputData, String outputSyntaxId)
        throws Exception
    {
        Parser parser = getComponentManager().getInstance(Parser.class, inputSyntaxId);
        XDOM xdom = parser.parse(new StringReader(inputData));
        BlockRenderer renderer = getComponentManager().getInstance(BlockRenderer.class, outputSyntaxId);
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.render(xdom, printer);
        String result = printer.toString();
        try {
            if (isXMLSyntax(outputSyntaxId)) {
                assertExpectedResult(normalizeXMLContent(expectedOutputData, outputSyntaxId), result);
            } else {
                assertExpectedResult(expectedOutputData, result);
            }
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Failed to compare expected result with [%s]", result), e);
        }
    }

    /**
     * @param syntaxId the syntax to check
     * @return true if the passed syntax id represents an XML syntax
     */
    private boolean isXMLSyntax(String syntaxId)
    {
        return syntaxId.startsWith("xdom+xml") || syntaxId.startsWith("docbook");
    }

    /**
     * Normalize the expected XML output by reading and rendering the passed content. We do this so that we can easily
     * compare the expected result with the result of the test and not have to care about license comments, whitespaces,
     * newlines, etc.
     *
     * @param content the XML content to normalize
     * @param syntaxId the syntax in which the XML content is written in
     * @return the normalized content
     * @throws Exception if the XML parser or Renderer cannot be found
     */
    private String normalizeXMLContent(String content, String syntaxId) throws Exception
    {
        Parser parser = getComponentManager().getInstance(Parser.class, syntaxId);
        XDOM xdom = parser.parse(new StringReader(content));
        BlockRenderer renderer = getComponentManager().getInstance(BlockRenderer.class, syntaxId);
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.render(xdom, printer);
        return printer.toString();
    }

    /**
     * Compare the passed expected string with the passed result.
     * We support regexes for comparison usng the format: ${{{regex:...}}}. For example:
     *
     * <pre><code>
     * beginDocument
     * beginMacroMarkerStandalone [useravatar] [username=XWiki.UserNotExisting]
     * beginGroup [[class]=[xwikirenderingerror]]
     * onWord [Failed to execute the [useravatar] macro]
     * endGroup [[class]=[xwikirenderingerror]]
     * beginGroup [[class]=[xwikirenderingerrordescription hidden]]
     * onVerbatim [org.xwiki.rendering.macro.MacroExecutionException: User [XWiki.UserNotExisting]${{{regex:.*}}}]
     * endGroup [[class]=[xwikirenderingerrordescription hidden]]
     * endMacroMarkerStandalone [useravatar] [username=XWiki.UserNotExisting]
     * endDocument
     * </code></pre>
     *
     * @param expected the content to compare to
     * @param result the result from the test
     */
    private void assertExpectedResult(String expected, String result)
    {
        String escapedExpected = escapeRegexContent(expected);

        Pattern pattern = Pattern.compile(escapedExpected, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()) {
            throw new ComparisonFailure("", expected, result);
        }
    }

    /**
     * Escape the passed content by locating regex syntaxes inside and regex-escaping the text so that the whole
     * content can be matched using a Regex Matcher.
     *
     * @param content the content to escape
     * @return the escaped content
     */
    private String escapeRegexContent(String content)
    {
        StringBuilder builder = new StringBuilder();
        int pos = content.indexOf("${{{regex:");
        if (pos > -1) {
            builder.append(Pattern.quote(content.substring(0, pos)));
            // Find end of regex definition
            int pos2 = content.indexOf("}}}", pos + 10);
            if (pos2 == -1) {
                throw new RuntimeException("Invalid regex declaration: missing closing part }}}");
            }
            builder.append(content.substring(pos + 10, pos2));
            builder.append(escapeRegexContent(content.substring(pos2 + 3)));
        } else {
            builder.append(Pattern.quote(content));
        }
        return builder.toString();
    }

    /**
     * @return the component manager used to find Parser and Renderers
     */
    private ComponentManager getComponentManager()
    {
        return this.componentManager;
    }
}

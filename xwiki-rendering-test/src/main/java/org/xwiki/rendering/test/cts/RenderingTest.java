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
    private TestData testData;

    private ComponentManager componentManager;

    public RenderingTest(TestData testData, ComponentManager componentManager)
    {
        this.testData = testData;
        this.componentManager = componentManager;
    }

    @Test
    public void execute() throws Throwable
    {
        if (this.testData.isSyntaxInputTest) {
            executeInputTest();
        } else {
            executeOutputTest();
        }
    }

    private void executeInputTest() throws Throwable
    {
        if (!executeTest(this.testData.syntaxData, this.testData.syntaxId, this.testData.ctsData, "xdom+xml/1.0")) {
            System.out.println(String.format("No input found for [%s]", this.testData.prefix));
        }
    }

    private void executeOutputTest() throws Throwable
    {
        if (!executeTest(this.testData.ctsData, "xdom+xml/1.0", this.testData.syntaxData, this.testData.syntaxId)) {
            System.out.println(String.format("No output found for [%s]", this.testData.prefix));
        }
    }

    private boolean executeTest(String inputData, String inputSyntaxId, String expectedOutputData, String outputSyntaxId)
        throws Throwable
    {
        boolean executed = false;
        if (this.testData.syntaxData != null) {
            Parser parser = getComponentManager().getInstance(Parser.class, inputSyntaxId);
            XDOM xdom = parser.parse(new StringReader(inputData));
            BlockRenderer renderer = getComponentManager().getInstance(BlockRenderer.class, outputSyntaxId);
            WikiPrinter printer = new DefaultWikiPrinter();
            renderer.render(xdom, printer);
            if (outputSyntaxId.contains("xdom+xml")) {
                assertExpectedResult(normalizeXDOMXMLContent(expectedOutputData), printer.toString());
            } else {
                assertExpectedResult(expectedOutputData, printer.toString());
            }
            executed = true;
        }
        return executed;
    }

    private String normalizeXDOMXMLContent(String content) throws Exception
    {
        Parser parser = getComponentManager().getInstance(Parser.class, "xdom+xml/1.0");
        XDOM xdom = parser.parse(new StringReader(content));
        BlockRenderer renderer = getComponentManager().getInstance(BlockRenderer.class, "xdom+xml/1.0");
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.render(xdom, printer);
        return printer.toString();
    }

    /**
     * Compare the passed expected string with the passed result.
     * We support regexes for comparison usng the format: ${{{regex:...}}}. For example:
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
     */
    private void assertExpectedResult(String expected, String result) throws Exception
    {
        StringBuilder builder = new StringBuilder();
        normalizeExpectedValue(builder, expected);

        Pattern pattern = Pattern.compile(builder.toString(), Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result);
        if (!matcher.matches()) {
            throw new ComparisonFailure("", expected, result);
        }
    }

    private void normalizeExpectedValue(StringBuilder builder, String expected)
    {
        int pos = expected.indexOf("${{{regex:");
        if (pos > -1) {
            builder.append(Pattern.quote(expected.substring(0, pos)));
            // Find end of regex definition
            int pos2 = expected.indexOf("}}}", pos + 10);
            if (pos2 == -1) {
                throw new RuntimeException("Invalid regex declaration: missing closing part }}}");
            }
            builder.append(expected.substring(pos + 10, pos2));
            normalizeExpectedValue(builder, expected.substring(pos2 + 3));
        } else {
            builder.append(Pattern.quote(expected));
        }
    }

    private ComponentManager getComponentManager() throws Exception
    {
        return this.componentManager;
    }
}

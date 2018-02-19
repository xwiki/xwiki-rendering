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
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.velocity.internal.log.SLF4JLogChute;
import org.xwiki.xml.XMLUtils;

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
     * The Velocity Engine we use to evaluate the test data. We do this to allow Velocity scripts to be added to test
     * data.
     */
    private static final VelocityEngine VELOCITY_ENGINE = new VelocityEngine();

    static {
        // Make velocity use SLF4J as logger
        VELOCITY_ENGINE.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());
    }

    /**
     * Symbols to start a special syntax block. For example: <code>${{{regex:...}}}</code> or
     * <code>${{{velocity:...}}}</code>
     */
    private static final String SPECIAL_SYNTAX_START = "${{{";

    /**
     * Symbols to close a special syntax block. For example: <code>${{{regex:...}}}</code> or
     * <code>${{{velocity:...}}}</code>
     */
    private static final String SPECIAL_SYNTAX_END = "}}}";

    /**
     * @see RenderingTest
     */
    private TestData testData;

    /**
     * @see RenderingTest
     */
    private ComponentManager componentManager;

    /**
     * @see RenderingTest
     */
    private String metadataSyntaxId;

    /**
     * @param testData the data for a single test
     * @param metadataSyntaxId the Syntax id of the syntax used as Metadata in the generated XDOM for parsers
     * @param componentManager see {@link #getComponentManager()}
     */
    public RenderingTest(TestData testData, String metadataSyntaxId, ComponentManager componentManager)
    {
        this.testData = testData;
        this.componentManager = componentManager;
        this.metadataSyntaxId = metadataSyntaxId;
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
     * <li>Parse the Syntax input</li>
     * <li>Render the generated XDOM using the CTS Renderer</li>
     * <li>Compare result with the CTS Output</li>
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
     * <li>Parse the CTS input</li>
     * <li>Render the generated XDOM using the Syntax Renderer</li>
     * <li>Compare result with the Syntax Output</li>
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
        // Get the syntax from the parser/renderer to be sure to get the right display name (Syntax#valueOf() may not
        // know it)
        org.xwiki.rendering.syntax.Syntax inputSyntax = getInputSyntax(inputSyntaxId, outputSyntaxId);
        org.xwiki.rendering.syntax.Syntax expectedSyntax = getOutputSyntax(inputSyntaxId, outputSyntaxId);

        org.xwiki.rendering.syntax.Syntax validatedSyntax;
        if (inputSyntax.toIdString().equals(this.metadataSyntaxId)) {
            validatedSyntax = inputSyntax;
        } else {
            validatedSyntax = expectedSyntax;
        }

        String evaluatedInputData = evaluateContent(inputData, validatedSyntax);
        String evaluatedOutputData = evaluateContent(expectedOutputData, validatedSyntax);

        String result = convert(evaluatedInputData, inputSyntax.toIdString(), expectedSyntax.toIdString());
        try {
            if (isXMLSyntax(outputSyntaxId)) {
                assertExpectedResult(
                    XMLUtils.formatXMLContent(normalizeXMLContent(evaluatedOutputData, outputSyntaxId)),
                    XMLUtils.formatXMLContent(result));
            } else {
                assertExpectedResult(evaluatedOutputData, result);
            }
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Failed to compare expected result with [%s]", result), e);
        }
    }

    private boolean isStreamingTest(String inputSyntaxId, String outputSyntaxId)
    {
        return getComponentManager().hasComponent(StreamParser.class, inputSyntaxId)
            && getComponentManager().hasComponent(PrintRendererFactory.class, outputSyntaxId);
    }

    private org.xwiki.rendering.syntax.Syntax getInputSyntax(String inputSyntaxId, String outputSyntaxId)
        throws Exception
    {
        org.xwiki.rendering.syntax.Syntax syntax;
        if (isStreamingTest(inputSyntaxId, outputSyntaxId)) {
            StreamParser parser = getComponentManager().getInstance(StreamParser.class, inputSyntaxId);
            syntax = parser.getSyntax();
        } else {
            Parser parser = getComponentManager().getInstance(Parser.class, inputSyntaxId);
            syntax = parser.getSyntax();
        }
        return syntax;
    }

    private org.xwiki.rendering.syntax.Syntax getOutputSyntax(String inputSyntaxId, String outputSyntaxId)
        throws Exception
    {
        org.xwiki.rendering.syntax.Syntax syntax;
        if (isStreamingTest(inputSyntaxId, outputSyntaxId)) {
            PrintRendererFactory rendererFactory =
                getComponentManager().getInstance(PrintRendererFactory.class, outputSyntaxId);
            syntax = rendererFactory.getSyntax();
        } else {
            syntax = org.xwiki.rendering.syntax.Syntax.valueOf(outputSyntaxId);
        }
        return syntax;
    }

    /**
     * @param syntaxId the syntax to check
     * @return true if the passed syntax id represents an XML syntax
     */
    private boolean isXMLSyntax(String syntaxId)
    {
        return syntaxId.startsWith("xdom+xml") || syntaxId.startsWith("docbook");
    }

    private String convert(String source, String sourceSyntaxId, String targetSyntaxId) throws Exception
    {
        String result;
        if (isStreamingTest(sourceSyntaxId, targetSyntaxId)) {
            StreamParser parser = getComponentManager().getInstance(StreamParser.class, sourceSyntaxId);
            PrintRendererFactory rendererFactory =
                getComponentManager().getInstance(PrintRendererFactory.class, targetSyntaxId);
            result = convert(source, parser, rendererFactory);
        } else {
            Parser parser = getComponentManager().getInstance(Parser.class, sourceSyntaxId);
            BlockRenderer blockRenderer = getComponentManager().getInstance(BlockRenderer.class, targetSyntaxId);
            result = convert(source, parser, blockRenderer);
        }
        return result;
    }

    private String convert(String source, StreamParser parser, PrintRendererFactory rendererFactory) throws Exception
    {
        PrintRenderer renderer = rendererFactory.createRenderer(new DefaultWikiPrinter());
        parser.parse(new StringReader(source), renderer);
        return renderer.getPrinter().toString();
    }

    private String convert(String source, Parser parser, BlockRenderer blockRenderer) throws Exception
    {
        XDOM xdom = parser.parse(new StringReader(source));
        WikiPrinter printer = new DefaultWikiPrinter();
        blockRenderer.render(xdom, printer);
        return printer.toString();
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
        return convert(content, syntaxId, syntaxId);
    }

    /**
     * Run Velocity when the <code>${{velocity:...}}}</code> syntax is used. The {@code $syntax} variable is replaced by
     * the test Syntax object.
     *
     * @param content the content to evaluate
     * @return the evaluated content
     */
    private String evaluateContent(String content, org.xwiki.rendering.syntax.Syntax syntax)
    {
        StringBuilder builder = new StringBuilder();
        String fullSpecialSyntaxStart = String.format("%svelocity:", SPECIAL_SYNTAX_START);
        int pos = content.indexOf(fullSpecialSyntaxStart);
        if (pos > -1) {
            builder.append(content.substring(0, pos));
            // Find end of velocity definition
            int pos2 = content.indexOf(SPECIAL_SYNTAX_END, pos + fullSpecialSyntaxStart.length());
            if (pos2 == -1) {
                throw new RuntimeException("Invalid velocity declaration: missing closing part " + SPECIAL_SYNTAX_END);
            }

            VelocityContext context = new VelocityContext();
            context.put("syntax", syntax);
            StringWriter writer = new StringWriter();
            VELOCITY_ENGINE.evaluate(context, writer, "Rendering CTS",
                content.substring(pos + fullSpecialSyntaxStart.length(), pos2));
            builder.append(writer.toString());

            builder.append(evaluateContent(content.substring(pos2 + SPECIAL_SYNTAX_END.length()), syntax));
        } else {
            builder.append(content);
        }
        return builder.toString();
    }

    /**
     * Compare the passed expected string with the passed result. We support regexes for comparison using the format:
     * ${{{regex:...}}}. For example:
     *
     * <pre>
     * <code>
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
     * </code>
     * </pre>
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
     * Escape the passed content by locating regex syntaxes inside and regex-escaping the text so that the whole content
     * can be matched using a Regex Matcher.
     *
     * @param content the content to escape
     * @return the escaped content
     */
    private String escapeRegexContent(String content)
    {
        StringBuilder builder = new StringBuilder();
        String fullSpecialSyntaxStart = String.format("%sregex:", SPECIAL_SYNTAX_START);
        int pos = content.indexOf(fullSpecialSyntaxStart);
        if (pos > -1) {
            builder.append(Pattern.quote(content.substring(0, pos)));
            // Find end of regex definition
            int pos2 = content.lastIndexOf(SPECIAL_SYNTAX_END);
            if (pos2 == -1) {
                throw new RuntimeException("Invalid regex declaration: missing closing part " + SPECIAL_SYNTAX_END);
            }
            builder.append(content.substring(pos + fullSpecialSyntaxStart.length(), pos2));
            builder.append(escapeRegexContent(content.substring(pos2 + SPECIAL_SYNTAX_END.length())));
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

    /**
     * Create a Syntax object from a Syntax id string.
     *
     * @param syntaxId the id of the Syntax to create
     * @return the Syntax object
     */
    private org.xwiki.rendering.syntax.Syntax parseSyntax(String syntaxId)
    {
        try {
            return org.xwiki.rendering.syntax.Syntax.valueOf(syntaxId);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to parse Syntax [%s]", syntaxId), e);
        }
    }
}

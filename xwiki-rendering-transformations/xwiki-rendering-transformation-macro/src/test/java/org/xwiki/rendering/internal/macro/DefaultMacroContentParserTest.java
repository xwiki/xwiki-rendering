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
package org.xwiki.rendering.internal.macro;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroPreparationException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ComponentTest
class DefaultMacroContentParserTest
{
    private static final Syntax TEST_SYNTAX_1 = new Syntax(new SyntaxType("test", "test"), "1.0");

    private static final Syntax TEST_SYNTAX_2 = new Syntax(new SyntaxType("test", "test"), "2.0");

    @MockComponent(classToMock = MutableRenderingContext.class)
    private RenderingContext renderingContext;

    @MockComponent
    @Named("test/1.0")
    private Parser mockParser1;

    @MockComponent
    @Named("test/2.0")
    private Parser mockParser2;

    @InjectMockComponents
    private DefaultMacroContentParser macroContentParser;

    private MacroTransformationContext macroContext = new MacroTransformationContext();

    @BeforeEach
    public void beforeEach() throws Exception
    {
        this.macroContext = new MacroTransformationContext();
        this.macroContext.setSyntax(TEST_SYNTAX_1);
    }

    // Tests

    @Test
    void parseInline() throws Exception
    {
        MacroBlock macroBlock = new MacroBlock("id", Map.of(), "content", false);
        this.macroContext.setCurrentMacroBlock(macroBlock);

        when(this.mockParser1.parse(any(Reader.class))).thenReturn(
            new XDOM(Arrays.<Block>asList(new ParagraphBlock(Arrays.<Block>asList(new WordBlock("word"))))));

        assertEquals(new XDOM(Arrays.<Block>asList(new WordBlock("word"))),
            this.macroContentParser.parse(macroBlock.getContent(), this.macroContext, false, true));
    }

    @Test
    void parseInlineWithStandaloneMacro() throws Exception
    {
        when(this.mockParser1.parse(any(Reader.class)))
            .thenReturn(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, false))));

        assertEquals(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, true))),
            this.macroContentParser.parse("content", this.macroContext, false, true));
    }

    @Test
    void parseInlineWithStandaloneMacroWithTransformations() throws Exception
    {
        when(this.mockParser1.parse(any(Reader.class)))
            .thenReturn(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, false))));

        this.macroContext.setTransformation(mock(Transformation.class));

        this.macroContentParser.parse("content", this.macroContext, true, true);

        verify((MutableRenderingContext) this.renderingContext).transformInContext(any(),
            argThat(context -> !context.isRestricted()),
            eq(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, true)))));
    }

    @Test
    void parseInlineWithStandaloneMacroWithRestrictedTransformations() throws Exception
    {
        when(this.mockParser1.parse(any(Reader.class)))
            .thenReturn(
                new XDOM(Collections.singletonList(new MacroBlock("macro", Collections.emptyMap(), null, false))));

        this.macroContext.setTransformation(mock(Transformation.class));
        this.macroContext.getTransformationContext().setRestricted(true);

        this.macroContentParser.parse("content", this.macroContext, true, true);

        verify((MutableRenderingContext) this.renderingContext).transformInContext(
            any(), argThat(TransformationContext::isRestricted),
            eq(new XDOM(Collections.singletonList(new MacroBlock("macro", Collections.emptyMap(), null, true))))
        );
    }

    @Test
    void parseWithCustomSyntax() throws Exception
    {
        when(this.mockParser2.parse(any(Reader.class))).thenReturn(
            new XDOM(Arrays.<Block>asList(new ParagraphBlock(Arrays.<Block>asList(new WordBlock("word2"))))));

        assertEquals(new XDOM(Arrays.<Block>asList(new WordBlock("word2"))),
            this.macroContentParser.parse("content", TEST_SYNTAX_2, this.macroContext, false, null, true));
    }

    @Test
    void prepareContentWikiWithNullContent() throws MacroPreparationException
    {
        MacroBlock macroBlock = new MacroBlock("id", Map.of(), false);

        this.macroContentParser.prepareContentWiki(macroBlock);

        assertNull(macroBlock.getAttribute(MacroContentParser.ATTRIBUTE_PREPARE_CONTENT_XDOM));
    }

    @Test
    void prepareContentWikiWithNoSyntax()
    {
        MacroBlock macroBlock = new MacroBlock("id", Map.of(), "content", false);

        assertThrows(MacroPreparationException.class, () -> this.macroContentParser.prepareContentWiki(macroBlock),
            "No syntax provided to parse the content");
    }

    @Test
    void parsePreparedContent() throws MacroPreparationException, ParseException, MacroExecutionException
    {
        MacroBlock macroBlock = new MacroBlock("id", Map.of(), "content", false);
        XDOM xdom = new XDOM(List.of(macroBlock));
        xdom.getMetaData().addMetaData(MetaData.SYNTAX, TEST_SYNTAX_1);
        this.macroContext.setCurrentMacroBlock(macroBlock);

        XDOM parsedXDOM1 = new XDOM(List.of(new WordBlock("1")));
        parsedXDOM1.getMetaData().addMetaData(MetaData.SYNTAX, TEST_SYNTAX_1);
        when(this.mockParser1.parse(any(), any())).thenReturn(parsedXDOM1);
        XDOM parsedXDOM2 = new XDOM(List.of(new WordBlock("2")));
        parsedXDOM2.getMetaData().addMetaData(MetaData.SYNTAX, TEST_SYNTAX_2);
        when(this.mockParser2.parse(any(), any())).thenReturn(parsedXDOM2);

        // Prepare without custom syntax
        this.macroContentParser.prepareContentWiki(macroBlock);

        XDOM preparedContent1 = (XDOM) macroBlock.getAttribute(MacroContentParser.ATTRIBUTE_PREPARE_CONTENT_XDOM);

        assertEquals(parsedXDOM1, preparedContent1);

        // Parse without custom syntax
        assertEquals(preparedContent1, this.macroContentParser.parse(macroBlock.getContent(), TEST_SYNTAX_1,
            this.macroContext, false, null, macroBlock.isInline()));

        // Prepare with custom syntax
        this.macroContentParser.prepareContentWiki(macroBlock, TEST_SYNTAX_2);

        XDOM preparedContent2 = (XDOM) macroBlock.getAttribute(MacroContentParser.ATTRIBUTE_PREPARE_CONTENT_XDOM);

        assertEquals(parsedXDOM2, preparedContent2);

        // Parse with custom syntax
        assertEquals(preparedContent2, this.macroContentParser.parse(macroBlock.getContent(), TEST_SYNTAX_2,
            this.macroContext, false, null, macroBlock.isInline()));
    }
}

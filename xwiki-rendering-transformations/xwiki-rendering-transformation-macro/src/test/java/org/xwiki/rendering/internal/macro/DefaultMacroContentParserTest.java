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

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ComponentTest
@ComponentList({DefaultMacroContentParser.class})
class DefaultMacroContentParserTest
{
    @MockComponent(classToMock = MutableRenderingContext.class)
    private RenderingContext renderingContext;

    @MockComponent
    @Named("test/1.0")
    private Parser mockParser;

    @InjectMockComponents
    private DefaultMacroContentParser macroContentParser;

    private MacroTransformationContext macroContext = new MacroTransformationContext();

    @BeforeEach
    public void beforeEach() throws Exception
    {
        Syntax testSyntax = new Syntax(new SyntaxType("test", "test"), "1.0");

        this.macroContext = new MacroTransformationContext();
        this.macroContext.setSyntax(testSyntax);
    }

    // Tests

    @Test
    void parseInline() throws Exception
    {
        when(this.mockParser.parse(any(Reader.class))).thenReturn(
            new XDOM(Arrays.<Block>asList(new ParagraphBlock(Arrays.<Block>asList(new WordBlock("word"))))));

        assertEquals(new XDOM(Arrays.<Block>asList(new WordBlock("word"))),
            this.macroContentParser.parse("content", this.macroContext, false, true));
    }

    @Test
    void parseInlineWithStandaloneMacro() throws Exception
    {
        when(this.mockParser.parse(any(Reader.class)))
            .thenReturn(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, false))));

        assertEquals(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, true))),
            this.macroContentParser.parse("content", this.macroContext, false, true));
    }

    @Test
    void parseInlineWithStandaloneMacroWithTransformations() throws Exception
    {
        when(this.mockParser.parse(any(Reader.class)))
            .thenReturn(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, false))));

        this.macroContext.setTransformation(mock(Transformation.class));

        this.macroContentParser.parse("content", this.macroContext, true, true);

        verify((MutableRenderingContext) this.renderingContext).transformInContext(any(), any(),
            eq(new XDOM(Arrays.<Block>asList(new MacroBlock("macro", Collections.emptyMap(), null, true)))));
    }
}

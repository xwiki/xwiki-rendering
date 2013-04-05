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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

@ComponentList({DefaultMacroContentParser.class})
public class DefaultMacroContentParserTest
{
    @Rule
    public final MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private MacroTransformationContext macroContext = new MacroTransformationContext();

    private Parser mockParser;

    private MacroContentParser macroContentParser;

    @Before
    public void setUp() throws Exception
    {
        Syntax testSyntax = new Syntax(new SyntaxType("test", "test"), "1.0");

        this.macroContext = new MacroTransformationContext();
        this.macroContext.setSyntax(testSyntax);

        this.mockParser = this.componentManager.registerMockComponent(Parser.class, testSyntax.toIdString());

        this.macroContentParser = this.componentManager.getInstance(MacroContentParser.class);
    }

    // Tests

    @Test
    public void testParseInline() throws Exception
    {
        when(mockParser.parse(any(Reader.class))).thenReturn(
            new XDOM(Arrays.<Block> asList(new ParagraphBlock(Arrays.<Block> asList(new WordBlock("word"))))));

        Assert.assertEquals(new XDOM(Arrays.<Block> asList(new WordBlock("word"))),
            this.macroContentParser.parse("content", this.macroContext, false, true));
    }

    @Test
    public void testParseInlineWithStandaloneMacro() throws Exception
    {
        when(mockParser.parse(any(Reader.class))).thenReturn(
            new XDOM(Arrays.<Block> asList(new MacroBlock("macro", Collections.EMPTY_MAP, null, false))));

        Assert.assertEquals(
            new XDOM(Arrays.<Block> asList(new MacroBlock("macro", Collections.EMPTY_MAP, null, true))),
            this.macroContentParser.parse("content", this.macroContext, false, true));
    }
}

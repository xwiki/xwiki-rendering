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
package org.xwiki.rendering.macro.figure;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.FigureCaptionBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.internal.macro.figure.DefaultFigureTypeRecognizer;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit and integration tests for {@link DefaultFigureTypeRecognizer}.
 *
 * @version $Id$
 * @since 10.2
 */
@ComponentTest
@AllComponents
class DefaultFigureTypeRecognizerTest
{
    private static final String FIGURE_CAPTION = "figureCaption";

    @InjectMockComponents
    private DefaultFigureTypeRecognizer figureTypeRecognizer;

    @Inject
    @Named("macro")
    private Transformation macroTransformation;

    @Inject
    @Named("xwiki/2.0")
    private Parser xwikiParser;

    @Test
    void isTableWithRealParser() throws ParseException, TransformationException
    {
        String testInput = "{{figure}}\n"
            + "| Some | Table\n"
            + "| With two | rows\n"
            + "\n"
            + "{{figureCaption}}caption{{/figureCaption}}\n"
            + "{{/figure}}\n";

        XDOM xdom = this.xwikiParser.parse(new StringReader(testInput));
        this.macroTransformation.transform(xdom, new TransformationContext());
        FigureBlock figureBlock = xdom.getFirstBlock(new ClassBlockMatcher(FigureBlock.class), Block.Axes.DESCENDANT);
        assertTrue(this.figureTypeRecognizer.isTable(figureBlock));
    }

    @Test
    void isTableWhenTableIsNestedInsideGroups() throws ParseException, TransformationException
    {
        String testInput = "{{figure}}\n"
            + "{{figureCaption}}caption{{/figureCaption}}\n"
            + "(% class=\"a\" %) (((\n"
            + "(% class=\"b\" %) (((\n"
            + "(% class=\"c\" %) (((\n"
            + "|A|a\n"
            + "|B|b\n"
            + ")))\n"
            + ")))\n"
            + ")))\n"
            + "{{/figure}}";

        XDOM xdom = this.xwikiParser.parse(new StringReader(testInput));
        this.macroTransformation.transform(xdom, new TransformationContext());
        FigureBlock figureBlock = xdom.getFirstBlock(new ClassBlockMatcher(FigureBlock.class), Block.Axes.DESCENDANT);
        assertTrue(this.figureTypeRecognizer.isTable(figureBlock));
    }

    @Test
    void isTableWhenNoCaption()
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList())));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWhenCaptionLast()
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList()),
            createMacroMarkerBlock(FIGURE_CAPTION, Collections.emptyList())));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWhenCaptionFirst()
    {
        FigureBlock fb = new FigureBlock(blocks(
            createMacroMarkerBlock(FIGURE_CAPTION, Collections.emptyList()),
            new TableBlock(Collections.emptyList())));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWhenTwoTableBlocks()
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList()),
            new TableBlock(Collections.emptyList()),
            createMacroMarkerBlock(FIGURE_CAPTION, Collections.emptyList())));
        assertFalse(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWhenFigureCaptionBlockInMacroMarkerBlock()
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList()),
            createMacroMarkerBlock(blocks(createMacroMarkerBlock(FIGURE_CAPTION, Collections.emptyList())))));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWithTableInsideNestedMacroMarkerBlocks()
    {
        FigureBlock fb = new FigureBlock(blocks(
            createMacroMarkerBlock(blocks(createMacroMarkerBlock(
                blocks(new TableBlock(Collections.emptyList()))))),
            createMacroMarkerBlock(FIGURE_CAPTION, Collections.emptyList())));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWhenMetaDataBlock()
    {
        FigureBlock fb = new FigureBlock(blocks(new MetaDataBlock(
            blocks(new TableBlock(Collections.emptyList())))));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    @Test
    void isTableWhenNoMacroMarkerBlockAroundCaption()
    {
        // Note: This is what happens for wikimacros because of https://jira.xwiki.org/browse/XWIKI-16708
        FigureBlock fb = new FigureBlock(blocks(new FigureCaptionBlock(blocks(new WordBlock("wordblock"))),
            new TableBlock(Collections.emptyList())));
        assertTrue(this.figureTypeRecognizer.isTable(fb));
    }

    private List<Block> blocks(Block... blocks)
    {
        return Arrays.asList(blocks);
    }

    private MacroMarkerBlock createMacroMarkerBlock(List<Block> blocks)
    {
        return createMacroMarkerBlock("whatever", blocks);
    }

    private MacroMarkerBlock createMacroMarkerBlock(String macroId, List<Block> blocks)
    {
        return new MacroMarkerBlock(macroId, Collections.emptyMap(), "", blocks, false);
    }
}

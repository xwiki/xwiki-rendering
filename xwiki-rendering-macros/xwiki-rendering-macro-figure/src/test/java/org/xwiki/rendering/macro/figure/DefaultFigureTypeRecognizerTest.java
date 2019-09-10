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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.rendering.block.*;
import org.xwiki.rendering.internal.macro.figure.DefaultFigureTypeRecognizer;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link DefaultFigureTypeRecognizer}.
 *
 * @version $Id$
 * @since 10.2
 */
public class DefaultFigureTypeRecognizerTest
{
    @Rule
    public MockitoComponentMockingRule<FigureTypeRecognizer> mocker =
        new MockitoComponentMockingRule<>(DefaultFigureTypeRecognizer.class);

    @Test
    public void isTableWhenNoCaption() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList())));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWhenCaptionLast() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList()),
            createMacroMarkerBlock("figureCaption", Collections.emptyList())));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWhenCaptionFirst() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(
            createMacroMarkerBlock("figureCaption", Collections.emptyList()),
            new TableBlock(Collections.emptyList())));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWhenTwoTableBlocks() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList()),
            new TableBlock(Collections.emptyList()),
            createMacroMarkerBlock("figureCaption", Collections.emptyList())));
        assertFalse(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWhenFigureCaptionBlockInMacroMarkerBlock() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(
            new TableBlock(Collections.emptyList()),
            createMacroMarkerBlock(blocks(createMacroMarkerBlock("figureCaption", Collections.emptyList())))));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWithTableInsideNestedMacroMarkerBlocks() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(
            createMacroMarkerBlock(blocks(createMacroMarkerBlock(
                blocks(new TableBlock(Collections.emptyList()))))),
            createMacroMarkerBlock("figureCaption", Collections.emptyList())));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWhenMetaDataBlock() throws Exception
    {
        FigureBlock fb = new FigureBlock(blocks(new MetaDataBlock(
            blocks(new TableBlock(Collections.emptyList())))));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
    }

    @Test
    public void isTableWhenNoMacroMarkerBlockAroundCaption() throws Exception
    {
        // Note: This is what happens for wikimacros because of https://jira.xwiki.org/browse/XWIKI-16708
        FigureBlock fb = new FigureBlock(blocks(new FigureCaptionBlock(
            blocks(new TableBlock(Collections.emptyList())))));
        assertTrue(this.mocker.getComponentUnderTest().isTable(fb));
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

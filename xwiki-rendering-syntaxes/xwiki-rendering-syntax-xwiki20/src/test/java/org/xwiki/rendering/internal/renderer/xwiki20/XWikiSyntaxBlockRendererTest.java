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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.DefinitionDescriptionBlock;
import org.xwiki.rendering.block.DefinitionListBlock;
import org.xwiki.rendering.block.DefinitionTermBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NewLineBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ComponentTest
@AllComponents
public class XWikiSyntaxBlockRendererTest
{
    private BlockRenderer renderer;

    @BeforeEach
    public void before(ComponentManager componentManager) throws Exception
    {
        this.renderer = componentManager.getInstance(BlockRenderer.class, Syntax.XWIKI_2_0.toIdString());
    }

    private String render(Block block)
    {
        DefaultWikiPrinter wikiPrinter = new DefaultWikiPrinter();
        this.renderer.render(block, wikiPrinter);

        return wikiPrinter.toString();
    }

    @Test
    public void inline()
    {
        assertEquals("word", render(new WordBlock("word")));
    }

    @Test
    public void tableCellsWithInlineContentOnly()
    {
        // Use case: simple word in cell
        Block tableBlock = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableCellBlock(
                    Arrays.asList(new WordBlock("word")))))));
        assertEquals("|word", render(tableBlock));
    }

    @Test
    public void tableCellsWithStandaloneContent()
    {
        // Use case: Header in a table cell
        Block tableBlock = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableCellBlock(
                    Arrays.asList(new SectionBlock(
                        Arrays.asList(new HeaderBlock(
                            Arrays.asList(new WordBlock("heading")), HeaderLevel.LEVEL1)
                        ))))))));
        assertEquals("|(((\n= heading =\n)))", render(tableBlock));

        // Use case: Paragraph in a table cell
        tableBlock = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableCellBlock(
                    Arrays.asList(new ParagraphBlock(
                        Arrays.asList(new WordBlock("word")))))))));
        assertEquals("|(((\nword\n)))", render(tableBlock));

        // Use Case: Group in a table cell
        // Note: A GroupBlock is considered inline content
        tableBlock = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableCellBlock(
                    Arrays.asList(new GroupBlock(
                        Arrays.asList(new WordBlock("word")))))))));
        assertEquals("|(((\nword\n)))", render(tableBlock));

        // Use case: List on a table cell
        tableBlock = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableCellBlock(
                    Arrays.asList(new BulletedListBlock(
                        Arrays.asList(new ListItemBlock(
                            Arrays.asList(new WordBlock("word")))))))))));
        assertEquals("|(((\n* word\n)))", render(tableBlock));
    }

    @Test
    public void tableHeadCellsWithInlineContentOnly()
    {
        // Use case: simple word in head cell
        Block block = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableHeadCellBlock(
                    Arrays.asList(new WordBlock("word")))))));
        assertEquals("|=word", render(block));
    }

    @Test
    public void tableHeadCellsWithStandaloneContent()
    {
        // Use case: Header in a table head cell
        Block block = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableHeadCellBlock(
                    Arrays.asList(new SectionBlock(
                        Arrays.asList(new HeaderBlock(
                            Arrays.asList(new WordBlock("heading")), HeaderLevel.LEVEL1)
                        ))))))));
        assertEquals("|=(((\n= heading =\n)))", render(block));

        // Use case: Paragraph in a table head cell
        block = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableHeadCellBlock(
                    Arrays.asList(new ParagraphBlock(
                        Arrays.asList(new WordBlock("word")))))))));
        assertEquals("|=(((\nword\n)))", render(block));

        // Use Case: Group in a table head cell
        // Note: A GroupBlock is considered inline content
        block = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableHeadCellBlock(
                    Arrays.asList(new GroupBlock(
                        Arrays.asList(new WordBlock("word")))))))));
        assertEquals("|=(((\nword\n)))", render(block));

        // Use case: List on a table head cell
        block = new TableBlock(
            Arrays.asList(new TableRowBlock(
                Arrays.asList(new TableHeadCellBlock(
                    Arrays.asList(new BulletedListBlock(
                        Arrays.asList(new ListItemBlock(
                            Arrays.asList(new WordBlock("word")))))))))));
        assertEquals("|=(((\n* word\n)))", render(block));
    }

    @Test
    public void listItemWithInlineContentOnly()
    {
        // Use case: List in a list item
        Block block = new BulletedListBlock(
            Arrays.asList(new ListItemBlock(
                Arrays.asList(new WordBlock("word1"), new BulletedListBlock(
                    Arrays.asList(new ListItemBlock(
                        Arrays.asList(new WordBlock("word2")))))))));
        assertEquals("* word1\n** word2", render(block));

        // Use case: definition list in a list item
        block = new BulletedListBlock(
            Arrays.asList(new ListItemBlock(
                Arrays.asList(new WordBlock("word1"), new DefinitionListBlock(
                    Arrays.asList(new DefinitionTermBlock(
                        Arrays.asList(new WordBlock("term"))),
                    new DefinitionDescriptionBlock(
                        Arrays.asList(new WordBlock("desc")))))))));
        assertEquals("* word1\n*; term\n*: desc", render(block));

        // Use case: Newline in a list item
        // New lines at the end of inline blocks are rendered as linebreaks in wiki syntax
        block = new BulletedListBlock(
            Arrays.asList(new ListItemBlock(
                Arrays.asList(new WordBlock("word"), new NewLineBlock()))));
        assertEquals("* word\\\\", render(block));
    }

    @Test
    public void listItemWithStandaloneContent()
    {
        // Use case: Header in a list item
        Block block = new BulletedListBlock(
            Arrays.asList(new ListItemBlock(
                Arrays.asList(new SectionBlock(
                    Arrays.asList(new HeaderBlock(
                        Arrays.asList(new WordBlock("heading")), HeaderLevel.LEVEL1)
                    ))))));
        assertEquals("* (((\n= heading =\n)))", render(block));

        // Use case: Paragraph in a list item
        block = new BulletedListBlock(
            Arrays.asList(new ListItemBlock(
                Arrays.asList(new ParagraphBlock(
                    Arrays.asList(new WordBlock("word")))))));
        assertEquals("* (((\nword\n)))", render(block));

        // Use Case: Group in a list item
        // Note: A GroupBlock is considered inline content
        block = new BulletedListBlock(
            Arrays.asList(new ListItemBlock(
                Arrays.asList(new GroupBlock(
                    Arrays.asList(new WordBlock("word")))))));
        assertEquals("* (((\nword\n)))", render(block));
    }
}
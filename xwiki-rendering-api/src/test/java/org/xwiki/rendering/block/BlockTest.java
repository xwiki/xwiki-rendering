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
package org.xwiki.rendering.block;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.match.AnyBlockMatcher;
import org.xwiki.rendering.block.match.BlockNavigatorTest;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for Block manipulation, testing {@link AbstractBlock}.
 *
 * @version $Id$
 * @since 1.5M2
 */
public class BlockTest
{
    @Test
    public void insertChildAfter()
    {
        Block wb1 = new WordBlock("block1");
        Block wb2 = new WordBlock("block2");
        ParagraphBlock pb = new ParagraphBlock(Arrays.asList(wb1, wb2));

        Block wb = new WordBlock("block");

        pb.insertChildAfter(wb, wb1);
        assertSame(wb, pb.getChildren().get(1));
        assertSame(wb1, wb.getPreviousSibling());
        assertSame(wb2, wb.getNextSibling());
        assertSame(wb, wb1.getNextSibling());
        assertSame(wb, wb2.getPreviousSibling());

        pb.insertChildAfter(wb, wb2);
        assertSame(wb, pb.getChildren().get(3));
        assertSame(wb2, wb.getPreviousSibling());
        assertSame(wb, wb2.getNextSibling());
        assertNull(wb.getNextSibling());
    }

    @Test
    public void insertChildBefore()
    {
        Block wb1 = new WordBlock("block1");
        Block wb2 = new WordBlock("block2");

        List<Block> children = new ArrayList<Block>();
        children.add(wb1);
        children.add(wb2);

        ParagraphBlock pb = new ParagraphBlock(children);

        Block wb = new WordBlock("block");

        pb.insertChildBefore(wb, wb1);
        assertSame(wb, pb.getChildren().get(0));

        pb.insertChildBefore(wb, wb2);
        assertSame(wb, pb.getChildren().get(2));
    }

    @Test
    public void replaceBlock()
    {
        // It's important all blocks have same content to make sure replacement api don't find the position of the
        // old block using Object#equals
        Block word1 = new WordBlock("block1");
        Block word2 = new WordBlock("block2");
        Block word3 = new WordBlock("block3");

        Block parentBlock = new ParagraphBlock(Arrays.asList(word1, word2));

        // replace by one
        parentBlock.replaceChild(word3, word1);

        assertEquals(2, parentBlock.getChildren().size());
        assertSame(word3, parentBlock.getChildren().get(0));
        assertSame(word2, parentBlock.getChildren().get(1));
        assertSame(word2, word3.getNextSibling());
        assertSame(word3, word2.getPreviousSibling());

        // replace by nothing
        parentBlock.replaceChild(Collections.<Block>emptyList(), word2);

        assertEquals(1, parentBlock.getChildren().size());
        assertSame(word3, parentBlock.getChildren().get(0));
        assertNull(word3.getNextSibling());
        assertNull(word3.getPreviousSibling());

        // replace by several
        parentBlock.replaceChild(Arrays.asList(word1, word2), word3);

        assertEquals(2, parentBlock.getChildren().size());
        assertSame(word1, parentBlock.getChildren().get(0));
        assertSame(word2, parentBlock.getChildren().get(1));
        assertSame(word2, word1.getNextSibling());
        assertSame(word1, word2.getPreviousSibling());

        // replace by empty block the top level one
        parentBlock.replaceChild(Collections.<Block>emptyList(), word1);
        assertEquals(1, parentBlock.getChildren().size());
        assertSame(word2, parentBlock.getChildren().get(0));
        assertNull(word2.getNextSibling());
        assertNull(word2.getPreviousSibling());

        // Provide not existing block to replace
        assertThrows(InvalidParameterException.class,
            () -> parentBlock.replaceChild(word3, new WordBlock("not existing")));
    }

    @Test
    public void testClone()
    {
        WordBlock wb = new WordBlock("block");
        ImageBlock ib = new ImageBlock(new ResourceReference("document@attachment", ResourceType.ATTACHMENT), true);
        DocumentResourceReference linkReference = new DocumentResourceReference("reference");
        LinkBlock lb = new LinkBlock(Arrays.asList((Block) new WordBlock("label")), linkReference, false);
        Block pb = new ParagraphBlock(Arrays.<Block>asList(wb, ib, lb));
        XDOM rootBlock = new XDOM(Arrays.<Block>asList(pb));

        XDOM newRootBlock = rootBlock.clone();

        assertNotSame(rootBlock, newRootBlock);
        assertNotSame(rootBlock.getMetaData(), newRootBlock.getMetaData());

        Block newPB = newRootBlock.getChildren().get(0);

        assertNotSame(pb, newPB);

        assertNotSame(wb, newPB.getChildren().get(0));
        assertNotSame(ib, newPB.getChildren().get(1));
        assertNotSame(lb, newPB.getChildren().get(2));

        assertEquals(wb.getWord(), ((WordBlock) newPB.getChildren().get(0)).getWord());
        assertNotSame(ib.getReference(), ((ImageBlock) newPB.getChildren().get(1)).getReference());
        assertNotSame(lb.getReference(), ((LinkBlock) newPB.getChildren().get(2)).getReference());
    }

    @Test
    public void getNextSibling()
    {
        WordBlock b1 = new WordBlock("b1");
        WordBlock b2 = new WordBlock("b2");
        ParagraphBlock p = new ParagraphBlock(Arrays.<Block>asList(b1, b2));

        assertSame(b2, b1.getNextSibling());
        assertNull(b2.getNextSibling());
        assertNull(p.getNextSibling());
        assertNull(new ParagraphBlock(Collections.<Block>emptyList()).getNextSibling());
    }

    @Test
    public void removeBlock()
    {
        WordBlock b1 = new WordBlock("b1");
        WordBlock b1bis = new WordBlock("b1");
        WordBlock b2 = new WordBlock("b2");
        ParagraphBlock p1 = new ParagraphBlock(Arrays.<Block>asList(b1, b1bis, b2));

        p1.removeBlock(b1bis);
        assertEquals(2, p1.getChildren().size());
        assertSame(b1, p1.getChildren().get(0));
        assertSame(b2, p1.getChildren().get(1));

        p1.removeBlock(b1);
        assertEquals(1, p1.getChildren().size());
        assertSame(b2, p1.getChildren().get(0));
        assertNull(b1.getPreviousSibling());
        assertNull(b1.getNextSibling());
        assertNull(b2.getPreviousSibling());

        p1.removeBlock(b2);
        assertEquals(0, p1.getChildren().size());
        assertNull(b2.getPreviousSibling());
        assertNull(b2.getNextSibling());
    }

    @Test
    public void getBlocks()
    {
        assertEquals(Arrays.asList(BlockNavigatorTest.parentBlock, BlockNavigatorTest.rootBlock),
            BlockNavigatorTest.contextBlock.getBlocks(AnyBlockMatcher.ANYBLOCKMATCHER, Block.Axes.ANCESTOR));
    }

    @Test
    public void getFirstBlock()
    {
        assertSame(BlockNavigatorTest.parentBlock,
            BlockNavigatorTest.contextBlock.getFirstBlock(AnyBlockMatcher.ANYBLOCKMATCHER, Block.Axes.ANCESTOR));
    }

    @Test
    public void setChildren()
    {
        ParagraphBlock paragraphBlock = new ParagraphBlock(Collections.EMPTY_LIST);

        List<Block> blocks = Arrays.<Block>asList(new WordBlock("1"), new WordBlock("2"));
        paragraphBlock.setChildren(blocks);

        assertArrayEquals(blocks.toArray(), paragraphBlock.getChildren().toArray());

        blocks = Arrays.<Block>asList(new WordBlock("3"), new WordBlock("4"));
        paragraphBlock.setChildren(blocks);

        assertArrayEquals(blocks.toArray(), paragraphBlock.getChildren().toArray());

        blocks = Arrays.<Block>asList();
        paragraphBlock.setChildren(blocks);

        assertArrayEquals(blocks.toArray(), paragraphBlock.getChildren().toArray());
    }

    @Test
    public void setAndGetParameter()
    {
        WordBlock wordBlock = new WordBlock("word");

        wordBlock.setParameter("param", "value");

        assertEquals("value", wordBlock.getParameter("param"));

        wordBlock.setParameter("param", "value2");

        assertEquals("value2", wordBlock.getParameter("param"));
    }

    @Test
    public void setAndGetParameters()
    {
        WordBlock wordBlock = new WordBlock("word");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");

        wordBlock.setParameters(parameters);

        assertEquals(parameters, wordBlock.getParameters());

        Map<String, String> parameters2 = new HashMap<String, String>();
        parameters.put("param21", "value21");
        parameters.put("param22", "value22");

        wordBlock.setParameters(parameters2);

        assertEquals(parameters2, wordBlock.getParameters());
    }

    @Test
    public void getRoot()
    {
        assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.rootBlock.getRoot());
        assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.contextBlock.getRoot());
        assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.contextBlockChild1.getRoot());
        assertSame(BlockNavigatorTest.rootBlock, BlockNavigatorTest.contextBlockChild11.getRoot());
    }

    @Test
    public void testAbstractBlockEquals()
    {
        final String ID = "Test id";
        final String CONTENT = "Test content";
        final boolean IS_INLINE = true;
        final Map<String, String> PARAMETERS = new HashMap<>();

        PARAMETERS.put("TestKey", "TestValue");

        AbstractMacroBlock macroBlock1, macroBlock2, macroBlock3;

        macroBlock1 = new MacroBlock(ID, PARAMETERS, CONTENT, IS_INLINE);
        macroBlock2 = new MacroBlock(ID, PARAMETERS, CONTENT, IS_INLINE);
        macroBlock3 = new MacroBlock(ID, PARAMETERS, CONTENT, IS_INLINE);

        // must be reflexive.
        assertEquals(macroBlock1, macroBlock1);

        // must be symmetric.
        assertEquals(macroBlock1, macroBlock2);
        assertEquals(macroBlock2, macroBlock1);

        // must be transitive.
        assertEquals(macroBlock1, macroBlock2);
        assertEquals(macroBlock2, macroBlock3);
        assertEquals(macroBlock1, macroBlock3);

        // must be consistent (already checked).

        // equals(null) == false.
        assertFalse(macroBlock1.equals(null));
        assertFalse(macroBlock2.equals(null));
        assertFalse(macroBlock3.equals(null));

        // hashCode must be equal.
        assertEquals(macroBlock1.hashCode(), macroBlock2.hashCode());

        AbstractMacroBlock macroMarkerBlock1, macroMarkerBlock2, macroMarkerBlock3;

        macroMarkerBlock1 = new MacroBlock(ID, PARAMETERS, CONTENT, IS_INLINE);
        macroMarkerBlock2 = new MacroBlock(ID, PARAMETERS, CONTENT, IS_INLINE);
        macroMarkerBlock3 = new MacroBlock(ID, PARAMETERS, CONTENT, IS_INLINE);

        // must be reflexive.
        assertEquals(macroMarkerBlock1, macroMarkerBlock1);

        // must be symmetric.
        assertEquals(macroMarkerBlock1, macroMarkerBlock2);
        assertEquals(macroMarkerBlock2, macroMarkerBlock1);

        // must be transitive.
        assertEquals(macroMarkerBlock1, macroMarkerBlock2);
        assertEquals(macroMarkerBlock2, macroMarkerBlock3);
        assertEquals(macroMarkerBlock1, macroMarkerBlock3);

        // must be consistent (already checked).

        // equals(null) == false.
        assertFalse(macroMarkerBlock1.equals(null));
        assertFalse(macroMarkerBlock2.equals(null));
        assertFalse(macroMarkerBlock3.equals(null));

        // hashCode must be equal.
        assertEquals(macroMarkerBlock1.hashCode(), macroMarkerBlock2.hashCode());
    }

    @Test
    public void indexOf()
    {
        Block wb1 = new WordBlock("block1");
        Block wb2 = new WordBlock("block2");
        ParagraphBlock pb = new ParagraphBlock(Arrays.asList(wb1, wb2));

        assertEquals(0, pb.indexOf(pb));
        assertEquals(1, pb.indexOf(wb1));
        assertEquals(2, pb.indexOf(wb2));
        assertEquals(-1, pb.indexOf(new WordBlock("block1")));
    }
}

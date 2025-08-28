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
package org.xwiki.rendering.block.match;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BlockTestHelper;
import org.xwiki.rendering.block.WordBlock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link BlockNavigator}.
 *
 * @version $Id$
 */
class BlockNavigatorTest
{
    @Test
    void getBlocks()
    {
        BlockNavigator navigator = new BlockNavigator();

        assertEquals(Arrays.asList(BlockTestHelper.parentBlock, BlockTestHelper.rootBlock),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.ANCESTOR));
        assertEquals(
            Arrays.asList(BlockTestHelper.contextBlock, BlockTestHelper.parentBlock, BlockTestHelper.rootBlock),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.ANCESTOR_OR_SELF));
        assertEquals(Arrays.asList(BlockTestHelper.contextBlockChild1, BlockTestHelper.contextBlockChild2),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.CHILD));
        assertEquals(Arrays.asList(BlockTestHelper.contextBlockChild1, BlockTestHelper.contextBlockChild11,
            BlockTestHelper.contextBlockChild12,
            BlockTestHelper.contextBlockChild2, BlockTestHelper.contextBlockChild21,
            BlockTestHelper.contextBlockChild22), navigator.getBlocks(BlockTestHelper.contextBlock,
            Block.Axes.DESCENDANT));
        assertEquals(Arrays.asList(BlockTestHelper.contextBlock, BlockTestHelper.contextBlockChild1,
            BlockTestHelper.contextBlockChild11, BlockTestHelper.contextBlockChild12,
            BlockTestHelper.contextBlockChild2, BlockTestHelper.contextBlockChild21,
            BlockTestHelper.contextBlockChild22), navigator.getBlocks(BlockTestHelper.contextBlock,
            Block.Axes.DESCENDANT_OR_SELF));
        assertEquals(Arrays.asList(BlockTestHelper.followingBlock, BlockTestHelper.followingBlockChild1,
                BlockTestHelper.followingBlockChild2),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.FOLLOWING));
        assertEquals(List.of(BlockTestHelper.followingBlock),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.FOLLOWING_SIBLING));
        assertEquals(List.of(BlockTestHelper.parentBlock),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.PARENT));
        assertEquals(Arrays.asList(BlockTestHelper.precedingBlock, BlockTestHelper.precedingBlockChild1,
                BlockTestHelper.precedingBlockChild2),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.PRECEDING));
        assertEquals(List.of(BlockTestHelper.precedingBlock),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.PRECEDING_SIBLING));
        assertEquals(List.of(BlockTestHelper.contextBlock),
            navigator.getBlocks(BlockTestHelper.contextBlock, Block.Axes.SELF));
    }

    @Test
    void getFirstBlock()
    {
        BlockNavigator navigator = new BlockNavigator();

        assertSame(BlockTestHelper.parentBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.ANCESTOR));
        assertSame(BlockTestHelper.contextBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.ANCESTOR_OR_SELF));
        assertSame(BlockTestHelper.contextBlockChild1,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.CHILD));
        assertSame(BlockTestHelper.contextBlockChild1,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.DESCENDANT));
        assertSame(BlockTestHelper.contextBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.DESCENDANT_OR_SELF));
        assertSame(BlockTestHelper.followingBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.FOLLOWING));

        assertSame(BlockTestHelper.followingBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.FOLLOWING_SIBLING));

        assertSame(BlockTestHelper.parentBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.PARENT));
        assertSame(BlockTestHelper.precedingBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.PRECEDING));

        assertSame(BlockTestHelper.precedingBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.PRECEDING_SIBLING));

        assertSame(BlockTestHelper.contextBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.SELF));

        // SameBlockMatcher

        navigator = new BlockNavigator(new SameBlockMatcher(BlockTestHelper.rootBlock));

        assertSame(BlockTestHelper.rootBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.ANCESTOR_OR_SELF));
        assertSame(BlockTestHelper.rootBlock,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.ANCESTOR));

        navigator = new BlockNavigator(new SameBlockMatcher(BlockTestHelper.contextBlockChild22));

        assertSame(BlockTestHelper.contextBlockChild22,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.DESCENDANT));

        assertSame(BlockTestHelper.contextBlockChild22,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.DESCENDANT_OR_SELF));

        navigator = new BlockNavigator(new SameBlockMatcher(BlockTestHelper.followingBlockChild2));

        assertSame(BlockTestHelper.followingBlockChild2,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.FOLLOWING));

        navigator = new BlockNavigator(new SameBlockMatcher(BlockTestHelper.contextBlockChild2));

        assertSame(BlockTestHelper.contextBlockChild2,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.CHILD));

        navigator = new BlockNavigator(new SameBlockMatcher(BlockTestHelper.precedingBlockChild2));

        assertSame(BlockTestHelper.precedingBlockChild2,
            navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.PRECEDING));

        navigator = new BlockNavigator(new SameBlockMatcher(new WordBlock("unexistingBlock")));

        assertNull(navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.PRECEDING_SIBLING));
        assertNull(navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.PARENT));
        assertNull(navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.FOLLOWING_SIBLING));
        assertNull(navigator.getFirstBlock(BlockTestHelper.contextBlock, Block.Axes.SELF));
    }
}

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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;

public class BlockNavigatorTest
{
    public static final WordBlock precedingBlockChild1 = new WordBlock("pc1");

    public static final WordBlock precedingBlockChild2 = new WordBlock("pc2");

    public static final ParagraphBlock precedingBlock = new ParagraphBlock(Arrays.<Block>asList(precedingBlockChild1,
        precedingBlockChild2))
    {
        @Override
        public String toString()
        {
            return "precedingBlock";
        }
    };

    public static final WordBlock contextBlockChild21 = new WordBlock("cc21");

    public static final WordBlock contextBlockChild22 = new WordBlock("cc22");

    public static final ParagraphBlock contextBlockChild2 = new ParagraphBlock(Arrays.<Block>asList(
        contextBlockChild21, contextBlockChild22))
    {
        @Override
        public String toString()
        {
            return "contextBlockChild2";
        }
    };

    public static final WordBlock contextBlockChild11 = new WordBlock("cc11");

    public static final WordBlock contextBlockChild12 = new WordBlock("cc12");

    public static final ParagraphBlock contextBlockChild1 = new ParagraphBlock(Arrays.<Block>asList(
        contextBlockChild11, contextBlockChild12))
    {
        @Override
        public String toString()
        {
            return "contextBlockChild1";
        }
    };

    public static final ParagraphBlock contextBlock = new ParagraphBlock(Arrays.<Block>asList(contextBlockChild1,
        contextBlockChild2))
    {
        @Override
        public String toString()
        {
            return "contextBlock";
        }
    };

    public static final WordBlock followingBlockChild1 = new WordBlock("fc1");

    public static final WordBlock followingBlockChild2 = new WordBlock("fc2");

    public static final ParagraphBlock followingBlock = new ParagraphBlock(Arrays.<Block>asList(followingBlockChild1,
        followingBlockChild2))
    {
        @Override
        public String toString()
        {
            return "followingBlock";
        }
    };

    public static final ParagraphBlock parentBlock = new ParagraphBlock(Arrays.<Block>asList(precedingBlock,
        contextBlock, followingBlock))
    {
        @Override
        public String toString()
        {
            return "parentBlock";
        }
    };

    public static final ParagraphBlock rootBlock = new ParagraphBlock(Arrays.<Block>asList(parentBlock))
    {
        @Override
        public String toString()
        {
            return "rootBlock";
        }
    };

    @Test
    public void testGetBlocks()
    {
        BlockNavigator navigator = new BlockNavigator();

        Assert.assertEquals(Arrays.asList(parentBlock, rootBlock),
            navigator.getBlocks(contextBlock, Block.Axes.ANCESTOR));
        Assert.assertEquals(Arrays.asList(contextBlock, parentBlock, rootBlock),
            navigator.getBlocks(contextBlock, Block.Axes.ANCESTOR_OR_SELF));
        Assert.assertEquals(Arrays.asList(contextBlockChild1, contextBlockChild2),
            navigator.getBlocks(contextBlock, Block.Axes.CHILD));
        Assert.assertEquals(Arrays.asList(contextBlockChild1, contextBlockChild11, contextBlockChild12,
            contextBlockChild2, contextBlockChild21, contextBlockChild22), navigator.getBlocks(contextBlock,
                Block.Axes.DESCENDANT));
        Assert.assertEquals(Arrays.asList(contextBlock, contextBlockChild1, contextBlockChild11, contextBlockChild12,
            contextBlockChild2, contextBlockChild21, contextBlockChild22), navigator.getBlocks(contextBlock,
                Block.Axes.DESCENDANT_OR_SELF));
        Assert.assertEquals(Arrays.asList(followingBlock, followingBlockChild1, followingBlockChild2),
            navigator.getBlocks(contextBlock, Block.Axes.FOLLOWING));
        Assert.assertEquals(Arrays.asList(followingBlock),
            navigator.getBlocks(contextBlock, Block.Axes.FOLLOWING_SIBLING));
        Assert.assertEquals(Arrays.asList(parentBlock), navigator.getBlocks(contextBlock, Block.Axes.PARENT));
        Assert.assertEquals(Arrays.asList(precedingBlock, precedingBlockChild1, precedingBlockChild2),
            navigator.getBlocks(contextBlock, Block.Axes.PRECEDING));
        Assert.assertEquals(Arrays.asList(precedingBlock),
            navigator.getBlocks(contextBlock, Block.Axes.PRECEDING_SIBLING));
        Assert.assertEquals(Arrays.asList(contextBlock), navigator.getBlocks(contextBlock, Block.Axes.SELF));
    }

    @Test
    public void testGetFirstBlock()
    {
        BlockNavigator navigator = new BlockNavigator();

        Assert.assertSame(parentBlock, navigator.getFirstBlock(contextBlock, Block.Axes.ANCESTOR));
        Assert.assertSame(contextBlock, navigator.getFirstBlock(contextBlock, Block.Axes.ANCESTOR_OR_SELF));
        Assert.assertSame(contextBlockChild1, navigator.getFirstBlock(contextBlock, Block.Axes.CHILD));
        Assert.assertSame(contextBlockChild1, navigator.getFirstBlock(contextBlock, Block.Axes.DESCENDANT));
        Assert.assertSame(contextBlock, navigator.getFirstBlock(contextBlock, Block.Axes.DESCENDANT_OR_SELF));
        Assert.assertSame(followingBlock, navigator.getFirstBlock(contextBlock, Block.Axes.FOLLOWING));

        Assert.assertSame(followingBlock, navigator.getFirstBlock(contextBlock, Block.Axes.FOLLOWING_SIBLING));

        Assert.assertSame(parentBlock, navigator.getFirstBlock(contextBlock, Block.Axes.PARENT));
        Assert.assertSame(precedingBlock, navigator.getFirstBlock(contextBlock, Block.Axes.PRECEDING));

        Assert.assertSame(precedingBlock, navigator.getFirstBlock(contextBlock, Block.Axes.PRECEDING_SIBLING));

        Assert.assertSame(contextBlock, navigator.getFirstBlock(contextBlock, Block.Axes.SELF));

        // SameBlockMatcher

        navigator = new BlockNavigator(new SameBlockMatcher(rootBlock));

        Assert.assertSame(rootBlock, navigator.getFirstBlock(contextBlock, Block.Axes.ANCESTOR_OR_SELF));
        Assert.assertSame(rootBlock, navigator.getFirstBlock(contextBlock, Block.Axes.ANCESTOR));

        navigator = new BlockNavigator(new SameBlockMatcher(contextBlockChild22));

        Assert.assertSame(contextBlockChild22, navigator.getFirstBlock(contextBlock, Block.Axes.DESCENDANT));

        Assert.assertSame(contextBlockChild22, navigator.getFirstBlock(contextBlock, Block.Axes.DESCENDANT_OR_SELF));

        navigator = new BlockNavigator(new SameBlockMatcher(followingBlockChild2));

        Assert.assertSame(followingBlockChild2, navigator.getFirstBlock(contextBlock, Block.Axes.FOLLOWING));

        navigator = new BlockNavigator(new SameBlockMatcher(contextBlockChild2));

        Assert.assertSame(contextBlockChild2, navigator.getFirstBlock(contextBlock, Block.Axes.CHILD));

        navigator = new BlockNavigator(new SameBlockMatcher(precedingBlockChild2));

        Assert.assertSame(precedingBlockChild2, navigator.getFirstBlock(contextBlock, Block.Axes.PRECEDING));

        navigator = new BlockNavigator(new SameBlockMatcher(new WordBlock("unexistingBlock")));

        Assert.assertNull(navigator.getFirstBlock(contextBlock, Block.Axes.PRECEDING_SIBLING));
        Assert.assertNull(navigator.getFirstBlock(contextBlock, Block.Axes.PARENT));
        Assert.assertNull(navigator.getFirstBlock(contextBlock, Block.Axes.FOLLOWING_SIBLING));
        Assert.assertNull(navigator.getFirstBlock(contextBlock, Block.Axes.SELF));
    }
}

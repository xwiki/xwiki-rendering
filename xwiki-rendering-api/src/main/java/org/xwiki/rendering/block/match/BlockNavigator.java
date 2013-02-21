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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.Block.Axes;

/**
 * Tool to navigate in a tree of blocks and extract them based on configurable criteria.
 * 
 * @version $Id$
 * @since 5.0M1
 */
public class BlockNavigator
{
    /**
     * Used to filter the result of the various methods.
     */
    private BlockMatcher matcher;

    /**
     * The default matcher does not filter anything.
     */
    public BlockNavigator()
    {
        this.matcher = AnyBlockMatcher.ANYBLOCKMATCHER;
    }

    /**
     * @param matcher used to filter the result of the various methods
     */
    public BlockNavigator(BlockMatcher matcher)
    {
        this.matcher = matcher;
    }

    // Blocks

    /**
     * Get all blocks following provided {@link BlockMatcher} and {@link Axes}.
     * 
     * @param <T> the class of the Blocks to return
     * @param currentBlock the block to start searching from
     * @param currentAxes indicate the search axes
     * @return the matched {@link Block}s, empty list of none was found
     */
    public <T extends Block> List<T> getBlocks(Block currentBlock, Axes currentAxes)
    {
        List<T> blocks = new ArrayList<T>();

        Block block = currentBlock;
        Axes axes = currentAxes;

        while (block != null) {
            Block nextBlock = null;

            switch (axes) {
            // SELF
                case SELF:
                    blocks = addBlock(block, blocks);
                    break;
                // ANCESTOR
                case ANCESTOR_OR_SELF:
                    blocks = addBlock(block, blocks);
                    nextBlock = block.getParent();
                    break;
                case ANCESTOR:
                    nextBlock = block.getParent();
                    axes = Axes.ANCESTOR_OR_SELF;
                    break;
                case PARENT:
                    nextBlock = block.getParent();
                    axes = Axes.SELF;
                    break;
                // DESCENDANT
                case CHILD:
                    if (!block.getChildren().isEmpty()) {
                        nextBlock = block.getChildren().get(0);
                        axes = Axes.FOLLOWING_SIBLING;
                        blocks = addBlock(nextBlock, blocks);
                    }
                    break;
                case DESCENDANT_OR_SELF:
                    blocks = addBlock(block, blocks);
                    blocks = getBlocks(block.getChildren(), Axes.DESCENDANT_OR_SELF, blocks);
                    break;
                case DESCENDANT:
                    blocks = getBlocks(block.getChildren(), Axes.DESCENDANT_OR_SELF, blocks);
                    break;
                // FOLLOWING
                case FOLLOWING_SIBLING:
                    nextBlock = block.getNextSibling();
                    blocks = addBlock(nextBlock, blocks);
                    break;
                case FOLLOWING:
                    for (Block nextSibling = block.getNextSibling(); nextSibling != null; nextSibling =
                            nextSibling.getNextSibling()) {
                        blocks = getBlocks(nextSibling, Axes.DESCENDANT_OR_SELF, blocks);
                    }
                    break;
                // PRECEDING
                case PRECEDING_SIBLING:
                    nextBlock = block.getPreviousSibling();
                    blocks = addBlock(nextBlock, blocks);
                    break;
                case PRECEDING:
                    for (Block previousSibling = block.getPreviousSibling(); previousSibling != null; previousSibling =
                            previousSibling.getPreviousSibling()) {
                        blocks = getBlocks(previousSibling, Axes.DESCENDANT_OR_SELF, blocks);
                    }
                    break;
                default:
                    break;
            }

            block = nextBlock;
        }

        return blocks != null ? blocks : Collections.<T> emptyList();
    }

    /**
     * Add provided {@link Block} to provided list (or create list of null) if block validate the provided
     * {@link BlockMatcher}.
     * 
     * @param <T> the class of the Blocks to return
     * @param currentBlock the block to search from
     * @param blocks the list of blocks to fill
     * @return the modified list, null if provided list is null and provided {@link Block} does not validate provided
     *         {@link BlockMatcher}
     */
    private <T extends Block> List<T> addBlock(Block currentBlock, List<T> blocks)
    {
        List<T> newBlocks = blocks;

        if (currentBlock != null && this.matcher.match(currentBlock)) {
            if (newBlocks == null) {
                newBlocks = new ArrayList<T>();
            }
            newBlocks.add((T) currentBlock);
        }

        return newBlocks;
    }

    /**
     * Add all blocks following provided {@link BlockMatcher} and {@link Axes} in the provide list (or create a new list
     * of provided list is null).
     * 
     * @param <T> the class of the Blocks to return
     * @param blocks the blocks from where to search
     * @param axes the axes
     * @param blocksOut the list of blocks to fill
     * @return the modified list, null if provided list is null and provided {@link Block} does not validate provided
     *         {@link BlockMatcher}
     */
    private <T extends Block> List<T> getBlocks(List<Block> blocks, Axes axes, List<T> blocksOut)
    {
        List<T> newBlocks = blocksOut;

        for (Block block : blocks) {
            newBlocks = getBlocks(block, axes, newBlocks);
        }

        return newBlocks;
    }

    /**
     * Add all blocks following provided {@link BlockMatcher} and {@link Axes} in the provide list (or create a new list
     * of provided list is null).
     * 
     * @param <T> the class of the Blocks to return
     * @param currentBlock the block to search from
     * @param axes the axes
     * @param blocksOut the list of blocks to fill
     * @return the modified list, null if provided list is null and provided {@link Block} does not validate provided
     *         {@link BlockMatcher}
     */
    private <T extends Block> List<T> getBlocks(Block currentBlock, Axes axes, List<T> blocksOut)
    {
        List<T> newBlocks = blocksOut;

        List<T> nextBlocks = getBlocks(currentBlock, axes);
        if (!nextBlocks.isEmpty()) {
            if (newBlocks == null) {
                newBlocks = nextBlocks;
            } else {
                newBlocks.addAll(nextBlocks);
            }
        }

        return newBlocks;
    }

    // First block

    /**
     * Get the first matched block in the provided {@link Axes}.
     * 
     * @param <T> the class of the Block to return
     * @param currentBlock the block to start searching from
     * @param axes indicate the search axes
     * @return the matched {@link Block}, null if none was found
     * @since 3.0M3
     */
    public <T extends Block> T getFirstBlock(Block currentBlock, Axes axes)
    {
        T block = null;

        if (axes == Axes.SELF) {
            if (this.matcher.match(currentBlock)) {
                block = (T) currentBlock;
            }
        } else if (axes.compareTo(Axes.ANCESTOR_OR_SELF) <= 0) {
            block = (T) getFirstAncestorBlock(currentBlock, axes);
        } else if (axes.compareTo(Axes.DESCENDANT_OR_SELF) <= 0) {
            block = (T) getFirstDescendantBlock(currentBlock, axes);
        } else if (axes.compareTo(Axes.FOLLOWING_SIBLING) <= 0) {
            block = (T) getFirstFollowingSiblingBlock(currentBlock, axes);
        } else {
            block = (T) getFirstPrecedingSiblingBlock(currentBlock, axes);
        }

        return block;
    }

    /**
     * Get the first matched block in the provided following sibling {@link Axes}.
     * 
     * @param currentBlock the block to search from
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstFollowingSiblingBlock(Block currentBlock, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case FOLLOWING_SIBLING:
                nextBlock = currentBlock.getNextSibling();
                if (nextBlock != null && this.matcher.match(nextBlock)) {
                    return nextBlock;
                }
                break;
            case FOLLOWING:
                for (Block nextSibling = currentBlock.getNextSibling(); nextSibling != null; nextSibling =
                        nextSibling.getNextSibling()) {
                    Block matchedBlock = getFirstBlock(nextSibling, Axes.DESCENDANT_OR_SELF);
                    if (matchedBlock != null) {
                        return matchedBlock;
                    }
                }
                break;
            default:
                break;
        }

        return nextBlock != null ? getFirstBlock(nextBlock, nextAxes) : null;
    }

    /**
     * Get the first matched block in the provided ancestor {@link Axes}.
     * 
     * @param currentBlock the block to search from
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstAncestorBlock(Block currentBlock, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case ANCESTOR_OR_SELF:
                if (this.matcher.match(currentBlock)) {
                    return currentBlock;
                }
            case ANCESTOR:
            case PARENT:
                nextAxes = axes == Axes.PARENT ? Axes.SELF : Axes.ANCESTOR_OR_SELF;
                nextBlock = currentBlock.getParent();
                break;
            default:
                break;
        }

        return nextBlock != null ? getFirstBlock(nextBlock, nextAxes) : null;
    }

    /**
     * Get the first matched block in the provided descendant {@link Axes}.
     * 
     * @param currentBlock the block to search from
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstDescendantBlock(Block currentBlock, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case CHILD:
                List<Block> children = currentBlock.getChildren();
                if (!children.isEmpty()) {
                    nextBlock = children.get(0);
                    nextAxes = Axes.FOLLOWING_SIBLING;
                    if (this.matcher.match(nextBlock)) {
                        return nextBlock;
                    }
                }
                break;
            case DESCENDANT_OR_SELF:
                if (this.matcher.match(currentBlock)) {
                    return currentBlock;
                }
            case DESCENDANT:
                for (Block child : currentBlock.getChildren()) {
                    Block matchedBlock = getFirstBlock(child, Axes.DESCENDANT_OR_SELF);
                    if (matchedBlock != null) {
                        return matchedBlock;
                    }
                }
                break;
            default:
                break;
        }

        return nextBlock != null ? getFirstBlock(nextBlock, nextAxes) : null;
    }

    /**
     * Get the first matched block in the provided preceding sibling {@link Axes}.
     * 
     * @param currentBlock the block to search from
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstPrecedingSiblingBlock(Block currentBlock, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case PRECEDING_SIBLING:
                nextBlock = currentBlock.getPreviousSibling();
                if (nextBlock != null && this.matcher.match(nextBlock)) {
                    return nextBlock;
                }
                break;
            case PRECEDING:
                for (Block previousSibling = currentBlock.getPreviousSibling(); previousSibling != null;
                    previousSibling = previousSibling.getPreviousSibling()) {
                    Block matchedBlock = getFirstBlock(previousSibling, Axes.DESCENDANT_OR_SELF);
                    if (matchedBlock != null) {
                        return matchedBlock;
                    }
                }
                break;
            default:
                break;
        }

        return nextBlock != null ? getFirstBlock(nextBlock, nextAxes) : null;
    }
}

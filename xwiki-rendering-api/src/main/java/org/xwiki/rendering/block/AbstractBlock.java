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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.Listener;

/**
 * Implementation for Block operations. All blocks should extend this class. Supports the notion of generic parameters
 * which can be added to a block (see {@link #getParameter(String)} for more details.
 * 
 * @version $Id$
 * @since 1.5M2
 */
public abstract class AbstractBlock implements Block
{
    /**
     * Store parameters, see {@link #getParameter(String)} for more explanations on what parameters are.
     */
    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    /**
     * The Blocks this Block contains.
     */
    private List<Block> childrenBlocks = new ArrayList<Block>();

    /**
     * The Block containing this Block.
     */
    private Block parentBlock;

    /**
     * The next Sibling Block or null if no next sibling exists.
     */
    private Block nextSiblingBlock;

    /**
     * The previous Sibling Block or null if no previous sibling exists.
     */
    private Block previousSiblingBlock;

    /**
     * Empty constructor to construct an empty block.
     */
    public AbstractBlock()
    {
        // Nothing to do
    }

    /**
     * Construct a block with parameters.
     * 
     * @param parameters the parameters to set
     */
    public AbstractBlock(Map<String, String> parameters)
    {
        this.parameters.putAll(parameters);
    }

    /**
     * Constructs a block with a child block.
     * 
     * @param childBlock the child block of this block
     * @since 3.0M1
     */
    public AbstractBlock(Block childBlock)
    {
        this(childBlock, Collections.<String, String> emptyMap());
    }

    /**
     * Constructs a block with children blocks.
     * 
     * @param childrenBlocks the list of children blocks of the block to construct
     * @since 3.0M1
     */
    public AbstractBlock(List< ? extends Block> childrenBlocks)
    {
        this(childrenBlocks, Collections.<String, String> emptyMap());
    }

    /**
     * Construct a block with a child block and parameters.
     * 
     * @param childBlock the child block of this block
     * @param parameters the parameters to set
     * @since 3.0M1
     */
    public AbstractBlock(Block childBlock, Map<String, String> parameters)
    {
        this(parameters);
        addChild(childBlock);
    }

    /**
     * Construct a block with children blocks and parameters.
     * 
     * @param childrenBlocks the list of children blocks of the block to construct
     * @param parameters the parameters to set
     * @since 3.0M1
     */
    public AbstractBlock(List< ? extends Block> childrenBlocks, Map<String, String> parameters)
    {
        this(parameters);
        addChildren(childrenBlocks);
    }

    @Override
    public void addChild(Block blockToAdd)
    {
        insertChildAfter(blockToAdd, null);
    }

    @Override
    public void addChildren(List< ? extends Block> blocksToAdd)
    {
        for (Block blockToAdd : blocksToAdd) {
            addChild(blockToAdd);
        }
    }

    @Override
    public void setChildren(List< ? extends Block> children)
    {
        this.childrenBlocks.clear();

        addChildren(children);
    }

    /**
     * {@inheritDoc}
     * @since 2.6RC1
     */
    @Override
    public void setNextSiblingBlock(Block nextSiblingBlock)
    {
        this.nextSiblingBlock = nextSiblingBlock;
    }

    /**
     * {@inheritDoc}
     * @since 2.6RC1
     */
    @Override
    public void setPreviousSiblingBlock(Block previousSiblingBlock)
    {
        this.previousSiblingBlock = previousSiblingBlock;
    }

    @Override
    public void insertChildBefore(Block blockToInsert, Block nextBlock)
    {
        blockToInsert.setParent(this);

        if (nextBlock == null) {
            // Last block becomes last but one
            if (!this.childrenBlocks.isEmpty()) {
                Block lastBlock = this.childrenBlocks.get(this.childrenBlocks.size() - 1);
                blockToInsert.setPreviousSiblingBlock(lastBlock);
                lastBlock.setNextSiblingBlock(blockToInsert);
            } else {
                blockToInsert.setPreviousSiblingBlock(null);
            }
            blockToInsert.setNextSiblingBlock(null);
            this.childrenBlocks.add(blockToInsert);
        } else {
            // If there's a previous block to nextBlock then get it to set its next sibling
            Block previousBlock = nextBlock.getPreviousSibling();
            if (previousBlock != null) {
                previousBlock.setNextSiblingBlock(blockToInsert);
                blockToInsert.setPreviousSiblingBlock(previousBlock);
            } else {
                blockToInsert.setPreviousSiblingBlock(null);
            }
            blockToInsert.setNextSiblingBlock(nextBlock);
            nextBlock.setPreviousSiblingBlock(blockToInsert);
            this.childrenBlocks.add(indexOfChild(nextBlock), blockToInsert);
        }
    }

    @Override
    public void insertChildAfter(Block blockToInsert, Block previousBlock)
    {
        if (previousBlock == null) {
            insertChildBefore(blockToInsert, null);
        } else {
            // If there's a next block to previousBlock then get it to set its previous sibling
            Block nextBlock = previousBlock.getNextSibling();
            if (nextBlock != null) {
                nextBlock.setPreviousSiblingBlock(blockToInsert);
                blockToInsert.setNextSiblingBlock(nextBlock);
            } else {
                blockToInsert.setNextSiblingBlock(null);
            }
            blockToInsert.setPreviousSiblingBlock(previousBlock);
            previousBlock.setNextSiblingBlock(blockToInsert);
            this.childrenBlocks.add(indexOfChild(previousBlock) + 1, blockToInsert);
        }
    }

    @Override
    public void replaceChild(Block newBlock, Block oldBlock)
    {
        replaceChild(Collections.singletonList(newBlock), oldBlock);
    }

    @Override
    public void replaceChild(List<Block> newBlocks, Block oldBlock)
    {
        int position = indexOfChild(oldBlock);

        if (position == -1) {
            throw new InvalidParameterException("Provided Block to replace is not a child");
        }

        List<Block> blocks = getChildren();

        // Remove old child
        blocks.remove(position);
        oldBlock.setParent(null);

        // Insert new children
        Block previousBlock = oldBlock.getPreviousSibling();
        if (newBlocks.isEmpty()) {
            previousBlock.setNextSiblingBlock(oldBlock.getNextSibling());
        }
        Block lastBlock = null;
        for (Block block : newBlocks) {
            block.setParent(this);
            block.setPreviousSiblingBlock(previousBlock);
            if (previousBlock != null) {
                previousBlock.setNextSiblingBlock(block);
            }
            previousBlock = block;
            lastBlock = block;
        }
        Block nextBlock = oldBlock.getNextSibling();
        if (nextBlock != null) {
            nextBlock.setPreviousSiblingBlock(lastBlock);
        }
        if (lastBlock != null) {
            lastBlock.setNextSiblingBlock(nextBlock);
        }

        blocks.addAll(position, newBlocks);

        oldBlock.setNextSiblingBlock(null);
        oldBlock.setPreviousSiblingBlock(null);
    }

    /**
     * Get the position of the provided block in the list of children.
     * <p>
     * Can't use {@link List#indexOf(Object)} since it's using {@link Object#equals(Object)} internally which is not
     * what we want since two WordBlock with the same text or two spaces are equals for example but we want to be able
     * to target one specific Block.
     * 
     * @param block the block
     * @return the position of the block, -1 if the block can't be found
     */
    private int indexOfChild(Block block)
    {
        return indexOfBlock(block, getChildren());
    }

    /**
     * Get the position of the provided block in the provided list of blocks.
     * <p>
     * Can't use {@link List#indexOf(Object)} since it's using {@link Object#equals(Object)} internally which is not
     * what we want since two WordBlock with the same text or two spaces are equals for example but we want to be able
     * to target one specific Block.
     * 
     * @param block the block for which to find the position
     * @param blocks the list of blocks in which to look for the passed block
     * @return the position of the block, -1 if the block can't be found
     */
    private int indexOfBlock(Block block, List<Block> blocks)
    {
        int position = 0;

        for (Block child : blocks) {
            if (child == block) {
                return position;
            }
            ++position;
        }

        return -1;
    }

    @Override
    public List<Block> getChildren()
    {
        return this.childrenBlocks;
    }

    @Override
    public Block getParent()
    {
        return this.parentBlock;
    }

    @Override
    public Map<String, String> getParameters()
    {
        return Collections.unmodifiableMap(this.parameters);
    }

    @Override
    public String getParameter(String name)
    {
        return this.parameters.get(name);
    }

    @Override
    public void setParameter(String name, String value)
    {
        this.parameters.put(name, value);
    }

    /**
     * {@inheritDoc}
     * @since 1.7M2
     */
    @Override
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters.putAll(parameters);
    }

    @Override
    public void setParent(Block parentBlock)
    {
        this.parentBlock = parentBlock;
    }

    @Override
    public Block getRoot()
    {
        Block block = this;

        while (block.getParent() != null) {
            block = block.getParent();
        }

        return block;
    }

    /**
     * {@inheritDoc}
     * @since 2.6RC1
     */
    @Override
    public Block getNextSibling()
    {
        return this.nextSiblingBlock;
    }

    /**
     * {@inheritDoc}
     * @since 2.6RC1
     */
    @Override
    public Block getPreviousSibling()
    {
        return this.previousSiblingBlock;
    }

    /**
     * {@inheritDoc}
     * @since 2.6RC1
     */
    @Override
    public void removeBlock(Block childBlockToRemove)
    {
        getChildren().remove(childBlockToRemove);
        if (childBlockToRemove != null) {
            Block previousBlock = childBlockToRemove.getPreviousSibling();
            if (previousBlock != null) {
                previousBlock.setNextSiblingBlock(childBlockToRemove.getNextSibling());
            }
            Block nextBlock = childBlockToRemove.getNextSibling();
            if (nextBlock != null) {
                nextBlock.setPreviousSiblingBlock(previousBlock);
            }
            childBlockToRemove.setNextSiblingBlock(null);
            childBlockToRemove.setPreviousSiblingBlock(null);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public Block clone()
    {
        return clone(null);
    }

    /**
     * {@inheritDoc}
     * @since 1.8RC2
     */
    @Override
    public Block clone(BlockFilter blockFilter)
    {
        Block block;
        try {
            block = (AbstractBlock) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen
            throw new RuntimeException("Failed to clone object", e);
        }

        ((AbstractBlock) block).parameters = new LinkedHashMap<String, String>(this.parameters);

        ((AbstractBlock) block).childrenBlocks = new ArrayList<Block>(this.childrenBlocks.size());
        for (Block childBlock : this.childrenBlocks) {
            if (blockFilter != null) {
                Block clonedChildBlocks = childBlock.clone(blockFilter);

                List<Block> filteredBlocks = blockFilter.filter(clonedChildBlocks);

                if (filteredBlocks.size() == 0) {
                    filteredBlocks = clonedChildBlocks.getChildren();
                }

                block.addChildren(filteredBlocks);
            } else {
                block.addChild(childBlock.clone());
            }
        }

        return block;
    }

    @Override
    public void traverse(Listener listener)
    {
        before(listener);

        for (Block block : getChildren()) {
            block.traverse(listener);
        }

        after(listener);
    }

    /**
     * Send {@link org.xwiki.rendering.listener.Listener} events corresponding to the start of the block. For example
     * for a Bold block, this allows an XHTML Listener (aka a Renderer) to output <code>&lt;b&gt;</code>.
     * 
     * @param listener the listener that will receive the events sent by this block before its children blocks have
     *            emitted their own events.
     */
    public void before(Listener listener)
    {
        // Do nothing by default, should be overridden by extending Blocks
    }

    /**
     * Send {@link Listener} events corresponding to the end of the block. For example for a Bold block, this allows an
     * XHTML Listener (aka a Renderer) to output <code>&lt;/b&gt;</code>.
     * 
     * @param listener the listener that will receive the events sent by this block before its children blocks have
     *            emitted their own events.
     */
    public void after(Listener listener)
    {
        // Do nothing by default, should be overridden by extending Blocks
    }

    @Override
    public <T extends Block> List<T> getBlocks(BlockMatcher matcher, Axes axes)
    {
        List<T> blocks = null;

        if (axes == Axes.SELF) {
            blocks = addBlock(this, matcher, blocks);
        } else if (axes.compareTo(Axes.ANCESTOR_OR_SELF) <= 0) {
            blocks = getAncestorBlocks(matcher, axes);
        } else if (axes.compareTo(Axes.DESCENDANT_OR_SELF) <= 0) {
            blocks = getDescendantBlocks(matcher, axes);
        } else {
            blocks = getSiblingBlocks(matcher, axes);
        }

        return blocks != null ? blocks : Collections.<T> emptyList();
    }

    /**
     * Get all blocks following provided {@link BlockMatcher} and ancestor {@link Axes}.
     * 
     * @param <T> the class of the Blocks to return
     * @param matcher filter the blocks to return
     * @param axes indicate the search axes
     * @return the matched {@link Block}s, empty list of none was found
     */
    private <T extends Block> List<T> getAncestorBlocks(BlockMatcher matcher, Axes axes)
    {
        List<T> blocks = null;

        T nextBlock = (T) getParent();
        Axes nextAxes = axes;

        switch (axes) {
            case ANCESTOR_OR_SELF:
                blocks = addBlock(this, matcher, blocks);
                break;
            case ANCESTOR:
                nextAxes = Axes.ANCESTOR_OR_SELF;
                break;
            case PARENT:
                nextAxes = Axes.SELF;
                break;
            default:
                break;
        }

        if (nextBlock != null) {
            blocks = getBlocks(nextBlock, matcher, nextAxes, blocks);
        }

        return blocks != null ? blocks : Collections.<T> emptyList();
    }

    /**
     * Get all blocks following provided {@link BlockMatcher} and descendant {@link Axes}.
     * 
     * @param <T> the class of the Blocks to return
     * @param matcher filter the blocks to return
     * @param axes indicate the search axes
     * @return the matched {@link Block}s, empty list of none was found
     */
    private <T extends Block> List<T> getDescendantBlocks(BlockMatcher matcher, Axes axes)
    {
        List<T> blocks = null;

        T nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case CHILD:
                if (!getChildren().isEmpty()) {
                    nextBlock = (T) getChildren().get(0);
                    nextAxes = Axes.FOLLOWING_SIBLING;
                    blocks = addBlock(nextBlock, matcher, blocks);
                }
                break;
            case DESCENDANT_OR_SELF:
                blocks = addBlock(this, matcher, blocks);
                blocks = getBlocks((List) getChildren(), matcher, Axes.DESCENDANT_OR_SELF, blocks);
                break;
            case DESCENDANT:
                blocks = getBlocks((List) getChildren(), matcher, Axes.DESCENDANT_OR_SELF, blocks);
                break;
            default:
                break;
        }

        if (nextBlock != null) {
            blocks = getBlocks(nextBlock, matcher, nextAxes, blocks);
        }

        return blocks != null ? blocks : Collections.<T> emptyList();
    }

    /**
     * Get all blocks following provided {@link BlockMatcher} and following/preceding sibling {@link Axes}.
     * 
     * @param <T> the class of the Blocks to return
     * @param matcher filter the blocks to return
     * @param axes indicate the search axes
     * @return the matched {@link Block}s, empty list of none was found
     */
    private <T extends Block> List<T> getSiblingBlocks(BlockMatcher matcher, Axes axes)
    {
        List<T> blocks = null;

        T nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            // FOLLOWING
            case FOLLOWING_SIBLING:
                nextBlock = (T) getNextSibling();
                blocks = addBlock(nextBlock, matcher, blocks);
                break;
            case FOLLOWING:
                for (Block nextSibling = getNextSibling(); nextSibling != null; nextSibling =
                        nextSibling.getNextSibling()) {
                    blocks = getBlocks((T) nextSibling, matcher, Axes.DESCENDANT_OR_SELF, blocks);
                }
                break;
            // PRECEDING
            case PRECEDING_SIBLING:
                nextBlock = (T) getPreviousSibling();
                blocks = addBlock(nextBlock, matcher, blocks);
                break;
            case PRECEDING:
                for (Block previousSibling = getPreviousSibling(); previousSibling != null; previousSibling =
                        previousSibling.getPreviousSibling()) {
                    blocks = getBlocks((T) previousSibling, matcher, Axes.DESCENDANT_OR_SELF, blocks);
                }
                break;
            default:
                break;
        }

        if (nextBlock != null) {
            blocks = getBlocks(nextBlock, matcher, nextAxes, blocks);
        }

        return blocks != null ? blocks : Collections.<T> emptyList();
    }

    /**
     * Add provided {@link Block} to provided list (or create list of null) if block validate the provided
     * {@link BlockMatcher}.
     * 
     * @param <T> the class of the Blocks to return
     * @param block the block
     * @param matcher the matcher
     * @param blocks the list of blocks to fill
     * @return the modified list, null if provided list is null and provided {@link Block} does not validate provided
     *         {@link BlockMatcher}
     */
    private <T extends Block> List<T> addBlock(Block block, BlockMatcher matcher, List<T> blocks)
    {
        List<T> newBlocks = blocks;

        if (block != null && matcher.match(block)) {
            if (newBlocks == null) {
                newBlocks = new ArrayList<T>();
            }
            newBlocks.add((T) block);
        }

        return newBlocks;
    }

    /**
     * Add all blocks following provided {@link BlockMatcher} and {@link Axes} in the provide list (or create a new list
     * of provided list is null).
     * 
     * @param <T> the class of the Blocks to return
     * @param blocks the blocks from where to search
     * @param matcher the block matcher
     * @param axes the axes
     * @param blocksOut the list of blocks to fill
     * @return the modified list, null if provided list is null and provided {@link Block} does not validate provided
     *         {@link BlockMatcher}
     */
    private <T extends Block> List<T> getBlocks(List<T> blocks, BlockMatcher matcher, Axes axes,
        List<T> blocksOut)
    {
        List<T> newBlocks = blocksOut;

        for (T child : blocks) {
            newBlocks = getBlocks(child, matcher, axes, newBlocks);
        }

        return newBlocks;
    }

    /**
     * Add all blocks following provided {@link BlockMatcher} and {@link Axes} in the provide list (or create a new list
     * of provided list is null).
     * 
     * @param <T> the class of the Blocks to return
     * @param block the block from where to search
     * @param matcher the block matcher
     * @param axes the axes
     * @param blocksOut the list of blocks to fill
     * @return the modified list, null if provided list is null and provided {@link Block} does not validate provided
     *         {@link BlockMatcher}
     */
    private <T extends Block> List<T> getBlocks(T block, BlockMatcher matcher, Axes axes, List<T> blocksOut)
    {
        List<T> newBlocks = blocksOut;

        List<T> nextBlocks = block.getBlocks(matcher, axes);
        if (!nextBlocks.isEmpty()) {
            if (newBlocks == null) {
                newBlocks = nextBlocks;
            } else {
                newBlocks.addAll(nextBlocks);
            }
        }

        return newBlocks;
    }

    @Override
    public <T extends Block> T getFirstBlock(BlockMatcher matcher, Axes axes)
    {
        T block = null;

        if (axes == Axes.SELF) {
            if (matcher.match(this)) {
                block = (T) this;
            }
        } else if (axes.compareTo(Axes.ANCESTOR_OR_SELF) <= 0) {
            block = (T) getFirstAncestorBlock(matcher, axes);
        } else if (axes.compareTo(Axes.DESCENDANT_OR_SELF) <= 0) {
            block = (T) getFirstDescendantBlock(matcher, axes);
        } else if (axes.compareTo(Axes.FOLLOWING_SIBLING) <= 0) {
            block = (T) getFirstFollowingSiblingBlock(matcher, axes);
        } else {
            block = (T) getFirstPrecedingSiblingBlock(matcher, axes);
        }

        return block;
    }

    /**
     * Get the first matched block in the provided ancestor {@link Axes}.
     * 
     * @param matcher the block matcher
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstAncestorBlock(BlockMatcher matcher, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case ANCESTOR_OR_SELF:
                if (matcher.match(this)) {
                    return this;
                }
            case ANCESTOR:
            case PARENT:
                nextAxes = axes == Axes.PARENT ? Axes.SELF : Axes.ANCESTOR_OR_SELF;
                nextBlock = getParent();
                break;
            default:
                break;
        }

        return nextBlock != null ? nextBlock.getFirstBlock(matcher, nextAxes) : null;
    }

    /**
     * Get the first matched block in the provided descendant {@link Axes}.
     * 
     * @param matcher the block matcher
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstDescendantBlock(BlockMatcher matcher, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case CHILD:
                if (!this.childrenBlocks.isEmpty()) {
                    nextBlock = this.childrenBlocks.get(0);
                    nextAxes = Axes.FOLLOWING_SIBLING;
                    if (matcher.match(nextBlock)) {
                        return nextBlock;
                    }
                }
                break;
            case DESCENDANT_OR_SELF:
                if (matcher.match(this)) {
                    return this;
                }
            case DESCENDANT:
                for (Block child : this.childrenBlocks) {
                    Block matchedBlock = child.getFirstBlock(matcher, Axes.DESCENDANT_OR_SELF);
                    if (matchedBlock != null) {
                        return matchedBlock;
                    }
                }
                break;
            default:
                break;
        }

        return nextBlock != null ? nextBlock.getFirstBlock(matcher, nextAxes) : null;
    }

    /**
     * Get the first matched block in the provided following sibling {@link Axes}.
     * 
     * @param matcher the block matcher
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstFollowingSiblingBlock(BlockMatcher matcher, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case FOLLOWING_SIBLING:
                nextBlock = getNextSibling();
                if (nextBlock != null && matcher.match(nextBlock)) {
                    return nextBlock;
                }
                break;
            case FOLLOWING:
                for (Block nextSibling = getNextSibling(); nextSibling != null; nextSibling =
                        nextSibling.getNextSibling()) {
                    Block matchedBlock = nextSibling.getFirstBlock(matcher, Axes.DESCENDANT_OR_SELF);
                    if (matchedBlock != null) {
                        return matchedBlock;
                    }
                }
                break;
            default:
                break;
        }

        return nextBlock != null ? nextBlock.getFirstBlock(matcher, nextAxes) : null;
    }

    /**
     * Get the first matched block in the provided preceding sibling {@link Axes}.
     * 
     * @param matcher the block matcher
     * @param axes the axes
     * @return the matched {@link Block}, null if none was found
     */
    private Block getFirstPrecedingSiblingBlock(BlockMatcher matcher, Axes axes)
    {
        Block nextBlock = null;
        Axes nextAxes = axes;

        switch (axes) {
            case PRECEDING_SIBLING:
                nextBlock = getPreviousSibling();
                if (nextBlock != null && matcher.match(nextBlock)) {
                    return nextBlock;
                }
                break;
            case PRECEDING:
                for (Block previousSibling = getPreviousSibling(); previousSibling != null; previousSibling =
                        previousSibling.getPreviousSibling()) {
                    Block matchedBlock = previousSibling.getFirstBlock(matcher, Axes.DESCENDANT_OR_SELF);
                    if (matchedBlock != null) {
                        return matchedBlock;
                    }
                }
                break;
            default:
                break;
        }

        return nextBlock != null ? nextBlock.getFirstBlock(matcher, nextAxes) : null;
    }

    @Deprecated
    @Override
    public <T extends Block> List<T> getChildrenByType(Class<T> blockClass, boolean recurse)
    {
        return getBlocks(new ClassBlockMatcher(blockClass), recurse ? Axes.DESCENDANT : Axes.CHILD);
    }

    @Deprecated
    @Override
    public <T extends Block> T getPreviousBlockByType(Class<T> blockClass, boolean recurse)
    {
        // Don't use #getFirstBlock(BlockMatcher, Axes) for retro-compatibility because it's a bit different:
        // #getFirstBlock follows XPATH axes specifications and does not include "ancestors" in "preceding" axis

        if (getParent() == null) {
            return null;
        }

        int index = indexOfBlock(this, getParent().getChildren());

        // test previous brothers
        List<Block> blocks = getParent().getChildren();
        for (int i = index - 1; i >= 0; --i) {
            Block previousBlock = blocks.get(i);
            if (blockClass.isAssignableFrom(previousBlock.getClass())) {
                return blockClass.cast(previousBlock);
            }
        }

        // test parent
        if (blockClass.isAssignableFrom(getParent().getClass())) {
            return blockClass.cast(getParent());
        }

        // recurse
        return recurse ? getParent().getPreviousBlockByType(blockClass, true) : null;
    }

    @Deprecated
    @Override
    public <T extends Block> T getParentBlockByType(Class<T> blockClass)
    {
        return blockClass.cast(getFirstBlock(new ClassBlockMatcher(blockClass), Axes.ANCESTOR));
    }
}

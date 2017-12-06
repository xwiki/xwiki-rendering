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
import org.xwiki.rendering.block.match.BlockNavigator;
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
    private Map<String, String> parameters;

    /**
     * The Blocks this Block contains.
     */
    private List<Block> childrenBlocks;

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
        setParameters(parameters);
    }

    /**
     * Constructs a block with a child block.
     *
     * @param childBlock the child block of this block
     * @since 3.0M1
     */
    public AbstractBlock(Block childBlock)
    {
        this(childBlock, Collections.<String, String>emptyMap());
    }

    /**
     * Constructs a block with children blocks.
     *
     * @param childrenBlocks the list of children blocks of the block to construct
     * @since 3.0M1
     */
    public AbstractBlock(List<? extends Block> childrenBlocks)
    {
        this(childrenBlocks, Collections.<String, String>emptyMap());
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
    public AbstractBlock(List<? extends Block> childrenBlocks, Map<String, String> parameters)
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
    public void addChildren(List<? extends Block> blocksToAdd)
    {
        if (!blocksToAdd.isEmpty()) {
            if (this.childrenBlocks == null) {
                // Create the list with just the exact required size
                this.childrenBlocks = new ArrayList<Block>(blocksToAdd.size());
            }

            for (Block blockToAdd : blocksToAdd) {
                addChild(blockToAdd);
            }
        }
    }

    @Override
    public void setChildren(List<? extends Block> children)
    {
        if (children.isEmpty()) {
            if (this.childrenBlocks != null) {
                this.childrenBlocks.clear();
            }
        } else {
            if (this.childrenBlocks != null) {
                this.childrenBlocks.clear();
            }

            addChildren(children);
        }
    }

    @Override
    public void setNextSiblingBlock(Block nextSiblingBlock)
    {
        this.nextSiblingBlock = nextSiblingBlock;
    }

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
            if (this.childrenBlocks != null && !this.childrenBlocks.isEmpty()) {
                Block lastBlock = this.childrenBlocks.get(this.childrenBlocks.size() - 1);
                blockToInsert.setPreviousSiblingBlock(lastBlock);
                lastBlock.setNextSiblingBlock(blockToInsert);
            } else {
                blockToInsert.setPreviousSiblingBlock(null);

                if (this.childrenBlocks == null) {
                    this.childrenBlocks = new ArrayList<Block>(1);
                }
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
            if (this.childrenBlocks == null || this.childrenBlocks.isEmpty()) {
                this.childrenBlocks = new ArrayList<Block>(1);
                this.childrenBlocks.add(blockToInsert);
            } else {
                this.childrenBlocks.add(indexOfChild(nextBlock), blockToInsert);
            }
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
            if (this.childrenBlocks == null) {
                this.childrenBlocks = new ArrayList<Block>(1);
            }
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
            if (child.equals(block)) {
                return position;
            }
            ++position;
        }

        return -1;
    }

    @Override
    public List<Block> getChildren()
    {
        return this.childrenBlocks == null ? Collections.<Block>emptyList() : this.childrenBlocks;
    }

    @Override
    public Block getParent()
    {
        return this.parentBlock;
    }

    @Override
    public Map<String, String> getParameters()
    {
        return this.parameters == null ? Collections.<String, String>emptyMap() : Collections
            .unmodifiableMap(this.parameters);
    }

    @Override
    public String getParameter(String name)
    {
        return this.parameters == null ? null : this.parameters.get(name);
    }

    @Override
    public void setParameter(String name, String value)
    {
        if (this.parameters == null) {
            this.parameters = new LinkedHashMap<>(1);
        }

        this.parameters.put(name, value);
    }

    @Override
    public void setParameters(Map<String, String> parameters)
    {
        if (this.parameters == null) {
            this.parameters = new LinkedHashMap<>(parameters);
        } else {
            this.parameters.clear();
            this.parameters.putAll(parameters);
        }
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

    @Override
    public Block getNextSibling()
    {
        return this.nextSiblingBlock;
    }

    @Override
    public Block getPreviousSibling()
    {
        return this.previousSiblingBlock;
    }

    @Override
    public void removeBlock(Block childBlockToRemove)
    {
        // Remove block
        List<Block> children = getChildren();
        int position = indexOfBlock(childBlockToRemove, children);
        if (position == -1) {
            throw new InvalidParameterException("Provided Block to remove is not a child");
        }
        getChildren().remove(position);

        // Re-calculate internal links between blocks
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
        if (obj == this) {
            return true;
        }

        if (obj instanceof Block) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getChildren(), ((Block) obj).getChildren());
            builder.append(getParameters(), ((Block) obj).getParameters());

            return builder.isEquals();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.childrenBlocks);
        builder.append(this.parameters);

        return builder.toHashCode();
    }

    @Override
    public Block clone()
    {
        return clone(null);
    }

    /**
     * {@inheritDoc}
     *
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

        if (this.parameters != null) {
            ((AbstractBlock) block).parameters = new LinkedHashMap<>(this.parameters);
        }

        if (this.childrenBlocks != null) {
            ((AbstractBlock) block).childrenBlocks = new ArrayList<Block>(this.childrenBlocks.size());
            for (Block childBlock : this.childrenBlocks) {
                if (blockFilter != null) {
                    Block clonedChildBlocks = childBlock.clone(blockFilter);

                    List<Block> filteredBlocks = blockFilter.filter(clonedChildBlocks);

                    if (filteredBlocks.isEmpty()) {
                        filteredBlocks = clonedChildBlocks.getChildren();
                    }

                    block.addChildren(filteredBlocks);
                } else {
                    block.addChild(childBlock.clone());
                }
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
        BlockNavigator navigator = new BlockNavigator(matcher);

        return navigator.getBlocks(this, axes);
    }

    @Override
    public <T extends Block> T getFirstBlock(BlockMatcher matcher, Axes axes)
    {
        BlockNavigator navigator = new BlockNavigator(matcher);

        return navigator.getFirstBlock(this, axes);
    }
}

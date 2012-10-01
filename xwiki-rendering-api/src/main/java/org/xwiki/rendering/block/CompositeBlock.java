package org.xwiki.rendering.block;

import java.util.Collections;
import java.util.List;

/**
 * Represent a list of blocks.
 * <p>
 * This block itself does not have any meaning (it does not have any corresponding rendering stream event) and is just
 * here to pass together several Blocks to a method or as a return value as a {@link Block}.
 * 
 * @version $Id$
 * @since 4.3M1
 */
public class CompositeBlock extends AbstractBlock
{
    /**
     * Create an empty composite block with no children. This is useful when the user wants to call
     * {@link #addChild(Block)} manually for adding children one by one after the block is constructed.
     */
    public CompositeBlock()
    {
        this(Collections.<Block> emptyList());
    }

    /**
     * @param blocks the blocks
     */
    public CompositeBlock(List<Block> blocks)
    {
        super(blocks);
    }
}

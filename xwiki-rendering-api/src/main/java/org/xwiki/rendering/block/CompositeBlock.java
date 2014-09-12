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

import java.util.Collections;
import java.util.List;

/**
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
        this(Collections.<Block>emptyList());
    }

    /**
     * @param blocks the blocks
     */
    public CompositeBlock(List<Block> blocks)
    {
        super(blocks);
    }

    @Override
    public String toString()
    {
        return getChildren().toString();
    }
}

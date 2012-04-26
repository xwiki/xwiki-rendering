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

import org.xwiki.rendering.block.Block;

/**
 * Add a backward compatibility layer to the {@link Block} class.
 *
 * @version $Id$
 * @since 4.1M1
 */
public interface CompatibilityBlock
{
    /**
     * Recursively look at parents to find a block which inherits or is provided type.
     * <p>
     * The difference with {@link #getPreviousBlockByType(Class, boolean)} is that this one only look at parent when
     * {@link #getPreviousBlockByType(Class, boolean)} look at previous block in the same parent.
     *
     * @param <T> the class of the Blocks to return
     * @param blockClass the block class to look for
     * @return the found block, null if nothing is found
     * @since 1.9.1
     * @deprecated since 3.0M3 use {@link #getBlocks(BlockMatcher, Axes)} instead
     */
    @Deprecated
    <T extends Block> T getParentBlockByType(Class<T> blockClass);
}

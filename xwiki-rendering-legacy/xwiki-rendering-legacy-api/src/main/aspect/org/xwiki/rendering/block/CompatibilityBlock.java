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

import java.util.List;

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
     * @deprecated since 3.0M3 use {@link Block#getBlocks(BlockMatcher, Axes)} instead
     */
    @Deprecated
    <T extends Block> T getParentBlockByType(Class<T> blockClass);

    /**
     * Look upward to find a block which inherit or is provided type.
     * <p>
     * The difference with {@link #getParentBlockByType(Class)} is that this one look also at previous block in the same
     * parent when {@link #getParentBlockByType(Class)} only look at parents.
     *
     * @param <T> the class of the Blocks to return
     * @param blockClass the block class to look for
     * @param recurse if true also search in parents levels
     * @return the found block, null if nothing is found
     * @since 1.6M1
     * @deprecated since 3.0M3 use {@link Block#getBlocks(BlockMatcher, Axes)} instead
     */
    @Deprecated
    <T extends Block> T getPreviousBlockByType(Class<T> blockClass, boolean recurse);

    /**
     * Gets all the Blocks in the tree which are of the passed Block class.
     *
     * @param <T> the class of the Blocks to return
     * @param blockClass the block class to look for
     * @param recurse if true also search recursively children
     * @return all the matching blocks
     * @since 1.6M1
     * @deprecated since 3.0M3 use {@code Block#getBlocks(new ClassBlockMatcher(blockClass), Axes.DESCENDANT)} instead
     *             if {@code recurse} was true and
     *             {@code Block#getBlocks(new ClassBlockMatcher(blockClass), Axes.CHILD)} otherwise
     */
    @Deprecated
    <T extends Block> List<T> getChildrenByType(Class<T> blockClass, boolean recurse);
}

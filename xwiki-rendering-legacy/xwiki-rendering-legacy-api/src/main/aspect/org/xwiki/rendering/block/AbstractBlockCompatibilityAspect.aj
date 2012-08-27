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

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Add a backward compatibility layer to the {@link AbstractBlock} class.
 *
 * @version $Id$
 * @since 4.1M1
 */
public privileged aspect AbstractBlockCompatibilityAspect
{
    @Deprecated
    public <T extends Block> T AbstractBlock.getParentBlockByType(Class<T> blockClass)
    {
        return blockClass.cast(getFirstBlock(new ClassBlockMatcher(blockClass), Axes.ANCESTOR));
    }

    @Deprecated
    public <T extends Block> T AbstractBlock.getPreviousBlockByType(Class<T> blockClass, boolean recurse)
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
    public <T extends Block> List<T> AbstractBlock.getChildrenByType(Class<T> blockClass, boolean recurse)
    {
        return getBlocks(new ClassBlockMatcher(blockClass), recurse ? Axes.DESCENDANT : Axes.CHILD);
    }
}

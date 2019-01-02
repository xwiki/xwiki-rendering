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

import org.xwiki.rendering.block.Block;

/**
 * Implementation of {@link BlockMatcher} which match any {@link Block} with the provided class.
 *
 * @version $Id$
 * @since 10.10RC1
 */
public class CounterBlockMatcher implements BlockMatcher
{
    private long count = -1;

    private Block stopBlock;

    /**
     * Count all blocks.
     */
    public CounterBlockMatcher()
    {

    }

    /**
     * Find the index of the passed block.
     * 
     * @param stopBlock the block where to stop counting
     */
    public CounterBlockMatcher(Block stopBlock)
    {
        this.stopBlock = stopBlock;
    }

    @Override
    public boolean match(Block block)
    {
        ++this.count;

        return this.stopBlock == block;
    }

    /**
     * @return the count
     */
    public long getCount()
    {
        return count;
    }
}

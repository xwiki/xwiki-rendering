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
import java.util.List;

import org.xwiki.rendering.block.Block;

/**
 * Implementation of {@link org.xwiki.rendering.block.match.BlockMatcher} which matches blocks by matching with any
 * of the configured matchers. This is different from {@link CompositeBlockMatcher} which does an {@code AND}.
 *
 * @version $Id$
 * @since 4.3M2
 */
public class OrBlockMatcher implements BlockMatcher
{
    /**
     * The matchers to match against.
     */
    private List<BlockMatcher> matchers = new ArrayList<BlockMatcher>();

    /**
     * @param matchers list of matchers to add
     */
    public OrBlockMatcher(List<BlockMatcher> matchers)
    {
        this.matchers.addAll(matchers);
    }

    /**
     * @param matchers vararg list of matchers to add
     */
    public OrBlockMatcher(BlockMatcher... matchers)
    {
        for (BlockMatcher matcher : matchers) {
            this.matchers.add(matcher);
        }
    }

    @Override
    public boolean match(Block block)
    {
        boolean result = false;
        for (BlockMatcher matcher : this.matchers) {
            if (matcher.match(block)) {
                result = true;
                break;
            }
        }
        return result;
    }
}

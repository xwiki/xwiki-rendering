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
import java.util.Arrays;
import java.util.List;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroMarkerBlock;

/**
 * Implementation of {@link BlockMatcher} which matches {@link MacroMarkerBlock}s for a list of provided macro names.
 *
 * @version $Id$
 * @since 11.4
 */
public class MacroMarkerBlockMatcher extends ClassBlockMatcher
{
    /**
     * The macro ids to match.
     */
    private List<String> macroIds;

    /**
     * Match {@link MacroMarkerBlock}s having the passed ids.
     *
     * @param macroIds the macro ids to match
     */
    public MacroMarkerBlockMatcher(String... macroIds)
    {
        super(MacroMarkerBlock.class);
        this.macroIds = new ArrayList<>(Arrays.asList(macroIds));
    }

    @Override
    public boolean match(Block block)
    {
        return super.match(block) && this.macroIds.contains(((MacroMarkerBlock) block).getId());
    }
}

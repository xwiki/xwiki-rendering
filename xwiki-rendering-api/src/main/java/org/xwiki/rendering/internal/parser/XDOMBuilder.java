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

package org.xwiki.rendering.internal.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;

/**
 * Helper class to build a {@link XDOM} from listener events.
 *
 * @version $Id$
 * @since 6.0M1
 */
public class XDOMBuilder
{
    private Deque<List<Block>> stack = new ArrayDeque<List<Block>>();

    /**
     * Default constructor.
     */
    public XDOMBuilder()
    {
        startBlockList();
    }

    /**
     * @return the resulting {@link XDOM}.
     */
    public XDOM getXDOM()
    {
        List<Block> blocks = endBlockList();

        if (!this.stack.isEmpty()) {
            throw new IllegalStateException("Unbalanced begin/end Block events, missing " + this.stack.size()
                + " calls to endBlockList().");
        }

        // support even events without begin/endDocument for partial content
        if (!blocks.isEmpty() && blocks.get(0) instanceof XDOM) {
            return (XDOM) blocks.get(0);
        } else {
            return new XDOM(blocks);
        }
    }

    /**
     * Start a new container element.
     */
    public void startBlockList()
    {
        this.stack.push(new ArrayList<Block>());
    }

    /**
     * End a container element.
     *
     * @return the list of blocks in that container.
     */
    public List<Block> endBlockList()
    {
        try {
            return this.stack.pop();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Unbalanced begin/end Block events, too many calls to endBlockList().");
        }
    }

    /**
     * Add a block to the current block container.
     *
     * @param block the block to be added.
     */
    public void addBlock(Block block)
    {
        try {
            this.stack.getFirst().add(block);
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("All container blocks are closed, too many calls to endBlockList().");
        }
    }
}

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
package org.xwiki.rendering.internal.parser.blocknote.blocks;

import java.util.Deque;
import java.util.function.Consumer;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Interface for BlockNote block parsers.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Role
public interface BlockParser
{
    /**
     * Parse the given BlockNote block.
     *
     * @param block the BlockNote block to parse
     * @param contextStack the stack of contexts to use during parsing
     * @throws ParseException if any error occurs during parsing
     */
    void parse(ObjectNode block, Deque<Context> contextStack) throws ParseException;

    /**
     * Hook method called at the end of the parent block parsing.
     * 
     * @param contextStack the parsing context stack
     */
    default void onParentBlockEnd(Deque<Context> contextStack)
    {
        // By default, do nothing at the end of the parent block.
    }

    /**
     * Traverse the given block and its descendants, calling the given consumer for each visited block.
     * 
     * @param block the block to traverse
     * @param blockConsumer the consumer to call for each visited block
     * @throws ParseException if any error occurs during traversal
     */
    default void traverse(ObjectNode block, Consumer<ObjectNode> blockConsumer) throws ParseException
    {
        // We don't need to implement this for all blocks.
    }
}

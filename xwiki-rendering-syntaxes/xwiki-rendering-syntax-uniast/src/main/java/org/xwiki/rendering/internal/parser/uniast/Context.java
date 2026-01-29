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
package org.xwiki.rendering.internal.parser.uniast;

import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.util.IdGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Holds the context information required during UniAst parsing.
 *
 * @param listener the listener used to notify the XDOM events
 * @param idGenerator the identifier generator to use to generate unique IDs for images and headings
 * @param inline whether we are in an inline context or not
 * @param parent the parent block of the current block being processed
 * @param siblings the siblings of the current block being processed
 * @param sectionLevel the section level in the scope of the current parent block, or {@code null} if no section is open
 *            directly under the current parent block
 * @version $Id$
 * @since 18.1.0RC1
 */
public record Context(Listener listener, IdGenerator idGenerator, boolean inline, ObjectNode parent, ArrayNode siblings,
    HeaderLevel sectionLevel)
{
    /**
     * @param listener the listener to use
     * @return a new context with the given listener
     */
    public Context withListener(Listener listener)
    {
        return new Context(listener, this.idGenerator, this.inline, this.parent, this.siblings, this.sectionLevel);
    }

    /**
     * @param inline whether we are in an inline context or not
     * @return a new context with the given inline value
     */
    public Context withInline(boolean inline)
    {
        return new Context(this.listener, this.idGenerator, inline, this.parent, this.siblings, this.sectionLevel);
    }

    /**
     * @param parent the parent block
     * @param siblings the siblings of the current block
     * @return a new context with the given parent and siblings
     */
    public Context withParentAndSiblings(ObjectNode parent, ArrayNode siblings)
    {
        return new Context(this.listener, this.idGenerator, this.inline, parent, siblings, null);
    }

    /**
     * @param sectionLevel the new section level
     * @return a new context with the given section level
     */
    public Context withSectionLevel(HeaderLevel sectionLevel)
    {
        return new Context(this.listener, this.idGenerator, this.inline, this.parent, this.siblings, sectionLevel);
    }

    /**
     * @param block the block to find the index for
     * @return the index of the given block among its siblings, or -1 if not found
     */
    public int indexOf(ObjectNode block)
    {
        for (int i = 0; i < this.siblings.size(); i++) {
            if (this.siblings.get(i) == block) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param child the child block to check
     * @return whether the given child is the first among its siblings
     */
    public boolean isFirstChild(JsonNode child)
    {
        return this.siblings.get(0) == child;
    }

    /**
     * @param child the child block to check
     * @return whether the given child is the last among its siblings
     */
    public boolean isLastChild(JsonNode child)
    {
        return this.siblings.get(this.siblings.size() - 1) == child;
    }

    /**
     * @param child the child block to check
     * @return whether the given child is either the first or the last among its siblings
     */
    public boolean isFirstOrLastChild(JsonNode child)
    {
        return isFirstChild(child) || isLastChild(child);
    }
}

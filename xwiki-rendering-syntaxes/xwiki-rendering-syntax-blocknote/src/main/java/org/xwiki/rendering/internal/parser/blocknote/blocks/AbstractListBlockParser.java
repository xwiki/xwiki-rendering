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

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base class for list block parsers.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public abstract class AbstractListBlockParser extends AbstractBlockParser
{
    /**
     * The parameter used to specify the list type. We need this because XWiki rendering doesn't support natively all
     * the list types supported by BlockNote (e.g. check list, toggle list).
     */
    public static final String LIST_TYPE = "data-xwiki-list-type";

    @Override
    public void parse(ObjectNode listItemBlock, Deque<Context> contextStack) throws ParseException
    {
        Listener listener = contextStack.peek().listener();
        Map<String, String> parameters = getListParameters(listItemBlock);
        beginList(listener, parameters);

        for (ObjectNode item : getListItems(listItemBlock, contextStack)) {
            visitListItem(item, contextStack);
        }

        endList(listener, parameters);
    }

    protected Map<String, String> getListParameters(ObjectNode listItemBlock)
    {
        return getBlockParameters(listItemBlock);
    }

    protected List<ObjectNode> getListItems(ObjectNode listItemBlock, Deque<Context> contextStack)
    {
        List<ObjectNode> listItems = new ArrayList<>();
        listItems.add(listItemBlock);
        ArrayNode siblings = contextStack.peek().siblings();
        for (int i = contextStack.peek().indexOf(listItemBlock); 0 <= i && i < siblings.size() - 1; i++) {
            JsonNode nextSibling = siblings.get(i + 1);
            if (inSameList(listItemBlock, nextSibling)) {
                listItems.add((ObjectNode) nextSibling);
            } else {
                break;
            }
        }
        return listItems;
    }

    protected boolean inSameList(JsonNode alice, JsonNode bob)
    {
        return Objects.equals(alice.get(TYPE), bob.get(TYPE));

    }

    protected void visitListItem(ObjectNode listItemBlock, Deque<Context> contextStack) throws ParseException
    {
        // Make sure this list item is not processed again.
        listItemBlock.put(SKIP, true);

        Listener listener = contextStack.peek().listener();
        beginListItem(listItemBlock, listener);

        visitInlineChildBlocks(listItemBlock, CONTENT, contextStack);
        visitChildBlocks(listItemBlock, CHILDREN, contextStack);

        endListItem(listItemBlock, listener);
    }

    protected abstract void beginList(Listener listener, Map<String, String> parameters);

    protected void beginListItem(ObjectNode listItemBlock, org.xwiki.rendering.listener.Listener listener)
    {
        listener.beginListItem();
    }

    protected void endListItem(ObjectNode listItemBlock, org.xwiki.rendering.listener.Listener listener)
    {
        listener.endListItem();
    }

    protected abstract void endList(Listener listener, Map<String, String> parameters);
}

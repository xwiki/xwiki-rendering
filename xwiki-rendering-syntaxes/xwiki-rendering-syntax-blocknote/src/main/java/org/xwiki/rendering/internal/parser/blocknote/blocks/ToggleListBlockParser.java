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

import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.GroupBlockParser.GROUP;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.NumberedListBlockParser.NUMBERED_LIST_ITEM;

/**
 * Toggle list block parser.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named(ToggleListBlockParser.TOGGLE_LIST_ITEM)
@Singleton
public class ToggleListBlockParser extends BulletListBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String TOGGLE_LIST_ITEM = "toggleListItem";

    /**
     * The toggle list type. XWiki rendering doesn't support toggle lists natively, so we're using standard bullet lists
     * that are marked with a specific parameter.
     */
    public static final String TOGGLE_LIST_TYPE = "toggle";

    private static final Set<String> LIST_TYPES = Set.of(TOGGLE_LIST_ITEM, BULLETED_LIST_ITEM, NUMBERED_LIST_ITEM);

    @Override
    protected void visitListChildren(ObjectNode parentBlock, Deque<Context> contextStack) throws ParseException
    {
        Listener listener = contextStack.peek().listener();

        ArrayNode childBlocks = getChildBlocks(parentBlock, CHILDREN);

        // Generally, we want the children to be grouped. Scenarios where this is false are: when there are no
        // children, there is a single group child, the children are all of a list type.
        Set<String> types = childBlocks.valueStream()
            .filter(JsonNode::isObject)
            .map(node -> node.path(TYPE).asText())
            .collect(Collectors.toSet());
        if (childBlocks.isEmpty() || isXWikiGroup(childBlocks.iterator().next()) || (types.size() == 1
            && LIST_TYPES.contains(types.iterator().next())))
        {
            super.visitListChildren(parentBlock, contextStack);
        } else {
            listener.beginGroup(Collections.emptyMap());
            super.visitListChildren(parentBlock, contextStack);
            listener.endGroup(Collections.emptyMap());
        }
    }

    private boolean isXWikiGroup(JsonNode jsonNode)
    {
        return jsonNode.isObject() && GROUP.equals(jsonNode.path(TYPE).asText());
    }

    @Override
    protected Map<String, String> getListParameters(ObjectNode listItemBlock)
    {
        Map<String, String> parameters = super.getListParameters(listItemBlock);
        parameters.put(LIST_TYPE, TOGGLE_LIST_TYPE);
        return parameters;
    }
}

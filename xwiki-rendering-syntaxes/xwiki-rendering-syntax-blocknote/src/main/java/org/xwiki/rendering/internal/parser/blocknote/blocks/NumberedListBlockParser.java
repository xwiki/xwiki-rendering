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

import java.util.Map;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.ListType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Numbered list block parser.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named(NumberedListBlockParser.NUMBERED_LIST_ITEM)
@Singleton
public class NumberedListBlockParser extends AbstractListBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String NUMBERED_LIST_ITEM = "numberedListItem";

    /**
     * The numbered list item property that specified the start index.
     */
    public static final String START = "start";

    @Override
    protected void beginList(org.xwiki.rendering.listener.Listener listener, java.util.Map<String, String> parameters)
    {
        listener.beginList(ListType.NUMBERED, parameters);
    }

    @Override
    protected void endList(org.xwiki.rendering.listener.Listener listener, java.util.Map<String, String> parameters)
    {
        listener.endList(ListType.NUMBERED, parameters);
    }

    @Override
    protected boolean inSameList(JsonNode alice, JsonNode bob)
    {
        // XWiki syntax doesn't support parameters for list items so we start a new list when we encounter an item with
        // the start number specified, in order to pass the start number as a parameter of the new list.
        return super.inSameList(alice, bob) && bob.path(PROPS).path(START).isMissingNode();
    }

    @Override
    protected Map<String, String> getListParameters(ObjectNode listItemBlock)
    {
        Map<String, String> parameters = super.getListParameters(listItemBlock);
        if (listItemBlock.path(PROPS).path(START).isInt()) {
            parameters.put(START, listItemBlock.path(PROPS).path(START).asText());
        }
        return parameters;
    }
}

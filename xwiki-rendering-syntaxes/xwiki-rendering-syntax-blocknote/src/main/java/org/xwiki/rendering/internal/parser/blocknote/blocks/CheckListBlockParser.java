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

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Check list block parser.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named(CheckListBlockParser.CHECK_LIST_ITEM)
@Singleton
public class CheckListBlockParser extends BulletListBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String CHECK_LIST_ITEM = "checkListItem";

    /**
     * The check list type. XWiki rendering doesn't support check lists natively, so we're using standard bullet lists
     * that are marked with a specific parameter.
     */
    public static final String CHECK_LIST_TYPE = "check";

    /**
     * The check list item property that indicates whether the item is checked or not.
     */
    public static final String CHECKED_PROPERTY = "checked";

    /**
     * The list parameter used to store the checked/unchecked state of the list items. The state is stored at the list
     * level because XWiki syntax doesn't support parameters on list items. The consequence is that all items in a check
     * list have the same checked/unchecked state. When the state changes for an item, the list is split into multiple
     * lists.
     */
    public static final String CHECKED_PARAMETER = "data-xwiki-checklist-checked";

    @Override
    protected java.util.Map<String, String> getListParameters(ObjectNode checkListItem)
    {
        java.util.Map<String, String> parameters = super.getListParameters(checkListItem);
        parameters.put(LIST_TYPE, CHECK_LIST_TYPE);
        parameters.put(CHECKED_PARAMETER, String.valueOf(isChecked(checkListItem)));
        return parameters;
    }

    @Override
    protected boolean inSameList(JsonNode alice, JsonNode bob)
    {
        // We have to group checked and unchecked items because XWiki syntax doesn't support parameters on list items,
        // so we can store the checked/unchecked state only as a list parameter.
        return super.inSameList(alice, bob) && isChecked(alice) == isChecked(bob);
    }

    private boolean isChecked(JsonNode checkListItem)
    {
        return checkListItem.path(PROPS).path(CHECKED_PROPERTY).asBoolean(false);
    }
}

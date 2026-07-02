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

import com.fasterxml.jackson.databind.node.ObjectNode;

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

    @Override
    protected Map<String, String> getListParameters(ObjectNode listItemBlock)
    {
        Map<String, String> parameters = super.getListParameters(listItemBlock);
        parameters.put(LIST_TYPE, TOGGLE_LIST_TYPE);
        return parameters;
    }
}

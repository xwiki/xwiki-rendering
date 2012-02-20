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
package org.xwiki.rendering.wikimodel.xml;

import org.xwiki.rendering.wikimodel.IWemListenerTable;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @author kotelnikov
 */
public class WemTableTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerTable
{
    /**
     * @param listener
     */
    public WemTableTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void beginTable(WikiParameters params)
    {
        fListener.beginTag(TABLE, EMPTY_MAP, userParams(params));
    }

    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        String tag = tableHead ? TABLE_HEAD : TABLE_CELL;
        fListener.beginTag(tag, EMPTY_MAP, userParams(params));
    }

    public void beginTableRow(WikiParameters params)
    {
        fListener.beginTag(TABLE_ROW, EMPTY_MAP, userParams(params));
    }

    public void endTable(WikiParameters params)
    {
        fListener.endTag(TABLE, EMPTY_MAP, userParams(params));
    }

    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        String tag = tableHead ? TABLE_HEAD : TABLE_CELL;
        fListener.endTag(tag, EMPTY_MAP, userParams(params));
    }

    public void endTableRow(WikiParameters params)
    {
        fListener.endTag(TABLE_ROW, EMPTY_MAP, userParams(params));
    }

    public void onTableCaption(String str)
    {
        fListener.onTag(
            TABLE_CAPTION,
            tagParams(TABLE_CAPTION_PARAM, str),
            EMPTY_MAP);
    }
}

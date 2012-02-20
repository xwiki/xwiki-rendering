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

import org.xwiki.rendering.wikimodel.IWemListenerList;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @author kotelnikov
 */
public class WemListTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerList
{
    /**
     * @param listener
     */
    public WemListTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void beginDefinitionDescription()
    {
        fListener.beginTag(DEFINITION_DESCRIPTION, EMPTY_MAP, EMPTY_MAP);
    }

    public void beginDefinitionList(WikiParameters params)
    {
        fListener.beginTag(DEFINITION_LIST, EMPTY_MAP, EMPTY_MAP);
    }

    public void beginDefinitionTerm()
    {
        fListener.beginTag(DEFINITION_TERM, EMPTY_MAP, EMPTY_MAP);
    }

    public void beginList(WikiParameters params, boolean ordered)
    {
        String tagName = ordered ? LIST_ORDERED : LIST_UNORDERED;
        fListener.beginTag(tagName, EMPTY_MAP, userParams(params));
    }

    public void beginListItem()
    {
        fListener.beginTag(LIST_ITEM, EMPTY_MAP, EMPTY_MAP);
    }

    public void beginQuotation(WikiParameters params)
    {
        fListener.beginTag(QUOTATION, EMPTY_MAP, userParams(params));
    }

    public void beginQuotationLine()
    {
        fListener.beginTag(QUOTATION_LINE, EMPTY_MAP, EMPTY_MAP);
    }

    public void endDefinitionDescription()
    {
        fListener.endTag(DEFINITION_DESCRIPTION, EMPTY_MAP, EMPTY_MAP);
    }

    public void endDefinitionList(WikiParameters params)
    {
        fListener.endTag(DEFINITION_LIST, EMPTY_MAP, userParams(params));
    }

    public void endDefinitionTerm()
    {
        fListener.endTag(DEFINITION_TERM, EMPTY_MAP, EMPTY_MAP);
    }

    public void endList(WikiParameters params, boolean ordered)
    {
        String tagName = ordered ? LIST_ORDERED : LIST_UNORDERED;
        fListener.endTag(tagName, EMPTY_MAP, userParams(params));
    }

    public void endListItem()
    {
        fListener.endTag(LIST_ITEM, EMPTY_MAP, EMPTY_MAP);
    }

    public void endQuotation(WikiParameters params)
    {
        fListener.endTag(QUOTATION, EMPTY_MAP, userParams(params));
    }

    public void endQuotationLine()
    {
        fListener.endTag(QUOTATION_LINE, EMPTY_MAP, EMPTY_MAP);
    }
}

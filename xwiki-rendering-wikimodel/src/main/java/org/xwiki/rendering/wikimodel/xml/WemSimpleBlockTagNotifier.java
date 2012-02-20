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

import java.util.Map;

import org.xwiki.rendering.wikimodel.IWemListenerSimpleBlocks;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @author kotelnikov
 */
public class WemSimpleBlockTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerSimpleBlocks
{
    /**
     * @param listener
     */
    public WemSimpleBlockTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        fListener.beginTag(
            INFO_BLOCK,
            tagParams(INFO_BLOCK_TYPE, infoType),
            userParams(params));
    }

    public void beginParagraph(WikiParameters params)
    {
        fListener.beginTag(PARAGRAPH, EMPTY_MAP, userParams(params));
    }

    public void endInfoBlock(String infoType, WikiParameters params)
    {
        fListener.endTag(
            INFO_BLOCK,
            tagParams(INFO_BLOCK_TYPE, infoType),
            userParams(params));
    }

    public void endParagraph(WikiParameters params)
    {
        fListener.endTag(PARAGRAPH, EMPTY_MAP, userParams(params));
    }

    public void onEmptyLines(int count)
    {
        fListener.onTag(
            EMPTY_LINES,
            tagParams(EMPTY_LINES_SIZE, "" + count),
            EMPTY_MAP);
    }

    public void onHorizontalLine(WikiParameters params)
    {
        fListener.onTag(HORIZONTAL_LINE, EMPTY_MAP, userParams(params));
    }

    public void onVerbatimBlock(String str, WikiParameters params)
    {
        Map<String, String> userParams = userParams(params);
        fListener.beginTag(VERBATIM_BLOCK, EMPTY_MAP, userParams);
        fListener.onCDATA(str);
        fListener.endTag(VERBATIM_BLOCK, EMPTY_MAP, userParams);
    }
}

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

import org.xwiki.rendering.wikimodel.IWemListenerInline;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WemInlineTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerInline
{
    /**
     * @param listener
     */
    public WemInlineTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void beginFormat(WikiFormat format)
    {
        fListener.beginTag(FORMAT, tagParams(format), userParams(format));
    }

    public void endFormat(WikiFormat format)
    {
        fListener.endTag(FORMAT, tagParams(format), userParams(format));
    }

    public void onEscape(String str)
    {
        fListener.beginTag(ESCAPE, EMPTY_MAP, EMPTY_MAP);
        fListener.onText(str);
        fListener.endTag(ESCAPE, EMPTY_MAP, EMPTY_MAP);
    }

    public void onImage(String ref)
    {
        WikiReference r = new WikiReference(ref);
        fListener.onTag(
            IMAGE,
            tagParams(tagParams(r), IMAGE_IMPLICIT, "true"),
            userParams(r));
    }

    public void onImage(WikiReference ref)
    {
        fListener.onTag(IMAGE, tagParams(
            tagParams(ref),
            IMAGE_IMPLICIT,
            "false"), userParams(ref));
    }

    public void onLineBreak()
    {
        fListener.onTag(LINE_BREAK, EMPTY_MAP, EMPTY_MAP);
    }

    public void onNewLine()
    {
        fListener.onText(NEW_LINE);
    }

    public void onReference(String ref)
    {
        WikiReference r = new WikiReference(ref);
        fListener.onTag(REF_IMPLICIT, tagParams(r), userParams(r));
    }

    public void onReference(WikiReference ref)
    {
        fListener.onTag(REF_IMPLICIT, tagParams(ref), userParams(ref));
    }

    public void onSpace(String str)
    {
        fListener.onText(str);
    }

    public void onSpecialSymbol(String str)
    {
        // fListener.beginTag(SPECIAL, EMPTY_MAP, EMPTY_MAP);
        fListener.onText(str);
        // fListener.endTag(SPECIAL, EMPTY_MAP, EMPTY_MAP);
    }

    public void onVerbatimInline(String str, WikiParameters params)
    {
        fListener.beginTag(VERBATIM_INLINE, EMPTY_MAP, userParams(params));
        fListener.onCDATA(str);
        fListener.endTag(VERBATIM_INLINE, EMPTY_MAP, userParams(params));
    }

    public void onWord(String str)
    {
        fListener.onText(str);
    }
}

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

import org.xwiki.rendering.wikimodel.IWemListenerDocument;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @author kotelnikov
 */
public class WemDocumentTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerDocument
{
    private int fSectionDepth;

    /**
     * @param listener
     */
    public WemDocumentTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void beginDocument(WikiParameters params)
    {
        fListener.beginTag(DOCUMENT, tagParams(), userParams(params));
    }

    public void beginHeader(int headerLevel, WikiParameters params)
    {
        fListener.beginTag(
            HEADER,
            tagParams(HEADER_LEVEL, "" + headerLevel),
            userParams(params));
    }

    public void beginSection(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        fSectionDepth++;
        fListener.beginTag(
            SECTION,
            getSectionParams(docLevel, headerLevel),
            userParams(params));
    }

    public void beginSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        fListener.beginTag(SECTION_CONTENT, getSectionParams(
            docLevel,
            headerLevel), userParams(params));
    }

    public void endDocument(WikiParameters params)
    {
        fListener.endTag(DOCUMENT, tagParams(), userParams(params));
    }

    public void endHeader(int headerLevel, WikiParameters params)
    {
        fListener.endTag(
            HEADER,
            tagParams(HEADER_LEVEL, "" + headerLevel),
            userParams(params));
    }

    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        fListener.endTag(
            SECTION,
            getSectionParams(docLevel, headerLevel),
            userParams(params));
        fSectionDepth--;
    }

    public void endSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        fListener.endTag(SECTION_CONTENT, getSectionParams(
            docLevel,
            headerLevel), userParams(params));
    }

    /**
     * @param docLevel
     * @param headerLevel
     * @return
     */
    private Map<String, String> getSectionParams(int docLevel, int headerLevel)
    {
        return tagParams(
            SECTION_LEVEL,
            "" + fSectionDepth,
            SECTION_DOC_LEVEL,
            "" + docLevel,
            SECTION_HEADER_LEVEL,
            "" + headerLevel);
    }
}

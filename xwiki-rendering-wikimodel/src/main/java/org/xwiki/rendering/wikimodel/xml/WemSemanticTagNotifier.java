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

import org.xwiki.rendering.wikimodel.IWemListenerSemantic;

/**
 * @author kotelnikov
 */
public class WemSemanticTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerSemantic
{
    /**
     * @param listener
     */
    public WemSemanticTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        fListener.beginTag(PROPERTY_BLOCK, tagParams(
            PROPERTY_URL,
            propertyUri,
            PROPERTY_DOC,
            "" + doc), EMPTY_MAP);
    }

    public void beginPropertyInline(String propertyUri)
    {
        fListener.beginTag(
            PROPERTY_INLINE,
            tagParams(PROPERTY_URL, propertyUri),
            EMPTY_MAP);
    }

    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        fListener.endTag(PROPERTY_BLOCK, tagParams(
            PROPERTY_URL,
            propertyUri,
            PROPERTY_DOC,
            "" + doc), EMPTY_MAP);
    }

    public void endPropertyInline(String propertyUri)
    {
        fListener.endTag(
            PROPERTY_INLINE,
            tagParams(PROPERTY_URL, propertyUri),
            EMPTY_MAP);
    }
}

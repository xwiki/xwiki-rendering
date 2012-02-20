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

import org.xwiki.rendering.wikimodel.IWemListenerProgramming;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WemProgrammingTagNotifier extends AbstractTagNotifier
    implements
    IWemListenerProgramming
{
    /**
     * @param listener
     */
    public WemProgrammingTagNotifier(ITagListener listener)
    {
        super(listener);
    }

    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        fListener.onTag(EXTENSION_BLOCK, tagParams(
            EXTENSION_NAME,
            extensionName), userParams(params));
    }

    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        fListener.onTag(EXTENSION_INLINE, tagParams(
            EXTENSION_NAME,
            extensionName), userParams(params));
    }

    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        Map<String, String> tagParams = tagParams(MACRO_NAME, macroName);
        Map<String, String> userParams = userParams(params);
        fListener.beginTag(MACRO_BLOCK, tagParams, userParams);
        fListener.onCDATA(content);
        fListener.endTag(MACRO_BLOCK, tagParams, userParams);
    }

    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        Map<String, String> tagParams = tagParams(MACRO_NAME, macroName);
        Map<String, String> userParams = userParams(params);
        fListener.beginTag(MACRO_INLINE, tagParams, userParams);
        fListener.onCDATA(content);
        fListener.endTag(MACRO_INLINE, tagParams, userParams);
    }
}

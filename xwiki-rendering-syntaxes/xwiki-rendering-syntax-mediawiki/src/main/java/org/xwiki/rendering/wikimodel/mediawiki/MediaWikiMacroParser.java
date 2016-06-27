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
package org.xwiki.rendering.wikimodel.mediawiki;

import org.xwiki.rendering.wikimodel.IWikiMacroParser;
import org.xwiki.rendering.wikimodel.WikiMacro;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class MediaWikiMacroParser implements IWikiMacroParser
{
    /* (non-Javadoc)
    * @see IWikiMacroParser#parse(java.lang.String)
    */
    public WikiMacro parse(String str)
    {
        String name;
        WikiParameters params = WikiParameters.EMPTY;
        if (str.equals("__TOC__")) {
            name = WikiMacro.MACRO_TOC;
            params = params.addParameter("numbered", "true");
        } else if (str.equals("__NOTOC__")) {
            name = WikiMacro.MACRO_NOTOC;
        } else if (str.equals("__FORCETOC__")) {
            name = WikiMacro.MACRO_FORCETOC;
        } else if (str.contains("references")) {
            // moved/borrowed from MediawikiScanner.jj [r]
            // FIXME: where is the documentation for THIS feature?
            name = WikiMacro.MACRO_FOOTNOTES;
        } else if (str.length() > 4 && str.startsWith("{{") && str.endsWith("}}")) {
            // template with named and unnamed parameters
            String macro = str.substring(2, str.length() - 2);
            // First param is separated by a ":"
            int colonPos = macro.indexOf(':');
            if (colonPos > -1) {
                name = macro.substring(0, colonPos);
                if (macro.length() > colonPos + 1) {
                    macro = macro.substring(colonPos + 1);
                    String[] parts = macro.split("[|:]");
                    for (int i = 0; i < parts.length; i++) {
                        String key = Integer.toString(i + 1);
                        String value = parts[i];
                        int equidx = parts[i].indexOf('=');
                        if (equidx > 0) {
                            key = parts[i].substring(0, equidx);
                            value = parts[i].substring(equidx + 1);
                        }
                        params = params.addParameter(key, value);
                    }
                }
            } else {
                name = macro;
            }
        } else {
            // seems to be an unsupported magic word, see
            // http://www.mediawiki.org/wiki/Help:Magic_words
            name = WikiMacro.UNHANDLED_MACRO;
        }
        return new WikiMacro(name, params);
    }
}

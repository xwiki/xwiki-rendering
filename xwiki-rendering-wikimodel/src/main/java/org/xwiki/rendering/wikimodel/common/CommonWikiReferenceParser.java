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
package org.xwiki.rendering.wikimodel.common;

import org.xwiki.rendering.wikimodel.IWikiReferenceParser;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class CommonWikiReferenceParser implements IWikiReferenceParser
{
    public WikiReference parse(String str)
    {
        str = str.trim();
        // Params
        int idx = str.indexOf('>');
        if (idx < 0) {
            idx = str.indexOf('|');
        }
        String params = null;
        if (idx >= 0) {
            params = str.substring(idx + 1);
            str = str.substring(0, idx);
        }

        // Label
        idx = str.indexOf(' ');
        String label = null;
        if (idx > 0) {
            label = str.substring(idx + 1);
            str = str.substring(0, idx);
        }
        WikiParameters parameters = new WikiParameters(params);
        return new WikiReference(str, label, parameters);
    }
}
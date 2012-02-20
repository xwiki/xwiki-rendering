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
package org.xwiki.rendering.wikimodel.confluence;

import java.util.ArrayList;

import org.xwiki.rendering.wikimodel.IWikiReferenceParser;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.impl.WikiScannerUtil;

/**
 * @author kotelnikov
 * @see org.xwiki.rendering.wikimodel.WikiReference
 */
public class ConfluenceImageWikiReferenceParser implements IWikiReferenceParser
{
    public WikiReference parse(String str)
    {
        str = str.trim();
        String[] array = str.split("[|]");
        String link;
        String label;
        String tip;
        WikiParameters params;
        if (array.length == 1) {
            link = str;
            label = null;
            tip = null;
            params = WikiParameters.EMPTY;
        } else {

            String s = array[1].trim();
            ArrayList<WikiParameter> kps = new ArrayList<WikiParameter>();
            WikiScannerUtil.splitToPairs(s, kps, ",");
            params = WikiParameters.newWikiParameters("");
            for (WikiParameter param : kps) {
                params = params.addParameter(param.getKey(), param.getValue());
            }
            label = array[0].trim();
            link = array[0].trim();
            tip = (array.length > 2) ? array[2].trim() : null;
        }
        if (tip != null) {
            params = params.addParameter("title", tip);
        }
        WikiReference ref = new WikiReference(link, label, params);
        return ref;
    }
}
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
package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.rendering.listener.HeaderLevel;

public class HeaderLevelConverter
{
    Map<String, HeaderLevel> STRINGTOHEADERLEVEL = new HashMap<String, HeaderLevel>()
    {
        {
            put("1", HeaderLevel.LEVEL1);
            put("2", HeaderLevel.LEVEL2);
            put("3", HeaderLevel.LEVEL3);
            put("4", HeaderLevel.LEVEL4);
            put("5", HeaderLevel.LEVEL5);
            put("6", HeaderLevel.LEVEL6);
        }
    };

    Map<HeaderLevel, String> HEADERLEVELTOSTRING = new HashMap<HeaderLevel, String>()
    {
        {
            put(HeaderLevel.LEVEL1, "1");
            put(HeaderLevel.LEVEL2, "2");
            put(HeaderLevel.LEVEL3, "3");
            put(HeaderLevel.LEVEL4, "4");
            put(HeaderLevel.LEVEL5, "5");
            put(HeaderLevel.LEVEL6, "6");
        }
    };

    public HeaderLevel toFormat(String str)
    {
        return STRINGTOHEADERLEVEL.containsKey(str) ? STRINGTOHEADERLEVEL.get(str) : HeaderLevel.LEVEL1;
    }

    public String toString(HeaderLevel headerLevel)
    {
        return HEADERLEVELTOSTRING.containsKey(headerLevel) ? HEADERLEVELTOSTRING.get(headerLevel)
            : HEADERLEVELTOSTRING.get(HeaderLevel.LEVEL1);
    }
}

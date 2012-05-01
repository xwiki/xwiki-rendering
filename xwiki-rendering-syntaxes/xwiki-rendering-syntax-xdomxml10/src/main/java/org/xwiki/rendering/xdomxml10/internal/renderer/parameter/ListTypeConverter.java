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
package org.xwiki.rendering.xdomxml10.internal.renderer.parameter;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.rendering.listener.ListType;

public class ListTypeConverter
{
    Map<String, ListType> STRINGTOLISTTYPE = new HashMap<String, ListType>()
    {
        {
            put("bulleted", ListType.BULLETED);
            put("numbered", ListType.NUMBERED);
        }
    };

    Map<ListType, String> LISTTYPETOSTRING = new HashMap<ListType, String>()
    {
        {
            put(ListType.BULLETED, "bulleted");
            put(ListType.NUMBERED, "numbered");
        }
    };

    public ListType toFormat(String str)
    {
        return STRINGTOLISTTYPE.containsKey(str) ? STRINGTOLISTTYPE.get(str) : ListType.BULLETED;
    }

    public String toString(ListType listType)
    {
        return LISTTYPETOSTRING.containsKey(listType) ? LISTTYPETOSTRING.get(listType) : LISTTYPETOSTRING
            .get(ListType.BULLETED);
    }
}

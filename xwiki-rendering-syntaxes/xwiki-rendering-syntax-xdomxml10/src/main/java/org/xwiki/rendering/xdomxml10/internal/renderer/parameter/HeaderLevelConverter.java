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

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xwiki.rendering.listener.HeaderLevel;

public class HeaderLevelConverter
{
    private static final Map<String, HeaderLevel> STRINGTOHEADERLEVEL = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>("1", HeaderLevel.LEVEL1),
        new AbstractMap.SimpleImmutableEntry<>("2", HeaderLevel.LEVEL2),
        new AbstractMap.SimpleImmutableEntry<>("3", HeaderLevel.LEVEL3),
        new AbstractMap.SimpleImmutableEntry<>("4", HeaderLevel.LEVEL4),
        new AbstractMap.SimpleImmutableEntry<>("5", HeaderLevel.LEVEL5),
        new AbstractMap.SimpleImmutableEntry<>("6", HeaderLevel.LEVEL6))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<HeaderLevel, String> HEADERLEVELTOSTRING = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>(HeaderLevel.LEVEL1, "1"),
        new AbstractMap.SimpleImmutableEntry<>(HeaderLevel.LEVEL2, "2"),
        new AbstractMap.SimpleImmutableEntry<>(HeaderLevel.LEVEL3, "3"),
        new AbstractMap.SimpleImmutableEntry<>(HeaderLevel.LEVEL4, "4"),
        new AbstractMap.SimpleImmutableEntry<>(HeaderLevel.LEVEL5, "5"),
        new AbstractMap.SimpleImmutableEntry<>(HeaderLevel.LEVEL6, "6"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

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

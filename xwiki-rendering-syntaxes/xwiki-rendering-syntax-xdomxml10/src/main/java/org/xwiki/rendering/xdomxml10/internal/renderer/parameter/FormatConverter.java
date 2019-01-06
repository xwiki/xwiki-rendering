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

import org.xwiki.rendering.listener.Format;

public class FormatConverter
{
    private static final Map<String, Format> STRINGTOFORMAT = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>("bold", Format.BOLD),
        new AbstractMap.SimpleImmutableEntry<>("italic", Format.ITALIC),
        new AbstractMap.SimpleImmutableEntry<>("underlined", Format.UNDERLINED),
        new AbstractMap.SimpleImmutableEntry<>("strikeout", Format.STRIKEDOUT),
        new AbstractMap.SimpleImmutableEntry<>("superscript", Format.SUPERSCRIPT),
        new AbstractMap.SimpleImmutableEntry<>("subscript", Format.SUBSCRIPT),
        new AbstractMap.SimpleImmutableEntry<>("monospace", Format.MONOSPACE),
        new AbstractMap.SimpleImmutableEntry<>("none", Format.NONE))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<Format, String> FORMATTOSTRING = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>(Format.BOLD, "bold"),
        new AbstractMap.SimpleImmutableEntry<>(Format.ITALIC, "italic"),
        new AbstractMap.SimpleImmutableEntry<>(Format.UNDERLINED, "underlined"),
        new AbstractMap.SimpleImmutableEntry<>(Format.STRIKEDOUT, "strikeout"),
        new AbstractMap.SimpleImmutableEntry<>(Format.SUPERSCRIPT, "superscript"),
        new AbstractMap.SimpleImmutableEntry<>(Format.SUBSCRIPT, "subscript"),
        new AbstractMap.SimpleImmutableEntry<>(Format.MONOSPACE, "monospace"),
        new AbstractMap.SimpleImmutableEntry<>(Format.NONE, "none"))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    public Format toFormat(String str)
    {
        return STRINGTOFORMAT.containsKey(str) ? STRINGTOFORMAT.get(str) : Format.NONE;
    }

    public String toString(Format format)
    {
        return FORMATTOSTRING.containsKey(format) ? FORMATTOSTRING.get(format) : FORMATTOSTRING.get(Format.NONE);
    }
}

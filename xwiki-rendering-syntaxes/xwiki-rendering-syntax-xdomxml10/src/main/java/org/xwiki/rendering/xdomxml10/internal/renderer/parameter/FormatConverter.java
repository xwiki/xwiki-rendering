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

/**
 * Converts a {@link Format} to and from its XDOM+XML String representation.
 *
 * @version $Id$
 */
public class FormatConverter
{
    private static final String BOLD = "bold";

    private static final String ITALIC = "italic";

    private static final String UNDERLINED = "underlined";

    private static final String STRIKEOUT = "strikeout";

    private static final String SUPERSCRIPT = "superscript";

    private static final String SUBSCRIPT = "subscript";

    private static final String MONOSPACE = "monospace";

    private static final String NONE = "none";

    private static final Map<String, Format> STRINGTOFORMAT = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>(BOLD, Format.BOLD),
        new AbstractMap.SimpleImmutableEntry<>(ITALIC, Format.ITALIC),
        new AbstractMap.SimpleImmutableEntry<>(UNDERLINED, Format.UNDERLINED),
        new AbstractMap.SimpleImmutableEntry<>(STRIKEOUT, Format.STRIKEDOUT),
        new AbstractMap.SimpleImmutableEntry<>(SUPERSCRIPT, Format.SUPERSCRIPT),
        new AbstractMap.SimpleImmutableEntry<>(SUBSCRIPT, Format.SUBSCRIPT),
        new AbstractMap.SimpleImmutableEntry<>(MONOSPACE, Format.MONOSPACE),
        new AbstractMap.SimpleImmutableEntry<>(NONE, Format.NONE))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static final Map<Format, String> FORMATTOSTRING = Stream.of(
        new AbstractMap.SimpleImmutableEntry<>(Format.BOLD, BOLD),
        new AbstractMap.SimpleImmutableEntry<>(Format.ITALIC, ITALIC),
        new AbstractMap.SimpleImmutableEntry<>(Format.UNDERLINED, UNDERLINED),
        new AbstractMap.SimpleImmutableEntry<>(Format.STRIKEDOUT, STRIKEOUT),
        new AbstractMap.SimpleImmutableEntry<>(Format.SUPERSCRIPT, SUPERSCRIPT),
        new AbstractMap.SimpleImmutableEntry<>(Format.SUBSCRIPT, SUBSCRIPT),
        new AbstractMap.SimpleImmutableEntry<>(Format.MONOSPACE, MONOSPACE),
        new AbstractMap.SimpleImmutableEntry<>(Format.NONE, NONE))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * @param str the String representation of the format
     * @return the matching {@link Format}, defaulting to {@link Format#NONE}
     */
    public Format toFormat(String str)
    {
        return STRINGTOFORMAT.containsKey(str) ? STRINGTOFORMAT.get(str) : Format.NONE;
    }

    /**
     * @param format the format
     * @return the String representation of the passed format
     */
    public String toString(Format format)
    {
        return FORMATTOSTRING.containsKey(format) ? FORMATTOSTRING.get(format) : FORMATTOSTRING.get(Format.NONE);
    }
}

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

import org.xwiki.rendering.listener.Format;

public class FormatConverter
{
    Map<String, Format> STRINGTOFORMAT = new HashMap<String, Format>()
    {
        {
            put("bold", Format.BOLD);
            put("italic", Format.ITALIC);
            put("underlined", Format.UNDERLINED);
            put("strikeout", Format.STRIKEDOUT);
            put("superscript", Format.SUPERSCRIPT);
            put("subscript", Format.SUBSCRIPT);
            put("monospace", Format.MONOSPACE);
            put("none", Format.NONE);
        }
    };

    Map<Format, String> FORMATTOSTRING = new HashMap<Format, String>()
    {
        {
            put(Format.BOLD, "bold");
            put(Format.ITALIC, "italic");
            put(Format.UNDERLINED, "underlined");
            put(Format.STRIKEDOUT, "strikeout");
            put(Format.SUPERSCRIPT, "superscript");
            put(Format.SUBSCRIPT, "subscript");
            put(Format.MONOSPACE, "monospace");
            put(Format.NONE, "none");
        }
    };

    public Format toFormat(String str)
    {
        return STRINGTOFORMAT.containsKey(str) ? STRINGTOFORMAT.get(str) : Format.NONE;
    }

    public String toString(Format format)
    {
        return FORMATTOSTRING.containsKey(format) ? FORMATTOSTRING.get(format) : FORMATTOSTRING.get(Format.NONE);
    }
}

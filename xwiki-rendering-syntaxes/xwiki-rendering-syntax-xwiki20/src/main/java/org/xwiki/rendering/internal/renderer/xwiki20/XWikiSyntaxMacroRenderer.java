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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.Map;

import org.xwiki.rendering.internal.renderer.ParametersPrinter;

/**
 * Generates XWiki Syntax for a Macro Block.
 *
 * @version $Id$
 * @since 2.0.1
 */
public class XWikiSyntaxMacroRenderer
{
    private static final String CLOSING_MARKER = "}}";

    private static final ParametersPrinter PARAMETERS_PRINTER = new ParametersPrinter('~');

    /**
     * @param id the macro id (i.e. the macro name)
     * @param parameters the macro parameters
     * @param content the macro content
     * @param isInline whether the macro is used inline or as a standalone block
     * @return the XWiki Syntax representation of the macro
     */
    public String renderMacro(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        StringBuffer buffer = new StringBuffer();

        // Print begin macro
        buffer.append("{{");
        buffer.append(id);

        // Print parameters
        if (!parameters.isEmpty()) {
            buffer.append(' ');
            buffer.append(renderMacroParameters(parameters));
        }

        // Print content and end macro
        if (content == null) {
            buffer.append("/}}");
        } else {
            buffer.append(CLOSING_MARKER);
            if (content.length() > 0) {
                if (!isInline) {
                    buffer.append('\n');
                }
                buffer.append(content);
                if (!isInline) {
                    buffer.append('\n');
                }
            }
            buffer.append("{{/").append(id).append(CLOSING_MARKER);
        }

        return buffer.toString();
    }

    /**
     * @param parameters the macro parameters
     * @return the XWiki Syntax representation of the passed macro parameters
     */
    public String renderMacroParameters(Map<String, String> parameters)
    {
        return PARAMETERS_PRINTER.print(parameters).replace(CLOSING_MARKER, "~}~}");
    }
}

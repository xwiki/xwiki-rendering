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
package org.xwiki.rendering.internal.macro.content;

import java.util.Map;

import javax.inject.Named;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.macro.descriptor.SyntaxDescriptor;
import org.xwiki.rendering.syntax.Syntax;

/**
 * The syntax used by {@link ContentMacro} for the inline edition.
 *
 * @version $Id$
 * @since 10.9RC1
 */
@Role
@Named("content")
public class ContentSyntaxDescriptor implements SyntaxDescriptor
{
    private static final String SYNTAX_PARAMETER_NAME = "syntax";

    @Override
    public Syntax getSyntax(Map<String, Object> macroParameters)
    {
        if (macroParameters != null && macroParameters.containsKey(SYNTAX_PARAMETER_NAME)) {
            return (Syntax) macroParameters.get(SYNTAX_PARAMETER_NAME);
        }
        return Syntax.PLAIN_1_0;
    }
}

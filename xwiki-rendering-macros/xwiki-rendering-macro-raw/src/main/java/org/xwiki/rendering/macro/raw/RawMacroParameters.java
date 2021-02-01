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
package org.xwiki.rendering.macro.raw;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.rendering.internal.macro.raw.RawMacro;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Parameters for the {@link RawMacro}.
 *
 * @version $Id$
 * @since 13.1RC1
 */
public class RawMacroParameters
{
    /**
     * The target syntax and also the syntax in which the content is written in.
     */
    private Syntax syntax;

    /**
     * @param syntax the target syntax and also the syntax in which the content is written in
     */
    @PropertyMandatory
    @PropertyDescription("The target syntax and also the syntax in which the content is written in")
    public void setSyntax(Syntax syntax)
    {
        this.syntax = syntax;
    }

    /**
     * @return the target syntax and also the syntax in which the content is written in
     */
    public Syntax getSyntax()
    {
        return this.syntax;
    }
}

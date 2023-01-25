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
package org.xwiki.rendering.macro.content;

import org.xwiki.properties.annotation.PropertyAdvanced;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Parameters for the {@link org.xwiki.rendering.internal.macro.content.ContentMacro} Macro.
 *
 * @version $Id$
 * @since 4.2M3
 */
public class ContentMacroParameters
{
    /**
     * @see #getSyntax()
     */
    private Syntax syntax;

    private MacroContentSourceReference source;

    /**
     * @param syntax see {@link #getSyntax()}
     */
    @PropertyDescription(
        "the wiki syntax in which the content is written (e.g. \"xwiki/2.1\", \"confluence/1.0\", etc)")
    public void setSyntax(Syntax syntax)
    {
        this.syntax = syntax;
    }

    /**
     * @return the wiki syntax in which the content is written (e.g. "xwiki/2.1", "confluence/1.0", etc)
     */
    public Syntax getSyntax()
    {
        return this.syntax;
    }

    /**
     * @param source the reference of the content to parse
     * @since 15.1RC1
     * @since 14.10.5
     */
    @PropertyDescription("the reference of the content to parse instead of the content of the macro"
        + " (script:myvariable for the entry with name myvariable in the script context)")
    @PropertyAdvanced
    public void setSource(MacroContentSourceReference source)
    {
        this.source = source;
    }

    /**
     * @return the reference of the content to parse
     * @since 15.1RC1
     * @since 14.10.5
     */
    public MacroContentSourceReference getSource()
    {
        return this.source;
    }
}

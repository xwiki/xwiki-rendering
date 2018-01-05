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
package org.xwiki.rendering.macro;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Represents a Macro identifier. This is used when we need to pass a reference of a macro around without having to
 * pass Macro instances; it's also required when we need to create a Macro instance from an identifier.
 * <p>
 * A Macro is identified by 2 parameters:
 * </p>
 * <ul>
 *   <li>a string representing a technical id (eg "toc")</li>
 *   <li>an optional syntax (can be null) if the macro is only available for a given syntax</li>
 * </ul>
 *
 * @version $Id$
 * @since 2.0M3
 */
public class MacroId
{
    /**
     * @see #getId()
     */
    private String id;

    /**
     * @see #getSyntax()
     */
    private Syntax syntax;

    /**
     * Constructor for macros registered for all syntaxes.
     *
     * @param id see {@link #getId()}
     */
    public MacroId(String id)
    {
        this(id, null);
    }

    /**
     * Constructor for macros registered for a specific syntax only.
     *
     * @param id see {@link #getId()}
     * @param syntax see {@link #getSyntax()}
     */
    public MacroId(String id, Syntax syntax)
    {
        this.id = id;
        this.syntax = syntax;
    }

    /**
     * @return the technical id of the macro (eg "toc" for the TOC Macro)
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the optional syntax (can be null) for which the macro represented by this id is available. If null then
     *         the macro is available for all syntaxes.
     */
    public Syntax getSyntax()
    {
        return this.syntax;
    }

    @Override
    public String toString()
    {
        return getId() + ((getSyntax() == null) ? "" : "/" + getSyntax().toIdString());
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 3)
            .append(getId())
            .append(getSyntax())
            .toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this) {
            return true;
        }
        
        if (!(object instanceof MacroId)) {
            return false;
        }
        
        MacroId rhs = (MacroId) object;
        return new EqualsBuilder()
            .append(getId(), rhs.getId())
            .append(getSyntax(), rhs.getSyntax())
            .isEquals();
    }
}

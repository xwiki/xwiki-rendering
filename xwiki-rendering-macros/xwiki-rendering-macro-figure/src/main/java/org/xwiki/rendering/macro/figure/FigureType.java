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
package org.xwiki.rendering.macro.figure;

import org.xwiki.stability.Unstable;

/**
 * Allowed figure types.
 *
 * @version $Id$
 * @since 14.9RC1
 */
@Unstable
public enum FigureType
{
    /**
     * Implicit type, let the type be defined according to the body of the figure.
     *
     * @see FigureTypeRecognizer
     */
    AUTOMATIC(""),
    /**
     * Explicit figure type.
     */
    FIGURE("figure"),

    /**
     * Explicit table type.
     */
    TABLE("table");

    private String name;

    /**
     * @param name the name of the figure type (e.g., {@link FigureType#FIGURE})
     */
    FigureType(String name)
    {
        this.name = name;
    }

    /**
     * @return the name of the figure type (e.g., {@link FigureType#FIGURE})
     */
    public String getName()
    {
        return this.name;
    }
}

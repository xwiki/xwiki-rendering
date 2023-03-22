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

/**
 * Allowed alignment types.
 *
 * @version $Id$
 * @since 14.10.8
 * @since 15.3RC1
 */
public enum AlignmentType
{
    /**
     * No specific figure alignment.
     */
    NONE(""),
    /**
     * Start alignment (e.g., at the left in a left-to-right language).
     */
    START("start"),
    /**
     * Centered alignment.
     */
    CENTER("center"),
    /**
     * End alignment (e.g., at the right in a left-to-right language).
     */
    END("end");

    private String id;

    AlignmentType(String id)
    {

        this.id = id;
    }

    /**
     * @return the technical id of the alignment
     */
    public String getId()
    {
        return this.id;
    }
}


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
package org.xwiki.rendering.macro.source;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Convert an object into a {@link MacroContentSourceReference}.
 * 
 * @param <T> the type of the value to convert to {@link MacroContentSourceReference}
 * @version $Id$
 * @since 15.1RC1
 * @since 14.10.5
 */
@Role
@Unstable
public interface MacroContentSourceReferenceConverter<T>
{
    /**
     * @param value the value to convert to a {@link MacroContentSourceReference}
     * @return the {@link MacroContentSourceReference} corresponding to the provided value
     */
    MacroContentSourceReference convert(T value);
}

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
package org.xwiki.rendering.macro.descriptor;

import java.lang.reflect.Type;

import org.xwiki.stability.Unstable;

/**
 * Define a macro content.
 *
 * @version $Id$
 * @since 1.9M1
 */
public interface ContentDescriptor
{
    /**
     * @return the description of the macro content.
     */
    String getDescription();

    /**
     * @return indicate if the macro content is mandatory.
     */
    boolean isMandatory();

    /**
     * This method will return the type of the macro content. By default it fallback to String.
     *
     * @return the type of the macro content.
     * @since 10.10RC1
     */
    default Type getType()
    {
        return String.class;
    }

    /**
     * @return the ordering value to use to display the property in the UI. The lower the value, the higher the
     * priority. {@code -1} means no defined order.
     * @since 17.5.0
     * @see ParameterDescriptor#getOrder()
     */
    @Unstable
    default int getOrder()
    {
        return -1;
    }
}

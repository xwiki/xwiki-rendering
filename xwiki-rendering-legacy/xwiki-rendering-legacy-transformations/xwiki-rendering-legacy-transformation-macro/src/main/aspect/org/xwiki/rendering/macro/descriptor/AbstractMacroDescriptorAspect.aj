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

import java.util.Set;

import org.xwiki.properties.BeanDescriptor;
import org.xwiki.rendering.macro.MacroId;

/**
 * Add a backward compatibility layer to the {@link AbstractMacroDescriptor} class.
 *
 * @version $Id$
 * @since 14.6RC1
 */
public privileged aspect AbstractMacroDescriptorAspect
{
    /**
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description the description of the macro.
     * @param contentDescriptor the description of the macro content. null indicate macro does not support content.
     * @param parametersBeanDescriptor the description of the parameters bean or null if there are no parameters for
     *            this macro.
     * @since 2.0M3
     * @deprecated since 2.3M1 use
     *             {@link #AbstractMacroDescriptor(MacroId, String, String, ContentDescriptor, BeanDescriptor)} instead
     */
    @Deprecated
    public AbstractMacroDescriptor.new(String name, String description, ContentDescriptor contentDescriptor,
        BeanDescriptor parametersBeanDescriptor)
        {
            this.name = name;
            this.description = description;
            this.contentDescriptor = contentDescriptor;
            this.parametersBeanDescriptor = parametersBeanDescriptor;
        }
    
    /**
     * @param defaultCategory default category under which this macro should be listed
     * @see MacroDescriptor#getDefaultCategories()
     * @deprecated since 14.6RC1 use {@link  AbstractMacroDescriptor#setDefaultCategories(Set)} instead
     */
    @Deprecated(since = "14.6RC1")
    public void AbstractMacroDescriptor.setDefaultCategory(String defaultCategory)
    {
        this.defaultCategories = Set.of(defaultCategory);
    }

    /**
     * Inherited from {@link CompatibilityMacroDescriptor}.
     */
    public String AbstractMacroDescriptor.getDefaultCategory()
    {
        // Takes any category if defaultCategories is not empty, to have a value to return when this method is called.
        // Note that there is no guarantee of which category will be returned.
        if (this.defaultCategories == null) {
            return null;
        }
        return this.defaultCategories.stream().findFirst().orElse(null);
    }
}

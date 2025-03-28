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

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

/**
 * Add a backward compatibility layer to {@link MacroDescriptor}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
@Aspect
public class MacroDescriptorAspect
{
    /**
     * Add legacy APIs to {@link MacroDescriptor}.
     */
    @DeclareParents("MacroDescriptor")
    public static CompatibilityMacroDescriptor compatibility;

    /**
     * Overwrite the default implementation of #getDefaultCategories based on the legacy #getDefaultCategory.
     *
     * @param descriptor the legacy descriptor API
     * @return a set containing the default category
     * @since 14.10.22
     * @since 15.10.11
     * @since 16.4.1
     * @since 16.5.0
     */
    @Around("execution(Set<String> MacroDescriptor.getDefaultCategories()) "
        + "&& within(org.xwiki.rendering.macro.descriptor.MacroDescriptor) "
        + "&& target(descriptor)")
    public Set<String> aroundGetDefaultCategories(CompatibilityMacroDescriptor descriptor)
    {
        String category = descriptor.getDefaultCategory();

        return category != null ? Set.of(category) : Set.of();
    }

    /**
     * Overwrite the default implementation of legacy #getDefaultCategory based on the legacy #getDefaultCategories.
     *
     * @param descriptor the legacy descriptor API
     * @return a set containing the default category
     * @since 15.10.14
     * @since 16.10.0RC1
     * @since 16.4.6
     */
    @Around("execution(String MacroDescriptor.getDefaultCategory()) "
        + "&& within(org.xwiki.rendering.macro.descriptor.MacroDescriptor) "
        + "&& target(descriptor)")
    public String aroundGetDefaultCategory(MacroDescriptor descriptor)
    {
        Set<String> categories = descriptor.getDefaultCategories();

        // Takes any category if defaultCategories is not empty, to have a value to return when this method is called.
        // Note that there is no guarantee of which category will be returned.
        if (categories == null) {
            return null;
        }

        return categories.stream().findFirst().orElse(null);
    }
}

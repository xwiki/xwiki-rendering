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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.macro.MacroId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate the retro compatibility of interface {@link MacroDescriptor}.
 * 
 * @version $Id$
 */
class DeprecatedMacroDescriptorTest
{
    private static class TestCategoryMacroDescriptor implements MacroDescriptor
    {
        @Override
        public MacroId getId()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public Class<?> getParametersBeanClass()
        {
            return null;
        }

        @Override
        public ContentDescriptor getContentDescriptor()
        {
            return null;
        }

        @Override
        public Map<String, ParameterDescriptor> getParameterDescriptorMap()
        {
            return null;
        }

        @Override
        public String getDefaultCategory()
        {
            return "deprecatedcategory";
        }
    }

    private static class TestNoCategoryMacroDescriptor implements MacroDescriptor
    {
        @Override
        public MacroId getId()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public Class<?> getParametersBeanClass()
        {
            return null;
        }

        @Override
        public ContentDescriptor getContentDescriptor()
        {
            return null;
        }

        @Override
        public Map<String, ParameterDescriptor> getParameterDescriptorMap()
        {
            return null;
        }

        @Override
        public String getDefaultCategory()
        {
            return null;
        }
    }

    private static class TestCategoriesMacroDescriptor implements MacroDescriptor
    {
        @Override
        public MacroId getId()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public Class<?> getParametersBeanClass()
        {
            return null;
        }

        @Override
        public ContentDescriptor getContentDescriptor()
        {
            return null;
        }

        @Override
        public Map<String, ParameterDescriptor> getParameterDescriptorMap()
        {
            return null;
        }

        @Override
        public Set<String> getDefaultCategories()
        {
            return new LinkedHashSet<>(List.of("category1", "category2"));
        }
    }

    private static class TestNoCategoriesMacroDescriptor implements MacroDescriptor
    {
        @Override
        public MacroId getId()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public Class<?> getParametersBeanClass()
        {
            return null;
        }

        @Override
        public ContentDescriptor getContentDescriptor()
        {
            return null;
        }

        @Override
        public Map<String, ParameterDescriptor> getParameterDescriptorMap()
        {
            return null;
        }
    }

    @Test
    void getDefaultCategoryWithCategory()
    {
        TestCategoryMacroDescriptor descriptor = new TestCategoryMacroDescriptor();

        assertEquals(Set.of("deprecatedcategory"), descriptor.getDefaultCategories());
    }

    @Test
    void getDefaultCategoryFromDefaultCategories()
    {
        TestCategoriesMacroDescriptor descriptor = new TestCategoriesMacroDescriptor();

        // The legacy getDefaultCategory() must be derived from the new getDefaultCategories() when only the latter is
        // implemented. This is the aspect advice being weaved onto the CompatibilityMacroDescriptor default method.
        assertEquals("category1", descriptor.getDefaultCategory());
    }

    @Test
    void getDefaultCategoryAndCategoriesWhenNeitherIsImplemented()
    {
        TestNoCategoriesMacroDescriptor descriptor = new TestNoCategoriesMacroDescriptor();

        // When a descriptor implements neither category method, the two default methods delegate to each other. Verify
        // that this does not lead to infinite recursion and returns the empty/null default values.
        assertEquals(Set.of(), descriptor.getDefaultCategories());
        assertNull(descriptor.getDefaultCategory());
    }

    @Test
    void getDefaultCategoryWithoutCategory()
    {
        TestNoCategoryMacroDescriptor descriptor = new TestNoCategoryMacroDescriptor();

        assertEquals(Set.of(), descriptor.getDefaultCategories());
    }

    @Test
    void getDefaultCategories()
    {
        DefaultMacroDescriptor descriptor = new DefaultMacroDescriptor(new MacroId("test"), "name");

        descriptor.setDefaultCategories(Set.of("category1", "category2"));

        assertEquals(Set.of("category1", "category2"), descriptor.getDefaultCategories());
    }

    @Test
    void getDefaultCategory()
    {
        DefaultMacroDescriptor descriptor = new DefaultMacroDescriptor(new MacroId("test"), "name");

        descriptor.setDefaultCategories(new LinkedHashSet<>(List.of("category1", "category2")));

        assertEquals("category1", descriptor.getDefaultCategory());
    }
}

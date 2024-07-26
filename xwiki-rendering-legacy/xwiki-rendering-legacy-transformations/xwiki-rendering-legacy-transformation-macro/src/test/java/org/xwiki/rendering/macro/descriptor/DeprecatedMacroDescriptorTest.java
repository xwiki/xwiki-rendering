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

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.macro.MacroId;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void getDefaultCategoryWithCategory()
    {
        TestCategoryMacroDescriptor descriptor = new TestCategoryMacroDescriptor();

        assertEquals(Set.of("deprecatedcategory"), descriptor.getDefaultCategories());
    }

    @Test
    void getDefaultCategoryWithoutCategory()
    {
        TestNoCategoryMacroDescriptor descriptor = new TestNoCategoryMacroDescriptor();

        assertEquals(Set.of(), descriptor.getDefaultCategories());
    }
}

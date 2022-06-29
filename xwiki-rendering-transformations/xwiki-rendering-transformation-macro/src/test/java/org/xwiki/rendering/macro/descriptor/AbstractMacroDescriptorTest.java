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

import org.junit.jupiter.api.Test;
import org.xwiki.properties.BeanDescriptor;
import org.xwiki.rendering.macro.MacroId;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Test of {@link AbstractMacroDescriptor}.
 *
 * @version $Id$
 * @since 14.6RC1
 */
class AbstractMacroDescriptorTest
{
    private AbstractMacroDescriptor macroDescriptor = new AbstractMacroDescriptor(new MacroId("testmacro"), "testmacro",
        "description", new DefaultContentDescriptor("description", false), mock(BeanDescriptor.class))
    {
    };

    @Test
    void getDefaultCategory()
    {
        this.macroDescriptor.setDefaultCategories(Set.of("CatA", "CatB"));

        assertThat(this.macroDescriptor.getDefaultCategory(),
            anyOf(equalTo("CatA"), equalTo("CatB")));
    }

    @Test
    void getDefaultCategoryNull()
    {
        this.macroDescriptor.setDefaultCategories(null);
        assertNull(this.macroDescriptor.getDefaultCategory());
    }

    @Test
    void getDefaultCategoryEmpty()
    {
        this.macroDescriptor.setDefaultCategories(Set.of());
        assertNull(this.macroDescriptor.getDefaultCategory());
    }
}

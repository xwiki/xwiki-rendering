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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link DefaultMacroTransformationConfiguration}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
@ComponentTest
class DefaultMacroTransformationConfigurationTest
{
    @InjectMockComponents
    private DefaultMacroTransformationConfiguration configuration;

    @Test
    void setCategories()
    {
        this.configuration.setCategories(new MacroId("test"), Set.of("C1", "C2"));
        Properties categories = this.configuration.getCategories();
        assertEquals(Set.of("test"), categories.stringPropertyNames());
        assertThat(Arrays.asList(((String) categories.get("test")).split(",")), containsInAnyOrder("C1", "C2"));
    }

    @Test
    void getHiddenCategories()
    {
        assertEquals(Set.of(), this.configuration.getHiddenCategories());
        this.configuration.setHiddenCategories(Set.of("C1", "C2"));
        assertEquals(Set.of("C1", "C2"), this.configuration.getHiddenCategories());
    }
}

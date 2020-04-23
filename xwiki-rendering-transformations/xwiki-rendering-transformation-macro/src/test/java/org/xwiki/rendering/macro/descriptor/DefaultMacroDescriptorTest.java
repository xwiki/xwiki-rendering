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

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.internal.DefaultBeanManager;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultMacroDescriptor} and {@link AbstractMacroDescriptor}.
 *
 * @version $Id$
 */
@ComponentTest
public class DefaultMacroDescriptorTest
{
    private DefaultMacroDescriptor macroDescriptor;

    @InjectMockComponents
    private DefaultBeanManager propertiesManager;

    @BeforeEach
    public void setUp()
    {
        this.macroDescriptor =
            new DefaultMacroDescriptor(new MacroId("Id"), "Name", "Description", new DefaultContentDescriptor(),
                this.propertiesManager.getBeanDescriptor(ParametersTests.class));
    }

    @Test
    public void parameterDescriptor()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        assertNull(map.get("hiddenParameter".toLowerCase()));

        ParameterDescriptor lowerParamDescriptor = map.get("lowerparam");

        assertNotNull(lowerParamDescriptor);
        assertEquals("lowerparam", lowerParamDescriptor.getId());
        assertEquals("lowerparam", lowerParamDescriptor.getDescription());
        assertSame(String.class, lowerParamDescriptor.getParameterType());
        assertNull(lowerParamDescriptor.getDefaultValue());
        assertFalse(lowerParamDescriptor.isMandatory());
        assertFalse(lowerParamDescriptor.isDeprecated());
        assertFalse(lowerParamDescriptor.isAdvanced());
        assertNull(lowerParamDescriptor.getGroupDescriptor().getGroup());
        assertNull(lowerParamDescriptor.getGroupDescriptor().getFeature());

        ParameterDescriptor param1Descriptor = map.get("param1");

        assertEquals("defaultparam1", param1Descriptor.getDefaultValue());

        ParameterDescriptor deprecatedDescriptor = map.get("deprecatedparameter");
        assertTrue(deprecatedDescriptor.isDeprecated());
        assertEquals(Arrays.asList("parentGroup", "childGroup"), deprecatedDescriptor.getGroupDescriptor().getGroup());
        assertEquals("feature", deprecatedDescriptor.getGroupDescriptor().getFeature());
        assertEquals(Boolean.class, deprecatedDescriptor.getDisplayType());

        ParameterDescriptor advancedDescriptor = map.get("advancedparameter");
        assertTrue(advancedDescriptor.isAdvanced());
        assertEquals(Arrays.asList("parentGroup", "childGroup"), advancedDescriptor.getGroupDescriptor().getGroup());
        assertEquals("feature", advancedDescriptor.getGroupDescriptor().getFeature());
        assertEquals(new DefaultParameterizedType(null, Map.class, String.class, Long.class),
                advancedDescriptor.getDisplayType());

        ParameterDescriptor displayHiddenDescriptor = map.get("displayhiddenparameter");
        assertTrue(displayHiddenDescriptor.isDisplayHidden());
    }

    @Test
    public void parameterDescriptorWithUpperCase()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor upperParamDescriptor = map.get("upperParam".toLowerCase());

        assertNotNull(upperParamDescriptor);
        assertEquals("upperParam", upperParamDescriptor.getId());
        assertEquals("upperParam", upperParamDescriptor.getDescription());
        assertSame(String.class, upperParamDescriptor.getParameterType());
        assertFalse(upperParamDescriptor.isMandatory());
    }

    @Test
    public void parameterDescriptorWithDescription()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor param1Descriptor = map.get("param1".toLowerCase());

        assertNotNull(param1Descriptor);
        assertEquals("param1", param1Descriptor.getId());
        assertEquals("param1 description", param1Descriptor.getDescription());
        assertSame(String.class, param1Descriptor.getParameterType());
        assertFalse(param1Descriptor.isMandatory());
    }

    @Test
    public void parameterDescriptorWithDescriptionAndMandatory()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor param2Descriptor = map.get("param2".toLowerCase());

        assertNotNull(param2Descriptor);
        assertEquals("param2", param2Descriptor.getId());
        assertEquals("param2 description", param2Descriptor.getDescription());
        assertSame(int.class, param2Descriptor.getParameterType());
        assertTrue(param2Descriptor.isMandatory());
    }

    @Test
    public void parameterDescriptorWithDescriptionAndMandatoryOnSetter()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor param3Descriptor = map.get("param3".toLowerCase());

        assertNotNull(param3Descriptor);
        assertEquals("param3", param3Descriptor.getId());
        assertEquals("param3 description", param3Descriptor.getDescription());
        assertSame(boolean.class, param3Descriptor.getParameterType());
        assertTrue(param3Descriptor.isMandatory());
    }

    @Test
    public void parameterDescriptorWithBackwardCompatible()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor deprecatedDescriptor =
                new BackwardCompatibleParameterDescriptor(map.get("deprecatedparameter"));
        assertFalse(deprecatedDescriptor.isDeprecated());

        ParameterDescriptor advancedDescriptor =
                new BackwardCompatibleParameterDescriptor(map.get("advancedparameter"));
        assertFalse(advancedDescriptor.isAdvanced());
        assertNull(advancedDescriptor.getGroupDescriptor());
    }
}

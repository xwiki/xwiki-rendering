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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyHidden;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Validate {@link DefaultMacroDescriptor} and {@link AbstractMacroDescriptor}.
 *
 * @version $Id$
 */
public class DefaultMacroDescriptorTest extends AbstractComponentTestCase
{
    public static class ParametersTests
    {
        private String lowerparam;

        private String upperParam;

        private String param1 = "defaultparam1";

        private int param2;

        private boolean param3;

        private String hiddenParameter;

        private String deprecatedParameter;

        private String advancedParameter;

        public void setLowerparam(String lowerparam)
        {
            this.lowerparam = lowerparam;
        }

        public String getLowerparam()
        {
            return this.lowerparam;
        }

        public void setUpperParam(String upperParam)
        {
            this.upperParam = upperParam;
        }

        public String getUpperParam()
        {
            return this.upperParam;
        }

        @PropertyDescription("param1 description")
        public void setParam1(String param1)
        {
            this.param1 = param1;
        }

        public String getParam1()
        {
            return this.param1;
        }

        @PropertyMandatory
        @PropertyDescription("param2 description")
        public void setParam2(int param1)
        {
            this.param2 = param1;
        }

        public int getParam2()
        {
            return this.param2;
        }

        public void setParam3(boolean param1)
        {
            this.param3 = param1;
        }

        @PropertyMandatory
        @PropertyDescription("param3 description")
        public boolean getParam3()
        {
            return this.param3;
        }

        @PropertyHidden
        public void setHiddenParameter(String hiddenParameter)
        {
            this.hiddenParameter = hiddenParameter;
        }

        public String getHiddenParameter()
        {
            return this.hiddenParameter;
        }

        @Deprecated
        public String getDeprecatedParameter()
        {
            return deprecatedParameter;
        }

        @Deprecated
        public void setDeprecatedParameter(String deprecatedParameter)
        {
            this.deprecatedParameter = deprecatedParameter;
        }

        public String getAdvancedParameter()
        {
            return advancedParameter;
        }

        public void setAdvancedParameter(String advancedParameter)
        {
            this.advancedParameter = advancedParameter;
        }
    }

    private DefaultMacroDescriptor macroDescriptor;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        BeanManager propertiesManager = getComponentManager().getInstance(BeanManager.class);
        this.macroDescriptor =
            new DefaultMacroDescriptor(new MacroId("Id"), "Name", "Description", new DefaultContentDescriptor(),
                propertiesManager.getBeanDescriptor(ParametersTests.class));
    }

    @Test
    public void testParameterDescriptor()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        Assert.assertNull(map.get("hiddenParameter".toLowerCase()));

        ParameterDescriptor lowerParamDescriptor = map.get("lowerparam");

        Assert.assertNotNull(lowerParamDescriptor);
        Assert.assertEquals("lowerparam", lowerParamDescriptor.getId());
        Assert.assertEquals("lowerparam", lowerParamDescriptor.getDescription());
        Assert.assertSame(String.class, lowerParamDescriptor.getParameterType());
        Assert.assertEquals(null, lowerParamDescriptor.getDefaultValue());
        Assert.assertEquals(false, lowerParamDescriptor.isMandatory());
        Assert.assertEquals(false, lowerParamDescriptor.isDeprecated());

        ParameterDescriptor param1Descriptor = map.get("param1");

        Assert.assertEquals("defaultparam1", param1Descriptor.getDefaultValue());

        ParameterDescriptor deprecatedDescriptor = map.get("deprecatedparameter");
        Assert.assertTrue(deprecatedDescriptor.isDeprecated());
    }

    @Test
    public void testParameterDescriptorWithUpperCase()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor upperParamDescriptor = map.get("upperParam".toLowerCase());

        Assert.assertNotNull(upperParamDescriptor);
        Assert.assertEquals("upperParam", upperParamDescriptor.getId());
        Assert.assertEquals("upperParam", upperParamDescriptor.getDescription());
        Assert.assertSame(String.class, upperParamDescriptor.getParameterType());
        Assert.assertEquals(false, upperParamDescriptor.isMandatory());
    }

    @Test
    public void testParameterDescriptorWithDescription()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor param1Descriptor = map.get("param1".toLowerCase());

        Assert.assertNotNull(param1Descriptor);
        Assert.assertEquals("param1", param1Descriptor.getId());
        Assert.assertEquals("param1 description", param1Descriptor.getDescription());
        Assert.assertSame(String.class, param1Descriptor.getParameterType());
        Assert.assertEquals(false, param1Descriptor.isMandatory());
    }

    @Test
    public void testParameterDescriptorWithDescriptionAndMandatory()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor param2Descriptor = map.get("param2".toLowerCase());

        Assert.assertNotNull(param2Descriptor);
        Assert.assertEquals("param2", param2Descriptor.getId());
        Assert.assertEquals("param2 description", param2Descriptor.getDescription());
        Assert.assertSame(int.class, param2Descriptor.getParameterType());
        Assert.assertEquals(true, param2Descriptor.isMandatory());
    }

    @Test
    public void testParameterDescriptorWithDescriptionAndMandatoryOnSetter()
    {
        Map<String, ParameterDescriptor> map = this.macroDescriptor.getParameterDescriptorMap();

        ParameterDescriptor param3Descriptor = map.get("param3".toLowerCase());

        Assert.assertNotNull(param3Descriptor);
        Assert.assertEquals("param3", param3Descriptor.getId());
        Assert.assertEquals("param3 description", param3Descriptor.getDescription());
        Assert.assertSame(boolean.class, param3Descriptor.getParameterType());
        Assert.assertEquals(true, param3Descriptor.isMandatory());
    }
}

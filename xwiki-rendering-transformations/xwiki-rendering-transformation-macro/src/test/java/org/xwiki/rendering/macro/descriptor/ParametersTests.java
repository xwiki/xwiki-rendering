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

import org.xwiki.properties.annotation.PropertyAdvanced;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyDisplayHidden;
import org.xwiki.properties.annotation.PropertyDisplayType;
import org.xwiki.properties.annotation.PropertyFeature;
import org.xwiki.properties.annotation.PropertyGroup;
import org.xwiki.properties.annotation.PropertyHidden;
import org.xwiki.properties.annotation.PropertyMandatory;

public class ParametersTests
{
    private String lowerparam;

    private String upperParam;

    private String param1 = "defaultparam1";

    private int param2;

    private boolean param3;

    private String hiddenParameter;

    private String deprecatedParameter;

    private String advancedParameter;

    private boolean displayHiddenParameter;

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
    @PropertyGroup({ "parentGroup", "childGroup" })
    @PropertyDisplayType(Boolean.class)
    public String getDeprecatedParameter()
    {
        return deprecatedParameter;
    }

    @Deprecated
    public void setDeprecatedParameter(String deprecatedParameter)
    {
        this.deprecatedParameter = deprecatedParameter;
    }

    @PropertyAdvanced
    @PropertyGroup({ "parentGroup", "childGroup" })
    @PropertyFeature("feature")
    @PropertyDisplayType({Map.class, String.class, Long.class})
    public String getAdvancedParameter()
    {
        return advancedParameter;
    }

    public void setAdvancedParameter(String advancedParameter)
    {
        this.advancedParameter = advancedParameter;
    }

    @PropertyDisplayHidden
    public boolean getDisplayHiddenParameter()
    {
        return this.displayHiddenParameter;
    }

    public void setDisplayHiddenParameter(boolean isDisplayHidden)
    {
        this.displayHiddenParameter = isDisplayHidden;
    }
}


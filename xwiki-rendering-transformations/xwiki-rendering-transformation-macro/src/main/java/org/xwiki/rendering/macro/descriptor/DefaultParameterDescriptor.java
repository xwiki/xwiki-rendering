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

import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.properties.PropertyGroupDescriptor;

/**
 * The default implementation of {@link ParameterDescriptor}.
 *
 * @version $Id$
 * @since 1.7M2
 */
public class DefaultParameterDescriptor implements ParameterDescriptor
{
    /**
     * Default content type of all content descriptors.
     *
     * @since 11.1RC1
     */
    public static final Type DEFAULT_PARAMETER_TYPE = String.class;

    /**
     * The description of the parameter.
     */
    private PropertyDescriptor propertyDescriptor;

    /**
     * Creates a new {@link DefaultParameterDescriptor} instance using the given {@link PropertyDescriptor}.
     *
     * @param propertyDescriptor The {@link PropertyDescriptor} instance.
     */
    public DefaultParameterDescriptor(PropertyDescriptor propertyDescriptor)
    {
        this.propertyDescriptor = propertyDescriptor;
    }

    @Override
    public String getId()
    {
        return this.propertyDescriptor.getId();
    }

    @Override
    public String getName()
    {
        return this.propertyDescriptor.getName();
    }

    @Override
    public String getDescription()
    {
        return this.propertyDescriptor.getDescription();
    }

    @Override
    @Deprecated
    public Class<?> getType()
    {
        return this.propertyDescriptor.getPropertyClass();
    }

    @Override
    public Type getParameterType()
    {
        return this.propertyDescriptor.getPropertyType();
    }

    @Override
    public Object getDefaultValue()
    {
        return this.propertyDescriptor.getDefaultValue();
    }

    @Override
    public boolean isMandatory()
    {
        return this.propertyDescriptor.isMandatory();
    }

    @Override
    public boolean isDeprecated()
    {
        return this.propertyDescriptor.isDeprecated();
    }

    @Override
    public boolean isAdvanced()
    {
        return this.propertyDescriptor.isAdvanced();
    }

    @Override
    public PropertyGroupDescriptor getGroupDescriptor()
    {
        return this.propertyDescriptor.getGroupDescriptor();
    }

    @Override
    public Type getDisplayType()
    {
        return this.propertyDescriptor.getDisplayType();
    }

    @Override
    public boolean isDisplayHidden()
    {
        return this.propertyDescriptor.isDisplayHidden();
    }
}

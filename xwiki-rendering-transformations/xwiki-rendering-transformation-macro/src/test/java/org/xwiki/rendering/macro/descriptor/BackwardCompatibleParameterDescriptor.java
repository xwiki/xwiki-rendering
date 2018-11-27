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

/**
 * Backward compatible implementation of {@link ParameterDescriptor}.
 */
public class BackwardCompatibleParameterDescriptor implements ParameterDescriptor
{
    private ParameterDescriptor parameterDescriptor;
    /**
     * Creates a new {@link BackwardCompatibleParameterDescriptor} instance using the given {@link ParameterDescriptor}.
     *
     * @param parameterDescriptor Parameter descriptor on which methods are called
     */
    public BackwardCompatibleParameterDescriptor(ParameterDescriptor parameterDescriptor)
    {
        this.parameterDescriptor = parameterDescriptor;
    }

    @Override
    public String getId()
    {
        return this.parameterDescriptor.getId();
    }

    @Override
    public String getName()
    {
        return this.parameterDescriptor.getName();
    }

    @Override
    public String getDescription()
    {
        return this.parameterDescriptor.getDescription();
    }

    @Override
    @Deprecated
    public Class<?> getType()
    {
        return this.parameterDescriptor.getType();
    }

    @Override
    public Type getParameterType()
    {
        return this.parameterDescriptor.getParameterType();
    }

    @Override
    public Object getDefaultValue()
    {
        return this.parameterDescriptor.getDefaultValue();
    }

    @Override
    public boolean isMandatory()
    {
        return this.parameterDescriptor.isMandatory();
    }
}

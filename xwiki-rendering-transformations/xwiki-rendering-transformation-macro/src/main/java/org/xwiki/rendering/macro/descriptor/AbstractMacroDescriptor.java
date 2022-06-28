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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.xwiki.properties.BeanDescriptor;
import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.stability.Unstable;

/**
 * Describe a macro.
 *
 * @version $Id$
 * @since 1.6M1
 */
public abstract class AbstractMacroDescriptor implements MacroDescriptor
{
    /**
     * @see #getId()
     */
    private MacroId id;

    /**
     * @see #getName()
     */
    private String name;

    /**
     * The description of the macro.
     */
    private String description;

    /**
     * Define a macro content.
     */
    private ContentDescriptor contentDescriptor;

    /**
     * The description of the parameters bean.
     */
    private BeanDescriptor parametersBeanDescriptor;

    /**
     * Default macro category.
     */
    private Set<String> defaultCategories;

    /**
     * @see #supportsInlineMode()
     */
    private boolean supportsInlineMode;

    /**
     * A map containing the {@link ParameterDescriptor} for each parameters supported for this macro.
     * <p>
     * The {@link Map} keys are lower cased for easier case insensitive search, to get the "real" name of the property
     * use {@link ParameterDescriptor#getName()}.
     */
    private Map<String, ParameterDescriptor> parameterDescriptorMap = new LinkedHashMap<String, ParameterDescriptor>();

    /**
     * @param id the id of the macro
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description the description of the macro.
     * @param contentDescriptor the description of the macro content. null indicate macro does not support content.
     * @param parametersBeanDescriptor the description of the parameters bean or null if there are no parameters for
     *            this macro.
     * @since 2.3M1
     */
    public AbstractMacroDescriptor(MacroId id, String name, String description, ContentDescriptor contentDescriptor,
        BeanDescriptor parametersBeanDescriptor)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.contentDescriptor = contentDescriptor;
        this.parametersBeanDescriptor = parametersBeanDescriptor;
    }

    /**
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description the description of the macro.
     * @param contentDescriptor the description of the macro content. null indicate macro does not support content.
     * @param parametersBeanDescriptor the description of the parameters bean or null if there are no parameters for
     *            this macro.
     * @since 2.0M3
     * @deprecated since 2.3M1 use
     *             {@link #AbstractMacroDescriptor(MacroId, String, String, ContentDescriptor, BeanDescriptor)} instead
     */
    @Deprecated
    public AbstractMacroDescriptor(String name, String description, ContentDescriptor contentDescriptor,
        BeanDescriptor parametersBeanDescriptor)
    {
        this.name = name;
        this.description = description;
        this.contentDescriptor = contentDescriptor;
        this.parametersBeanDescriptor = parametersBeanDescriptor;
    }

    /**
     * Extract parameters informations from {@link #parametersBeanDescriptor} and insert it in
     * {@link #parameterDescriptorMap}.
     *
     * @since 1.7M2
     */
    protected void extractParameterDescriptorMap()
    {
        for (PropertyDescriptor propertyDescriptor : this.parametersBeanDescriptor.getProperties()) {
            DefaultParameterDescriptor desc = new DefaultParameterDescriptor(propertyDescriptor);
            this.parameterDescriptorMap.put(desc.getId().toLowerCase(), desc);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.3M1
     */
    @Override
    public MacroId getId()
    {
        return this.id;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0M3
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public ContentDescriptor getContentDescriptor()
    {
        return this.contentDescriptor;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public Class<?> getParametersBeanClass()
    {
        return (null != this.parametersBeanDescriptor) ? this.parametersBeanDescriptor.getBeanClass() : Object.class;
    }

    @Override
    public Map<String, ParameterDescriptor> getParameterDescriptorMap()
    {
        return (null != this.parametersBeanDescriptor) ? Collections.unmodifiableMap(this.parameterDescriptorMap)
            : Collections.<String, ParameterDescriptor>emptyMap();
    }

    @Override
    public String getDefaultCategory()
    {
        // Takes any category if defaultCategories is not empty, to have a value to return when this method is called.
        // Note that there is no guarantee of which category will be returned.
        if (this.defaultCategories == null) {
            return null;
        }
        return this.defaultCategories.stream().findFirst().orElse(null);
    }

    /**
     * @param defaultCategories the list of default categories which the macro should be listed
     * @see MacroDescriptor#getDefaultCategories()
     * @since 14.6RC1
     */
    @Unstable
    public void setDefaultCategories(Set<String> defaultCategories)
    {
        this.defaultCategories = defaultCategories;
    }
    
    @Override
    public Set<String> getDefaultCategories()
    {
        return this.defaultCategories;
    }

    @Override
    public boolean supportsInlineMode()
    {
        return this.supportsInlineMode;
    }

    /**
     * @param supportsInlineMode {@code true} to support in-line mode, {@code false} otherwise
     * @see #supportsInlineMode()
     * @since 10.10RC1
     */
    public void setSupportsInlineMode(boolean supportsInlineMode)
    {
        this.supportsInlineMode = supportsInlineMode;
    }
}

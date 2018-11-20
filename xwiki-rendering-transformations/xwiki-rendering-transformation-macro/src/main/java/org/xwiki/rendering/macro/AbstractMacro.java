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
package org.xwiki.rendering.macro;

import java.lang.reflect.Type;

import javax.inject.Inject;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.ConverterManager;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.descriptor.AbstractMacroDescriptor;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.descriptor.DefaultMacroDescriptor;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;

/**
 * Helper to implement Macro, providing some default implementation. We recommend Macro writers to extend this class.
 *
 * @param <P> the type of the macro parameters bean
 * @version $Id$
 * @since 1.5M2
 */
public abstract class AbstractMacro<P> implements Macro<P>, Initializable
{
    /**
     * "Formatting" default macro category.
     */
    public static final String DEFAULT_CATEGORY_FORMATTING = "Formatting";

    /**
     * "Development" default macro category.
     */
    public static final String DEFAULT_CATEGORY_DEVELOPMENT = "Development";

    /**
     * "Content" default macro category.
     */
    public static final String DEFAULT_CATEGORY_CONTENT = "Content";

    /**
     * "Navigation" default macro category.
     */
    public static final String DEFAULT_CATEGORY_NAVIGATION = "Navigation";

    /**
     * "Internal" default macro category.
     */
    public static final String DEFAULT_CATEGORY_INTERNAL = "Internal";

    /**
     * "Layout" default macro category.
     */
    public static final String DEFAULT_CATEGORY_LAYOUT = "Layout";

    /**
     * The {@link BeanManager} component.
     */
    @Inject
    protected BeanManager beanManager;

    @Inject
    private ComponentDescriptor<Macro> componentDescriptor;

    @Inject
    private ConverterManager converterManager;

    /**
     * The human-readable macro name (eg "Table of Contents" for the TOC macro).
     */
    private String name;

    /**
     * Macro description used to generate the macro descriptor.
     */
    private String description;

    /**
     * Content descriptor used to generate the macro descriptor.
     */
    private ContentDescriptor contentDescriptor;

    /**
     * Parameter bean class used to generate the macro descriptor.
     */
    private Class<?> parametersBeanClass;

    /**
     * The descriptor of the macro.
     */
    private MacroDescriptor macroDescriptor;

    /**
     * @see Macro#getPriority()
     */
    private int priority = 1000;

    /**
     * The default category under which this macro should be listed.
     */
    private String defaultCategory;

    /**
     * Creates a new {@link Macro} instance.
     *
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @since 2.0M3
     */
    public AbstractMacro(String name)
    {
        this(name, null);
    }

    /**
     * Creates a new {@link Macro} instance.
     *
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description a string describing this macro.
     * @since 2.0M3
     */
    public AbstractMacro(String name, String description)
    {
        this(name, description, null, Object.class);
    }

    /**
     * Creates a new {@link Macro} instance.
     *
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description a string describing this macro.
     * @param contentDescriptor {@link ContentDescriptor} for this macro.
     * @since 2.0M3
     */
    public AbstractMacro(String name, String description, ContentDescriptor contentDescriptor)
    {
        this(name, description, contentDescriptor, Object.class);
    }

    /**
     * Creates a new {@link Macro} instance.
     *
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description a string describing this macro.
     * @param parametersBeanClass class of the parameters bean of this macro.
     * @since 2.0M3
     */
    public AbstractMacro(String name, String description, Class<?> parametersBeanClass)
    {
        this(name, description, null, parametersBeanClass);
    }

    /**
     * Creates a new {@link Macro} instance.
     *
     * @param name the name of the macro (eg "Table Of Contents" for the TOC macro)
     * @param description string describing this macro.
     * @param contentDescriptor the {@link ContentDescriptor} describing the content of this macro.
     * @param parametersBeanClass class of the parameters bean.
     * @since 2.0M3
     */
    public AbstractMacro(String name, String description, ContentDescriptor contentDescriptor,
        Class<?> parametersBeanClass)
    {
        this.name = name;
        this.description = description;
        this.contentDescriptor = contentDescriptor;
        this.parametersBeanClass = parametersBeanClass;
    }

    @Override
    public void initialize() throws InitializationException
    {
        MacroId macroId = new MacroId(this.componentDescriptor.getRoleHint());

        DefaultMacroDescriptor descriptor = new DefaultMacroDescriptor(macroId, this.name, this.description,
            this.contentDescriptor, this.beanManager.getBeanDescriptor(this.parametersBeanClass));
        descriptor.setDefaultCategory(this.defaultCategory);
        descriptor.setSupportsInlineMode(this.supportsInlineMode());
        setDescriptor(descriptor);
    }

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    /**
     * @param priority the macro priority to use (lower means execute before others)
     */
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    @Override
    public MacroDescriptor getDescriptor()
    {
        return this.macroDescriptor;
    }

    @Override
    public int compareTo(Macro<?> macro)
    {
        return getPriority() - macro.getPriority();
    }

    /**
     * Allows macro classes extending other macro classes to override the macro descriptor with their own.
     *
     * @param descriptor the overriding descriptor to set
     */
    protected void setDescriptor(MacroDescriptor descriptor)
    {
        this.macroDescriptor = descriptor;
    }

    /**
     * Allows sub classes to set the default macro category. This method only has an effect if the internal
     * {@link MacroDescriptor} is of type {@link AbstractMacroDescriptor}.
     *
     * @param defaultCategory the default macro category to be set.
     */
    protected void setDefaultCategory(String defaultCategory)
    {
        // If setDefaultCategory() method is invoked before macro initialization, this will make sure the macro will
        // have correct default category after initialization.
        this.defaultCategory = defaultCategory;

        // In case if setDefaultCategory() is invoked after macro initialization. Only works if the internal
        // MacroDescriptor is of type AbstractMacroDescriptor.
        if (getDescriptor() instanceof AbstractMacroDescriptor) {
            ((AbstractMacroDescriptor) getDescriptor()).setDefaultCategory(defaultCategory);
        }
    }

    /**
     * Helper to get the proper metadata for unchanged content (i.e. content that has not gone through a
     * Transformation).
     *
     * @return the new metadata with the content type for the content represented as a string (e.g.
     *         {@code java.util.List< org.xwiki.rendering.block.Block >} for content of type {@code Listw<Block>}
     * @since 10.10RC1
     */
    protected MetaData getUnchangedContentMetaData()
    {
        MetaData metaData = new MetaData();
        Type contentType;

        if (this.contentDescriptor != null) {
            contentType = this.contentDescriptor.getType();
        } else {
            contentType = DefaultContentDescriptor.DEFAULT_CONTENT_TYPE;
        }

        String converted = this.converterManager.convert(String.class, contentType);

        metaData.addMetaData(MetaData.UNCHANGED_CONTENT, converted);
        return metaData;
    }
}

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
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.properties.BeanManager;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.descriptor.AbstractMacroDescriptor;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.descriptor.DefaultMacroDescriptor;
import org.xwiki.rendering.macro.descriptor.DefaultParameterDescriptor;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.macro.descriptor.ParameterDescriptor;
import org.xwiki.stability.Unstable;

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
    private Set<String> defaultCategories;

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
        descriptor.setDefaultCategories(this.defaultCategories);
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
     * @deprecated since 14.6RC1, use {@link #setDefaultCategories(Set)} instead
     */
    @Deprecated(since = "14.6RC1")
    // TODO: move to legacy once cleaned-up from xwiki-platform.
    protected void setDefaultCategory(String defaultCategory)
    {
        setDefaultCategories(Set.of(defaultCategory));
    }

    /**
     * Allows sub-classes to set default macro categories. This method only has an effect of the internal
     * {@link MacroDescriptor} is of type {@link AbstractMacroDescriptor}.
     *
     * @param defaultCategories the default macro categories to set
     * @since 14.6RC1
     */
    @Unstable
    protected void setDefaultCategories(Set<String> defaultCategories)
    {
        // If this setDefaultCategories() is invoked before macro initialization, this will make sure the macro will 
        // have correct default categories after initialization.
        this.defaultCategories = defaultCategories;

        // In case if setDefaultCategories() is invoked after macro initialization. Only works if the internal
        // MacroDescriptor is of type AbstractMacroDescriptor.
        if (getDescriptor() instanceof AbstractMacroDescriptor) {
            ((AbstractMacroDescriptor) getDescriptor()).setDefaultCategories(defaultCategories);
        }
    }

    /**
     * Helper to get the proper metadata for non generated content (i.e. content that has not gone through a
     * Transformation). This content can be used for inline editing.
     *
     * @param contentDescriptor the {@link ContentDescriptor} from which to get the type for the metadata.
     * @return the new metadata with the content type for the content represented as a string (e.g.
     *         {@code java.util.List<org.xwiki.rendering.block.Block>} for content of type {@code List<Block>}
     * @since 11.4RC1
     */
    public static MetaData getNonGeneratedContentMetaData(ContentDescriptor contentDescriptor)
    {
        MetaData metaData = new MetaData();
        Type contentType;

        if (contentDescriptor != null) {
            contentType = contentDescriptor.getType();
        } else {
            contentType = DefaultContentDescriptor.DEFAULT_CONTENT_TYPE;
        }

        String converted = ReflectionUtils.serializeType(contentType);

        metaData.addMetaData(MetaData.NON_GENERATED_CONTENT, converted);
        return metaData;
    }

    /**
     * Helper to get the proper metadata for non generated content (i.e. content that has not gone through a
     * Transformation). This content can be used for inline editing.
     *
     * @return the new metadata with the content type for the content represented as a string (e.g.
     *         {@code java.util.List<org.xwiki.rendering.block.Block>} for content of type {@code List<Block>}
     * @since 10.10
     */
    protected MetaData getNonGeneratedContentMetaData()
    {
        return getNonGeneratedContentMetaData(contentDescriptor);
    }

    /**
     * Helper to get the proper metadata for non generated content (i.e. content that has not gone through a
     * Transformation) for a specific parameter. This content can be used for inline editing.
     *
     * @param parameterDescriptorMap the descriptor map of the parameters
     * @param name the name of the parameter for which to get the metadata
     * @return the new metadata with the content type for the content represented as a string (e.g.
     *         {@code java.util.List<org.xwiki.rendering.block.Block>} for content of type {@code List<Block>}
     * @since 11.4RC1
     */
    public static MetaData getNonGeneratedContentMetaData(Map<String, ParameterDescriptor> parameterDescriptorMap,
        String name)
    {
        MetaData metaData = new MetaData();
        Type contentType;

        if (parameterDescriptorMap != null && parameterDescriptorMap.containsKey(name)) {
            contentType = parameterDescriptorMap.get(name).getDisplayType();
        } else {
            contentType = DefaultParameterDescriptor.DEFAULT_PARAMETER_TYPE;
        }

        String converted = ReflectionUtils.serializeType(contentType);

        metaData.addMetaData(MetaData.NON_GENERATED_CONTENT, converted);
        metaData.addMetaData(MetaData.PARAMETER_NAME, name);
        return metaData;
    }

    /**
     * Helper to get the proper metadata for non generated content (i.e. content that has not gone through a
     * Transformation) for a specific parameter. This content can be used for inline editing.
     *
     * @param parameterName the name of the parameter as defined in the macro
     * @return the new metadata with the content type for the content represented as a string (e.g.
     *         {@code java.util.List<org.xwiki.rendering.block.Block>} for content of type {@code List<Block>}
     * @since 11.1RC1
     */
    protected MetaData getNonGeneratedContentMetaData(String parameterName)
    {
        MetaData result;
        if (this.macroDescriptor != null) {
            result = getNonGeneratedContentMetaData(
                this.macroDescriptor.getParameterDescriptorMap(), parameterName);
        } else {
            result = getNonGeneratedContentMetaData(null, parameterName);
        }

        return result;
    }
}

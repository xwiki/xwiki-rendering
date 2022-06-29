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
import java.util.Set;

import org.xwiki.rendering.macro.MacroId;

/**
 * Describe a Macro (macro description and macro parameters description).
 *
 * @version $Id$
 * @since 1.6M1
 */
public interface MacroDescriptor
{
    /**
     * @return the id of the macro
     * @since 2.3M1
     */
    MacroId getId();

    /**
     * @return the human-readable name of the macro (eg "Table Of Contents" for the TOC macro).
     * @since 2.0M3
     */
    String getName();

    /**
     * @return the description of the macro.
     */
    String getDescription();

    /**
     * @return the class of the JAVA bean containing macro parameters.
     */
    Class<?> getParametersBeanClass();

    /**
     * @return describe the macro content. If null the macro does not support content.
     * @since 1.9M1
     */
    ContentDescriptor getContentDescriptor();

    /**
     * Get all the parameters descriptors.
     * <p>
     * The {@link Map} key is lower case. {@link ParameterDescriptor#getId()} can be used to access the source parameter
     * identifier (with the source case).
     * 
     * @return a {@link Map} containing the {@link ParameterDescriptor} for each parameter (the keys are lower cased).
     * @since 1.7M2
     */
    Map<String, ParameterDescriptor> getParameterDescriptorMap();

    /**
     * A macro can define a default classification category under which it falls. For an example, the "skype" macro
     * would fall under the "Communication" category of macros. However, a wiki administrator has the ability to
     * override the default category for a given macro in order to organize categories as he sees fit. Thus, this
     * default category is only an indication from the macro author about what category the macro should fall under.
     *
     * @return the default category under which this macro should be listed or null if the macro doesn't have a default
     *         category defined
     * @since 2.0M3
     * @deprecated since 14.6RC1, use {@link #getDefaultCategories()} instead
     */
    @Deprecated(since = "14.6RC1")
    // TODO: move to legacy once cleaned-up from xwiki-platform.
    String getDefaultCategory();

    /**
     * A macro defines a set of classification categories under which it falls. For instance, the "skype" macro would
     * fall under the "Communication" and "Video" categories. However, a wiki administrator has the ability to override
     * the default categories for a given macro in order to organize categories as he or she sees fit. Thus, these
     * default categories are only an indication from the macro author about what categories the macro should fall.
     *
     * @return the default categories under which this macro should be listed, or the empty list of the macro does not
     *     have any default category defined
     * @since 14.6RC1
     */
    default Set<String> getDefaultCategories()
    {
        return Set.of();
    }

    /**
     * @return true if the macro can be inserted in some existing content such as a paragraph, a list item etc. For
     *         example if I have <code>== hello {{velocity}}world{{/velocity}}</code> then the Velocity macro must
     *         support the inline mode and not generate a paragraph.
     * @since 10.10RC1
     */
    default boolean supportsInlineMode()
    {
        return false;
    }
}

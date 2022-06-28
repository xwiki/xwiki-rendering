package org.xwiki.rendering.macro.descriptor;

import java.util.Set;

/**
 * Add a backward compatibility layer to the {@link AbstractMacroDescriptor} class.
 *
 * @version $Id$
 * @since 14.6RC1
 */
public privileged aspect AbstractMacroDescriptorAspect
{
    /**
     * @param defaultCategory default category under which this macro should be listed
     * @see MacroDescriptor#getDefaultCategories()
     * @deprecated since 14.6RC1 use {@link  AbstractMacroDescriptor#setDefaultCategories(Set)} instead
     */
    @Deprecated(since = "14.6RC1")
    public void AbstractMacroDescriptor.setDefaultCategory(String defaultCategory)
    {
        this.defaultCategories = Set.of(defaultCategory);
    }
}

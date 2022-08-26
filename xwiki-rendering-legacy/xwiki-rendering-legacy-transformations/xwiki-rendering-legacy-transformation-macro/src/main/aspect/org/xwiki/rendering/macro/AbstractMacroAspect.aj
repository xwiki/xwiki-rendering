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

import java.util.Set;

import org.xwiki.rendering.macro.descriptor.AbstractMacroDescriptor;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;

/**
 * Add a backward compatibility layer to {@link AbstractMacro}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
public privileged aspect AbstractMacroAspect
{
    // setDefaultCategory while previously {@code protected} are now {@code public} due of technical limitations of
    // AspectJ.
    // See https://doanduyhai.wordpress.com/2011/12/12/advanced-aspectj-part-ii-inter-type-declaration/
    // "If their is one thing to remember from access modifier, it’s that their semantic applies with respect to the
    // declaring aspect, and not to the target."
    // They must still be used as if {@code protected}.
    /**
     * Allows sub-classes to set the default macro category. This method only has an effect if the internal
     * {@link MacroDescriptor} is of type {@link AbstractMacroDescriptor}.
     *
     * @param defaultCategory the default macro category to be set.
     * @deprecated since 14.6RC1, use {@link #setDefaultCategories(Set)} instead
     */
    @Deprecated(since = "14.6RC1")
    public void AbstractMacro.setDefaultCategory(String defaultCategory)
    {
        setDefaultCategories(Set.of(defaultCategory));
    }
}

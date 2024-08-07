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
package org.xwiki.rendering.internal.macro.message;

import org.xwiki.component.annotation.Component;

import javax.inject.Singleton;
import java.util.Map;

/**
 * Default component to provide an alternative text for an icon.
 *
 * @version $Id$
 * @since 16.7.0RC1
 */
@Singleton
@Component
public class DefaultMacroIconPrettyNameProvider implements MacroIconPrettyNameProvider 
{
    private static final Map<String, String> PRETTYNAME_MAPPING = Map.of(
        "exclamation", "Error",
        "information", "Information",
        "accept", "Success",
        "error", "Warning"
    );

    /**
     * @param iconName the name of the icon that needs an alternative text
     * @return the alternative text associated to the provided icon
     */
    public String getIconPrettyName(String iconName) 
    {
        return PRETTYNAME_MAPPING.get(iconName);
    }
}

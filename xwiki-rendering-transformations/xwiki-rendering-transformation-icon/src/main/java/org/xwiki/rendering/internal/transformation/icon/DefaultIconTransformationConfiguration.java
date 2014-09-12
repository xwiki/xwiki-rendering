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
package org.xwiki.rendering.internal.transformation.icon;

import java.util.Properties;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;

/**
 * All configuration options for the Icon transformation.
 *
 * @version $Id$
 * @since 2.6RC1
 */
@Component
@Singleton
public class DefaultIconTransformationConfiguration implements IconTransformationConfiguration, Initializable
{
    /**
     * Default Mappings.
     */
    private Properties mappings = new Properties();

    @Override
    public void initialize() throws InitializationException
    {
        // Default Mappings
        this.mappings.setProperty(":)", "emoticon_smile");
        this.mappings.setProperty(":(", "emoticon_unhappy");
        this.mappings.setProperty(":P", "emoticon_tongue");
        this.mappings.setProperty(":D", "emoticon_grin");
        this.mappings.setProperty(";)", "emoticon_wink");
        this.mappings.setProperty("(y)", "thumb_up");
        this.mappings.setProperty("(n)", "thumb_down");
        this.mappings.setProperty("(i)", "information");
        this.mappings.setProperty("(/)", "accept");
        this.mappings.setProperty("(x)", "cancel");
        this.mappings.setProperty("(!)", "error");
        this.mappings.setProperty("(+)", "add");
        this.mappings.setProperty("(-)", "delete");
        this.mappings.setProperty("(?)", "help");
        this.mappings.setProperty("(on)", "lightbulb");
        this.mappings.setProperty("(off)", "lightbulb_off");
        this.mappings.setProperty("(*)", "star");
    }

    @Override
    public Properties getMappings()
    {
        Properties properties = new Properties();
        properties.putAll(this.mappings);
        return properties;
    }

    @Override
    public void addMapping(String iconCharacters, String iconName)
    {
        this.mappings.setProperty(iconCharacters, iconName);
    }
}

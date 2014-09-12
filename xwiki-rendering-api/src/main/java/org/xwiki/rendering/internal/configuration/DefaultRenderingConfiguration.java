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
package org.xwiki.rendering.internal.configuration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.transformation.Transformation;

/**
 * Basic default implementation to be used when using the XWiki Rendering system standalone.
 *
 * @version $Id$
 * @since 2.0M1
 */
@Component
@Singleton
public class DefaultRenderingConfiguration implements RenderingConfiguration, Initializable
{
    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to look up Transformations at runtime.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Holds the names of transformations to apply (in any order, the Transformation Manager will execute them in the
     * proper order).
     */
    private List<String> transformationNames = new ArrayList<String>();

    /**
     * @see #getLinkLabelFormat()
     */
    private String linkLabelFormat = "%p";

    /**
     * @see #getInterWikiDefinitions()
     */
    private Properties interWikiDefinitions = new Properties();

    @Override
    public void initialize() throws InitializationException
    {
        // Find the names of all registered Transformations.
        List<ComponentDescriptor<Transformation>> descriptors =
            this.componentManager.getComponentDescriptorList((Type) Transformation.class);
        for (ComponentDescriptor<Transformation> descriptor : descriptors) {
            this.transformationNames.add(descriptor.getRoleHint());
        }
    }

    @Override
    public String getLinkLabelFormat()
    {
        return this.linkLabelFormat;
    }

    /**
     * @param linkLabelFormat the format used to decide how to display links that have no label
     */
    public void setLinkLabelFormat(String linkLabelFormat)
    {
        // This method is useful for those using the XWiki Rendering in standalone mode since it allows the rendering
        // to work even without a configuration store.
        this.linkLabelFormat = linkLabelFormat;
    }

    @Override
    public Properties getInterWikiDefinitions()
    {
        return this.interWikiDefinitions;
    }

    /**
     * @param interWikiAlias see {@link org.xwiki.rendering.listener.reference.InterWikiResourceReference}
     * @param interWikiURL see {@link org.xwiki.rendering.listener.reference.InterWikiResourceReference}
     */
    public void addInterWikiDefinition(String interWikiAlias, String interWikiURL)
    {
        // This method is useful for those using the XWiki Rendering in standalone mode since it allows the rendering
        // to work even without a configuration store.
        this.interWikiDefinitions.setProperty(interWikiAlias, interWikiURL);
    }

    /**
     * @param transformationNames the explicit list of transformation names to execute (overrides the default list)
     */
    public void setTransformationNames(List<String> transformationNames)
    {
        this.transformationNames = transformationNames;
    }

    @Override
    public List<String> getTransformationNames()
    {
        return this.transformationNames;
    }
}

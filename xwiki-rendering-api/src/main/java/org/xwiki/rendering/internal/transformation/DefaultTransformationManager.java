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
package org.xwiki.rendering.internal.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * Calls all existing transformations (executed by priority) on an existing XDOM object to generate a new transformed
 * XDOM.
 * 
 * @version $Id$
 * @since 1.5M2
 */
@Component
@Singleton
public class DefaultTransformationManager implements TransformationManager
{
    /**
     * Used to get the ordered list of transformations to execute.
     */
    @Inject
    private RenderingConfiguration configuration;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to look up transformations at runtime.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    @Deprecated
    public void performTransformations(XDOM dom, Syntax syntax) throws TransformationException
    {
        performTransformations(dom, new TransformationContext(dom, syntax));
    }

    @Override
    public void performTransformations(Block block, TransformationContext context) throws TransformationException
    {
        boolean error = false;
        for (Transformation transformation : getTransformations()) {
            try {
                transformation.transform(block, context);
            } catch (Exception e) {
                // Continue running the other transformations
                this.logger.error("Failed to execute transformation", e);
                error = true;
            }
        }
        if (error) {
            throw new TransformationException("One or several transformations failed to execute properly. "
                + "See the logs for details.");
        }
    }

    /**
     * @return the ordered list of Transformations to execute
     */
    public List<Transformation> getTransformations()
    {
        List<Transformation> transformations = new ArrayList<Transformation>();
        for (String hint : this.configuration.getTransformationNames()) {
            try {
                transformations.add(this.componentManagerProvider.get().<Transformation> getInstance(
                    Transformation.class, hint));
            } catch (ComponentLookupException e) {
                this.logger.warn("Failed to locate transformation with hint [" + hint + "], ignoring it.");
            }
        }
        Collections.sort(transformations);
        return transformations;
    }
}

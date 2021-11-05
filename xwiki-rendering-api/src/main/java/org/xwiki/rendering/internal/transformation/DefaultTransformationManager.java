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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.transformation.RenderingContext;
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
    protected RenderingConfiguration configuration;

    /**
     * Used to updated the rendering context.
     */
    @Inject
    private RenderingContext renderingContext;

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
    public void performTransformations(Block block, TransformationContext context) throws TransformationException
    {
        Map<String, String> transformationsInError = null;
        for (Transformation transformation : getTransformations()) {
            try {
                ((MutableRenderingContext) this.renderingContext).transformInContext(transformation, context, block);
            } catch (Exception e) {
                // Continue running the other transformations
                if (transformationsInError == null) {
                    transformationsInError = new HashMap<>();
                }
                transformationsInError.put(transformation.getClass().getName(),
                    ExceptionUtils.getStackTrace(e));
            }
        }
        if (transformationsInError != null) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : transformationsInError.entrySet()) {
                builder.append(String.format("- Transformation: [%s]\n", entry.getKey()));
                builder.append(entry.getValue());
            }
            throw new TransformationException(String.format("The following transformations failed to execute "
                + "properly: [\n%s]", builder.toString()));
        }
    }

    /**
     * @return the ordered list of Transformations to execute
     */
    public List<Transformation> getTransformations()
    {
        return getTransformations(this.configuration.getTransformationNames());
    }

    /**
     * @return the ordered list of Transformations to execute
     */
    protected List<Transformation> getTransformations(List<String> transformationNames)
    {
        List<Transformation> transformations = new ArrayList<>();
        for (String hint : transformationNames) {
            try {
                transformations.add(this.componentManagerProvider.get().getInstance(Transformation.class, hint));
            } catch (ComponentLookupException e) {
                this.logger.warn("Failed to locate transformation with hint [{}], ignoring it. "
                    + "Root reason [{}]", hint, ExceptionUtils.getRootCauseMessage(e));
            }
        }
        Collections.sort(transformations);
        return transformations;
    }
}

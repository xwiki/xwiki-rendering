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
package org.xwiki.rendering.internal.macro.source;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.source.MacroContentWikiSource;
import org.xwiki.rendering.macro.source.MacroContentWikiSourceFactory;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Call the right {@link MacroContentWikiSourceFactory} based on the type.
 * 
 * @version $Id$
 * @since 15.1RC1
 * @since 14.10.5
 */
@Component
@Singleton
public class DefaultMacroWikiContentSourceFactory implements MacroContentWikiSourceFactory
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public MacroContentWikiSource getContent(MacroContentSourceReference reference, MacroTransformationContext context)
        throws MacroExecutionException
    {
        String type = reference.getType();

        if (type != null && !type.isEmpty() && !type.equals("default")) {
            ComponentManager componentManager = this.componentManagerProvider.get();

            if (componentManager.hasComponent(MacroContentWikiSourceFactory.class, type)) {
                MacroContentWikiSourceFactory factory;
                try {
                    factory = componentManager.getInstance(MacroContentWikiSourceFactory.class, type);
                } catch (ComponentLookupException e) {
                    throw new MacroExecutionException("Failed to lookup code macro source factory for [" + type + "]",
                        e);
                }

                return factory.getContent(reference, context);
            }
        }

        throw new MacroExecutionException("Unsupported source type [" + type + "]");
    }
}

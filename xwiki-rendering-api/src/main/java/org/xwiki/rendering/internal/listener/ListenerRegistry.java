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
package org.xwiki.rendering.internal.listener;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.ListenerProvider;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Loads and register a list of {@link ChainingListener} provided by {@link ListenerProvider} in the provided chain.
 *
 * @version $Id$
 * @since 15.3RC1
 * @since 14.10.8
 */
@Component(roles = ListenerRegistry.class)
@Singleton
public class ListenerRegistry
{
    /**
     * Parse action identifier.
     */
    public static final String PARSE_ACTION = "parse";

    /**
     * Render action identifier.
     */
    public static final String RENDER_ACTION = "render";

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    /**
     * Register a list of {@link ChainingListener} provided by {@link ListenerProvider} in the provided chain.
     *
     * @param listenerChain the listener chain in which new listener will be added
     * @param action the action performed by the caller ("parse" or "render")
     * @param syntaxHint the hint of the syntax of the action (e.g., "xwiki/2.1")
     */
    public void registerListeners(ListenerChain listenerChain, String action, String syntaxHint)
    {
        try {
            this.componentManager.<ListenerProvider>getInstanceList(ListenerProvider.class)
                .stream()
                .filter(listenerProvider -> listenerProvider.accept(action, syntaxHint))
                .forEach(listenerProvider -> listenerChain.addListener(listenerProvider.getListener(listenerChain)));
        } catch (ComponentLookupException e) {
            this.logger.warn("Failed to load and register the list of [{}]. Cause [{}].", ListenerProvider.class,
                getRootCauseMessage(e));
        }
    }
}

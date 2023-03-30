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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.ListenerProvider;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.syntax.Syntax;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Returns a list of {@link ChainingListener} provided by {@link ListenerProvider}.
 *
 * @version $Id$
 * @since 15.3RC1
 * @since 14.10.8
 */
@Component(roles = ListenerRegistry.class)
@Singleton
public class ListenerRegistry
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    /**
     * Return a list of {@link ChainingListener} provided by {@link ListenerProvider}.
     *
     * @param listenerChain the listener chain in which new listener will be added
     * @param action the action performed by the caller ({@link ListenerProvider#PARSE_ACTION} or
     *     {@link ListenerProvider#RENDER_ACTION})
     * @param syntax the syntax of the action (e.g., {@link Syntax#XWIKI_2_1})
     * @return the initialized list of {@link ChainingListener}
     */
    public List<ChainingListener> getListeners(ListenerChain listenerChain, String action, Syntax syntax)
    {
        try {
            return this.componentManagerProvider.get()
                .<ListenerProvider>getInstanceList(ListenerProvider.class)
                .stream()
                .filter(listenerProvider -> listenerProvider.accept(action, syntax))
                .map(listenerProvider -> listenerProvider.getListener(listenerChain))
                .collect(Collectors.toList());
        } catch (ComponentLookupException e) {
            this.logger.warn("Failed to load the list of [{}] for action [{}] and syntax [{}]. Cause [{}].",
                ListenerProvider.class, action, syntax, getRootCauseMessage(e));
            return List.of();
        }
    }
}

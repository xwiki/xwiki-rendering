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

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * @version $Id$
 * @since x.y.z
 */
@Component(roles = ListenerRegistry.class)
public class ListenerRegistry
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private Logger logger;

    public void resigterListeners(ListenerChain chain)
    {
        try {
            this.componentManager.<ListenerProvider>getInstanceList(ListenerProvider.class)
                .forEach(listenerProvider -> chain.addListener(listenerProvider.getListener(chain)));
        } catch (ComponentLookupException e) {
            this.logger.warn("Failed to load and register the list of [{}]. Cause [{}].", ListenerProvider.class,
                getRootCauseMessage(e));
        }
    }
}

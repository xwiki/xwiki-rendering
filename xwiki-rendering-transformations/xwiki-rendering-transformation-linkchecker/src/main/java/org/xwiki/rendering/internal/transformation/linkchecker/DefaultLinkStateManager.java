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
package org.xwiki.rendering.internal.transformation.linkchecker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;

/**
 * Default implementation of {@link LinkStateManager} which supports multithreaded access to the link states.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Component
@Singleton
public class DefaultLinkStateManager implements LinkStateManager
{
    /**
     * @see #getLinkStates() to understand the map structure
     */
    private Map<String, Map<String, LinkState>> linkStates = new ConcurrentHashMap<String, Map<String, LinkState>>();

    @Override
    public Map<String, Map<String, LinkState>> getLinkStates()
    {
        return this.linkStates;
    }
}

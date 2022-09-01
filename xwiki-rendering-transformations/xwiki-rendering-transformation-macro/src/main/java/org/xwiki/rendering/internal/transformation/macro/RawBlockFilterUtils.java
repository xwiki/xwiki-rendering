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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.transformation.macro.RawBlockFilter;

/**
 * Utility components to retrieve raw blocker filters ordered by priority.
 *
 * @version $Id$
 * @since 14.8RC1
 * @deprecated This class should be removed once <a href="https://jira.xwiki.org/browse/XCOMMONS-2507">XCOMMONS-2507</a>
 *             is done.
 */
@Deprecated(since = "14.8RC1")
@Component(roles = RawBlockFilterUtils.class)
@Singleton
public class RawBlockFilterUtils
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Retrieve and return the list of filters ordered by priority.
     *
     * @return the list of filters ordered by priority.
     * @throws ComponentLookupException in case of problem for retrieving the instance list.
     */
    public List<RawBlockFilter> getRawBlockFilters() throws ComponentLookupException
    {
        List<RawBlockFilter> filters =
            new ArrayList<>(this.componentManagerProvider.get().getInstanceList(RawBlockFilter.class));
        filters.sort(Comparator.comparingInt(RawBlockFilter::getPriority));
        return filters;
    }
}

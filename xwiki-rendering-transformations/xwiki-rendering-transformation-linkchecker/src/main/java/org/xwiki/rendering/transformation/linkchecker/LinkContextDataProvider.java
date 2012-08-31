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
package org.xwiki.rendering.transformation.linkchecker;

import java.util.Map;

import org.xwiki.component.annotation.Role;

/**
 * Allows implementation to provide data related to a Link. For example this can be used to add a HTTP Request URL
 * so that when displaying the link status the user could see to what HTTP request it corresponds.
 *
 * <p>
 * Note: Be careful not to implement heavy data in providers since they are stored in cache for all external links
 * found and thus could lead to an Out Of Memory error if too large.
 * </p>
 *
 * @version $Id$
 * @since 3.3RC1
 */
@Role
public interface LinkContextDataProvider
{
    /**
     * @param linkURL the URL to the link being checked
     * @param contentReference the reference to the source where the link was found
     * @return the link context data to add to the link information
     */
    Map<String, Object> getContextData(String linkURL, String contentReference);
}

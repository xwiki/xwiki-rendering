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
package org.xwiki.rendering.renderer.reference.link;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.stability.Unstable;

/**
 * Generates the title of a wanted link, which is the link rendered for a resource that does not exist yet.
 * <p>
 * An implementation is looked up by a role hint equal to the scheme of the kind of reference it handles, as
 * returned by {@link org.xwiki.rendering.listener.reference.ResourceType#getScheme()}, so {@code doc} or
 * {@code space} for instance. An implementation registered under any other hint is never called: the lookup
 * falls back to the implementation registered without a hint, and reports nothing.
 *
 * @version $Id$
 * @since 18.8.0RC1
 */
@Role
@Unstable
public interface WantedLinkTitleGenerator
{
    /**
     * @param reference the reference of the resource the wanted link points to
     * @return the title to display on the rendered wanted link, as plain text since it ends up in an XHTML
     *         {@code title} attribute, or {@code null} to render the link without a title
     */
    String generateWantedLinkTitle(ResourceReference reference);
}

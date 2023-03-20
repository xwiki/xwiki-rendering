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
package org.xwiki.rendering.internal.renderer;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.reference.link.URITitleGenerator;

/**
 * Generate link titles for DOC URIs.
 *
 * @version $Id$
 * @since 15.2RC1
 */
@Component(hints = {"doc", "page"})
@Singleton
public class DefaultDocumentURITitleGenerator implements URITitleGenerator
{
    private static final String DEFAULT_TITLE = "Create page: %s";

    @Override
    public String generateCreateTitle(ResourceReference reference)
    {
        return String.format(DEFAULT_TITLE, reference.getReference());
    }
}

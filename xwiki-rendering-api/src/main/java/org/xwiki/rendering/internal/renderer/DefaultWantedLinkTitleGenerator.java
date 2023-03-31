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
import org.xwiki.rendering.renderer.reference.link.WantedLinkTitleGenerator;

/**
 * Generates wanted link titles for resource references.
 * Using this implementation should be avoided, another implementation should be used instead.
 * E.g. XWikiDocumentURITitleGenerator in xwiki-platform which is used to provide proper translations.
 * This implementation is a fallback and should only be used when xwiki-rendering is running by itself.
 * This implementation uses the reference as the title.
 * @version $Id$
 * @since 15.3RC1
 */
@Component
@Singleton
public class DefaultWantedLinkTitleGenerator implements WantedLinkTitleGenerator
{
    private static final String DEFAULT_TITLE = "Create resource: %s";

    /**
     * Generates wanted link titles for resource references.
     * @param reference the reference for which we want to generate a wanted link title
     * @return the wanted link title used when rendering a resource reference.
     */
    @Override
    public String generateWantedLinkTitle(ResourceReference reference)
    {
        return String.format(DEFAULT_TITLE, reference.getReference());
    }
}

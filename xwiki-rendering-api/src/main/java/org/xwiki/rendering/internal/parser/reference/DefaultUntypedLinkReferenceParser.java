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
package org.xwiki.rendering.internal.parser.reference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceTypeParser;

/**
 * Considers all passed link references to be untyped and tries to guess the type by first looking for an URL. If an URL
 * is not found, then consider it's an untyped reference to a document. Any client that wants to use this parsed
 * reference needs to first resolve it, since it could either point to a document or a space reference.
 *
 * @version $Id$
 * @since 2.6M1
 */
@Component
@Named("link/untyped")
@Singleton
public class DefaultUntypedLinkReferenceParser extends AbstractUntypedReferenceParser
{
    /**
     * Parser to parse link references pointing to documents.
     */
    @Inject
    @Named("doc")
    private ResourceReferenceTypeParser documentResourceReferenceTypeParser;

    /**
     * @param rawReference the untyped reference string to parse
     * @return an untyped (isTyped returns {@code false}) document reference that needs to be resolved before being
     *         used, since it could point to either a document or a space.
     */
    @Override
    protected ResourceReference getWikiResource(String rawReference)
    {
        // For backwards compatibility reasons and to not affect the ability to cache XDOMs, always resolve it as a
        // document
        ResourceReference reference = this.documentResourceReferenceTypeParser.parse(rawReference);

        // ...but make sure to mark it as untyped so that clients know that it needs to be resolved to the proper type.
        reference.setTyped(false);

        return reference;
    }
}

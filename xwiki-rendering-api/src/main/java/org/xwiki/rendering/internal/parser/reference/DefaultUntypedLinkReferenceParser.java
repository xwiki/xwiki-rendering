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
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceTypeParser;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Considers all passed link references to be untyped and tries to guess the type by first looking for an URL. If an URL
 * is not found, then consider it's a reference to a document. If the document does not exist, consider it's a reference
 * to a space, regardless if it exists or not.
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
     * Parser to parse link references pointing to spaces.
     */
    @Inject
    @Named("space")
    private ResourceReferenceTypeParser spaceResourceReferenceTypeParser;

    /**
     * @param rawReference the untyped reference string to parse
     * @return a reference to a document, if it exists, or a reference to a space instead
     */
    @Override
    protected ResourceReference getWikiResource(String rawReference)
    {
        ResourceReference reference;

        // It can be a link to an existing terminal document.
        ResourceReference documentResourceRefence = this.documentResourceReferenceTypeParser.parse(rawReference);
        if (resolveDocumentResource(documentResourceRefence)) {
            reference = documentResourceRefence;
        } else {
            // Otherwise, treat it as a link to an existing or inexistent space. If the space does not exist, it will be
            // a wanted link.
            ResourceReference spaceResourceReference = spaceResourceReferenceTypeParser.parse(rawReference);

            reference = spaceResourceReference;
        }
        return reference;
    }

    /**
     * @param resourceReference the reference to resolve
     * @return true if the given reference resolves to a valid document reference that should be used to resolve the
     *         untyped link; false otherwise
     */
    protected boolean resolveDocumentResource(ResourceReference resourceReference)
    {
        boolean result = false;

        WikiModel wikiModel;
        try {
            wikiModel = this.componentManagerProvider.get().getInstance(WikiModel.class);
        } catch (ComponentLookupException e) {
            // Should not happen since we`ve checked that we are in wiki mode.
            throw new RuntimeException(e);
        }

        // We resolve to this document reference only if it exists.
        if (wikiModel.isDocumentAvailable(resourceReference)) {
            result = true;
        }

        return result;
    }
}

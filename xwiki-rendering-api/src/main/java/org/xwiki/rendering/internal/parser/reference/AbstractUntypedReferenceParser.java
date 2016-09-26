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

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceTypeParser;

/**
 * Considers all passed references to be untyped and tries to guess the type by first looking for an URL. If an URL is
 * not found, then a wiki resource (based on the implementation) will be resolved.
 *
 * @version $Id$
 * @since 7.4.1
 * @since 8.0M1
 */
public abstract class AbstractUntypedReferenceParser extends AbstractResourceReferenceParser
{
    /**
     * Parser to parse link references pointing to URLs.
     */
    @Inject
    @Named("url")
    private ResourceReferenceTypeParser urlResourceReferenceTypeParser;

    @Override
    public ResourceReference parse(String rawReference)
    {
        ResourceReference reference;

        // If we're not in wiki mode then references are considered URLs.
        if (!isInWikiMode()) {
            reference = new ResourceReference(rawReference, ResourceType.URL);
        } else {
            // Try to guess the link type. It can be either:
            // - a URL (specified without the "url" type)
            // - a reference to a wiki resource (document, space, attachment, etc, specified without the type prefix).
            reference = this.urlResourceReferenceTypeParser.parse(rawReference);
            if (reference == null) {
                reference = getWikiResource(rawReference);
            }
        }
        reference.setTyped(false);

        return reference;
    }

    /**
     * @param rawReference the untyped reference string
     * @return the resource reference that points to a wiki resource
     */
    protected abstract ResourceReference getWikiResource(String rawReference);

}

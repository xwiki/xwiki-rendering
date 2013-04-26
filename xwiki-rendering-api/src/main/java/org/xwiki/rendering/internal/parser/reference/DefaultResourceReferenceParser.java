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

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceTypeParser;

/**
 * Parses the content of resource references. The format of a resource reference is the following:
 * {@code (type):(reference)} where {@code type} represents the type (see
 * {@link org.xwiki.rendering.listener.reference.ResourceType} of the resource pointed to (e.g. document, mailto,
 * attachment, image, document in another wiki, etc), and {@code reference} defines the target. The syntax of
 * {@code reference} depends on the Resource type and is documented in the javadoc of the various
 * {@link org.xwiki.rendering.parser.ResourceReferenceTypeParser} implementations. Note that the implementation is
 * pluggable and it's allowed plug new resource reference types by implementing
 * {@link org.xwiki.rendering.parser.ResourceReferenceTypeParser}s and registering the implementation as a component.
 * 
 * @version $Id$
 * @since 2.6M1
 */
@Component
@Singleton
public class DefaultResourceReferenceParser extends AbstractResourceReferenceParser
{
    /**
     * Link Reference Type separator (eg "mailto:mail@address").
     */
    public static final String TYPE_SEPARATOR = ":";

    /**
     * {@inheritDoc}
     * 
     * @return the parsed resource reference or a Resource Reference with {@link ResourceType#UNKNOWN} if no reference
     *         type was specified
     * @see org.xwiki.rendering.parser.ResourceReferenceParser#parse(String)
     */
    @Override
    public ResourceReference parse(String rawReference)
    {
        ResourceReference parsedResourceReference = null;
        
        // Step 1: Find the type parser matching the specified prefix type (if any).
        int pos = rawReference.indexOf(TYPE_SEPARATOR);
        if (pos > -1) {
            String typePrefix = rawReference.substring(0, pos);
            String reference = rawReference.substring(pos + 1);
            try {
                ResourceReferenceTypeParser parser =
                    this.componentManagerProvider.get().getInstance(ResourceReferenceTypeParser.class, typePrefix);
                parsedResourceReference = parser.parse(reference);
            } catch (ComponentLookupException e) {
                // Couldn't find a link type parser for the specified type.
            }
        }

        // Step 2: If there's no specific type parser found, then consider it's an Unknown type.
        if (parsedResourceReference == null) {
            parsedResourceReference = new ResourceReference(rawReference, ResourceType.UNKNOWN);
        }

        // Step 3: If we're not in wiki mode then wiki references to documents or attachments are considered URLs.
        if (!isInWikiMode()
            && (parsedResourceReference.getType().equals(ResourceType.ATTACHMENT)
            || parsedResourceReference.getType().equals(ResourceType.DOCUMENT)))
        {
            parsedResourceReference = new ResourceReference(rawReference, ResourceType.URL);
            parsedResourceReference.setTyped(false);
        }

        return parsedResourceReference;
    }
}

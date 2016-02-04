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
package org.xwiki.rendering.internal.renderer.xwiki20.reference;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;

/**
 * Serialize a link reference pointing to a space using the format {@code (space reference)(#anchor)(?query string)}.
 *
 * @version $Id$
 * @since 7.4.1, 8.0M1
 */
@Component
@Named("xwiki/2.0/space")
@Singleton
public class SpaceReferenceTypeSerializer extends DocumentReferenceTypeSerializer
{
    @Override
    public String serialize(ResourceReference reference)
    {
        StringBuilder buffer = new StringBuilder();

        if (reference.getReference() != null) {
            // Make sure we escape special chars: # and ? as they have special meaning in links, but only for
            // links to documents. Also escape \ since it's the escape char.
            String normalizedReference = addEscapesToReferencePart(reference.getReference());
            buffer.append(normalizedReference);

            // Since we don`t have typed references in the 2.0 syntax, we need to reuse the untyped document reference
            // syntax and make it explicit that it's about the WebHome document of the space.
            // This is most useful when converting from 2.1 back to 2.0.
            buffer.append(".WebHome");
        }

        String anchor = reference.getParameter(DocumentResourceReference.ANCHOR);
        if (anchor != null) {
            buffer.append('#');
            buffer.append(addEscapesToExtraParts(anchor));
        }
        String queryString = reference.getParameter(DocumentResourceReference.QUERY_STRING);
        if (queryString != null) {
            buffer.append('?');
            buffer.append(addEscapesToExtraParts(queryString));
        }

        return buffer.toString();
    }
}

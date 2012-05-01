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
package org.xwiki.rendering.xdomxml10.internal.renderer.parameter;

import org.xml.sax.ContentHandler;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.xdomxml10.internal.XDOMXMLConstants;

public class ResourceReferenceSerializer extends AbstractSerializer
{
    public void serialize(ResourceReference reference, ContentHandler contentHandler)
    {
        startElement("reference", EMPTY_ATTRIBUTES, contentHandler);

        serializeParameter("type", reference.getType().getScheme(), null, contentHandler);
        serializeParameter("reference", reference.getReference(), null, contentHandler);
        if (!reference.isTyped()) {
            serializeParameter("typed", reference.isTyped(), false, contentHandler);
        }
        if (reference.getParameters() != null && !reference.getParameters().isEmpty()) {
            serializeParameter(XDOMXMLConstants.ELEM_PARAMETERS, reference.getParameters(), false, contentHandler);
        }

        endElement("reference", contentHandler);
    }
}

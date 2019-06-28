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
package org.xwiki.rendering.internal.parser.reference.type;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link DataURIResourceReferenceTypeParser}.
 *
 * @version $Id$
 * @since 5.4RC1
 */
public class DataURIResourceReferenceTypeParserTest
{
    @Test
    public void validReferenceStartingWithImage()
    {
        DataURIResourceReferenceTypeParser parser = new DataURIResourceReferenceTypeParser();
        ResourceReference resourceReference = parser.parse("image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA...");
        assertEquals(ResourceType.DATA, resourceReference.getType());
        assertEquals("image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA...", resourceReference.getReference());
    }

    @Test
    public void invalidReferenceNotStartingWithImage()
    {
        DataURIResourceReferenceTypeParser parser = new DataURIResourceReferenceTypeParser();
        ResourceReference resourceReference = parser.parse("text/html;base64,iVBORw0KGgoAAAANSUhEUgAAA...");
        assertNull(resourceReference);
    }
}

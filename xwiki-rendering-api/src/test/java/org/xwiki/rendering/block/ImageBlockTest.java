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
package org.xwiki.rendering.block;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ImageBlock}.
 *
 * @version $Id$
 */
class ImageBlockTest
{
    @Test
    void testCloneWithNull()
    {
        ImageBlock imageBlock = new ImageBlock(null, false, null, Map.of());
        ImageBlock clone = (ImageBlock) imageBlock.clone();
        assertNotSame(imageBlock, clone);
        assertEquals(imageBlock, clone);
        assertNull(clone.getReference());
        assertFalse(clone.isFreeStandingURI());
        assertNull(clone.getId());
        assertNotSame(imageBlock.getParameters(), clone.getParameters());
        assertEquals(imageBlock.getParameters(), clone.getParameters());
    }

    @Test
    void testCloneWithValues()
    {
        ResourceReference reference = new ResourceReference("test", ResourceType.DATA);
        String id = "id";
        Map<String, String> parameters = Map.of("key", "value");
        ImageBlock imageBlock = new ImageBlock(reference, true, id, parameters);
        ImageBlock clone = (ImageBlock) imageBlock.clone();
        assertNotSame(imageBlock, clone);
        assertEquals(imageBlock, clone);
        assertNotSame(reference, clone.getReference());
        assertEquals(reference, clone.getReference());
        assertTrue(clone.isFreeStandingURI());
        assertEquals(id, clone.getId());
        assertNotSame(imageBlock.getParameters(), clone.getParameters());
        assertEquals(parameters, clone.getParameters());
    }
}

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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LinkBlock}.
 *
 * @version $Id$
 */
class LinkBlockTest
{
    @Test
    void testCloneWithNull()
    {
        LinkBlock linkBlock = new LinkBlock(List.of(), null, false);
        LinkBlock clone = (LinkBlock) linkBlock.clone();
        assertNotSame(linkBlock, clone);
        assertNull(clone.getReference());
        assertNotSame(linkBlock.getParameters(), clone.getParameters());
        assertEquals(linkBlock.getParameters(), clone.getParameters());
        assertFalse(clone.isFreeStandingURI());
        assertTrue(clone.getChildren().isEmpty());
    }

    @Test
    void testCloneWithValues()
    {
        ResourceReference reference = new ResourceReference("test", ResourceType.DATA);
        Block child = new WordBlock("TestBlock");
        LinkBlock linkBlock = new LinkBlock(List.of(child), reference, true);
        LinkBlock clone = (LinkBlock) linkBlock.clone();
        assertNotSame(linkBlock, clone);
        assertEquals(linkBlock, clone);
        assertNotSame(linkBlock.getReference(), clone.getReference());
        assertEquals(linkBlock.getReference(), clone.getReference());
        assertEquals(List.of(child), clone.getChildren());
        assertNotSame(child, clone.getChildren().get(0));
        assertTrue(clone.isFreeStandingURI());
    }
}

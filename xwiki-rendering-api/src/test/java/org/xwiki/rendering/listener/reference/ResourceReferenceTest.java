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
package org.xwiki.rendering.listener.reference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link ResourceReference}.
 *
 * @version $Id$
 * @since 3.0M2
 */
class ResourceReferenceTest
{
    @Test
    void testToString()
    {
        ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);
        assertEquals("Typed = [true] Type = [doc] Reference = [reference]", reference.toString());

        reference.addBaseReference("baseref1");
        reference.addBaseReference("baseref2");
        assertEquals(
            "Typed = [true] Type = [doc] Reference = [reference] " + "Base References = [[baseref1], [baseref2]]",
            reference.toString());

        reference.setParameter("name1", "value1");
        reference.setParameter("name2", "value2");
        assertEquals("Typed = [true] Type = [doc] Reference = [reference] "
            + "Base References = [[baseref1], [baseref2]] " + "Parameters = [[name1] = [value1], [name2] = [value2]]",
            reference.toString());
    }

    @Test
    void testEquals()
    {
        ResourceReference reference1 = new ResourceReference("reference", ResourceType.DOCUMENT);
        ResourceReference reference2 = new ResourceReference("reference", ResourceType.DOCUMENT);
        assertEquals(reference1, reference2);

        reference2.addBaseReference("base");
        assertFalse(reference1.equals(reference2));
        assertFalse(reference1.equals(null));
        assertFalse(reference1.equals("different object class"));

        reference1.addBaseReference("base");
        assertEquals(reference1, reference2);
        assertEquals(reference1, reference1);
    }

    @Test
    void testHashCode()
    {
        ResourceReference reference1 = new ResourceReference("reference", ResourceType.DOCUMENT);
        ResourceReference reference2 = new ResourceReference("reference", ResourceType.DOCUMENT);
        assertEquals(reference1.hashCode(), reference2.hashCode());

        reference1.addBaseReference("base");
        reference2.addBaseReference("base");
        assertEquals(reference1.hashCode(), reference2.hashCode());
    }

    @Test
    void cloneResource()
    {
        ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);

        reference.addBaseReference("base1");
        reference.setParameter("parameter1", "value1");

        ResourceReference clonedReference = reference.clone();

        assertEquals(reference, clonedReference);

        clonedReference.addBaseReference("base2");
        clonedReference.setParameter("parameter2", "value2");

        assertNotEquals(reference, clonedReference);
    }
}

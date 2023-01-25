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
package org.xwiki.rendering.internal.macro.source;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Validate {@link MacroContentSourceReference}.
 * 
 * @version $Id$
 */
class MacroContentSourceReferenceTest
{
    @Test
    void equals()
    {
        MacroContentSourceReference reference = new MacroContentSourceReference("type", "reference");

        assertEquals(reference, new MacroContentSourceReference("type", "reference"));
        assertEquals(reference, reference);

        assertNotEquals(reference, null);
        assertNotEquals(reference, new MacroContentSourceReference("type", "reference2"));
        assertNotEquals(reference, new MacroContentSourceReference("type2", "reference"));
        assertNotEquals(reference, new MacroContentSourceReference("type2", "reference2"));
    }

    @Test
    void hasCode()
    {
        MacroContentSourceReference reference = new MacroContentSourceReference("type", "reference");

        assertEquals(reference.hashCode(), new MacroContentSourceReference("type", "reference").hashCode());
        assertEquals(reference.hashCode(), reference.hashCode());

        assertNotEquals(reference.hashCode(), new MacroContentSourceReference("type", "reference2").hashCode());
        assertNotEquals(reference.hashCode(), new MacroContentSourceReference("type2", "reference").hashCode());
        assertNotEquals(reference.hashCode(), new MacroContentSourceReference("type2", "reference2").hashCode());
    }
}

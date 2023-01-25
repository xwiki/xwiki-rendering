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
package org.xwiki.rendering.macro.source;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link MacroContentSourceReference}.
 * 
 * @version $Id$
 */
class MacroContentSourceReferenceTest
{
    @Test
    void tostring()
    {
        assertEquals("type:reference", new MacroContentSourceReference("type", "reference").toString());
    }

    @Test
    void equals()
    {
        assertTrue(new MacroContentSourceReference("type", "reference")
            .equals(new MacroContentSourceReference("type", "reference")));

        assertFalse(new MacroContentSourceReference("type", "reference")
            .equals(new MacroContentSourceReference("type", "reference2")));
        assertFalse(new MacroContentSourceReference("type", "reference")
            .equals(new MacroContentSourceReference("type2", "reference")));
        assertFalse(new MacroContentSourceReference("type", "reference").equals(null));
        assertFalse(new MacroContentSourceReference("type", "reference").equals("other"));
    }

    @Test
    void hasCode()
    {
        MacroContentSourceReference reference1 = new MacroContentSourceReference("type", "reference");
        MacroContentSourceReference reference2 = new MacroContentSourceReference("type", "reference");

        assertEquals(reference1.hashCode(), reference2.hashCode());
    }
}

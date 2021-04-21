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
package org.xwiki.rendering.internal.macro;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MacroId}.
 *
 * @version $Id$
 * @since 2.0M3
 */
 class MacroIdTest
{
    @Test
    void testEquality()
    {
        Syntax syntax1 = new Syntax(new SyntaxType("syntax1", "Syntax 1"), "1.0");
        Syntax syntax2 = new Syntax(new SyntaxType("syntax2", "Syntax 2"), "1.0");
        MacroId id1 = new MacroId("id", syntax1);
        MacroId id2 = new MacroId("id", syntax1);
        MacroId id3 = new MacroId("otherid", syntax1);
        MacroId id4 = new MacroId("id", syntax2);
        MacroId id5 = new MacroId("otherid", syntax2);
        MacroId id6 = new MacroId("id");
        MacroId id7 = new MacroId("id");

        assertEquals(id2, id1);
        // Equal objects must have equal hashcode
        assertTrue(id1.hashCode() == id2.hashCode());

        assertFalse(id3 == id1);
        assertFalse(id3.equals(id1));
        assertFalse(id1.equals(id3));

        assertFalse(id4 == id1);
        assertFalse(id4.equals(id1));
        assertFalse(id1.equals(id4));

        assertFalse(id5 == id3);
        assertFalse(id5.equals(id1));
        assertFalse(id1.equals(id5));

        assertFalse(id6 == id1);
        assertFalse(id6.equals(id1));
        assertFalse(id1.equals(id6));

        assertEquals(id7, id6);
        // Equal objects must have equal hashcode
        assertTrue(id6.hashCode() == id7.hashCode());
    }
}

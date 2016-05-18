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
package org.xwiki.rendering.syntax;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.xwiki.rendering.syntax.Syntax}.
 *
 * @version $Id$
 * @since 1.5M2
 */
public class SyntaxTest
{
    @Test
    public void testEquality()
    {
        Syntax syntax1 = new Syntax(new SyntaxType("mytype", "My Type"), "1.0");
        Syntax syntax2 = new Syntax(new SyntaxType("mytype", "My Type"), "1.0");
        Syntax syntax3 = new Syntax(new SyntaxType("mytype", "Still same type"), "1.0");

        assertEquals("mytype", syntax1.getType().getId());
        assertEquals("My Type", syntax1.getType().getName());

        assertEquals(syntax2, syntax1);
        // The syntax type name is not part of the equality test.
        assertEquals(syntax3, syntax1);
    }

    @Test
    public void testNonEquality()
    {
        Syntax syntax1 = new Syntax(SyntaxType.XWIKI, "1.0");
        Syntax syntax2 = new Syntax(SyntaxType.XWIKI, "2.0");
        Syntax syntax3 = new Syntax(SyntaxType.CONFLUENCE, "1.0");

        assertFalse(syntax2.equals(syntax1));
        assertFalse(syntax3.equals(syntax1));
    }

    @Test
    public void testToString()
    {
        Syntax syntax = new Syntax(SyntaxType.XWIKI, "1.0");
        assertEquals("XWiki 1.0", syntax.toString());
        assertEquals("xwiki/1.0", syntax.toIdString());
    }

    @Test
    public void getWellKnownSyntaxes()
    {
        assertEquals(18, SyntaxType.getSyntaxTypes().size());
        assertEquals(new SyntaxType("xwiki", "XWiki"), SyntaxType.getSyntaxTypes().get("xwiki"));
    }

    @Test
    public void comparisons()
    {
        Syntax syntax1 = new Syntax(new SyntaxType("mytype1", "BBB"), "1.0");
        Syntax syntax2 = new Syntax(new SyntaxType("mytype2", "AAA"), "1.0");
        Syntax syntax3 = new Syntax(new SyntaxType("mytype3", "BBB"), "1.1");

        assertEquals(0, syntax1.compareTo(syntax1));
        assertTrue(syntax1.compareTo(syntax2) > 0);
        assertTrue(syntax3.compareTo(syntax1) > 0);
    }
}

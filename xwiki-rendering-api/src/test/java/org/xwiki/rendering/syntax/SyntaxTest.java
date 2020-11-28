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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.parser.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link org.xwiki.rendering.syntax.Syntax}.
 *
 * @version $Id$
 * @since 1.5M2
 */
class SyntaxTest
{
    @Test
    void equality()
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
    void nonEquality()
    {
        Syntax syntax1 = new Syntax(SyntaxType.XWIKI, "1.0");
        Syntax syntax2 = new Syntax(SyntaxType.XWIKI, "2.0");
        Syntax syntax3 = new Syntax(SyntaxType.CONFLUENCE, "1.0");

        assertFalse(syntax2.equals(syntax1));
        assertFalse(syntax3.equals(syntax1));
    }

    @Test
    void toStringValidation()
    {
        Syntax syntax1 = new Syntax(SyntaxType.XWIKI, "1.0");
        assertEquals("XWiki 1.0", syntax1.toString());
        assertEquals("xwiki/1.0", syntax1.toIdString());
    }

    @Test
    void getSyntaxTypes()
    {
        assertEquals(18, SyntaxType.getSyntaxTypes().size());
        assertEquals(new SyntaxType("xwiki", "XWiki"), SyntaxType.getSyntaxTypes().get("xwiki"));
    }

    @Test
    void compareToValidation()
    {
        Syntax syntax1 = new Syntax(new SyntaxType("mytype1", "BBB"), "1.0");
        Syntax syntax2 = new Syntax(new SyntaxType("mytype2", "AAA"), "1.0");
        Syntax syntax3 = new Syntax(new SyntaxType("mytype3", "BBB"), "1.1");

        assertEquals(0, syntax1.compareTo(syntax1));
        assertTrue(syntax1.compareTo(syntax2) > 0);
        assertTrue(syntax3.compareTo(syntax1) > 0);

        List<Syntax> syntaxes = new ArrayList<>();
        syntaxes.add(syntax3);
        syntaxes.add(syntax2);
        syntaxes.add(syntax1);
        assertThat(syntaxes, contains(syntax3, syntax2, syntax1));
        Collections.sort(syntaxes);
        assertThat(syntaxes, contains(syntax2, syntax1, syntax3));
    }

    @Test
    void valueOfOk() throws Exception
    {
        Syntax syntax1 = Syntax.valueOf("type/version");
        assertEquals("type", syntax1.getType().getId());
        assertEquals("type", syntax1.getType().getName());
        assertEquals("version", syntax1.getVersion());
    }

    @Test
    void valueOfWhenInvalid()
    {
        Throwable exception = assertThrows(ParseException.class, () -> {
            Syntax.valueOf("invalid");
        });
        assertEquals("Invalid Syntax format [invalid]", exception.getMessage());
    }

    @Test
    void valueOfWhenNull()
    {
        Throwable exception = assertThrows(ParseException.class, () -> {
            Syntax.valueOf(null);
        });
        assertEquals("The passed Syntax cannot be NULL", exception.getMessage());
    }
}

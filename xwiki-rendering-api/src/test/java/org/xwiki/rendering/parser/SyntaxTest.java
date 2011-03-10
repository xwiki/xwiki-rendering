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
package org.xwiki.rendering.parser;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

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

        Assert.assertEquals("mytype", syntax1.getType().getId());
        Assert.assertEquals("My Type", syntax1.getType().getName());

        Assert.assertEquals(syntax2, syntax1);
        // The syntax type name is not part of the equality test.
        Assert.assertEquals(syntax3, syntax1);
    }

    @Test
    public void testNonEquality()
    {
        Syntax syntax1 = new Syntax(SyntaxType.XWIKI, "1.0");
        Syntax syntax2 = new Syntax(SyntaxType.XWIKI, "2.0");
        Syntax syntax3 = new Syntax(SyntaxType.CONFLUENCE, "1.0");

        Assert.assertFalse(syntax2.equals(syntax1));
        Assert.assertFalse(syntax3.equals(syntax1));
    }

    @Test
    public void testToString()
    {
        Syntax syntax = new Syntax(SyntaxType.XWIKI, "1.0");
        Assert.assertEquals("XWiki 1.0", syntax.toString());
        Assert.assertEquals("xwiki/1.0", syntax.toIdString());
    }
}

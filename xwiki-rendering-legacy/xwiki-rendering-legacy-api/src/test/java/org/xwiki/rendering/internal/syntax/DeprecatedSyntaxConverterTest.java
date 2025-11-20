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
package org.xwiki.rendering.internal.syntax;

import org.junit.jupiter.api.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for {@link DeprecatedSyntaxConverter}.
 * 
 * @version $Id$
 */
@ComponentList({
    SyntaxConverter.class,
    DefaultSyntaxRegistry.class
})
@ComponentTest
class DeprecatedSyntaxConverterTest
{
    @InjectMockComponents
    private DeprecatedSyntaxConverter converter;

    // Tests

    @Test
    void convertToSyntaxObject()
    {
        Syntax syntax = (Syntax) this.converter.convert(Syntax.class, "xwiki/2.1");
        assertEquals(Syntax.XWIKI_2_1, syntax);
    }

    @Test
    void convertToSyntaxObjectWhenUnknownSyntax()
    {
        try {
            this.converter.convert(Syntax.class, "invalid");
            fail("Should have thrown ConversionException");
        } catch (ConversionException expected) {
            assertEquals("Unknown syntax [invalid]", expected.getMessage());
        }
    }

    @Test
    void convertToSyntaxObjectWhenNull()
    {
        Syntax syntax = (Syntax) this.converter.convert(Syntax.class, null);
        assertNull(syntax);
    }

    @Test
    void convertToString()
    {
        String syntaxId = (String) this.converter.convert(String.class, Syntax.XWIKI_2_1);
        assertEquals(Syntax.XWIKI_2_1.toIdString(), syntaxId);
    }

    @Test
    void convertToStringWhenNull()
    {
        String syntaxId = (String) this.converter.convert(String.class, null);
        assertNull(syntaxId);
    }
}

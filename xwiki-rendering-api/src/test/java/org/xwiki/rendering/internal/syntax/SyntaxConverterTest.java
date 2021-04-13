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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SyntaxConverter}.
 *
 * @version $Id$
 */
@ComponentTest
class SyntaxConverterTest
{
    @InjectMockComponents
    private SyntaxConverter syntaxConverter;

    @MockComponent
    private SyntaxRegistry syntaxRegistry;

    @Test
    void convertToSyntaxObject() throws Exception
    {
        Syntax expectedSyntax = new Syntax(new SyntaxType("mysyntax", "My Syntax"), "1.0");
        when(this.syntaxRegistry.resolveSyntax("mysyntax/1.0")).thenReturn(expectedSyntax);

        Syntax syntax = this.syntaxConverter.convert(Syntax.class, "mysyntax/1.0");
        assertEquals(expectedSyntax, syntax);
    }

    @Test
    void convertToSyntaxObjectWhenUnknownSyntax() throws Exception
    {
        when(this.syntaxRegistry.resolveSyntax("invalid")).thenThrow(new ParseException("error"));

        Throwable exception = assertThrows(ConversionException.class,
            () -> this.syntaxConverter.convert(Syntax.class, "invalid"));
        assertEquals("Unknown syntax [invalid]", exception.getMessage());
        assertEquals("ParseException: error", ExceptionUtils.getRootCauseMessage(exception));
    }

    @Test
    void convertToSyntaxObjectWhenNull()
    {
        Syntax syntax = this.syntaxConverter.convert(Syntax.class, null);
        assertNull(syntax);
    }

    @Test
    void convertToString()
    {
        Syntax syntax = new Syntax(new SyntaxType("mysyntax", "My Syntax"), "1.0");
        String syntaxId = this.syntaxConverter.convert(String.class, syntax);
        assertEquals("mysyntax/1.0", syntaxId);
    }

    @Test
    void convertToStringWhenNull()
    {
        String syntaxId = this.syntaxConverter.convert(String.class, null);
        assertNull(syntaxId);
    }
}

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

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link DeprecatedSyntaxConverter}.
 * 
 * @version $Id$
 */
@ComponentList({
    SyntaxConverter.class,
    DefaultSyntaxRegistry.class
})
public class DeprecatedSyntaxConverterTest
{
    @Rule
    public MockitoComponentMockingRule<Converter> mocker =
        new MockitoComponentMockingRule<>(DeprecatedSyntaxConverter.class);

    @BeforeComponent
    public void beforeComponent() throws Exception
    {
    }

    // Tests

    @Test
    public void convertToSyntaxObject() throws Exception
    {
        Syntax syntax = (Syntax) this.mocker.getComponentUnderTest().convert(Syntax.class, "xwiki/2.1");
        assertEquals(Syntax.XWIKI_2_1, syntax);
    }

    @Test
    public void convertToSyntaxObjectWhenUnknownSyntax() throws Exception
    {
        try {
            this.mocker.getComponentUnderTest().convert(Syntax.class, "invalid");
            fail("Should have thrown ConversionException");
        } catch (ConversionException expected) {
            assertEquals("Unknown syntax [invalid]", expected.getMessage());
        }
    }

    @Test
    public void convertToSyntaxObjectWhenNull() throws Exception
    {
        Syntax syntax = (Syntax) this.mocker.getComponentUnderTest().convert(Syntax.class, null);
        assertNull(syntax);
    }

    @Test
    public void convertToString() throws Exception
    {
        String syntaxId = (String) this.mocker.getComponentUnderTest().convert(String.class, Syntax.XWIKI_2_1);
        assertEquals(Syntax.XWIKI_2_1.toIdString(), syntaxId);
    }

    @Test
    public void convertToStringWhenNull() throws Exception
    {
        String syntaxId = (String) this.mocker.getComponentUnderTest().convert(String.class, null);
        assertNull(syntaxId);
    }
}

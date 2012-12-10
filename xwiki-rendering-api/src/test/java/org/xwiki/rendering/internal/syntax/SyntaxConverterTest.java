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

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for {@link SyntaxConverter}.
 *
 * @version $Id$
 * @since 4.2M3
 */
@MockingRequirement(SyntaxConverter.class)
public class SyntaxConverterTest extends AbstractMockingComponentTestCase<Converter>
{
    @Test
    public void convertToSyntaxObject() throws Exception
    {
        final SyntaxFactory factory = getComponentManager().getInstance(SyntaxFactory.class);
        getMockery().checking(new Expectations()
        {
            {
                oneOf(factory).createSyntaxFromIdString("xwiki/2.1");
                will(returnValue(Syntax.XWIKI_2_1));
            }
        });

        Syntax syntax = getMockedComponent().convert(Syntax.class, "xwiki/2.1");
        Assert.assertEquals(Syntax.XWIKI_2_1, syntax);
    }

    @Test
    public void convertToSyntaxObjectWhenUnknownSyntax() throws Exception
    {
        final SyntaxFactory factory = getComponentManager().getInstance(SyntaxFactory.class);
        getMockery().checking(new Expectations()
        {
            {
                oneOf(factory).createSyntaxFromIdString("invalid");
                will(throwException(new ParseException("invalid syntax")));
            }
        });

        try {
            getMockedComponent().convert(Syntax.class, "invalid");
            Assert.fail("Should have thrown ConversionException");
        } catch (ConversionException expected) {
            Assert.assertEquals("Unknown syntax [invalid]", expected.getMessage());
        }
    }

    @Test
    public void convertToSyntaxObjectWhenNull() throws Exception
    {
        Syntax syntax = getMockedComponent().convert(Syntax.class, null);
        Assert.assertNull(syntax);
    }

    @Test
    public void convertToString() throws Exception
    {
        String syntaxId = getMockedComponent().convert(String.class, Syntax.XWIKI_2_1);
        Assert.assertEquals(Syntax.XWIKI_2_1.toIdString(), syntaxId);
    }

    @Test
    public void convertToStringWhenNull() throws Exception
    {
        String syntaxId = getMockedComponent().convert(String.class, null);
        Assert.assertNull(syntaxId);
    }
}

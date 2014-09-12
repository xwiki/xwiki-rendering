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
package org.xwiki.rendering.internal.macro.ctsreport;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link TestParser}.
 *
 * @version $Id$
 * @since 4.1M2
 */
public class TestParserTest
{
    @Test
    public void parseOk()
    {
        TestParser parser = new TestParser();
        String input = "simple/bold/bold1 [xwiki/2.0, IN:bold1.in.txt, CTS:bold1.inout.xml]";
        Result result = parser.parse(input);
        Assert.assertEquals("simple/bold/bold1", result.test.prefix);
        Assert.assertEquals("xwiki/2.0", result.syntaxId);
        Assert.assertEquals("bold1.in.txt", result.test.syntaxExtension);
        Assert.assertEquals("bold1.inout.xml", result.test.ctsExtension);
        Assert.assertTrue(result.isSyntaxInputTest);
        Assert.assertEquals(State.UNKNOWN, result.test.state);

        result = parser.parse(input + " - Failing");
        Assert.assertEquals(State.FAILING, result.test.state);

        result = parser.parse(input + " - Missing");
        Assert.assertEquals(State.MISSING, result.test.state);

        result = parser.parse(input + " - Passed");
        Assert.assertEquals(State.PASSED, result.test.state);
    }

    @Test
    public void parseWithInvalidInput()
    {
        TestParser parser = new TestParser();
        try {
            parser.parse("invalid");
            Assert.fail();
        } catch (Exception expected) {
            Assert.assertEquals("Invalid Syntax Test format for [invalid]", expected.getMessage());
        }
    }

}

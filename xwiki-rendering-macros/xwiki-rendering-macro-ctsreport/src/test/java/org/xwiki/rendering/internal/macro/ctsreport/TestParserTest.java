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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TestParser}.
 *
 * @version $Id$
 * @since 4.1M2
 */
class TestParserTest
{
    @Test
    void parseStateUnknown()
    {
        TestParser parser = new TestParser();
        Result result = parser.parse("simple/bold/bold1 [xwiki/2.0, IN:bold1.in.txt, CTS:bold1.inout.xml]");
        assertEquals("simple/bold/bold1", result.test.prefix);
        assertEquals("xwiki/2.0", result.syntaxId);
        assertEquals("bold1.in.txt", result.test.syntaxExtension);
        assertEquals("bold1.inout.xml", result.test.ctsExtension);
        assertTrue(result.isSyntaxInputTest);
        assertEquals(State.UNKNOWN, result.test.state);
    }

    @Test
    void parseStateFailing()
    {
        TestParser parser = new TestParser();
        String input = "simple/bold/bold1 [xwiki/2.0, IN:bold1.in.txt, CTS:bold1.inout.xml] - Failing";
        Result result = parser.parse(input);
        assertEquals(State.FAILING, result.test.state);
    }

    @Test
    void parseStateMissing()
    {
        TestParser parser = new TestParser();
        Result result = parser.parse("simple/bold/bold1 [xwiki/2.0, IN:bold1.in.txt, CTS:bold1.inout.xml] - Missing");
        assertEquals(State.MISSING, result.test.state);
    }

    @Test
    void parseStatePassed()
    {
        TestParser parser = new TestParser();
        Result result = parser.parse("simple/bold/bold1 [xwiki/2.0, IN:bold1.in.txt, CTS:bold1.inout.xml] - Passed");
        assertEquals(State.PASSED, result.test.state);
    }

    @Test
    void parseWithInvalidInput()
    {
        TestParser parser = new TestParser();
        Exception exception = assertThrows(RuntimeException.class, () -> parser.parse("invalid"));
        assertEquals("Invalid Syntax Test format for [invalid]", exception.getMessage());
    }
}

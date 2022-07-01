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
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.xwiki.rendering.syntax.Syntax.HTML_5_0;

/**
 * Test of {@link DefaultMacroIdFactory}.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@ComponentTest
class DefaultMacroIdFactoryTest
{
    @InjectMockComponents
    private DefaultMacroIdFactory macroIdFactory;

    @MockComponent
    private SyntaxRegistry syntaxRegistry;

    @Test
    void createMacroId() throws Exception
    {
        when(this.syntaxRegistry.resolveSyntax("html/5.0")).thenReturn(HTML_5_0);
        assertEquals(new MacroId("testMacro", HTML_5_0), this.macroIdFactory.createMacroId("testMacro/html/5.0"));
    }

    @Test
    void createMacroIdUnknownSyntax() throws Exception
    {
        when(this.syntaxRegistry.resolveSyntax("html/5.0")).thenThrow(ParseException.class);
        ParseException parseException =
            assertThrows(ParseException.class, () -> this.macroIdFactory.createMacroId("testMacro/html/5.0"));
        assertEquals(ParseException.class, parseException.getCause().getClass());
        assertEquals("Invalid macro id format [testMacro/html/5.0]", parseException.getMessage());
    }
}

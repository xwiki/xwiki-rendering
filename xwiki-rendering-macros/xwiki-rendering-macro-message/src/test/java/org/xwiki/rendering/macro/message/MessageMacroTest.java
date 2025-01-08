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
package org.xwiki.rendering.macro.message;

import java.io.Reader;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.internal.macro.message.MessageMacroParameters;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.IconProvider;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link org.xwiki.rendering.internal.macro.message.AbstractMessageMacro} macros (for tests that cannot
 * be performed with the rendering test framework).
 *
 * @version $Id$
 * @since 2.0M3
 */
@AllComponents
@ComponentTest
class MessageMacroTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private MacroTransformationContext context;

    /**
     * Tests whether message macro content descriptor is never null.
     */
    @Test
    void macroContentDescriptorIsNotNull() throws Exception
    {
        Macro messageMacro = this.componentManager.getInstance(Macro.class, "info");
        assertNotNull(messageMacro.getDescriptor().getContentDescriptor());
        messageMacro = this.componentManager.getInstance(Macro.class, "warning");
        assertNotNull(messageMacro.getDescriptor().getContentDescriptor());
        messageMacro = this.componentManager.getInstance(Macro.class, "error");
        assertNotNull(messageMacro.getDescriptor().getContentDescriptor());
        messageMacro = this.componentManager.getInstance(Macro.class, "success");
        assertNotNull(messageMacro.getDescriptor().getContentDescriptor());
    }

    @Test
    void executeWithRawBlockForIconBlock() throws Exception
    {
        IconProvider iconProvider = this.componentManager.registerMockComponent(IconProvider.class);
        when(iconProvider.get("information")).thenReturn(new RawBlock("some html", Syntax.HTML_4_01));
        Macro messageMacro = this.componentManager.getInstance(Macro.class, "info");
        MessageMacroParameters parameters = new MessageMacroParameters();
        when(this.context.getSyntax()).thenReturn(Syntax.XWIKI_2_1);

        List<Block> blocks = messageMacro.execute(parameters, "content", this.context);

        // Assert the result by rendering it in xwiki/2.1 syntax for easy assertion.
        BlockRenderer renderer = this.componentManager.getInstance(BlockRenderer.class, "xwiki/2.1");
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.render(blocks, printer);
        assertEquals("(% class=\"box infomessage\" %)\n"
            + "(((\n"
            + "(% class=\"sr-only\" %)Info(%%)\n\n"
            + "(((\n"
            + "content\n"
            + ")))\n"
            + ")))", printer.toString());
    }

    @Test
    void executeWhenParsingErrorForIconPrettyName() throws Exception
    {
        IconProvider iconProvider = this.componentManager.registerMockComponent(IconProvider.class);
        when(iconProvider.get("information")).thenReturn(new RawBlock("some html", Syntax.HTML_4_01));
        Parser plainTextParser = this.componentManager.registerMockComponent(Parser.class, "plain/1.0");
        doThrow(new ParseException("error")).when(plainTextParser).parse(any(Reader.class));
        Macro messageMacro = this.componentManager.getInstance(Macro.class, "info");
        MessageMacroParameters parameters = new MessageMacroParameters();
        when(this.context.getSyntax()).thenReturn(Syntax.XWIKI_2_1);

        Throwable exception = assertThrows(MacroExecutionException.class,
            () -> messageMacro.execute(parameters, "content", this.context));
        assertEquals("Failed to parse icon pretty name [Info] to compute a text alternative", exception.getMessage());
    }
}

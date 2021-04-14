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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.internal.transformation.macro.TestSimpleMacro;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroNotFoundException;
import org.xwiki.rendering.syntax.Syntax;

import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.macro.DefaultMacroManager}.
 *
 * @version $Id$
 * @since 1.9M1
 */
@ComponentTest
@AllComponents
class DefaultMacroManagerTest
{
    @InjectMockComponents
    private DefaultMacroManager macroManager;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private class TestInvalidMacro extends AbstractNoParameterMacro
    {
        /**
         * @param unusedParameter a parameter that shouldn't be there in a component. We do that to make sure that the
         *        CM will fail to instantiate this macro and to test this error case.
         */
        TestInvalidMacro(String unusedParameter)
        {
            super("Invalid Macro");
        }

        @Override
        public boolean supportsInlineMode()
        {
            throw new RuntimeException("Not used");
        }

        @Override
        public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
            throws MacroExecutionException
        {
            throw new RuntimeException("Not used");
        }
    }

    @Test
    void exists()
    {
        assertTrue(this.macroManager.exists(new MacroId("testsimplemacro")));
    }

    @Test
    void getMacroWhenExisting() throws Exception
    {
        assertNotNull(this.macroManager.getMacro(new MacroId("testsimplemacro")));
    }

    @Test
    void getMacroWhenNotExisting()
    {
        Throwable exception = assertThrows(MacroNotFoundException.class,
            () -> this.macroManager.getMacro(new MacroId("notregisteredmacro")));
        assertEquals("No macro [notregisteredmacro] could be found.", exception.getMessage());
    }

    @Test
    void getMacroWhenMacroFailsToInstantiate() throws Exception
    {
        // Register the macro. note that we don't register it in components.txt since it would cause some errors in
        // other tests.
        DefaultComponentDescriptor<Macro> cd = new DefaultComponentDescriptor<>();
        cd.setRoleType(Macro.class);
        cd.setRoleHint("testinvalidmacro");
        cd.setImplementation(TestInvalidMacro.class);
        this.componentManager.registerComponent(cd);

        Throwable exception = assertThrows(MacroLookupException.class,
            () -> this.macroManager.getMacro(new MacroId("testinvalidmacro")));
        assertEquals("Macro [testinvalidmacro] failed to be instantiated.", exception.getMessage());
    }

    @Test
    void existsWhenMacroIsRegisteredForAllSyntaxes()
    {
        assertFalse(this.macroManager.exists(new MacroId("testsimplemacro",
            new Syntax(SyntaxType.XWIKI, "2.0"))));
    }

    @Test
    void getMacroWhenMacroIsRegisteredForAllSyntaxes() throws Exception
    {
        assertNotNull(this.macroManager.getMacro(new MacroId("testsimplemacro",
            new Syntax(SyntaxType.XWIKI, "2.0"))));
    }

    @Test
    void getMacroWhenMacroRegisteredForAGivenSyntaxOnly() throws Exception
    {
        Macro<?> macro = new TestSimpleMacro();
        DefaultComponentDescriptor<Macro> descriptor = new DefaultComponentDescriptor<>();
        descriptor.setRoleType(Macro.class);
        descriptor.setRoleHint("macro/xwiki/2.0");
        this.componentManager.registerComponent(descriptor, macro);

        assertFalse(this.macroManager.exists(new MacroId("macro")));
        assertTrue(this.macroManager.exists(new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0"))));

        Macro<?> macroResult = this.macroManager.getMacro(
            new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0")));
        assertSame(macro, macroResult);
    }

    @Test
    void getMacroWhenMacroRegisteredForAGivenSyntaxOverridesMacroRegisteredForAllSyntaxes() throws Exception
    {
        Macro<?> macro1 = new TestSimpleMacro();
        Macro<?> macro2 = new TestSimpleMacro();

        DefaultComponentDescriptor<Macro> descriptor = new DefaultComponentDescriptor<>();
        descriptor.setRoleType(Macro.class);
        descriptor.setRoleHint("macro");
        this.componentManager.registerComponent(descriptor, macro1);

        descriptor = new DefaultComponentDescriptor<>();
        descriptor.setRoleType(Macro.class);
        descriptor.setRoleHint("macro/xwiki/2.0");
        this.componentManager.registerComponent(descriptor, macro2);

        assertTrue(this.macroManager.exists(new MacroId("macro")));
        assertTrue(this.macroManager.exists(new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0"))));

        Macro<?> macroResult1 = this.macroManager.getMacro(
            new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0")));
        assertSame(macro2, macroResult1);

        Macro<?> macroResult2 = this.macroManager.getMacro(new MacroId("macro"));
        assertSame(macro1, macroResult2);
    }

    /**
     * Verify that we get a log warning when a macro is registered with an invalid hint.
     */
    @Test
    void getMacroIdsWhenInvalidMacroHint() throws Exception
    {
        Macro<?> macro = new TestSimpleMacro();
        DefaultComponentDescriptor<Macro> descriptor = new DefaultComponentDescriptor<>();
        descriptor.setRoleType(Macro.class);
        descriptor.setRoleHint("macro/invalidsyntax");
        this.componentManager.registerComponent(descriptor, macro);

        this.macroManager.getMacroIds(new Syntax(new SyntaxType("syntax", "Syntax"), "whatever"));

        assertEquals(1, logCapture.size());
        assertEquals("Invalid Macro descriptor format for hint [macro/invalidsyntax]. The hint should contain either "
            + "the macro name only or the macro name followed by the syntax for which it is valid. In that case the "
            + "macro name should be followed by a \"/\" followed by the syntax name followed by another \"/\" followed "
            + "by the syntax version. For example \"html/xwiki/2.0\". This macro will not be available in the system.",
            logCapture.getMessage(0));
    }
}

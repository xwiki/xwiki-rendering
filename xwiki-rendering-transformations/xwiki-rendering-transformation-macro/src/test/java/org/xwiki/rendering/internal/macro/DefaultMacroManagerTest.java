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

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.internal.transformation.macro.TestSimpleMacro;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroIdFactory;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.MacroNotFoundException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.test.jmock.AbstractMockingComponentTestCase;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.MockingRequirement;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.macro.DefaultMacroManager}.
 * 
 * @version $Id$
 * @since 1.9M1
 */
@MockingRequirement(value = DefaultMacroManager.class,
    // Mock all required components except for some for which we want to use the real implementations since they make
    // the test easier to write (no need to mock them).
    exceptions = { ComponentManager.class, MacroIdFactory.class, Provider.class })
@AllComponents
public class DefaultMacroManagerTest extends AbstractMockingComponentTestCase<MacroManager>
{
    private class TestInvalidMacro extends AbstractNoParameterMacro
    {
        /**
         * @param invalidParameter a parameter that shouldn't be there in a component
         */
        public TestInvalidMacro(String invalidParameter)
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
    public void testMacroExists() throws Exception
    {
        Assert.assertTrue(getMockedComponent().exists(new MacroId("testsimplemacro")));
    }

    @Test
    public void testGetExistingMacro() throws Exception
    {
        Assert.assertNotNull(getMockedComponent().getMacro(new MacroId("testsimplemacro")));
    }

    @Test
    public void testGetNotExistingMacro() throws Exception
    {
        try {
            getMockedComponent().getMacro(new MacroId("notregisteredmacro"));
            Assert.fail("Expected a MacroNotFoundException when looking for not registered macro");
        } catch (MacroNotFoundException expected) {
            Assert.assertEquals("No macro [notregisteredmacro] could be found.", expected.getMessage());
        }
    }

    @Test
    public void testGetInvalidMacro() throws Exception
    {
        // Register the macro. note that we don't register it in components.txt since it would cause some errors in
        // other tests.
        DefaultComponentDescriptor<Macro> cd = new DefaultComponentDescriptor<Macro>();
        cd.setRoleType(Macro.class);
        cd.setRoleHint("testinvalidmacro");
        cd.setImplementation(TestInvalidMacro.class);
        getComponentManager().registerComponent(cd);

        try {
            getMockedComponent().getMacro(new MacroId("testinvalidmacro"));
            Assert.fail("Expected a MacroLookupException when looking for an invalid macro");
        } catch (MacroLookupException expected) {
            Assert.assertEquals("Macro [testinvalidmacro] failed to be instantiated.", expected.getMessage());
        }
    }

    @Test
    public void testSyntaxSpecificMacroExistsWhenMacroIsRegisteredForAllSyntaxes() throws Exception
    {
        Assert.assertFalse(getMockedComponent().exists(new MacroId("testsimplemacro",
            new Syntax(SyntaxType.XWIKI, "2.0"))));
    }

    @Test
    public void testGetExistingMacroForASpecificSyntaxWhenMacroIsRegisteredForAllSyntaxes() throws Exception
    {
        Assert.assertNotNull(getMockedComponent().getMacro(new MacroId("testsimplemacro",
            new Syntax(SyntaxType.XWIKI, "2.0"))));
    }

    @Test
    public void testMacroRegisteredForAGivenSyntaxOnly() throws Exception
    {
        Macro< ? > macro = new TestSimpleMacro();
        DefaultComponentDescriptor<Macro> descriptor = new DefaultComponentDescriptor<Macro>();
        descriptor.setRole(Macro.class);
        descriptor.setRoleHint("macro/xwiki/2.0");
        getComponentManager().registerComponent(descriptor, macro);

        Assert.assertFalse(getMockedComponent().exists(new MacroId("macro")));
        Assert.assertTrue(getMockedComponent().exists(new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0"))));

        Macro< ? > macroResult = getMockedComponent().getMacro(
            new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0")));
        Assert.assertSame(macro, macroResult);
    }

    @Test
    public void testMacroRegisteredForAGivenSyntaxOverridesMacroRegisteredForAllSyntaxes() throws Exception
    {
        Macro< ? > macro1 = new TestSimpleMacro();
        Macro< ? > macro2 = new TestSimpleMacro();

        DefaultComponentDescriptor<Macro> descriptor = new DefaultComponentDescriptor<Macro>();
        descriptor.setRole(Macro.class);
        descriptor.setRoleHint("macro");
        getComponentManager().registerComponent(descriptor, macro1);

        descriptor = new DefaultComponentDescriptor<Macro>();
        descriptor.setRole(Macro.class);
        descriptor.setRoleHint("macro/xwiki/2.0");
        getComponentManager().registerComponent(descriptor, macro2);

        Assert.assertTrue(getMockedComponent().exists(new MacroId("macro")));
        Assert.assertTrue(getMockedComponent().exists(new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0"))));

        Macro< ? > macroResult1 = getMockedComponent().getMacro(
            new MacroId("macro", new Syntax(SyntaxType.XWIKI, "2.0")));
        Assert.assertSame(macro2, macroResult1);

        Macro< ? > macroResult2 = getMockedComponent().getMacro(new MacroId("macro"));
        Assert.assertSame(macro1, macroResult2);
    }

    /**
     * Tests what happens when a macro is registered with an invalid hint.
     */
    @Test
    public void testInvalidMacroHint() throws Exception
    {
        // Control the list of macros found in the system by replacing the real ComponentManager in MacroManager with
        // a mock one.
        final ComponentManager mockRootComponentManager = registerMockComponent(ComponentManager.class, "context");

        // Note: Make sure to get the mocked component before calling getMockLogger() since this is what injects the
        // mock loggers...
        MacroManager macroManager = getMockedComponent();
        final Logger logger = getMockLogger();

        getMockery().checking(new Expectations() {{
            allowing(mockRootComponentManager).getInstance(ComponentManager.class, "context");
            will(returnValue(mockRootComponentManager));
            allowing(mockRootComponentManager).getInstanceMap(Macro.class);
            will(returnValue(Collections.singletonMap("macro/invalidsyntax", "dummy")));

            // Test: Make sure the logger is called with the following content. This is the assert for this test.
            oneOf(logger).warn("Invalid Macro descriptor format for hint "
                + "[macro/invalidsyntax]. The hint should contain either the macro name only or the macro name "
                + "followed by the syntax for which it is valid. In that case the macro name should be followed by "
                + "a \"/\" followed by the syntax name followed by another \"/\" followed by the syntax version. "
                + "For example \"html/xwiki/2.0\". This macro will not be available in the system.");
        }});

        SyntaxFactory syntaxFactory = getComponentManager().getInstance(SyntaxFactory.class);
        macroManager.getMacroIds(syntaxFactory.createSyntaxFromIdString("macro/xwiki/2.0"));
    }
}

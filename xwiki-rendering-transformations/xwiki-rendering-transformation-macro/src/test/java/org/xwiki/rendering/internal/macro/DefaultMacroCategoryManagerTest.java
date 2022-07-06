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

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.Converter;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroCategoryManager;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.descriptor.DefaultMacroDescriptor;
import org.xwiki.rendering.transformation.macro.MacroTransformationConfiguration;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static ch.qos.logback.classic.Level.WARN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xwiki.rendering.syntax.Syntax.JSPWIKI_1_0;
import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_0;

/**
 * Unit tests for {@link MacroCategoryManager}.
 *
 * @version $Id$
 * @since 2.0M3
 */
@ComponentTest
class DefaultMacroCategoryManagerTest
{
    @InjectMockComponents
    private DefaultMacroCategoryManager macroCategoryManager;

    @MockComponent
    private MacroTransformationConfiguration configuration;

    @MockComponent
    private MacroManager macroManager;

    @MockComponent
    private ConverterManager converterManager;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @Mock
    private Converter<Object> converter;

    @BeforeEach
    void setUp()
    {
        when(this.converterManager.getConverter(List.class)).thenReturn(this.converter);
    }

    @Test
    void getMacroCategories() throws Exception
    {
        Properties properties = new Properties();
        properties.put("testcontentmacro", "Content");
        properties.put("testsimplemacro", "Simple");
        when(this.configuration.getCategories()).thenReturn(properties);
        when(this.macroManager.getMacroIds())
            .thenReturn(Set.of(new MacroId("testcontentmacro"), new MacroId("testsimplemacro")));

        when(this.converter.convert(List.class, "Content")).thenReturn(List.of("Content"));
        when(this.converter.convert(List.class, "Simple")).thenReturn(List.of("Simple"));

        Set<String> macroCategories = this.macroCategoryManager.getMacroCategories();

        // Check for a default category.
        assertEquals(Set.of("Content", "Simple"), macroCategories);
    }

    @Test
    void getMacroCategoriesNoCategory() throws Exception
    {
        MacroId macroAId = new MacroId("macroA");
        DefaultMacroDescriptor macroADescriptor = new DefaultMacroDescriptor(macroAId, "Macro A", "Macro A");
        Macro macroA = mock(Macro.class);

        when(this.configuration.getCategories()).thenReturn(new Properties());
        when(this.macroManager.getMacroIds()).thenReturn(Set.of(macroAId));
        when(this.macroManager.getMacro(macroAId)).thenReturn(macroA);
        when(macroA.getDescriptor()).thenReturn(macroADescriptor);

        // Check whether our macros are registered under correct categories.
        Set<Object> expected = new HashSet<>();
        expected.add(null);
        assertEquals(expected, this.macroCategoryManager.getMacroCategories());
    }

    @Test
    void getMacroIds() throws Exception
    {
        Properties properties = new Properties();
        properties.put("mytestmacro1", "Cat1");
        properties.put("mytestmacro2", "Cat2");
        properties.put("mytestmacro3", "Cat1,Cat2");
        when(this.configuration.getCategories()).thenReturn(properties);
        when(this.macroManager.getMacroIds())
            .thenReturn(Set.of(new MacroId("mytestmacro1"), new MacroId("mytestmacro2"), new MacroId("mytestmacro3")));
        when(this.converter.convert(List.class, "Cat1,Cat2")).thenReturn(List.of("Cat1", "Cat2"));
        when(this.converter.convert(List.class, "Cat2")).thenReturn(List.of("Cat2"));
        when(this.converter.convert(List.class, "Cat1")).thenReturn(List.of("Cat1"));

        // Check whether our macros are registered under correct categories.
        assertEquals(Set.of(new MacroId("mytestmacro1"), new MacroId("mytestmacro3")),
            this.macroCategoryManager.getMacroIds("Cat1"));
    }

    @Test
    void getMacroIdsWithSyntax() throws Exception
    {
        Properties properties = new Properties();
        properties.put("mytestmacro1", "Cat1");
        properties.put("mytestmacro2", "Cat2");
        when(this.configuration.getCategories()).thenReturn(properties);
        when(this.macroManager.getMacroIds())
            .thenReturn(Set.of(new MacroId("mytestmacro1"), new MacroId("mytestmacro2")));
        when(this.converter.convert(List.class, "Cat1")).thenReturn(List.of("Cat1"));
        when(this.converter.convert(List.class, "Cat2")).thenReturn(List.of("Cat2"));

        // These macros should be registered for all syntaxes.
        assertEquals(Set.of(new MacroId("mytestmacro1")), this.macroCategoryManager.getMacroIds("Cat1", JSPWIKI_1_0));
    }

    @Test
    void getMacroIdsWithSyntaxSpecificMacros() throws Exception
    {
        MacroId myTestMacroId = new MacroId("mytestmacro", XWIKI_2_0);
        Properties properties = new Properties();
        properties.put("mytestmacro", "Test");
        when(this.configuration.getCategories()).thenReturn(properties);
        when(this.macroManager.getMacroIds()).thenReturn(Set.of(myTestMacroId));
        when(this.converter.convert(List.class, "Test")).thenReturn(List.of("Test"));

        // Make sure our macro is put into the correct category & registered under correct syntax.
        assertEquals(Set.of(myTestMacroId), this.macroCategoryManager.getMacroIds("Test"));
        assertEquals(Set.of(myTestMacroId), this.macroCategoryManager.getMacroIds("Test", XWIKI_2_0));
        assertEquals(Set.of(), this.macroCategoryManager.getMacroIds("Test", JSPWIKI_1_0));
    }

    @Test
    void getMacroCategoriesByMacroIdNoOverride() throws Exception
    {
        MacroId macroAId = new MacroId("macroA");
        Macro macroA = mock(Macro.class);
        DefaultMacroDescriptor macroADescriptor = new DefaultMacroDescriptor(macroAId, "Macro A", "Macro A");
        macroADescriptor.setDefaultCategories(Set.of("Cat A, Cat B"));

        when(this.configuration.getCategories()).thenReturn(new Properties());
        when(this.macroManager.getMacro(macroAId)).thenReturn(macroA);
        when(macroA.getDescriptor()).thenReturn(macroADescriptor);

        assertEquals(Set.of("Cat A, Cat B"), this.macroCategoryManager.getMacroCategories(macroAId));
    }

    @Test
    void getMacroCategoriesByMacroIdNoOverrideError() throws Exception
    {
        MacroId macroAId = new MacroId("macroA");
        Macro macroA = mock(Macro.class);
        DefaultMacroDescriptor macroADescriptor = new DefaultMacroDescriptor(macroAId, "Macro A", "Macro A");
        macroADescriptor.setDefaultCategories(Set.of("Cat A, Cat B"));

        when(this.configuration.getCategories()).thenReturn(new Properties());
        when(this.macroManager.getMacro(macroAId)).thenThrow(MacroLookupException.class);
        when(macroA.getDescriptor()).thenReturn(macroADescriptor);

        assertEquals(Set.of(), this.macroCategoryManager.getMacroCategories(macroAId));
        assertEquals("Failed to get macro [macroA]. Cause: [MacroLookupException: ]", this.logCapture.getMessage(0));
        assertEquals(WARN, this.logCapture.getLogEvent(0).getLevel());
    }

    @Test
    void getMacroCategoriesByMacroIdWithOverride() throws Exception
    {
        MacroId macroAId = new MacroId("macroA");
        Macro macroA = mock(Macro.class);
        DefaultMacroDescriptor macroADescriptor = new DefaultMacroDescriptor(macroAId, "Macro A", "Macro A");
        macroADescriptor.setDefaultCategories(Set.of("Cat A, Cat B"));

        Properties properties = new Properties();
        properties.put("macroA", "O1,O2");
        when(this.configuration.getCategories()).thenReturn(properties);
        when(this.macroManager.getMacro(macroAId)).thenReturn(macroA);
        when(macroA.getDescriptor()).thenReturn(macroADescriptor);
        when(this.configuration.getCategories()).thenReturn(properties);
        when(this.macroManager.getMacro(macroAId)).thenThrow(MacroLookupException.class);
        when(this.converter.convert(List.class, "O1,O2")).thenReturn(List.of("O1", "O2"));

        assertEquals(Set.of("O1", "O2"), this.macroCategoryManager.getMacroCategories(macroAId));
    }
}

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
package org.xwiki.rendering.macro.toc;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyException;
import org.xwiki.properties.internal.DefaultBeanManager;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.rendering.internal.macro.toc.TocMacro;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.rendering.wiki.WikiModelException;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validate {@link TocMacro}.
 *
 * @version $Id$
 */
@ComponentList({
    DefaultBeanManager.class,
    DefaultConverterManager.class,
    EnumConverter.class,
    ConvertUtilsConverter.class
})
@ComponentTest
class TocMacroTest
{
    @InjectMockComponents
    private TocMacro tocMacro;

    @InjectComponentManager
    private ComponentManager componentManager;

    @MockComponent
    private WikiModel wikiModel;

    @MockComponent
    private TocTreeBuilderFactory tocTreeBuilderFactory;

    private BeanManager beanManager;

    @BeforeEach
    void setUp() throws Exception
    {
        this.beanManager = this.componentManager.getInstance(BeanManager.class);
    }

    @Test
    void startTooLow()
    {
        TocMacroParameters bean = new TocMacroParameters();
        Map<String, ?> values = Map.of("start", "0");
        PropertyException propertyException =
            assertThrows(PropertyException.class, () -> this.beanManager.populate(bean, values));
        assertEquals("Failed to populate property [start]", propertyException.getMessage());
    }

    @Test
    void depthTooLow() throws Exception
    {
        TocMacroParameters bean = new TocMacroParameters();
        Map<String, ?> values = Map.of("depth", "0");
        PropertyException propertyException =
            assertThrows(PropertyException.class, () -> this.beanManager.populate(bean, values));
        assertEquals("Failed to populate property [depth]", propertyException.getMessage());
    }

    @Test
    void executeWhenXDOMRetrievalFailed() throws Exception
    {
        TocMacroParameters parameters = new TocMacroParameters();
        parameters.setReference("reference");
        when(this.wikiModel.getXDOM(new DocumentResourceReference("reference")))
            .thenThrow(new WikiModelException("error"));

        MacroTransformationContext mtc = mock(MacroTransformationContext.class);

        MacroExecutionException macroExecutionException =
            assertThrows(MacroExecutionException.class, () -> this.tocMacro.execute(parameters, null, mtc));
        assertEquals("Failed to get XDOM for [Typed = [true] Type = [doc] Reference = [reference]]",
            macroExecutionException.getMessage());
    }

    @Test
    void executeInitializationException() throws Exception
    {
        when(this.tocTreeBuilderFactory.build()).thenThrow(ComponentLookupException.class);
        InitializationException initializationException =
            assertThrows(InitializationException.class, () -> this.tocMacro.initialize());
        assertEquals("Failed to initialize [class org.xwiki.rendering.internal.macro.toc.TocTreeBuilder]",
            initializationException.getMessage());
        assertEquals(ComponentLookupException.class, initializationException.getCause().getClass());
    }
}

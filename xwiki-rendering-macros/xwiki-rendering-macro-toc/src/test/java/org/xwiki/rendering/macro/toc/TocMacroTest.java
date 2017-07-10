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

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
public class TocMacroTest
{
    @Rule
    public MockitoComponentMockingRule<TocMacro> mocker = new MockitoComponentMockingRule<TocMacro>(TocMacro.class);

    private BeanManager beanManager;

    @Before
    public void setUp() throws Exception
    {
        this.beanManager = this.mocker.getInstance(BeanManager.class);
    }

    @Test(expected = PropertyException.class)
    public void testStartTooLow() throws PropertyException
    {
        this.beanManager.populate(new TocMacroParameters(), Collections.singletonMap("start", "0"));
    }

    @Test(expected = PropertyException.class)
    public void testDepthTooLow() throws PropertyException
    {
        this.beanManager.populate(new TocMacroParameters(), Collections.singletonMap("depth", "0"));
    }

    @Test
    public void executeWhenXDOMRetrievalFailed() throws Exception
    {
        TocMacroParameters parameters = new TocMacroParameters();
        parameters.setReference("reference");
        WikiModel wikiModel = this.mocker.getInstance(WikiModel.class);
        when(wikiModel.getXDOM(new DocumentResourceReference("reference"))).thenThrow(new WikiModelException("error"));

        MacroTransformationContext mtc = mock(MacroTransformationContext.class);

        try {
            this.mocker.getComponentUnderTest().execute(parameters, null, mtc);
            fail("Should have thrown an exception here");
        } catch (MacroExecutionException expected) {
            assertEquals("Failed to get XDOM for [Typed = [true] Type = [doc] Reference = [reference]]",
                expected.getMessage());
        }

    }
}

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
package org.xwiki.rendering.macro.raw;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.internal.macro.raw.RawMacro;
import org.xwiki.rendering.internal.transformation.macro.RawBlockFilterUtils;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.macro.RawBlockFilter;
import org.xwiki.rendering.transformation.macro.RawBlockFilterParameters;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link RawMacro}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
@ComponentTest
class RawMacroTest
{
    private static final String TEST_CONTENT = "Test content";

    @Component
    @InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
    public static class ThrowingComponent implements Initializable, RawBlockFilter
    {
        private static final String MESSAGE = "Test exception";

        @Override
        public void initialize() throws InitializationException
        {
            throw new InitializationException(MESSAGE);
        }

        @Override
        public RawBlock filter(RawBlock block, RawBlockFilterParameters parameters)
        {
            return null;
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    }

    @InjectMockComponents
    private RawMacro rawMacro;

    @MockComponent
    private RawBlockFilterUtils rawBlockFilterUtils;

    @Test
    void filtering() throws Exception
    {
        RawBlock expectedRawBlock = new RawBlock(TEST_CONTENT, Syntax.EVENT_1_0);

        MacroTransformationContext transformationContext = new MacroTransformationContext();

        RawBlockFilterParameters expectedFilterParameters = new RawBlockFilterParameters(transformationContext);

        RawBlock filteredRawBlock = new RawBlock("filtered", Syntax.EVENT_1_0);

        RawBlockFilter filter = mock(RawBlockFilter.class);
        when(filter.filter(expectedRawBlock, expectedFilterParameters)).thenReturn(filteredRawBlock);
        when(rawBlockFilterUtils.getRawBlockFilters()).thenReturn(Collections.singletonList(filter));

        RawMacroParameters parameters = new RawMacroParameters();
        parameters.setSyntax(Syntax.EVENT_1_0);
        List<Block> result = this.rawMacro.execute(parameters, TEST_CONTENT, transformationContext);

        verify(filter).filter(expectedRawBlock, expectedFilterParameters);
        verifyNoMoreInteractions(filter);

        assertEquals(1, result.size());
        assertEquals(filteredRawBlock, result.get(0));
    }

    @Test
    void throwingFilter() throws Exception
    {
        MacroTransformationContext transformationContext = new MacroTransformationContext();

        when(rawBlockFilterUtils.getRawBlockFilters())
            .thenThrow(new ComponentLookupException("Error initializing component",
                new InitializationException(ThrowingComponent.MESSAGE)));

        Exception exception = assertThrows(MacroExecutionException.class, () ->
            this.rawMacro.execute(new RawMacroParameters(), "Hello", transformationContext));

        assertTrue(exception.getMessage().contains("raw content filtering"));
        assertEquals(ThrowingComponent.MESSAGE, exception.getCause().getCause().getMessage());
    }
}

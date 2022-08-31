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
package org.xwiki.rendering.internal.transformation.macro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.macro.RawBlockFilterParameters;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xml.html.DefaultHTMLCleanerComponentList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the {@link HTMLRawBlockFilter}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
@ComponentTest
@DefaultHTMLCleanerComponentList
class HTMLRawBlockFilterTest
{
    private static final String INPUT = "<a href=\"javascript:alert(1)\">Hello!</a>";

    private static final String P_START = "<p>";

    private static final String P_END = "</p>";

    private static final String BLOCK_OUTPUT = P_START + INPUT + P_END;

    private static final String RESTRICTED_OUTPUT = "<a>Hello!</a>";

    private static final String RESTRICTED_BLOCK_OUTPUT = P_START + RESTRICTED_OUTPUT + P_END;

    @InjectMockComponents
    private HTMLRawBlockFilter htmlRawBlockFilter;

    @ParameterizedTest
    @CsvSource({
        "false, false, true,  false, " + RESTRICTED_BLOCK_OUTPUT,
        "false, false, false, false, " + INPUT,
        "false, true,  false,  true, " + RESTRICTED_OUTPUT,
        "true,  true,  false,  true, " + RESTRICTED_OUTPUT,
        "true,  false, false, false, " + BLOCK_OUTPUT })
    void cleaning(boolean clean, boolean restricted, boolean restrictedContext, boolean inline,
        String output) throws MacroExecutionException
    {
        RawBlock input = new RawBlock(INPUT, Syntax.HTML_5_0);

        MacroTransformationContext context = new MacroTransformationContext();
        context.setInline(inline);
        context.getTransformationContext().setRestricted(restrictedContext);
        RawBlockFilterParameters parameters = new RawBlockFilterParameters(context);
        parameters.setClean(clean);
        parameters.setRestricted(restricted);

        RawBlock outputBlock = this.htmlRawBlockFilter.filter(input, parameters);

        assertEquals(output, outputBlock.getRawContent());
        assertEquals(input.getSyntax(), outputBlock.getSyntax());
    }

    @Test
    void invalidInlineCleaning()
    {
        RawBlock input = new RawBlock("<div>Block content.</div>", Syntax.XHTML_1_0);

        MacroTransformationContext macroTransformationContext = new MacroTransformationContext();
        macroTransformationContext.setInline(true);
        RawBlockFilterParameters parameters = new RawBlockFilterParameters(macroTransformationContext);
        parameters.setClean(true);
        Exception exception = assertThrows(MacroExecutionException.class,
            () -> this.htmlRawBlockFilter.filter(input, parameters));
        assertTrue(exception.getMessage().contains("inline HTML content"));
    }
}

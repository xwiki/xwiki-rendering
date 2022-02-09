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
package org.xwiki.rendering.macro.html;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.internal.macro.html.HTMLMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link HTMLMacro} that cannot be performed using the Rendering Test framework.
 *
 * @version $Id$
 * @since 1.8.3
 */
@ComponentTest
@AllComponents
class HTMLMacroTest
{
    @InjectMockComponents
    private HTMLMacro macro;

    /**
     * Verify that inline HTML macros with non inline content generate an exception.
     */
    @Test
    void executeMacroWhenNonInlineContentInInlineContext()
    {
        HTMLMacroParameters parameters = new HTMLMacroParameters();
        MacroTransformationContext context = new MacroTransformationContext();
        context.setInline(true);
        assertThrows(MacroExecutionException.class, () ->
            this.macro.execute(parameters, "<ul><li>item</li></ul>", context));
    }

    @Test
    void macroDescriptor()
    {
        assertEquals("Indicate if the HTML should be transformed into valid XHTML or not.",
            this.macro.getDescriptor().getParameterDescriptorMap().get("clean").getDescription());
    }

    @Test
    void restrictedHtml() throws MacroExecutionException
    {
        HTMLMacroParameters parameters = new HTMLMacroParameters();
        MacroTransformationContext context = new MacroTransformationContext();
        context.getTransformationContext().setRestricted(true);
        List<Block> blocks = this.macro.execute(parameters, "<script>alert('Hello!');</script>", context);

        for (Block block : blocks) {
            if (block instanceof RawBlock) {
                RawBlock rawBlock = (RawBlock) block;
                assertEquals("<pre>alert('Hello!');</pre>", rawBlock.getRawContent());
            }
        }
    }
}

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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.internal.macro.html.HTMLMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Unit tests for {@link HTMLMacro} that cannot be performed using the Rendering Test framework.
 *
 * @version $Id$
 * @since 1.8.3
 */
public class HTMLMacroTest extends AbstractComponentTestCase
{
    /**
     * Verify that inline HTML macros with non inline content generate an exception.
     */
    @Test(expected = MacroExecutionException.class)
    public void executeMacroWhenNonInlineContentInInlineContext() throws Exception
    {
        HTMLMacro macro = getComponentManager().getInstance(Macro.class, "html");
        HTMLMacroParameters parameters = new HTMLMacroParameters();
        MacroTransformationContext context = new MacroTransformationContext();
        context.setInline(true);
        macro.execute(parameters, "<ul><li>item</li></ul>", context);
    }

    @Test
    public void macroDescriptor() throws Exception
    {
        HTMLMacro macro = getComponentManager().getInstance(Macro.class, "html");

        Assert.assertEquals("Indicate if the HTML should be transformed into valid XHTML or not.",
            macro.getDescriptor().getParameterDescriptorMap().get("clean").getDescription());
    }

    @Test
    public void restrictedHtml() throws Exception
    {
        HTMLMacro macro = getComponentManager().getInstance(Macro.class, "html");
        HTMLMacroParameters parameters = new HTMLMacroParameters();
        MacroTransformationContext context = new MacroTransformationContext();
        context.getTransformationContext().setRestricted(true);
        List<Block> blocks = macro.execute(parameters, "<script>alert('Hello!');</script>", context);

        for (Block block : blocks) {
            if (block instanceof RawBlock) {
                RawBlock rawBlock = (RawBlock) block;
                Assert.assertEquals("<pre>alert('Hello!');</pre>", rawBlock.getRawContent());
            }
        }
    }
}

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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.Format;

/**
 * Unit tests for {@link MacroErrorManager}.
 *
 * @version $Id$
 * @since 4.3M2
 */
public class MacroErrorManagerTest
{
    @Test
    public void containsErrorWhenNoError()
    {
        MacroErrorManager errorManager = new MacroErrorManager();
        XDOM xdom = new XDOM(Arrays.<Block>asList(new ParagraphBlock(Arrays.<Block>asList(new WordBlock("test")))));
        Assert.assertFalse(errorManager.containsError(xdom));
    }

    @Test
    public void containsErrorWhenNoErrorButGroupAndFormatBlocks()
    {
        MacroErrorManager errorManager = new MacroErrorManager();
        XDOM xdom = new XDOM(Arrays.<Block>asList(new GroupBlock(Arrays.<Block>asList(
            new FormatBlock(Arrays.<Block>asList(new WordBlock("test")), Format.BOLD)))));
        Assert.assertFalse(errorManager.containsError(xdom));
    }

    @Test
    public void containsErrorWhenInlineMacroError()
    {
        MacroErrorManager errorManager = new MacroErrorManager();
        MacroBlock macroBlock = new MacroBlock("testmacro", Collections.<String, String>emptyMap(), true);
        XDOM xdom = new XDOM(Arrays.<Block>asList(macroBlock));
        errorManager.generateError(macroBlock, "test message", "test description");
        Assert.assertTrue(errorManager.containsError(xdom));
    }
}

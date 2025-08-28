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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.util.DefaultErrorBlockGenerator;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.util.ErrorBlockGenerator;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MacroErrorManager}.
 *
 * @version $Id$
 * @since 4.3M2
 */
@ComponentTest
@ComponentList({
    DefaultErrorBlockGenerator.class
})
class MacroErrorManagerTest
{
    @Inject
    private ErrorBlockGenerator errorGenerator;

    @Test
    void containsErrorWhenNoError()
    {
        MacroErrorManager errorManager = new MacroErrorManager(this.errorGenerator);
        XDOM xdom = new XDOM(List.of(new ParagraphBlock(List.of(new WordBlock("test")))));
        assertFalse(errorManager.containsError(xdom));
    }

    @Test
    void containsErrorWhenNoErrorButGroupAndFormatBlocks()
    {
        MacroErrorManager errorManager = new MacroErrorManager(this.errorGenerator);
        XDOM xdom = new XDOM(List.of(new GroupBlock(List.of(
            new FormatBlock(List.of(new WordBlock("test")), Format.BOLD)))));
        assertFalse(errorManager.containsError(xdom));
    }

    @Test
    void containsErrorWhenInlineMacroError()
    {
        MacroErrorManager errorManager = new MacroErrorManager(this.errorGenerator);
        MacroBlock macroBlock = new MacroBlock("testmacro", Collections.emptyMap(), true);
        XDOM xdom = new XDOM(List.of(macroBlock));
        errorManager.generateError(macroBlock, null, "test message", "test description");
        assertTrue(errorManager.containsError(xdom));
    }
}

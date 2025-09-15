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

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MacroTransformationContext}.
 *
 * @version $Id$
 * @since 3.0M1
 */
class MacroTransformationContextTest
{
    @Test
    void testClone()
    {
        MacroTransformationContext context = new MacroTransformationContext();
        context.setId("id");
        context.setInline(true);
        context.setSyntax(Syntax.XWIKI_2_0);

        XDOM xdom = new XDOM(List.of(new WordBlock("test1")));
        context.setXDOM(xdom);

        MacroBlock macroBlock = new MacroBlock("testmacro", Collections.emptyMap(), null, false);
        context.setCurrentMacroBlock(macroBlock);

        Transformation transformation = new AbstractTransformation()
        {
            @Override
            public void transform(Block block, TransformationContext context) throws TransformationException
            {
                throw new RuntimeException("dummy");
            }
        };
        context.setTransformation(transformation);

        MacroTransformationContext newContext = context.clone();
        assertNotSame(context, newContext);
        assertEquals("id", newContext.getId());
        assertTrue(newContext.isInline());
        assertEquals(Syntax.XWIKI_2_0, newContext.getSyntax());
        assertEquals(xdom, newContext.getXDOM());
        assertEquals(macroBlock, newContext.getCurrentMacroBlock());
        assertEquals(transformation, newContext.getTransformation());
    }
}

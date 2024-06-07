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

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Replacement macro for the {@link TestReplaceMeMacro}.
 *
 * @version $Id$
 */
@Component
@Named(TestReplacementMacro.NAME)
@Singleton
public class TestReplacementMacro extends AbstractMacro<TestMacroParameter>
{
    /**
     * The name of the macro.
     */
    public static final String NAME = "testReplacement";

    /**
     * Default constructor.
     */
    public TestReplacementMacro()
    {
        super(NAME, "Replacement Macro", new DefaultContentDescriptor("The content of the macro.",
            false, Block.LIST_BLOCK_TYPE), TestMacroParameter.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(TestMacroParameter parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        String parameter1 = parameters.getParam1();
        return List.of(new WordBlock(NAME), new WordBlock(content), new WordBlock(parameter1));
    }
}

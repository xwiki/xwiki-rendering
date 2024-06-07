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
import java.util.Map;
import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * A macro that replaces itself with another macro.
 *
 * @version $Id$
 */
@Component
@Named("testReplaceMe")
@Singleton
public class TestReplaceMeMacro extends AbstractNoParameterMacro
{
    /**
     * Default constructor.
     */
    public TestReplaceMeMacro()
    {
        super("testReplaceMe");
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        // Construct a new macro block to replace the old macro block.
        MacroBlock oldMacroBlock = context.getCurrentMacroBlock();
        Optional<String> oldValue = Optional.ofNullable(oldMacroBlock.getParameter("oldParameter"));
        Map<String, String> newParameters = Map.of("param1", oldValue.orElse("default").replace("old", "new"));
        MacroBlock replacement = new MacroBlock("testReplacement", newParameters, content, context.isInline());

        // Replace the old macro block with the new macro block.
        oldMacroBlock.getParent().replaceChild(replacement, oldMacroBlock);

        return List.of();
    }
}

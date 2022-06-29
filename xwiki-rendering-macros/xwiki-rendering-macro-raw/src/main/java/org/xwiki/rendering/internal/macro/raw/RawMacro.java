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
package org.xwiki.rendering.internal.macro.raw;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.raw.RawMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Directly output content in a target syntax (this generates a {@link org.xwiki.rendering.block.RawBlock}).
 * This is useful when wanting to output some content directly in a target syntax (for example you're writing content
 * in a UIX wiki page and you wish to output LaTeX content since the corresponding UIXP is expecting LaTeX content).
 *
 * @version $Id$
 * @since 13.1RC1
 */
@Component
@Named("raw")
@Singleton
public class RawMacro extends AbstractMacro<RawMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Directly output content in a target syntax";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The content written in the target syntax";

    /**
     * Create and initialize the descriptor of the macro.
     */
    public RawMacro()
    {
        super("Raw", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION, true, Block.LIST_BLOCK_TYPE),
            RawMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_CONTENT));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(RawMacroParameters parameters, String content, MacroTransformationContext context)
    {
        RawBlock rawBlock = new RawBlock(content, parameters.getSyntax());
        return Collections.singletonList(rawBlock);
    }
}

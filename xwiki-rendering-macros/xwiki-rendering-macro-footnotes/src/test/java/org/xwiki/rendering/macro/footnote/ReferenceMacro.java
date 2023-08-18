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
package org.xwiki.rendering.macro.footnote;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Macro for testing referencing content outside the footnote macro.
 *
 * @version $Id$
 */
@Component
@Named("reference")
@Singleton
public class ReferenceMacro extends AbstractMacro<ReferenceMacroParameters>
{
    /**
     * Create and initialize the descriptor of the macro.
     */
    public ReferenceMacro()
    {
        super("Reference", "Reference Macro", ReferenceMacroParameters.class);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(ReferenceMacroParameters parameters, String content, MacroTransformationContext context)
    {
        List<HeaderBlock> headerBlocks =
            context.getXDOM().getBlocks(new ClassBlockMatcher(HeaderBlock.class), Block.Axes.DESCENDANT);

        List<Block> result = Collections.emptyList();

        for (HeaderBlock headerBlock : headerBlocks) {
            if (Objects.equals(headerBlock.getId(), parameters.getId())) {
                result = headerBlock.getChildren();
                break;
            }
        }

        return result;
    }
}

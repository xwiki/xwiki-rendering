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
package org.xwiki.rendering.internal.transformation.autotoc;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Automatically adds a TOC Macro at the top of the content.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Named("autotoc")
@Singleton
public class AutoTOCTransformation extends AbstractTransformation
{
    @Override
    public int getPriority()
    {
        // High priority transformation that must execute before the Macro Transformation since it's injecting a macro!
        return 50;
    }

    @Override
    public void transform(Block block, TransformationContext transformationContext) throws TransformationException
    {
        // Insert the TOC as the first block
        List<Block> childrenBlocks = block.getChildren();
        if (!childrenBlocks.isEmpty()) {
            SectionBlock sectionBlocks = block.getFirstBlock(
                new ClassBlockMatcher(SectionBlock.class), Block.Axes.DESCENDANT_OR_SELF);
            if (sectionBlocks != null) {
                Block blockToInsertAfter = childrenBlocks.get(0);
                MacroBlock tocBlock = new MacroBlock("toc", Collections.EMPTY_MAP, false);
                block.insertChildBefore(tocBlock, blockToInsertAfter);
            }
        }
    }
}

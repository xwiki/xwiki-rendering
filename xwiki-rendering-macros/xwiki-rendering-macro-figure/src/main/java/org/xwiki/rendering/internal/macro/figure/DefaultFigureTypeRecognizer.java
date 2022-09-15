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
package org.xwiki.rendering.internal.macro.figure;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.EmptyLinesBlock;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.FigureCaptionBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.macro.figure.FigureTypeRecognizer;

/**
 * Recognize a FigureBlock content and more specifically if it contains a single table or not. Should be executed on an
 * XDOM that has had the Macro Transformation executed on it (i.e. that contains MacroMarkerBlock blocks).
 *
 * @version $Id$
 * @since 10.2
 */
@Component
@Singleton
public class DefaultFigureTypeRecognizer implements FigureTypeRecognizer
{
    @Override
    public boolean isTable(FigureBlock figureBlock)
    {
        List<Block> blocks = getBlocksIgnoringMacroMarkerBlocks(figureBlock.getChildren());
        return blocks.size() == 1 && blocks.get(0) instanceof TableBlock;
    }

    private List<Block> getBlocksIgnoringMacroMarkerBlocks(List<Block> blocks)
    {
        List<Block> results = new ArrayList<>();

        // Traverse all blocks until they're not a MacroMarkerBlock
        for (Block block : blocks) {
            if (block instanceof MacroMarkerBlock) {
                MacroMarkerBlock macroMarkerBlock = (MacroMarkerBlock) block;
                if (!"figureCaption".equals(macroMarkerBlock.getId())) {
                    results.addAll(getBlocksIgnoringMacroMarkerBlocks(block.getChildren()));
                }
            } else if (block instanceof MetaDataBlock || block instanceof GroupBlock) {
                // Ignore MetaData/Group blocks since they're not structural
                results.addAll(getBlocksIgnoringMacroMarkerBlocks(block.getChildren()));
            } else if (!(block instanceof FigureCaptionBlock || block instanceof EmptyLinesBlock)) {
                // Ignore figure caption blocks since they're special and do not affect whether the content contains
                // a table or not.
                // Similarly to MetaData/Group blocks, empty lines are also ignored since they are not structural.
                results.add(block);
            }
        }

        return results;
    }
}

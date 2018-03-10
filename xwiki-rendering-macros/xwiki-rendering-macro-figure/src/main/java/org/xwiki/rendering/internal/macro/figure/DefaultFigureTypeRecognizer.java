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
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.marcro.figure.FigureTypeRecognizer;

/**
 * Recognize a FigureBlock content and more specifically if contains a single table or not. Should be executed on an
 * XDOM that has had the Macro Transformation executed on it (i.e. that contains MacroMarkerBlock blocks).
 *
 * @version $Id$
 * @since 10.2RC1
 */
@Component
@Singleton
public class DefaultFigureTypeRecognizer implements FigureTypeRecognizer
{
    @Override
    public boolean isTable(FigureBlock figureBlock)
    {
        List<Block> blocks = getBlocksIgnoringMacroMarkerBlocks(figureBlock.getChildren());
        return (blocks.size() == 1 && blocks.get(0) instanceof TableBlock);
    }

    private List<Block> getBlocksIgnoringMacroMarkerBlocks(List<Block> blocks)
    {
        List<Block> results = new ArrayList<>();

        // Traverse all blocks until they're not a MacroMarkerBlock
        for (Block block : blocks) {
            if (block instanceof MacroMarkerBlock) {
                MacroMarkerBlock macroMarkerBlock = (MacroMarkerBlock) block;
                if (macroMarkerBlock.getId().equals("figureCaption")) {
                    continue;
                } else {
                    results.addAll(getBlocksIgnoringMacroMarkerBlocks(block.getChildren()));
                }
            } else {
                results.add(block);
                continue;
            }
        }

        return results;
    }
}

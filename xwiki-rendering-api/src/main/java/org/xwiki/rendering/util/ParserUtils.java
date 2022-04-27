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
package org.xwiki.rendering.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.stability.Unstable;

/**
 * Methods for helping in parsing.
 *
 * @version $Id$
 * @since 1.7M1
 */
public class ParserUtils
{
    /**
     * Removes any top level paragraph since for example for the following use case we don't want an extra paragraph
     * block: <code>= hello {{velocity}}world{{/velocity}}</code>.
     *
     * @param blocks the blocks to check and convert
     */
    public void removeTopLevelParagraph(List<Block> blocks)
    {
        // Remove any top level paragraph so that the result of a macro can be used inline for example.
        // We only remove the paragraph if there's only one top level element and if it's a paragraph.
        if ((blocks.size() == 1) && blocks.get(0) instanceof ParagraphBlock) {
            Block paragraphBlock = blocks.remove(0);
            blocks.addAll(0, paragraphBlock.getChildren());

            // Remove parent block
            for (Block block : blocks) {
                block.setParent(null);
            }
        }
    }

    /**
     * Make its best to convert a passed block to its inline version. Sometime it's simply impossible to convert an
     * inline block, in which case it will just be returned as is.
     * 
     * @param rootBlock the block to convert
     * @param preserveXDOM true of the XDOM should be returned
     * @return the inline version of the passed block
     * @since 14.0RC1
     */
    @Unstable
    public Block convertToInline(Block rootBlock, boolean preserveXDOM)
    {
        List<Block> blocks;
        if (rootBlock instanceof XDOM || rootBlock instanceof CompositeBlock) {
            // We can't modify directly the block's children list
            blocks = new ArrayList<>(rootBlock.getChildren());
        } else {
            blocks = Arrays.asList(rootBlock);
        }

        convertToInline(blocks);

        // Preserve source metadata if any
        if (preserveXDOM && rootBlock instanceof XDOM) {
            rootBlock.setChildren(blocks);

            return rootBlock;
        }

        return blocks.size() == 1 ? blocks.get(0) : new CompositeBlock(blocks);
    }

    /**
     * Make its best to convert a passed blocks to their inline version. Sometime it's simply impossible to convert an
     * inline block, in which case it will just be returned as is.
     * 
     * @param blocks the blocks to convert
     * @since 14.0RC1
     */
    @Unstable
    // TODO: improve the implementation to really convert to inline everything that can be converted
    public void convertToInline(List<Block> blocks)
    {
        if (!blocks.isEmpty()) {
            // Clean top level paragraph
            removeTopLevelParagraph(blocks);

            // Synchronize macro
            if (!blocks.isEmpty()) {
                handleMacroInline(blocks);
            }
        }
    }

    /**
     * Make sure included macro is inline when script macro itself is inline.
     *
     * @param blocks the blocks to align
     */
    private void handleMacroInline(List<Block> blocks)
    {
        Block block = blocks.get(0);
        if (block instanceof MacroBlock) {
            MacroBlock macro = (MacroBlock) block;
            if (!macro.isInline()) {
                blocks.set(0, new MacroBlock(macro.getId(), macro.getParameters(), macro.getContent(), true));
            }
        }
    }
}

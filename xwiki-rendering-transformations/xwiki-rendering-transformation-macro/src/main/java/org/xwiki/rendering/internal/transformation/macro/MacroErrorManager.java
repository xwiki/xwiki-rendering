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

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.OrBlockMatcher;
import org.xwiki.rendering.util.ErrorBlockGenerator;

/**
 * Generates Blocks to signify that a Macro has failed to execute.
 *
 * @version $Id$
 * @since 4.3M2
 */
public class MacroErrorManager
{
    private ErrorBlockGenerator errorBlockGenerator;

    /**
     * @param errorBlockGenerator the error generator to use to generate the error blocks
     */
    public MacroErrorManager(ErrorBlockGenerator errorBlockGenerator)
    {
        this.errorBlockGenerator = errorBlockGenerator;
    }

    /**
     * Generates Blocks to signify that the passed Macro Block has failed to execute.
     *
     * @param macroToReplace the block for the macro that failed to execute and that we'll replace with Block
     *        showing to the user that macro has failed
     * @param message the message to display to the user in place of the macro result
     * @param description the long description of the error to display to the user in place of the macro result
     */
    public void generateError(MacroBlock macroToReplace, String message, String description)
    {
        List<Block> errorBlocks =
            this.errorBlockGenerator.generateErrorBlocks(message, description, macroToReplace.isInline());
        macroToReplace.getParent().replaceChild(wrapInMacroMarker(macroToReplace, errorBlocks), macroToReplace);
    }

    /**
     * Generates Blocks to signify that the passed Macro Block has failed to execute.
     *
     * @param macroToReplace the block for the macro that failed to execute and that we'll replace with Block
     *        showing to the user that macro has failed
     * @param message the message to display to the user in place of the macro result
     * @param throwable the exception for the failed macro execution to display to the user in place of the macro result
     */
    public void generateError(MacroBlock macroToReplace, String message, Throwable throwable)
    {
        List<Block> errorBlocks =
            this.errorBlockGenerator.generateErrorBlocks(message, throwable, macroToReplace.isInline());
        macroToReplace.getParent().replaceChild(wrapInMacroMarker(macroToReplace, errorBlocks), macroToReplace);
    }

    /**
     * @param xdom the XDOM on which to check if there's a macro error
     * @return true if the passed XDOM contains a macro error or false otherwise
     */
    public boolean containsError(XDOM xdom)
    {
        boolean foundError = false;
        List<Block> groupAndFormatBlocks = xdom.getBlocks(
            new OrBlockMatcher(
                new ClassBlockMatcher(GroupBlock.class),
                new ClassBlockMatcher(FormatBlock.class)),
            Block.Axes.DESCENDANT);
        for (Block block : groupAndFormatBlocks) {
            String classParameter = block.getParameters().get(ErrorBlockGenerator.CLASS_ATTRIBUTE_NAME);
            if (classParameter != null && classParameter.contains(ErrorBlockGenerator.CLASS_ATTRIBUTE_MESSAGE_VALUE)) {
                foundError = true;
                break;
            }
        }
        return foundError;
    }

    /**
     * Wrap the output of a macro block with a {@link org.xwiki.rendering.block.MacroMarkerBlock}.
     *
     * @param macroBlockToWrap the block that should be replaced
     * @param newBlocks list of blocks to wrap
     * @return the wrapper
     */
    private Block wrapInMacroMarker(MacroBlock macroBlockToWrap, List<Block> newBlocks)
    {
        return new MacroMarkerBlock(macroBlockToWrap.getId(), macroBlockToWrap.getParameters(),
            macroBlockToWrap.getContent(), newBlocks, macroBlockToWrap.isInline());
    }
}

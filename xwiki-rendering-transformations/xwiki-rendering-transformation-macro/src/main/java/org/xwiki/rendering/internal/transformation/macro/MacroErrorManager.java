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

import org.slf4j.Logger;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.XDOM;
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
     * @param xdom the XDOM on which to check if there's a macro error
     * @return true if the passed XDOM contains a macro error or false otherwise
     */
    public boolean containsError(XDOM xdom)
    {
        return this.errorBlockGenerator.containsError(xdom);
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

    /**
     * Generates Blocks to signify that the passed Macro Block has failed to execute.
     * 
     * @param macroToReplace
     * @param messageId an identifier associated to the message. It's generally used, among other things, to find a
     *            translation for the message and the description in implementation which supports it.
     * @param defaultMessage the default message following SLF4J's {@link Logger} syntax
     * @param defaultDescription the default description following SLF4J's {@link Logger} syntax
     * @param arguments a list arguments to insert in the message and the description and/or a {@link Throwable}
     * @since 14.0RC1
     */
    public void generateError(MacroBlock macroToReplace, String messageId, String defaultMessage,
        String defaultDescription, Object... arguments)
    {
        List<Block> errorBlocks = this.errorBlockGenerator.generateErrorBlocks(macroToReplace.isInline(), messageId,
            defaultMessage, defaultDescription, arguments);
        macroToReplace.getParent().replaceChild(wrapInMacroMarker(macroToReplace, errorBlocks), macroToReplace);
    }
}

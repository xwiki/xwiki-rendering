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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.OrBlockMatcher;
import org.xwiki.rendering.listener.Format;

/**
 * Generates Blocks to signify that a Macro has failed to execute.
 *
 * @version $Id$
 * @since 4.3M2
 */
public class MacroErrorManager
{
    /**
     * The formatting parameter to use to signify that the new Blocks represent a macro execution error.
     */
    private static final String CLASS_PARAMETER_NAME = "class";

    /**
     * The formatting parameter value to use to signify that the new Blocks represent a macro execution error.
     */
    private static final String CLASS_PARAMETER_VALUE = "xwikirenderingerror";

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
        List<Block> errorBlocks = new ArrayList<Block>();

        Map<String, String> errorBlockParams = Collections.singletonMap(CLASS_PARAMETER_NAME, CLASS_PARAMETER_VALUE);
        Map<String, String> errorDescriptionBlockParams =
                Collections.singletonMap(CLASS_PARAMETER_NAME, "xwikirenderingerrordescription hidden");

        Block descriptionBlock = new VerbatimBlock(description, macroToReplace.isInline());

        if (macroToReplace.isInline()) {
            errorBlocks.add(new FormatBlock(Arrays.<Block> asList(new WordBlock(message)), Format.NONE,
                    errorBlockParams));
            errorBlocks.add(new FormatBlock(Arrays.asList(descriptionBlock), Format.NONE, errorDescriptionBlockParams));
        } else {
            errorBlocks.add(new GroupBlock(Arrays.<Block> asList(new WordBlock(message)), errorBlockParams));
            errorBlocks.add(new GroupBlock(Arrays.asList(descriptionBlock), errorDescriptionBlockParams));
        }

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
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        generateError(macroToReplace, message, writer.getBuffer().toString());
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
            String classParameter = block.getParameters().get(CLASS_PARAMETER_NAME);
            if (classParameter != null && classParameter.contains(CLASS_PARAMETER_VALUE)) {
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

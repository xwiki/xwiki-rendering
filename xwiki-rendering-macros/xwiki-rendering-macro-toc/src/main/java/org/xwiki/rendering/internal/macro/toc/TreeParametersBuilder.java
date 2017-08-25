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
package org.xwiki.rendering.internal.macro.toc;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.macro.toc.TocMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Constructs the input parameters for {@link TocTreeBuilder}.
 *
 * @version $Id$
 * @since 9.6RC1
 */
public class TreeParametersBuilder
{
    /**
     * @param rootBlock the optional XDOM block from where to start generating the TOC from. If null then starts from
     *                  the current macro block
     * @param macroParameters the TOC macro parameters as passed by the user
     * @param context the Macro Context from which we extract the root block is not specified
     * @return a {@link TreeParameters} object containing the resolved parameters that will then be used to call
     *         {@link TocTreeBuilder}
     */
    public TreeParameters build(Block rootBlock, TocMacroParameters macroParameters, MacroTransformationContext context)
    {
        TreeParameters parameters = new TreeParameters();
        parameters.start = macroParameters.getStart();
        parameters.depth = macroParameters.getDepth();
        parameters.isNumbered = macroParameters.isNumbered();
        parameters.documentReference = macroParameters.getReference();

        Block resolvedRootBlock = rootBlock;

        // If no root block is passed then use the TOC macro location and the scope parameter to compute the root
        // block to use.
        if (resolvedRootBlock == null) {
            if (macroParameters.getScope() == TocMacroParameters.Scope.LOCAL) {
                resolvedRootBlock = context.getCurrentMacroBlock().getParent();
            } else {
                resolvedRootBlock = context.getXDOM();
            }
        }
        parameters.rootBlock = resolvedRootBlock;

        // For local scope, if no start parameter was specified, compute it so that the TOC starts from the next
        // heading level found in the next section in the XDOM (from the root block).
        if (macroParameters.getScope() == TocMacroParameters.Scope.LOCAL && !macroParameters.isCustomStart()) {
            SectionBlock rootSection = context.getCurrentMacroBlock().getFirstBlock(
                new ClassBlockMatcher(SectionBlock.class), Block.Axes.ANCESTOR);
            if (rootSection != null) {
                HeaderBlock header = rootSection.getHeaderBlock();
                if (header != null) {
                    parameters.start = header.getLevel().getAsInt() + 1;
                }
            }
        }

        return parameters;
    }
}

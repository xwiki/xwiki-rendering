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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.FigureCaptionBlock;
import org.xwiki.rendering.block.UnchangedContentBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Provides a caption inside the Figure Macro content. Needs to be used as the first or last block.
 *
 * @version $Id$
 * @since 10.2
 */
@Component
@Named("figureCaption")
@Singleton
public class FigureCaptionMacro extends AbstractNoParameterMacro
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Provide a figure caption when used inside the Figure macro.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "Caption content";

    @Inject
    private MacroContentParser contentParser;

    private ParserUtils parserUtils = new ParserUtils();

    /**
     * Create and initialize the descriptor of the macro.
     */
    public FigureCaptionMacro()
    {
        super("Figure Caption", DESCRIPTION,
            new DefaultContentDescriptor(CONTENT_DESCRIPTION, true, Block.LIST_BLOCK_TYPE));
        setDefaultCategory(DEFAULT_CATEGORY_DEVELOPMENT);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(Object unusedParameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        List<Block> result = Collections.emptyList();

        // If we're not inside a FigureBlock then don't do anything.
        Block parent = context.getCurrentMacroBlock().getParent().getParent();
        if (parent != null && parent instanceof FigureBlock) {
            XDOM xdom = this.contentParser.parse(content, context, false, false);
            List<Block> figureCaptionChildren = xdom.getChildren();
            this.parserUtils.removeTopLevelParagraph(figureCaptionChildren);
            result = Collections.singletonList(new FigureCaptionBlock(Collections.singletonList(
                new UnchangedContentBlock(figureCaptionChildren)
            )));
        }

        return result;
    }
}

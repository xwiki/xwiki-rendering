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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.figure.FigureMacroParameters;
import org.xwiki.rendering.macro.figure.FigureType;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import static org.xwiki.rendering.internal.macro.figure.FigureTypeRecognizerMacro.DATA_XWIKI_RENDERING_FIGURE_TYPE;

/**
 * Tag content as an illustration and with an optional caption.
 *
 * @version $Id$
 * @since 10.2
 */
@Component
@Named("figure")
@Singleton
public class FigureMacro extends AbstractMacro<FigureMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Tag content as an illustration and with an optional caption.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "Illustration(s) and caption";

    @Inject
    private MacroContentParser contentParser;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public FigureMacro()
    {
        super("Figure", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION, false, Block.LIST_BLOCK_TYPE),
            FigureMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_DEVELOPMENT));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(FigureMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        XDOM xdom = this.contentParser.parse(content, context, false, false);
        // Mark the macro content as being content that has not been transformed (so that it can be edited inline).
        List<Block> contentBlock = List.of(new MetaDataBlock(xdom.getChildren(), getNonGeneratedContentMetaData()));

        FigureBlock figureBlock = new FigureBlock(contentBlock);
        Block block;
        // If a type is explicitly defined, it is used directly. Otherwise, we try to infer it from the macro's content.
        if (FigureType.AUTOMATIC != parameters.getType()) {
            figureBlock.setParameter(DATA_XWIKI_RENDERING_FIGURE_TYPE, parameters.getType().getName());
            block = figureBlock;
        } else {
            block = new CompositeBlock(List.of(
                new MacroBlock("figureTypeRecognizer", Map.of(), false),
                figureBlock)
            );
        }
        return List.of(block);
    }
}

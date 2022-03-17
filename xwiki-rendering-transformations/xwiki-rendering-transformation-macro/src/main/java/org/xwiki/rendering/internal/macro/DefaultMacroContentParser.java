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
package org.xwiki.rendering.internal.macro;

import java.io.StringReader;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MetadataBlockMatcher;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Default implementation for {@link org.xwiki.rendering.macro.MacroContentParser}.
 *
 * @version $Id$
 * @since 3.0M1
 */
@Component
@Singleton
public class DefaultMacroContentParser implements MacroContentParser
{
    /**
     * Used to look up the syntax parser to use for parsing the content.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Used to update rendering context during content transformation.
     */
    @Inject
    private RenderingContext renderingContext;

    /**
     * Utility to remove the top level paragraph.
     */
    private ParserUtils parserUtils = new ParserUtils();

    @Override
    public XDOM parse(String content, MacroTransformationContext macroContext, boolean transform, boolean inline)
        throws MacroExecutionException
    {
        return parse(content, macroContext, transform, null, inline);
    }

    @Override
    public XDOM parse(String content, MacroTransformationContext macroContext, boolean transform, MetaData metadata,
        boolean inline) throws MacroExecutionException
    {
        // If the content is empty return an empty list
        if (StringUtils.isEmpty(content)) {
            return new XDOM(Collections.<Block>emptyList(), metadata != null ? metadata : MetaData.EMPTY);
        }

        Syntax syntax = getCurrentSyntax(macroContext);

        // If there's no syntax specified in the Transformation throw an error
        if (syntax == null) {
            throw new MacroExecutionException("Invalid Transformation: missing Syntax");
        }

        return createXDOM(content, macroContext, transform, metadata, inline, syntax);
    }

    /**
     * creates XDOM.
     */
    private XDOM createXDOM(String content, MacroTransformationContext macroContext, boolean transform,
        MetaData metadata, boolean inline, Syntax syntax) throws MacroExecutionException
    {
        try {
            XDOM result;

            if (macroContext.getXDOM() != null && macroContext.getXDOM().getIdGenerator() != null) {
                result = getSyntaxParser(syntax).parse(new StringReader(content),
                    macroContext.getXDOM().getIdGenerator());
            } else {
                result = getSyntaxParser(syntax).parse(new StringReader(content));
            }

            // Inject metadata
            if (metadata != null) {
                result.getMetaData().addMetaData(metadata);
            }

            // Try to convert the content to inline content
            // TODO: ideally we would use a real inline parser
            if (inline) {
                result = convertToInline(result);
            }

            // Execute the content
            if (transform && macroContext.getTransformation() != null) {
                TransformationContext txContext = new TransformationContext(result, syntax);
                txContext.setId(macroContext.getId());
                performTransformation((MutableRenderingContext) this.renderingContext, macroContext.getTransformation(),
                    txContext, result);
            }

            return result;
        } catch (Exception e) {
            throw new MacroExecutionException("Failed to parse content [" + content + "]", e);
        }
    }

    /**
     * Calls transformInContext on renderingContext.
     */
    private void performTransformation(MutableRenderingContext renderingContext, Transformation transformation,
        TransformationContext context, Block block) throws MacroExecutionException
    {
        try {
            renderingContext.transformInContext(transformation, context, block);
        } catch (Exception e) {
            throw new MacroExecutionException("Failed to perform transformation", e);
        }
    }

    /**
     * @param xdom the {@link XDOM} to convert
     * @return an inline version of the passed {@link XDOM}
     */
    private XDOM convertToInline(XDOM xdom)
    {
        if (!xdom.getChildren().isEmpty()) {
            return (XDOM) this.parserUtils.convertToInline(xdom, true);
        }

        return xdom;
    }

    /**
     * Get the parser for the current syntax.
     *
     * @param syntax the current syntax of the title content
     * @return the parser for the current syntax
     * @throws org.xwiki.rendering.macro.MacroExecutionException Failed to find source parser.
     */
    private Parser getSyntaxParser(Syntax syntax) throws MacroExecutionException
    {
        try {
            return this.componentManager.getInstance(Parser.class, syntax.toIdString());
        } catch (ComponentLookupException e) {
            throw new MacroExecutionException("Failed to find source parser for syntax [" + syntax + "]", e);
        }
    }

    @Override
    public Syntax getCurrentSyntax(MacroTransformationContext context)
    {
        Syntax currentSyntax = context.getSyntax();

        MacroBlock currentMacroBlock = context.getCurrentMacroBlock();

        if (currentMacroBlock != null) {
            MetaDataBlock metaDataBlock =
                currentMacroBlock.getFirstBlock(new MetadataBlockMatcher(MetaData.SYNTAX), Axes.ANCESTOR_OR_SELF);

            if (metaDataBlock != null) {
                currentSyntax = (Syntax) metaDataBlock.getMetaData().getMetaData(MetaData.SYNTAX);
            }
        }

        return currentSyntax;
    }
}

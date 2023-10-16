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
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.MacroPreparationException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.util.IdGenerator;
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

    @Inject
    @Named("macro")
    private Transformation transformation;

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
        return parse(content, null, macroContext, transform, metadata, inline);
    }

    @Override
    public XDOM parse(String content, Syntax syntax, MacroTransformationContext macroContext, boolean transform,
        MetaData metadata, boolean inline) throws MacroExecutionException
    {
        // If the content is empty return an empty list
        if (StringUtils.isEmpty(content)) {
            return new XDOM(Collections.<Block>emptyList(), metadata != null ? metadata : MetaData.EMPTY);
        }

        // Resolve the syntax
        Syntax finalSyntax = syntax;
        if (finalSyntax == null) {
            finalSyntax = getCurrentSyntax(macroContext);
        }

        // If there's no syntax specified in the Transformation throw an error
        if (finalSyntax == null) {
            throw new MacroExecutionException("Invalid Transformation: missing Syntax");
        }

        return createXDOM(content, macroContext, transform, metadata, inline, finalSyntax);
    }

    private XDOM getPreparedXDOM(String content, MacroTransformationContext macroContext, Syntax syntax)
    {
        if (macroContext.getCurrentMacroBlock() != null
            // Make sure the passed content is actually the current block content as otherwise it's a different use case
            && macroContext.getCurrentMacroBlock().getContent() == content) {
            XDOM preparedXDOM = (XDOM) macroContext.getCurrentMacroBlock().getAttribute(ATTRIBUTE_PREPARE_CONTENT_XDOM);

            if (preparedXDOM != null) {
                // Make sure the resolved syntax is the one that was used to prepare the content
                Syntax preparedSyntax = (Syntax) preparedXDOM.getMetaData().getMetaData(MetaData.SYNTAX);
                if (syntax.equals(preparedSyntax)) {
                    return preparedXDOM;
                }
            }
        }

        return null;
    }

    private XDOM createXDOM(String content, MacroTransformationContext macroContext, boolean transform,
        MetaData metadata, boolean inline, Syntax syntax) throws MacroExecutionException
    {
        XDOM result = getPreparedXDOM(content, macroContext, syntax);

        // Parse the content if not already prepared
        if (result == null) {
            IdGenerator idGenerator = null;
            if (macroContext.getXDOM() != null) {
                idGenerator = macroContext.getXDOM().getIdGenerator();
            } else {
                idGenerator = null;
            }
            result = parse(content, syntax, inline, idGenerator);
        } else {
            // Clone the prepared content to be sure to not modify the potentially cached version
            result = result.clone();
        }

        // Inject metadata
        if (metadata != null) {
            result.getMetaData().addMetaData(metadata);
        }

        // Transform the content
        if (transform && macroContext.getTransformation() != null) {
            try {
                TransformationContext wrappingContext = macroContext.getTransformationContext();
                boolean isRestricted = wrappingContext != null && wrappingContext.isRestricted();
                TransformationContext txContext = new TransformationContext(result, syntax, isRestricted);
                txContext.setId(macroContext.getId());
                performTransformation((MutableRenderingContext) this.renderingContext, macroContext.getTransformation(),
                    txContext, result);
            } catch (Exception e) {
                throw new MacroExecutionException("Failed to tranform the content [" + content + "]", e);
            }
        }

        return result;
    }

    private XDOM parse(String content, Syntax syntax, boolean inline, IdGenerator idGenerator)
        throws MacroExecutionException
    {
        try {
            XDOM result;

            Parser parser = getSyntaxParser(syntax);
            if (idGenerator != null) {
                result = parser.parse(new StringReader(content), idGenerator);
            } else {
                result = parser.parse(new StringReader(content));
            }

            // Try to convert the content to inline content
            // TODO: ideally we would use a real inline parser
            if (inline) {
                result = convertToInline(result);
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

    @Override
    public void prepareContentWiki(MacroBlock macroBlock, Syntax syntax) throws MacroPreparationException
    {
        if (macroBlock.getContent() != null) {
            // Find the syntax
            Syntax contentSyntax = syntax;
            if (contentSyntax == null) {
                Optional<Syntax> syntaxMetadata = macroBlock.getSyntaxMetadata();
                contentSyntax = syntaxMetadata
                    .orElseThrow(() -> new MacroPreparationException("No syntax provided to parse the content"));
            }

            // Find the id generator
            Optional<IdGenerator> idGenerator = macroBlock.get(
                b -> b instanceof XDOM ? Optional.ofNullable(((XDOM) b).getIdGenerator()) : Optional.empty(),
                Axes.DESCENDANT_OR_SELF);

            try {
                // Parse the macro wiki content
                XDOM result = parse(macroBlock.getContent(), contentSyntax, macroBlock.isInline(),
                    idGenerator.isPresent() ? idGenerator.get() : null);

                // Prepare the macro wiki content
                this.transformation.prepare(result);

                // Store the prepared content as attribute
                macroBlock.setAttribute(ATTRIBUTE_PREPARE_CONTENT_XDOM, result);
            } catch (MacroExecutionException e) {
                throw new MacroPreparationException("Failed to parse the content", e);
            }
        }
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
            return currentMacroBlock.getSyntaxMetadata().orElse(currentSyntax);
        }

        return currentSyntax;
    }
}

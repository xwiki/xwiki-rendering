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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
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
     * Utility to remove the top level paragraph.
     */
    private ParserUtils parserUtils = new ParserUtils();

    @Override
    public XDOM parse(String content, MacroTransformationContext macroContext, boolean transform, boolean inline)
        throws MacroExecutionException
    {
        // If the content is empty return an empty list
        if (StringUtils.isEmpty(content)) {
            return new XDOM(Collections.<Block> emptyList());
        }

        Syntax syntax = getCurrentSyntax(macroContext);

        // If there's no syntax specified in the Transformation throw an error
        if (syntax == null) {
            throw new MacroExecutionException("Invalid Transformation: missing Syntax");
        }

        try {
            XDOM result = getSyntaxParser(syntax).parse(new StringReader(content));

            if (transform && macroContext.getTransformation() != null) {
                TransformationContext txContext = new TransformationContext(result, syntax);
                txContext.setId(macroContext.getId());
                try {
                    macroContext.getTransformation().transform(result, txContext);
                } catch (Exception e) {
                    throw new MacroExecutionException("Failed to perform transformation", e);
                }
            }

            if (inline) {
                result = convertToInline(result);
            }

            return result;
        } catch (Exception e) {
            throw new MacroExecutionException("Failed to parse content [" + content + "]", e);
        }
    }

    /**
     * @param xdom the {@link XDOM} to convert
     * @return an inline version of the passed {@link XDOM}
     */
    private XDOM convertToInline(XDOM xdom)
    {
        List<Block> blocks = new ArrayList<Block>(xdom.getChildren());

        // TODO: use inline parser instead
        if (!blocks.isEmpty()) {
            this.parserUtils.removeTopLevelParagraph(blocks);

            // Make sure included macro is inline when script macro itself is inline
            Block block = blocks.get(0);
            if (block instanceof MacroBlock) {
                MacroBlock macro = (MacroBlock) block;
                if (!macro.isInline()) {
                    blocks.set(0, new MacroBlock(macro.getId(), macro.getParameters(), macro.getContent(), true));
                }
            }

            xdom.setChildren(blocks);
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

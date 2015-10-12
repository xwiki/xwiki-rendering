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
package org.xwiki.rendering.internal.macro.quote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.QuotationBlock;
import org.xwiki.rendering.block.QuotationLineBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.quote.QuoteMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Displays text with special quote formatting.
 *
 * @version $Id$
 * @since 7.3M1
 */
@Component
@Named("quote")
@Singleton
public class QuoteMacro extends AbstractMacro<QuoteMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Displays text with special quote formatting.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "Content to quote";

    /**
     * Used to parse the macro content.
     */
    @Inject
    private MacroContentParser macroContentParser;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public QuoteMacro()
    {
        super("Quote", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION), QuoteMacroParameters.class);
        setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(QuoteMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        List<Block> quoteBlocks = new ArrayList<>();
        for (String line : content.split("\\r?\\n")) {
            List<Block> lineContentBlocks;
            if (parameters.getSyntax() == null) {
                lineContentBlocks = Collections.<Block>singletonList(new VerbatimBlock(line, true));
            } else {
                lineContentBlocks = this.macroContentParser.parse(line, context, true, true).getChildren();
            }
            quoteBlocks.add(new QuotationLineBlock(lineContentBlocks));
        }
        return Collections.<Block>singletonList(new QuotationBlock(quoteBlocks));
    }
}

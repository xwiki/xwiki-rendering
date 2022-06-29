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
package org.xwiki.rendering.internal.macro.content;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.content.ContentMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Allows writing content in any syntax (wiki syntax, HTML, etc). This allows to have to main content in a given syntax
 * and mix content written in another syntax in it.
 *
 * @version $Id$
 * @since 4.2M3
 */
@Component
@Named("content")
@Singleton
public class ContentMacro extends AbstractMacro<ContentMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Allows writing content in any wiki markup";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The content to execute";

    /**
     * Used to find the Parser corresponding to the user-specified syntax for the Macro.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public ContentMacro()
    {
        super("Content", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION, true,
            Block.LIST_BLOCK_TYPE), ContentMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_CONTENT));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(ContentMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        try {
            List<Block> blocks = getSyntaxParser(parameters.getSyntax()).parse(new StringReader(content)).getChildren();
            MetaDataBlock metaDataBlock = new MetaDataBlock(blocks, MetaData.SYNTAX,
                parameters.getSyntax().toIdString());

            metaDataBlock.getMetaData().addMetaData(this.getNonGeneratedContentMetaData());
            return Collections.singletonList(metaDataBlock);
        } catch (ParseException e) {
            throw new MacroExecutionException(
                String.format("Failed to parse macro content in syntax [%s]", parameters.getSyntax()), e);
        }
    }

    /**
     * Get the parser for the passed Syntax.
     *
     * @param syntax the Syntax for which to find the Parser
     * @return the matching Parser that can be used to parse content in the passed Syntax
     * @throws MacroExecutionException if there's no Parser in the system for the passed Syntax
     */
    protected Parser getSyntaxParser(Syntax syntax) throws MacroExecutionException
    {
        ComponentManager componentManager = this.componentManagerProvider.get();
        if (componentManager.hasComponent(Parser.class, syntax.toIdString())) {
            try {
                return componentManager.getInstance(Parser.class, syntax.toIdString());
            } catch (ComponentLookupException e) {
                throw new MacroExecutionException(
                    String.format("Failed to lookup Parser for syntax [%s]", syntax.toIdString()), e);
            }
        } else {
            throw new MacroExecutionException(String.format("Cannot find Parser for syntax [%s]", syntax.toIdString()));
        }
    }
}

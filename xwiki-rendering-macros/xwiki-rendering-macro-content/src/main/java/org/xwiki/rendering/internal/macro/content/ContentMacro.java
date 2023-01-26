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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.content.ContentMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.source.MacroContentWikiSource;
import org.xwiki.rendering.macro.source.MacroContentWikiSourceFactory;
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

    @Inject
    private MacroContentWikiSourceFactory contentFactory;

    @Inject
    private MacroContentParser macroContentParser;

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
        super("Content", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION, false, Block.LIST_BLOCK_TYPE),
            ContentMacroParameters.class);

        setDefaultCategories(Set.of(DEFAULT_CATEGORY_CONTENT));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(ContentMacroParameters parameters, String macroContent,
        MacroTransformationContext context) throws MacroExecutionException
    {
        Syntax syntax = parameters.getSyntax();
        String content = macroContent;
        MetaData metadata = new MetaData();
        if (syntax != null) {
            // Remember the custom syntax as it can be needed for sub macros
            metadata.addMetaData(MetaData.SYNTAX, syntax);
        }
        if (parameters.getSource() != null) {
            MacroContentWikiSource wikiSource = this.contentFactory.getContent(parameters.getSource(), context);
            if (parameters.getSyntax() == null) {
                // Use the source syntax if no explicit one is given as parameter
                syntax = wikiSource.getSyntax();
            }
            content = wikiSource.getContent();
        } else {
            // Make the content editable inline only if it's not coming from a source
            metadata.addMetaData(this.getNonGeneratedContentMetaData());
        }

        // Parse the content
        XDOM xdom = this.macroContentParser.parse(content, syntax, context, false, metadata, context.isInline());

        // Remember the metadata of the XDOM
        return List.of(new MetaDataBlock(xdom.getChildren(), xdom.getMetaData()));
    }
}

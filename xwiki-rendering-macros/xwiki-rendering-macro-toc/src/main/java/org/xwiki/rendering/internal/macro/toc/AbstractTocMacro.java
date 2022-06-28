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

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.toc.TocMacroParameters;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.rendering.wiki.WikiModelException;

/**
 * Base class for macros that generate a Table Of Contents based on the document sections.
 *
 * @param <T> the type that describes the macro parameters
 * @version $Id$
 * @since 11.5RC1
 */
public abstract class AbstractTocMacro<T extends TocMacroParameters> extends AbstractMacro<T>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Generates a Table Of Contents.";

    private TocTreeBuilder tocTreeBuilder;

    /**
     * A parser that knows how to parse plain text; this is used to transform link labels into plain text.
     */
    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    /**
     * Generate link label.
     */
    @Inject
    private LinkLabelGenerator linkLabelGenerator;

    @Inject
    private Provider<WikiModel> wikiModelProvider;

    /**
     * Create and initialize the descriptor of the macro.
     */
    protected AbstractTocMacro(Class<T> parametersBeanClass)
    {
        super("Table Of Contents", DESCRIPTION, parametersBeanClass);

        // Make sure this macro is executed as one of the last macros to be executed since
        // other macros can generate headers which need to be taken into account by the TOC
        // macro.
        setPriority(2000);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_NAVIGATION));
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();

        this.tocTreeBuilder = new TocTreeBuilder(new TocBlockFilter(this.plainTextParser, this.linkLabelGenerator));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(T parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        Block rootBlock = null;
        WikiModel wikiModel = this.wikiModelProvider.get();
        if (parameters.getReference() != null) {
            if (wikiModel != null) {
                // Remote TOC always has a PAGE scope since a LOCAL scope would have no meaning
                parameters.setScope(TocMacroParameters.Scope.PAGE);
                // Get the referenced source's XDOM
                rootBlock = getXDOM(new DocumentResourceReference(parameters.getReference()), wikiModel);
            } else {
                throw new MacroExecutionException(
                    "The \"reference\" parameter can only be used when a WikiModel implementation is available");
            }
        }

        TreeParametersBuilder builder = new TreeParametersBuilder();
        TreeParameters treeParameters = builder.build(rootBlock, parameters, context);
        return this.tocTreeBuilder.build(treeParameters);
    }

    private XDOM getXDOM(ResourceReference reference, WikiModel wikiModel) throws MacroExecutionException
    {
        try {
            return wikiModel.getXDOM(reference);
        } catch (WikiModelException e) {
            throw new MacroExecutionException(String.format("Failed to get XDOM for [%s]", reference), e);
        }
    }
}

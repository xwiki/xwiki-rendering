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
package org.xwiki.rendering.internal.macro.html;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.html.HTMLMacroParameters;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.macro.RawBlockFilter;
import org.xwiki.rendering.transformation.macro.RawBlockFilterParameters;

/**
 * Allows inserting HTML and XHTML in wiki pages. This macro also accepts wiki syntax alongside (X)HTML elements (it's
 * also possible to disable this feature using a macro parameter). When wiki syntax is used inside XML elements, the
 * leading and trailing spaces and newlines are stripped.
 *
 * @version $Id$
 * @since 1.6M1
 */
@Component
@Named("html")
@Singleton
public class HTMLMacro extends AbstractMacro<HTMLMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Inserts HTML or XHTML code into the page.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The HTML content to insert in the page.";

    /**
     * Used to search for inner macros.
     */
    private static final ClassBlockMatcher MACROBLOCKMATCHER = new ClassBlockMatcher(MacroBlock.class);

    /**
     * Default Factory to create special HTML renderer for the HTML Macro. It is used as fallback in
     * {@link #getRendererFactory(Syntax)}.
     */
    @Inject
    @Named("htmlmacro+html/5.0")
    private PrintRendererFactory defaultHTMLRendererFactory;

    /**
     * The parser used to parse macro content.
     */
    @Inject
    private MacroContentParser contentParser;

    /**
     * Use to update the rendering context during transformation of the content.
     */
    @Inject
    private RenderingContext renderingContext;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public HTMLMacro()
    {
        super("HTML", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION), HTMLMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_DEVELOPMENT));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(HTMLMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        List<Block> blocks;

        if (!StringUtils.isEmpty(content)) {
            Syntax targetSyntax = getHTMLTargetSyntax();

            String normalizedContent = content;

            // If the user has mentioned that there's wiki syntax in the macro then we parse the content using
            // a wiki syntax parser and render it back using a special renderer to print the XDOM blocks into
            // a text representing the resulting XHTML content.
            if (parameters.getWiki()) {
                normalizedContent = renderWikiSyntax(normalizedContent, context.getTransformation(), context);
            }

            RawBlock contentBlock = new RawBlock(normalizedContent, targetSyntax);

            try {
                RawBlockFilterParameters filterParameters = new RawBlockFilterParameters();
                filterParameters.setClean(parameters.getClean());
                filterParameters.setRestricted(parameters.getRestricted());
                filterParameters.setMacroTransformationContext(context);
                for (RawBlockFilter filter
                    : this.componentManager.<RawBlockFilter>getInstanceList(RawBlockFilter.class)) {
                    contentBlock = filter.filter(contentBlock, filterParameters);
                }
            } catch (ComponentLookupException e) {
                throw new MacroExecutionException("Couldn't initialize the HTML filtering.", e);
            }

            blocks = List.of(contentBlock);
        } else {
            blocks = Collections.emptyList();
        }

        return blocks;
    }

    /**
     * Parse the passed context using a wiki syntax parser and render the result as an XHTML string.
     *
     * @param content the content to parse
     * @param transformation the macro transformation to execute macros when wiki is set to true
     * @param context the context of the macros transformation process
     * @return the output XHTML as a string containing the XWiki Syntax resolved as XHTML
     * @throws MacroExecutionException in case there's a parsing problem
     */
    private String renderWikiSyntax(String content, Transformation transformation, MacroTransformationContext context)
        throws MacroExecutionException
    {
        String xhtml;

        try {
            // Parse the wiki syntax
            XDOM xdom = this.contentParser.parse(content, context, false, false);

            // Force clean=false for sub HTML macro:
            // - at this point we don't know the context of the macro, it can be some <div> directly followed by the
            // html macro, it this case the macro will be parsed as inline block
            // - by forcing clean=false, we also make the html macro merge the whole html before cleaning so the cleaner
            // have the chole context and can clean better
            List<MacroBlock> macros = xdom.getBlocks(MACROBLOCKMATCHER, Axes.DESCENDANT);
            for (MacroBlock macro : macros) {
                if ("html".equals(macro.getId())) {
                    macro.setParameter("clean", "false");
                }
            }

            MacroBlock htmlMacroBlock = context.getCurrentMacroBlock();

            MacroMarkerBlock htmlMacroMarker =
                new MacroMarkerBlock(htmlMacroBlock.getId(), htmlMacroBlock.getParameters(),
                    htmlMacroBlock.getContent(), xdom.getChildren(), htmlMacroBlock.isInline());

            // Make sure the context XDOM contains the html macro content
            htmlMacroBlock.getParent().replaceChild(htmlMacroMarker, htmlMacroBlock);

            try {
                // Execute the Macro transformation
                ((MutableRenderingContext) this.renderingContext).transformInContext(transformation,
                    context.getTransformationContext(), htmlMacroMarker);
            } finally {
                // Restore context XDOM to its previous state
                htmlMacroMarker.getParent().replaceChild(htmlMacroBlock, htmlMacroMarker);
            }

            // Render the whole parsed content as a XHTML string
            WikiPrinter printer = new DefaultWikiPrinter();
            PrintRenderer renderer =
                this.getRendererFactory(this.renderingContext.getTargetSyntax()).createRenderer(printer);
            for (Block block : htmlMacroMarker.getChildren()) {
                block.traverse(renderer);
            }

            xhtml = printer.toString();
        } catch (Exception e) {
            throw new MacroExecutionException("Failed to parse content [" + content + "].", e);
        }

        return xhtml;
    }

    /**
     * Retrieve the renderer factory based on the given target syntax. In practice it's always a
     * {@link HTMLMacroXHTMLRendererFactory} which is returned but the hint is used to build the right renderer in the
     * factory.
     *
     * @param targetSyntax the syntax for which we want a {@link PrintRenderer}.
     * @return a {@link HTMLMacroXHTMLRendererFactory} with the hint to build the right {@link PrintRenderer}. It
     *     fallbacks on {@link #defaultHTMLRendererFactory} in case of ComponentLookupException.
     * @since 11.4RC1
     */
    private PrintRendererFactory getRendererFactory(Syntax targetSyntax)
    {
        PrintRendererFactory result = this.defaultHTMLRendererFactory;

        if (targetSyntax != null) {
            String hint = HTMLMacroXHTMLRendererFactory.PREFIX_SYNTAX + targetSyntax.toIdString();
            try {
                result = this.componentManagerProvider.get().getInstance(PrintRendererFactory.class, hint);
            } catch (ComponentLookupException ignored) {
                // Unsupported syntax - keep default.
            }
        }

        return result;
    }

    /**
     * @return The target syntax if it is a supported HTML syntax or {@link Syntax#HTML_5_0} otherwise.
     * @since 14.1RC1
     */
    private Syntax getHTMLTargetSyntax()
    {
        Syntax targetSyntax = this.renderingContext.getTargetSyntax();
        if (getRendererFactory(targetSyntax) == this.defaultHTMLRendererFactory) {
            // If the renderer is the default renderer, it is either indeed HTML 5.0 or an unsupported syntax - in
            // both cases returning HTML 5.0 is the right consequence.
            return Syntax.HTML_5_0;
        }
        return targetSyntax;
    }
}

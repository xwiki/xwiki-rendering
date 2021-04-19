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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

import static org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener.createMetaData;
import static org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener.isMetaDataElement;

/**
 * This class aims at handling specific macro metadata.
 *
 * @version $Id$
 * @since 10.10RC1
 */
public class XWikiMacroHandler implements XWikiWikiModelHandler
{
    private static final String WIKI_CONTENT_TYPE = ReflectionUtils.serializeType(Block.LIST_BLOCK_TYPE);

    private ComponentManager componentManager;

    private XHTMLParser parser;

    @Inject
    private Logger logger;

    /**
     * Default constructor for XWikiMacroHandler.
     * @param componentManager the component manager to retrieve the renderers.
     * @param parser the current parser.
     */
    public XWikiMacroHandler(ComponentManager componentManager, XHTMLParser parser)
    {
        this.componentManager = componentManager;
        this.parser = parser;
    }

    private String getSyntax(TagContext previousNodes, String macroType)
    {
        // if the type is not wiki content type, then we should parse the content as plain text.
        if (!macroType.equals(WIKI_CONTENT_TYPE)) {
            return Syntax.PLAIN_1_0.toIdString();
        } else if (previousNodes.getTagStack().getStackParameter(CURRENT_SYNTAX) != null) {
            return (String) previousNodes.getTagStack().popStackParameter(CURRENT_SYNTAX);

        // if the current syntax is not retrieved from the context, we get it from the RenderingContext
        // target syntax. Now if this one is not set, we fallback on the parser own syntax.
        } else {
            Syntax syntax = null;
            try {
                RenderingContext renderingContext = this.componentManager.getInstance(RenderingContext.class);
                syntax = renderingContext.getTargetSyntax();
            } catch (ComponentLookupException e) {
                this.logger.error("Error while retrieving the rendering context", e);
            }
            if (syntax == null) {
                syntax = this.parser.getSyntax();
            }
            return syntax.toIdString();
        }
    }

    private XWikiGeneratorListener createMacroListener(TagContext context, String currentSyntaxParameter)
        throws ComponentLookupException
    {
        PrintRenderer renderer = this.componentManager.getInstance(PrintRenderer.class,
            currentSyntaxParameter);
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        renderer.setPrinter(printer);
        Listener listener;

        if (context.getTagStack().isInsideBlockElement()) {
            listener = new InlineFilterListener();
            ((InlineFilterListener) listener).setWrappedListener(renderer);
        } else {
            listener = renderer;
        }
        return this.parser.createXWikiGeneratorListener(listener,
            null);
    }

    /**
     * Handle the begin of a container element (div or span) which could contain a syntax metadata and/or a non 
     * generated content metadata.
     * @param context the current tag context.
     * @return true if a non generated content metadata has been found.
     */
    public boolean handleBegin(TagContext context)
    {
        WikiParameters params = context.getParams();
        MacroInfo macroInfo = (MacroInfo) context.getTagStack().getStackParameter(MACRO_INFO);

        boolean withNonGeneratedContent = false;
        if (isMetaDataElement(params)) {
            MetaData metaData = createMetaData(params);

            if (metaData.contains(MetaData.SYNTAX)) {
                String currentSyntax = (String) metaData.getMetaData(MetaData.SYNTAX);
                context.getTagStack().pushStackParameter(CURRENT_SYNTAX, currentSyntax);
            }

            if (metaData.contains(MetaData.NON_GENERATED_CONTENT)) {
                String currentSyntaxParameter =
                    this.getSyntax(context, (String) metaData.getMetaData(MetaData.NON_GENERATED_CONTENT));
                try {
                    String parameterName = (String) metaData.getMetaData(MetaData.PARAMETER_NAME);
                    withNonGeneratedContent = true;

                    // we check macroInfo to avoid creating a supplementary scanner in case they are multiple content
                    // div.
                    if (parameterName != null && macroInfo.getParameterScannerContext(parameterName) == null) {
                        // It is a non-generated div for a specific parameter and we did not already
                        // created a scanner context for it: we create the scanner and push it in the context.
                        context.getTagStack().pushStackParameter(PARAMETER_CONTENT_NAME, parameterName);
                        context.getTagStack().pushScannerContext(
                            new WikiScannerContext(createMacroListener(context, currentSyntaxParameter)));
                        context.getTagStack().getScannerContext().beginDocument();
                        macroInfo.setParameterScannerContext(parameterName, context.getScannerContext());
                    } else if (parameterName == null && macroInfo.getContentScannerContext() == null) {
                        // It is a non-generated content div and we did not already
                        // created a scanner context for it: we create the scanner and push it in the context.
                        context.getTagStack().pushScannerContext(
                            new WikiScannerContext(createMacroListener(context, currentSyntaxParameter)));
                        macroInfo.setContentScannerContext(context.getTagStack().getScannerContext());
                        context.getTagStack().resetEmptyLinesCount();
                        context.getTagStack().getScannerContext().beginDocument();
                    }
                } catch (ComponentLookupException e) {
                    this.logger.error("Error while getting the appropriate renderer for syntax [{}]",
                        currentSyntaxParameter, e);
                }
            }
        }

        context.getTagStack().pushStackParameter(NON_GENERATED_CONTENT_STACK, withNonGeneratedContent);
        return withNonGeneratedContent;
    }

    private String getRenderedContentFromMacro(TagContext context)
    {
        context.getTagStack().getScannerContext().endDocument();

        XWikiGeneratorListener xWikiGeneratorListener =
            (XWikiGeneratorListener) context.getTagStack().popScannerContext().getfListener();

        PrintRenderer renderer;
        if (context.getTagStack().isInsideBlockElement()
            && xWikiGeneratorListener.getListener() instanceof WrappingListener) {
            WrappingListener wrappingListener = (WrappingListener) xWikiGeneratorListener.getListener();
            renderer = (PrintRenderer) wrappingListener.getWrappedListener();
        } else {
            renderer = (PrintRenderer) xWikiGeneratorListener.getListener();
        }
        return renderer.getPrinter().toString();
    }

    /**
     * Handle the end of a container (div or span) which can contain a non generated content metadata.
     * @param context the context of the current tag.
     * @return true if a non generated content tag has been detected.
     */
    public boolean handleEnd(TagContext context)
    {
        boolean nonGeneratedContent = (boolean) context.getTagStack().popStackParameter(NON_GENERATED_CONTENT_STACK);
        MacroInfo macroInfo = (MacroInfo) context.getTagStack().getStackParameter(MACRO_INFO);

        if (nonGeneratedContent && macroInfo != null) {
            String parameterName = (String) context.getTagStack().getStackParameter(PARAMETER_CONTENT_NAME);
            // Case 1: there is a parameterName and a scanner context for this parameter in the macro
            // so we need to handle this as a parameter content.
            if (parameterName != null
                && macroInfo.getParameterScannerContext(parameterName) != null) {
                context.getTagStack().popStackParameter(PARAMETER_CONTENT_NAME);
                WikiParameters parameters = macroInfo.getParameters();

                // WikiParameters are immutable
                macroInfo.setParameters(parameters.setParameter(parameterName, getRenderedContentFromMacro(context)));
                macroInfo.setParameterScannerContext(parameterName, null);
            // Case 2: there is a content scanner context and no parameterName: this needs to be processed as a
            // wiki macro content.
            // Case 3: there is a content scanner context, and a parameter name but this one is not associated to
            // the current wiki macro info: we are in a case of a macro inside a parameter content, so it must be
            // processed as a macro content.
            } else if (macroInfo.getContentScannerContext() != null
                && (parameterName == null || macroInfo.getParameters().getParameter(parameterName) == null)) {
                macroInfo.setContent(getRenderedContentFromMacro(context));
                macroInfo.setContentScannerContext(null);
            }
        }

        return nonGeneratedContent;
    }
}

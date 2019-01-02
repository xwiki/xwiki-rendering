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
import org.xwiki.stability.Unstable;

import static org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener.createMetaData;
import static org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener.isMetaDataElement;

/**
 * This class aims at handling specific macro metadata.
 *
 * @version $Id$
 * @since 10.10RC1
 */
@Unstable
public class XWikiMacroHandler implements XWikiWikiModelHandler
{
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

    private String getSyntax(TagContext previousNodes)
    {
        if (previousNodes.getTagStack().getStackParameter(CURRENT_SYNTAX) != null) {
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

    /**
     * Handle the begin of a container element (div or span) which could contain a syntax metadata and/or a non 
     * generated content metadata.
     * @param context the current tag context.
     * @return true if a non generated content metadata has been found or the macro info is not null.
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
                String currentSyntaxParameter = this.getSyntax(context);
                try {
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
                    XWikiGeneratorListener xWikiGeneratorListener = this.parser.createXWikiGeneratorListener(listener,
                        null);

                    context.getTagStack().pushScannerContext(new WikiScannerContext(xWikiGeneratorListener));
                    context.getTagStack().getScannerContext().beginDocument();
                    withNonGeneratedContent = true;
                } catch (ComponentLookupException e) {
                    this.logger.error("Error while getting the appropriate renderer for syntax [{}]",
                        currentSyntaxParameter, e);
                }
            }
        }

        context.getTagStack().pushStackParameter(NON_GENERATED_CONTENT_STACK, withNonGeneratedContent);
        return withNonGeneratedContent || macroInfo != null;
    }

    /**
     * Handle the end of a container (div or span) which can contain a non generated content metadata.
     * @param context the context of the current tag.
     * @return true if a non generated content tag has been detected or it's in an macro info.
     */
    public boolean handleEnd(TagContext context)
    {
        boolean nonGeneratedContent = (boolean) context.getTagStack().popStackParameter(NON_GENERATED_CONTENT_STACK);
        MacroInfo macroInfo = (MacroInfo) context.getTagStack().getStackParameter(MACRO_INFO);

        if (nonGeneratedContent) {
            context.getTagStack().getScannerContext().endDocument();

            XWikiGeneratorListener xWikiGeneratorListener =
                (XWikiGeneratorListener) context.getTagStack().popScannerContext().getfListener();

            PrintRenderer renderer;

            if (context.getTagStack().isInsideBlockElement()) {
                WrappingListener wrappingListener = (WrappingListener) xWikiGeneratorListener.getListener();
                renderer = (PrintRenderer) wrappingListener.getWrappedListener();
            } else {
                renderer = (PrintRenderer) xWikiGeneratorListener.getListener();
            }
            String content = renderer.getPrinter().toString();
            macroInfo.setContent(content);
        }

        return nonGeneratedContent || macroInfo != null;
    }
}

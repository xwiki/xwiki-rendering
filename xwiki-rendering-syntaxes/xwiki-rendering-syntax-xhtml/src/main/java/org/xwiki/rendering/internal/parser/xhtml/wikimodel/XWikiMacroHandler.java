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

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
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
 * @since 10.9
 */
public class XWikiMacroHandler implements XWikiWikiModelHandler
{
    private ComponentManager componentManager;

    private XHTMLParser parser;

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

    /**
     * Handle the begin of a container element (div or span) which could contain a syntax metadata and/or an unchanged
     * content metadata.
     * @param context the current tag context.
     * @return true if an unchanged content metadata has been found or the macro info is not null.
     */
    public boolean handleBegin(TagContext context)
    {
        WikiParameters params = context.getParams();
        MacroInfo macroInfo = (MacroInfo) context.getTagStack().getStackParameter(MACRO_INFO);

        boolean withUnchangedContent = false;
        if (isMetaDataElement(params)) {
            MetaData metaData = createMetaData(params);

            if (metaData.contains(MetaData.SYNTAX)) {
                String currentSyntax = (String) metaData.getMetaData(MetaData.SYNTAX);
                context.getTagStack().pushStackParameter(CURRENT_SYNTAX, currentSyntax);
            }

            if (metaData.contains(MetaData.UNCHANGED_CONTENT)) {
                try {
                    PrintRenderer renderer = this.componentManager.getInstance(PrintRenderer.class,
                        (String) context.getTagStack().popStackParameter(CURRENT_SYNTAX));
                    DefaultWikiPrinter printer = new DefaultWikiPrinter();
                    renderer.setPrinter(printer);
                    XWikiGeneratorListener xWikiGeneratorListener = this.parser.createXWikiGeneratorListener(renderer,
                        null);

                    context.getTagStack().pushScannerContext(new WikiScannerContext(xWikiGeneratorListener));
                    context.getTagStack().getScannerContext().beginDocument(params);
                    withUnchangedContent = true;
                } catch (ComponentLookupException e) {
                    e.printStackTrace();
                }
            }
        }

        return withUnchangedContent || macroInfo != null;
    }

    /**
     * Handle the end of a container (div or span) which can contain unchanged content metadata.
     * @param context the context of the current tag.
     * @return true if an unchanged content tag has been detected or it's in an macro info.
     */
    public boolean handleEnd(TagContext context)
    {
        boolean unchangedContent = (boolean) context.getTagStack().popStackParameter(UNCHANGED_CONTENT_STACK);
        MacroInfo macroInfo = (MacroInfo) context.getTagStack().getStackParameter(MACRO_INFO);

        if (unchangedContent) {
            context.getTagStack().getScannerContext().endDocument();

            XWikiGeneratorListener xWikiGeneratorListener =
                (XWikiGeneratorListener) context.getTagStack().popScannerContext().getfListener();

            PrintRenderer renderer = (PrintRenderer) xWikiGeneratorListener.getListener();
            String content = renderer.getPrinter().toString();
            macroInfo.setContent(content);
        }

        return unchangedContent || macroInfo != null;
    }
}

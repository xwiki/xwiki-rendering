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
package org.xwiki.rendering.internal.parser.html5.wikimodel;

import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.handler.ReferenceTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler.TagStack;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler.TagStack.TagContext;

import java.util.Collections;

/**
 * Override the default WikiModel Reference handler to handle XWiki references since we store some information in
 * comments. We also need to handle the span elements introduced by XWiki.
 * 
 * @version $Id$
 * @since 1.7M1
 */
public class XWikiReferenceTagHandler extends ReferenceTagHandler
{
    private XHTMLParser parser;

    private PrintRendererFactory xwikiSyntaxPrintRendererFactory;

    /**
     * @since 2.2.5
     * @todo Remove the need to pass a Parser when WikiModel implements support for wiki syntax in links. See
     *       http://code.google.com/p/wikimodel/issues/detail?id=87
     */
    public XWikiReferenceTagHandler(XHTMLParser parser, PrintRendererFactory xwikiSyntaxPrintRendererFactory)
    {
        this.parser = parser;
        this.xwikiSyntaxPrintRendererFactory = xwikiSyntaxPrintRendererFactory;
    }

    @Override
    public void initialize(TagStack stack)
    {
        stack.setStackParameter("isInLink", false);
        stack.setStackParameter("isFreeStandingLink", false);
        stack.setStackParameter("linkParameters", WikiParameters.EMPTY);
    }

    @Override
    protected void begin(TagContext context)
    {
        boolean isInLink = (Boolean) context.getTagStack().getStackParameter("isInLink");
        if (isInLink) {
            XWikiGeneratorListener listener =
                (XWikiGeneratorListener) context.getTagStack().getStackParameter("linkListener");
            context.getTagStack().pushScannerContext(new WikiScannerContext(listener));

            // Ensure we simulate a new document being parsed
            context.getScannerContext().beginDocument();

            // Verify if it's a freestanding link and if so save the information so that we can get it in
            // XWikiCommentHandler.
            if (isFreeStandingReference(context)) {
                context.getTagStack().setStackParameter("isFreeStandingLink", true);
            } else {
                context.getTagStack().setStackParameter("linkParameters",
                    removeMeaningfulParameters(context.getParams()));
            }

            setAccumulateContent(false);
        } else if (!isFreeStandingReference(context)) {
            WikiParameter ref = context.getParams().getParameter("href");

            if (ref != null) {
                DefaultWikiPrinter printer = new DefaultWikiPrinter();

                PrintRenderer linkLabelRenderer = this.xwikiSyntaxPrintRendererFactory.createRenderer(printer);
                // Make sure to flush whatever the renderer implementation
                linkLabelRenderer.beginDocument(MetaData.EMPTY);

                XWikiGeneratorListener xwikiListener =
                    this.parser.createXWikiGeneratorListener(linkLabelRenderer, null);
                context.getTagStack().pushScannerContext(new WikiScannerContext(xwikiListener));

                // Ensure we simulate a new document being parsed
                context.getScannerContext().beginDocument();
            } else {
                WikiParameter idName = context.getParams().getParameter("id");

                if (idName == null) {
                    idName = context.getParams().getParameter("name");
                }

                if (idName != null) {
                    WikiParameter parameter = new WikiParameter("name", idName.getValue());
                    WikiParameters parameters = new WikiParameters(Collections.singletonList(parameter));
                    context.getScannerContext().onExtensionBlock(DefaultXWikiGeneratorListener.EXT_ID, parameters);
                }
            }
        } else {
            super.begin(context);
        }
    }

    @Override
    protected void end(TagContext context)
    {
        boolean isInLink = (Boolean) context.getTagStack().getStackParameter("isInLink");
        if (isInLink) {
            // Ensure we simulate a document parsing end
            context.getScannerContext().endDocument();

            context.getTagStack().popScannerContext();
        } else if (!isFreeStandingReference(context)) {
            WikiParameters parameters = context.getParams();

            WikiParameter ref = parameters.getParameter("href");

            if (ref != null) {
                // Ensure we simulate a document parsing end
                context.getScannerContext().endDocument();

                WikiScannerContext scannerContext = context.getTagStack().popScannerContext();

                XWikiGeneratorListener xwikiListener = (XWikiGeneratorListener) scannerContext.getfListener();
                PrintRenderer linkLabelRenderer = (PrintRenderer) xwikiListener.getListener();

                // Make sure to flush whatever the renderer implementation
                linkLabelRenderer.endDocument(MetaData.EMPTY);

                String label = linkLabelRenderer.getPrinter().toString();
                WikiReference reference =
                    new WikiReference(ref.getValue(), label, removeMeaningfulParameters(parameters));

                context.getScannerContext().onReference(reference);
            }
        } else {
            super.end(context);
        }
    }
}
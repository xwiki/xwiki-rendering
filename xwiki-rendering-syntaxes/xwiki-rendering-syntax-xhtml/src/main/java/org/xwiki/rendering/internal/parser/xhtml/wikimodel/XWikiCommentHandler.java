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

import java.util.ArrayDeque;
import java.util.Deque;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.reference.link.URILabelGenerator;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.xhtml.handler.CommentHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;
import org.xwiki.xml.XMLUtils;

/**
 * Handle Link and Macro definitions in comments (we store links in a comment since otherwise there are situations where
 * it's not possible to reconstruct the original reference from the rendered HTML value and for macros it wouldn't be
 * possible at all to reconstruct the macro).
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XWikiCommentHandler extends CommentHandler implements XWikiWikiModelHandler
{
    private XHTMLParser parser;

    private PrintRendererFactory xwikiSyntaxPrintRendererFactory;

    private ComponentManager componentManager;

    private ResourceReferenceParser xhtmlMarkerResourceReferenceParser;

    /**
     * We're using a stack so that we can have nested comment handling. For example when we have a link to an image we
     * need nested comment support.
     */
    private Deque<String> commentContentStack = new ArrayDeque<String>();

    /**
     * @since 2.5RC1
     * @todo Remove the need to pass a Parser when WikiModel implements support for wiki syntax in links. See
     *       http://code.google.com/p/wikimodel/issues/detail?id=87
     */
    public XWikiCommentHandler(ComponentManager componentManager, XHTMLParser parser,
        PrintRendererFactory xwikiSyntaxPrintRendererFactory, ResourceReferenceParser xhtmlMarkerResourceReferenceParser)
    {
        this.componentManager = componentManager;
        this.parser = parser;
        this.xwikiSyntaxPrintRendererFactory = xwikiSyntaxPrintRendererFactory;
        this.xhtmlMarkerResourceReferenceParser = xhtmlMarkerResourceReferenceParser;
    }

    @Override
    public void onComment(String content, TagStack stack)
    {
        // if ignoreElements is true it means we are inside a macro or another block we don't want to parse content
        boolean ignoreElements = stack.shouldIgnoreElements();

        // If the comment starts with "startwikilink" then we need to gather all XHTML tags inside
        // the A tag, till we get a "stopwikilink" comment.
        // Same for "startimage" and "stopimage".
        if(!ignoreElements) {
        if (content.startsWith("startwikilink:")) {
            handleLinkCommentStart(XMLUtils.unescapeXMLComment(content), stack);
        } else if (content.startsWith("stopwikilink")) {
            handleLinkCommentStop(stack);
        } else if (content.startsWith("startimage:")) {
            handleImageCommentStart(XMLUtils.unescapeXMLComment(content), stack);
        } else if (content.startsWith("stopimage")) {
            handleImageCommentStop(stack);
        } else (content.startsWith("startmacro")) {
            super.onComment(XMLUtils.unescapeXMLComment(content), stack);
        } }
        else {
            super.onComment(content, stack);
        }
    }

    private void handleLinkCommentStart(String content, TagStack stack)
    {
        // Since wikimodel does not support wiki syntax in link labels we need to pass the link label "as is" (as it
        // originally appears in the parsed source) and handle it specially in DefaultXWikiGeneratorListener, with the
        // parser passed as the first parameter in the DefaultXWikiGeneratorListener constructor.
        // Since we cannot get this label as it originally appeared in the HTML source ( we are doing a SAX-like
        // parsing), we should render the XDOM as HTML to get an HTML label.
        // Since any syntax would do it, as long as this renderer matches the corresponding
        // DefaultXWikiGeneratorListener
        // parser, we use an xwiki 2.1 renderer for it is less complex (no context needed to render xwiki 2.1, no url
        // resolution needed, no reference validity tests).
        // see DefaultXWikiGeneratorListener#DefaultXWikiGeneratorListener(Parser, ResourceReferenceParser, ImageParser)
        // see WikiModelXHTMLParser#getLinkLabelParser()
        // see http://code.google.com/p/wikimodel/issues/detail?id=87
        // TODO: remove this workaround when wiki syntax in link labels will be supported by wikimodel
        DefaultWikiPrinter printer = new DefaultWikiPrinter();

        PrintRenderer linkLabelRenderer = this.xwikiSyntaxPrintRendererFactory.createRenderer(printer);
        // Make sure to flush whatever the renderer implementation
        linkLabelRenderer.beginDocument(MetaData.EMPTY);

        XWikiGeneratorListener xwikiListener = this.parser.createXWikiGeneratorListener(linkLabelRenderer, null);

        stack.pushStackParameter(LINK_LISTENER, xwikiListener);

        stack.pushStackParameter(IS_IN_LINK, Boolean.TRUE);
        stack.pushStackParameter(IS_FREE_STANDING_LINK, Boolean.FALSE);
        stack.pushStackParameter(LINK_PARAMETERS, WikiParameters.EMPTY);

        this.commentContentStack.push(content.substring("startwikilink:".length()));
    }

    private void handleLinkCommentStop(TagStack stack)
    {
        XWikiGeneratorListener xwikiListener =
            (XWikiGeneratorListener) stack.popStackParameter(LINK_LISTENER);
        PrintRenderer linkLabelRenderer = (PrintRenderer) xwikiListener.getListener();

        // Make sure to flush whatever the renderer implementation
        linkLabelRenderer.endDocument(MetaData.EMPTY);

        boolean isFreeStandingLink = (Boolean) stack.getStackParameter(IS_FREE_STANDING_LINK);

        ResourceReference linkReference = this.xhtmlMarkerResourceReferenceParser.parse(this.commentContentStack.pop());
        WikiParameters linkParams = WikiParameters.EMPTY;
        String label = null;
        if (!isFreeStandingLink) {
            label = linkLabelRenderer.getPrinter().toString();

            // Add the Link reference parameters to the link parameters.
            linkParams = (WikiParameters) stack.getStackParameter(LINK_PARAMETERS);
        }

        WikiReference wikiReference = new XWikiWikiReference(linkReference, label, linkParams, isFreeStandingLink);
        stack.getScannerContext().onReference(wikiReference);

        stack.popStackParameter(IS_IN_LINK);
        stack.popStackParameter(IS_FREE_STANDING_LINK);
        stack.popStackParameter(LINK_PARAMETERS);
    }

    private void handleImageCommentStart(String content, TagStack stack)
    {
        stack.setStackParameter(IS_IN_IMAGE, Boolean.TRUE);
        this.commentContentStack.push(content.substring("startimage:".length()));
    }

    private void handleImageCommentStop(TagStack stack)
    {
        boolean isFreeStandingImage = (Boolean) stack.getStackParameter(IS_FREE_STANDING_IMAGE);

        ResourceReference imageReference =
            this.xhtmlMarkerResourceReferenceParser.parse(this.commentContentStack.pop());

        WikiParameters imageParams = WikiParameters.EMPTY;
        if (!isFreeStandingImage) {
            // Remove the ALT attribute if the content has the same value as the original image location
            // This is because the XHTML renderer automatically adds an ALT attribute since it is mandatory
            // in the XHTML specifications.
            imageParams = (WikiParameters) stack.getStackParameter(IMAGE_PARAMETERS);
            WikiParameter alt = imageParams.getParameter("alt");
            if (alt != null && alt.getValue().equals(computeAltAttributeValue(imageReference))) {
                imageParams = imageParams.remove("alt");
            }
        }

        WikiReference reference = new XWikiWikiReference(imageReference, null, imageParams, isFreeStandingImage);
        stack.getScannerContext().onImage(reference);

        stack.setStackParameter(IS_IN_IMAGE, Boolean.FALSE);
        stack.setStackParameter(IS_FREE_STANDING_IMAGE, Boolean.FALSE);
        stack.setStackParameter(IMAGE_PARAMETERS, WikiParameters.EMPTY);
    }

    private String computeAltAttributeValue(ResourceReference reference)
    {
        String label;
        try {
            URILabelGenerator uriLabelGenerator =
                this.componentManager.getInstance(URILabelGenerator.class, reference.getType().getScheme());
            label = uriLabelGenerator.generateLabel(reference);
        } catch (ComponentLookupException e) {
            label = reference.getReference();
        }
        return label;
    }
}

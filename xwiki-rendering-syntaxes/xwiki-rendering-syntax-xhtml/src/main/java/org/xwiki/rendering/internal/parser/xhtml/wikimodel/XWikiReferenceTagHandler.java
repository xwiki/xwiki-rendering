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

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.wikimodel.WikiModelStreamParser;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.handler.ReferenceTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;

/**
 * Override the default WikiModel Reference handler to handle XWiki references since we store some information in
 * comments. We also need to handle the span elements introduced by XWiki.
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XWikiReferenceTagHandler extends ReferenceTagHandler implements XWikiWikiModelHandler
{
    /**
     * URL matching pattern.
     */
    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z0-9+.-]*://");

    /**
     * Prefix for mailto-links.
     */
    private static final String MAILTO_PREFIX = "mailto:";

    private WikiModelStreamParser parser;

    /**
     * @param parser the XHTML parser, used for the label
     * @since 14.10RC1
     * @todo Remove the need to pass a Parser when WikiModel implements support for wiki syntax in links. See
     *       http://code.google.com/p/wikimodel/issues/detail?id=87
     */
    public XWikiReferenceTagHandler(WikiModelStreamParser parser)
    {
        this.parser = parser;
    }

    @Override
    public void initialize(TagStack stack)
    {
        stack.setStackParameter(IS_IN_LINK, Boolean.FALSE);
        stack.setStackParameter(IS_FREE_STANDING_LINK, Boolean.FALSE);
        stack.setStackParameter(LINK_PARAMETERS, WikiParameters.EMPTY);
    }

    @Override
    protected void begin(TagContext context)
    {
        boolean isInLink = (Boolean) context.getTagStack().getStackParameter(IS_IN_LINK);
        if (isInLink) {
            XWikiGeneratorListener listener =
                (XWikiGeneratorListener) context.getTagStack().getStackParameter(LINK_LISTENER);
            context.getTagStack().pushScannerContext(new WikiScannerContext(listener));

            // Ensure we simulate a new document being parsed
            context.getScannerContext().beginDocument();

            // Verify if it's a freestanding link and if so save the information so that we can get it in
            // XWikiCommentHandler.
            if (isFreeStandingReference(context)) {
                context.getTagStack().setStackParameter(IS_FREE_STANDING_LINK, Boolean.TRUE);
            } else {
                context.getTagStack().setStackParameter(LINK_PARAMETERS,
                    removeMeaningfulParameters(context.getParams()));
            }

            setAccumulateContent(false);
        } else if (!isFreeStandingReference(context)) {
            WikiParameter ref = context.getParams().getParameter("href");

            if (ref != null) {
                XWikiGeneratorListener xwikiListener =
                    this.parser.createXWikiGeneratorListener(new XDOMGeneratorListener(), null);
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
        boolean isInLink = (Boolean) context.getTagStack().getStackParameter(IS_IN_LINK);
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
                XDOMGeneratorListener linkLabelRenderer = (XDOMGeneratorListener) xwikiListener.getListener();

                XDOM label = linkLabelRenderer.getXDOM();

                ResourceReference resourceReference = computeResourceReference(ref.getValue());

                XWikiWikiReference reference =
                    new XWikiWikiReference(resourceReference, label, removeMeaningfulParameters(parameters), false);

                context.getScannerContext().onReference(reference);
            }
        } else {
            super.end(context);
        }
    }

    /**
     * Recognize the passed reference and figure out what type of link it should be:
     * <ul>
     *   <li>UC1: the reference points to a valid URL, we return a reference of type "url",
     *       e.g. {@code http://server/path/reference#anchor}</li>
     *   <li>UC2: the reference is a mailto: link, we return a reference of type "mailto",
     *       e.g., {@code mailto:user@example.com}</li>
     *   <li>UC3: the reference is not a valid URL, we return a reference of type "path",
     *       e.g. {@code path/reference#anchor}</li>
     * </ul>
     *
     * @param rawReference the full reference (e.g. "/some/path/something#other")
     * @return the properly typed {@link ResourceReference} matching the use cases
     */
    private ResourceReference computeResourceReference(String rawReference)
    {
        ResourceReference reference;

        // Do we have a valid URL?
        Matcher matcher = URL_SCHEME_PATTERN.matcher(rawReference);
        if (matcher.lookingAt()) {
            // We have UC1
            reference = new ResourceReference(rawReference, ResourceType.URL);
        } else if (rawReference.startsWith(MAILTO_PREFIX)) {
            // We have UC2
            reference = new ResourceReference(rawReference.substring(MAILTO_PREFIX.length()), ResourceType.MAILTO);
        } else {
            // We have UC3
            reference = new ResourceReference(rawReference, ResourceType.PATH);
        }

        return reference;
    }
}

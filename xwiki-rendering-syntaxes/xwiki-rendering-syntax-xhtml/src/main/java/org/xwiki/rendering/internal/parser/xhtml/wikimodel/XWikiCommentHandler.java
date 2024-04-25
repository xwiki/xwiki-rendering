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
import java.util.Map;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.renderer.reference.link.URILabelGenerator;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.xhtml.handler.CommentHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.IgnoreElementRule;
import org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;
import org.xwiki.xml.XMLUtils;

import static org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener.METADATA_ATTRIBUTE_PREFIX;
import static org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo.MACRO_START;
import static org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo.MACRO_STOP;
import static org.xwiki.rendering.wikimodel.xhtml.impl.TagStack.IGNORE_ALL;

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
        ResourceReferenceParser xhtmlMarkerResourceReferenceParser)
    {
        this.componentManager = componentManager;
        this.parser = parser;
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
        if (!ignoreElements && content.startsWith("startwikilink:")) {
            handleLinkCommentStart(XMLUtils.unescapeXMLComment(content), stack);
        } else if (!ignoreElements && content.startsWith("stopwikilink")) {
            handleLinkCommentStop(stack);
        } else if (!ignoreElements && content.startsWith("startimage:")) {
            handleImageCommentStart(XMLUtils.unescapeXMLComment(content), stack);
        } else if (!ignoreElements && content.startsWith("stopimage")) {
            handleImageCommentStop(stack);
        } else if (content.startsWith(MACRO_START)) {
            this.handleMacroCommentStart(XMLUtils.unescapeXMLComment(content), stack);
        } else if (content.startsWith(MACRO_STOP)) {
            this.handleMacroCommentStop(stack);
        } else {
            super.onComment(content, stack);
        }
    }

    private void handleMacroCommentStart(String content, TagStack stack)
    {
        // If we're currently ignoring elements, the whole macro needs to be ignored regardless if it contains
        // non-generated content or not.
        boolean shouldIgnoreAll = stack.shouldIgnoreElements();

        MacroInfo macroInfo = new MacroInfo(content);
        stack.pushStackParameter(MACRO_INFO, macroInfo);

        // we ignore all elements
        if (shouldIgnoreAll) {
            stack.setIgnoreElements();

        // we ignore elements until we get a non generated content: then the rule will be deactivated
        // see IgnoreElementRule
        } else {
            stack.pushIgnoreElementRule(new IgnoreElementRule(ignoreElementRule -> {
                boolean result = false;

                TagContext tagContext = ignoreElementRule.getTagContext();
                boolean beginElement = ignoreElementRule.isBeginElement();
                boolean isCurrentlyActive = ignoreElementRule.isActive();
                WikiParameters wikiParameters = tagContext.getParams();
                Map<String, Object> ruleContext = ignoreElementRule.getRuleContext();
                int divCounter = 0;

                if (ruleContext.containsKey(MetaData.NON_GENERATED_CONTENT)) {
                    divCounter = (Integer) ruleContext.get(MetaData.NON_GENERATED_CONTENT);
                }

                if (wikiParameters != null) {
                    boolean onNotGeneratedContentDiv =
                        wikiParameters.getParameter(METADATA_ATTRIBUTE_PREFIX + MetaData.NON_GENERATED_CONTENT) != null;

                    // We are handling a non-generated content div, so we should check if we need to de/activate
                    // the rule.
                    if (onNotGeneratedContentDiv) {

                        // in case of a beginElement, the rule must be already active to be activated: we don't want
                        // to switch it off in case of redundant unchanged-content div.
                        if (beginElement) {
                            if (isCurrentlyActive && divCounter == 0) {
                                result = true;
                            } else {

                                // if the rule was already deactivated, then count the number of redundant div
                                // to be sure to reactivate it at the right moment.
                                divCounter++;
                            }
                        } else {
                            if (!isCurrentlyActive && divCounter == 0) {
                                result = true;
                            } else {
                                divCounter--;
                            }
                        }
                    }
                }
                ruleContext.put(MetaData.NON_GENERATED_CONTENT, divCounter);
                return result;
            }, true));
        }
    }

    private void handleMacroCommentStop(TagStack stack)
    {
        if (stack.getStackParameter(MACRO_INFO) != null) {
            MacroInfo macroInfo = (MacroInfo) stack.popStackParameter(MACRO_INFO);

            String parameterName = (String) stack.getStackParameter(PARAMETER_CONTENT_NAME);
            if (macroInfo.getContentScannerContext() != null
                || (parameterName != null && macroInfo.getParameterScannerContext(parameterName) != null))
            {
                // Wrong/extra end of macro comment, ignore it as the content of the macro or one of its parameters
                // didn't end yet.
                stack.pushStackParameter(MACRO_INFO, macroInfo);
                return;
            }

            IgnoreElementRule ignoreElementRule = stack.popIgnoreElementRule();

            // if we were ignoring all we don't want to output the macro
            if (!ignoreElementRule.equals(IGNORE_ALL)) {
                if (stack.isInsideBlockElement()) {
                    stack.getScannerContext().onMacroInline(macroInfo.getName(), macroInfo.getParameters(),
                        macroInfo.getContent());
                } else {
                    TagHandler.sendEmptyLines(stack);
                    stack.getScannerContext().onMacroBlock(macroInfo.getName(), macroInfo.getParameters(),
                        macroInfo.getContent());
                }
            }
        }
    }

    private void handleLinkCommentStart(String content, TagStack stack)
    {
        // Since wikimodel does not support wiki syntax in link labels we need to pass the link label "as is" (as it
        // originally appears in the parsed source) and handle it specially in DefaultXWikiGeneratorListener, with the
        // parser passed as the first parameter in the DefaultXWikiGeneratorListener constructor.
        // Since we cannot get this label as it originally appeared in the HTML source ( we are doing a SAX-like
        // parsing), we directly parse it and instead pass the resulting XDOM via the XWikiWikiReference class.
        // see DefaultXWikiGeneratorListener#DefaultXWikiGeneratorListener(Parser, ResourceReferenceParser, ImageParser)
        // see WikiModelXHTMLParser#getLinkLabelParser()
        // see http://code.google.com/p/wikimodel/issues/detail?id=87
        // TODO: remove this workaround when wiki syntax in link labels will be supported by wikimodel
        XDOMGeneratorListener linkLabelListener = new XDOMGeneratorListener();
        linkLabelListener.beginDocument(MetaData.EMPTY);

        XWikiGeneratorListener xwikiListener = this.parser.createXWikiGeneratorListener(linkLabelListener, null);

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
        XDOMGeneratorListener linkLabelRenderer = (XDOMGeneratorListener) xwikiListener.getListener();

        // Make sure to flush whatever the renderer implementation
        linkLabelRenderer.endDocument(MetaData.EMPTY);

        boolean isFreeStandingLink = (Boolean) stack.getStackParameter(IS_FREE_STANDING_LINK);

        ResourceReference linkReference = this.xhtmlMarkerResourceReferenceParser.parse(this.commentContentStack.pop());
        WikiParameters linkParams = WikiParameters.EMPTY;
        XDOM label = null;
        if (!isFreeStandingLink) {
            label = linkLabelRenderer.getXDOM();

            // Add the Link reference parameters to the link parameters.
            linkParams = (WikiParameters) stack.getStackParameter(LINK_PARAMETERS);
        }

        XWikiWikiReference wikiReference = new XWikiWikiReference(linkReference, label, linkParams, isFreeStandingLink);
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

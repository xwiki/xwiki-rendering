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
package org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel;

import java.util.Collections;

import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.AttachmentTagHandler.ConfluenceAttachment;
import org.xwiki.rendering.internal.parser.wikimodel.WikiModelParserUtils;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.listener.reference.UserResourceReference;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * WikiModel listener bridge for the XHTML Syntax.
 * 
 * @version $Id$
 * @since 2.5RC1
 */
public class ConfluenceXWikiGeneratorListener extends XHTMLXWikiGeneratorListener
{
    private StreamParser plainParser;

    /**
     * @param parser the parser to use to parse link labels
     * @param listener the XWiki listener to which to forward WikiModel events
     * @param linkReferenceParser the parser to parse link references
     * @param imageReferenceParser the parser to parse image references
     * @param plainRendererFactory used to generate header ids
     * @param idGenerator used to generate header ids
     * @param syntax the syntax of the parsed source
     * @since 3.0M3
     */
    public ConfluenceXWikiGeneratorListener(StreamParser parser, Listener listener,
        ResourceReferenceParser linkReferenceParser, ResourceReferenceParser imageReferenceParser,
        PrintRendererFactory plainRendererFactory, IdGenerator idGenerator, Syntax syntax, StreamParser plainParser)
    {
        super(parser, listener, linkReferenceParser, imageReferenceParser, plainRendererFactory, idGenerator, syntax);

        this.plainParser = plainParser;
    }

    @Override
    public void onReference(WikiReference reference)
    {
        if (reference instanceof ConfluenceLinkWikiReference) {
            ConfluenceLinkWikiReference confluenceReference = (ConfluenceLinkWikiReference) reference;

            ResourceReference resourceReference = null;

            if (confluenceReference.getPage() != null) {
                StringBuilder str = new StringBuilder();
                if (confluenceReference.getSpace() != null) {
                    str.append(confluenceReference.getSpace());
                    str.append('.');
                }
                if (confluenceReference.getPage() != null) {
                    str.append(confluenceReference.getPage());
                } else if (confluenceReference.getSpace() != null) {
                    str.append("WebHome");
                }

                DocumentResourceReference documentResourceReference = new DocumentResourceReference(str.toString());

                if (confluenceReference.getAnchor() != null) {
                    documentResourceReference.setAnchor(confluenceReference.getAnchor());
                }

                resourceReference = documentResourceReference;
            } else if (confluenceReference.getSpace() != null) {
                DocumentResourceReference documentResourceReference =
                    new DocumentResourceReference(confluenceReference.getSpace() + ".WebHome");

                if (confluenceReference.getAnchor() != null) {
                    documentResourceReference.setAnchor(confluenceReference.getAnchor());
                }

                resourceReference = documentResourceReference;
            } else if (confluenceReference.getAttachment() != null) {
                ConfluenceAttachment attachment = confluenceReference.getAttachment();

                StringBuilder str = new StringBuilder();
                if (attachment.space != null) {
                    str.append(attachment.space);
                    str.append('.');
                }
                if (attachment.page != null) {
                    str.append(attachment.page);
                    str.append('@');
                } else if (attachment.space != null) {
                    str.append("WebHome");
                    str.append('@');
                }

                if (attachment.user != null) {
                    // TODO
                }

                str.append(attachment.filename);

                AttachmentResourceReference attachmentResourceReference =
                    new AttachmentResourceReference(str.toString());

                if (confluenceReference.getAnchor() != null) {
                    attachmentResourceReference.setAnchor(confluenceReference.getAnchor());
                }

                resourceReference = attachmentResourceReference;
            } else if (confluenceReference.getUser() != null) {
                UserResourceReference userResourceReference = new UserResourceReference(confluenceReference.getUser());

                if (confluenceReference.getAnchor() != null) {
                    userResourceReference.setAnchor(confluenceReference.getAnchor());
                }

                resourceReference = userResourceReference;
            }

            if (resourceReference != null) {
                // Since WikiModel doesn't handle syntax in link labels and thus doesn't have begin/end events for
                // links, we
                // need to call the XWiki events and use an inline parser to parse the syntax in the label.
                getListener().beginLink(resourceReference, false, Collections.<String, String> emptyMap());
                if (reference.getLabel() != null) {
                    try {
                        WikiModelParserUtils parserUtils = new WikiModelParserUtils();
                        parserUtils.parseInline(this.plainParser, reference.getLabel(), getListener(), false);
                    } catch (ParseException e) {
                        // TODO supposedly impossible with plain test parser
                    }
                }
                getListener().endLink(resourceReference, false, Collections.<String, String> emptyMap());
            }
        } else {
            super.onReference(reference);
        }
    }

    @Override
    public void onImage(WikiReference reference)
    {
        if (reference instanceof ConfluenceImageWikiReference) {
            ConfluenceImageWikiReference confluenceReference = (ConfluenceImageWikiReference) reference;

            ResourceReference resourceReference = null;

            if (confluenceReference.getAttachment() != null) {
                ConfluenceAttachment attachment = confluenceReference.getAttachment();

                StringBuilder str = new StringBuilder();
                if (attachment.space != null) {
                    str.append(attachment.space);
                    str.append('.');
                }
                if (attachment.page != null) {
                    str.append(attachment.page);
                    str.append('@');
                } else if (attachment.space != null) {
                    str.append("WebHome");
                    str.append('@');
                }

                if (attachment.user != null) {
                    // TODO
                }

                str.append(attachment.filename);

                resourceReference = new AttachmentResourceReference(str.toString());
            } else if (confluenceReference.getURL() != null) {
                resourceReference = new ResourceReference(confluenceReference.getURL(), ResourceType.URL);
            }

            if (resourceReference != null) {
                onImage(resourceReference, false, Collections.<String, String> emptyMap());
            }
        } else {
            super.onImage(reference);
        }
    }
}

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
package org.xwiki.rendering.internal.renderer.blocknote;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.InterWikiResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.wiki.WikiModel;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.REFERENCE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.FREE_STANDING;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.HREF;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.LINK;

/**
 * Renders inline content to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public class InlineContentChainingListener extends AbstractChainingListener
{
    private final Context context;

    private final WikiModel wikiModel;

    private final RenderingConfiguration renderingConfiguration;

    /**
     * Creates a new instance using the provided listener chain.
     *
     * @param listenerChain the listener chain
     * @param wikiModel used to compute the link URL from the XWiki resource reference; may be {@code null} when
     *            rendering outside a wiki context
     * @param renderingConfiguration used to look up InterWiki definitions
     */
    public InlineContentChainingListener(ListenerChain listenerChain, WikiModel wikiModel,
        RenderingConfiguration renderingConfiguration)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
        this.wikiModel = wikiModel;
        this.renderingConfiguration = renderingConfiguration;
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            ObjectNode link = this.context.getBlockNoteState().beginBlock(LINK, true, true, false, false);
            ObjectNode linkProperties = (ObjectNode) link.path(PROPS);
            linkProperties.set(REFERENCE, this.context.getBlockNoteState().toJSON(reference));
            link.put(HREF, getLinkURL(reference));
            if (freestanding) {
                linkProperties.put(FREE_STANDING, freestanding);
            }
        }
    }

    private String getLinkURL(ResourceReference reference)
    {
        String url = reference.getReference();
        ResourceType type = reference.getType();
        if (ResourceType.ATTACHMENT.equals(type) || ResourceType.PAGE_ATTACHMENT.equals(type)) {
            url = getAttachmentLinkURL(reference);
        } else if (ResourceType.DOCUMENT.equals(type) || ResourceType.PAGE.equals(type)
            || ResourceType.SPACE.equals(type)) {
            url = getDocumentLinkURL(reference);
        } else if (ResourceType.MAILTO.equals(type)) {
            url = type.getScheme() + ':' + reference.getReference();
        } else if (ResourceType.UNC.equals(type)) {
            url = "file:///" + reference.getReference().replace("\\", "/");
        } else if (ResourceType.INTERWIKI.equals(type)) {
            url = getInterWikiLinkURL(reference);
        }
        return url;
    }

    private String getAttachmentLinkURL(ResourceReference reference)
    {
        if (this.wikiModel != null) {
            return this.wikiModel.getLinkURL(reference);
        }
        return reference.getType().getScheme() + ':' + reference.getReference();
    }

    private String getDocumentLinkURL(ResourceReference reference)
    {
        if (StringUtils.isEmpty(reference.getReference())) {
            return getAutoLinkURL(reference);
        } else if (this.wikiModel == null) {
            return reference.getReference();
        } else if (this.wikiModel.isDocumentAvailable(reference)) {
            return this.wikiModel.getDocumentViewURL(reference);
        } else {
            return this.wikiModel.getDocumentEditURL(reference);
        }
    }

    private String getAutoLinkURL(ResourceReference reference)
    {
        StringBuilder url = new StringBuilder();
        String queryString = reference.getParameter(DocumentResourceReference.QUERY_STRING);
        if (queryString != null) {
            url.append('?').append(queryString);
        }
        url.append('#');
        String anchor = reference.getParameter(DocumentResourceReference.ANCHOR);
        if (anchor != null) {
            url.append(anchor);
        }
        return url.toString();
    }

    private String getInterWikiLinkURL(ResourceReference reference)
    {
        String alias = reference.getParameter(InterWikiResourceReference.INTERWIKI_ALIAS);
        Properties definitions = this.renderingConfiguration.getInterWikiDefinitions();
        if (definitions.containsKey(alias)) {
            return definitions.getProperty(alias) + reference.getReference();
        }
        return reference.getReference();
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        beginFormat();
    }

    private void beginFormat()
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            // Text style has changed so we need to end the current text block.
            // Make sure the styles for the ended text block don't include the new format.
            Block formatBlock = this.context.getXDOMPath().pop();
            this.context.getBlockNoteState().endTextBlock();
            this.context.getXDOMPath().push(formatBlock);
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        endFormat();
    }

    private void endFormat()
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            this.context.getBlockNoteState().maybeAddTextBlock(true);
        }
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (inline) {
            beginFormat();
            this.context.getTextState().addText(content, inline);
            endFormat();
        }
    }
}

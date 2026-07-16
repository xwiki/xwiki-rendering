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
package org.xwiki.rendering.internal.parser.blocknote.blocks;

import java.util.Deque;
import java.util.Map;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Link block parser.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named(LinkBlockParser.LINK)
@Singleton
public class LinkBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String LINK = "link";

    /**
     * The property that holds the link reference (target).
     */
    public static final String HREF = "href";

    /**
     * The link block property that indicates whether the link is free-standing or not.
     */
    public static final String FREE_STANDING = "xwikiFreestanding";

    /**
     * The link block property that holds the label generated for a link that has no label. When present, the link
     * content is a generated label that must be discarded on parse, so that the link is rendered back without a label.
     */
    public static final String GENERATED_LABEL = "xwikiGeneratedLabel";

    @Override
    public void parse(ObjectNode linkBlock, Deque<Context> contextStack) throws ParseException
    {
        JsonNode xwikiReference = linkBlock.path(PROPS).path(REFERENCE);
        ResourceReference target = asResourceReference(
            xwikiReference.isMissingNode() || xwikiReference.isNull() ? linkBlock.path(HREF) : xwikiReference);
        boolean freeStanding = linkBlock.path(PROPS).path(FREE_STANDING).asBoolean(false);
        Map<String, String> parameters = getBlockParameters(linkBlock);
        contextStack.peek().listener().beginLink(target, freeStanding, parameters);

        // Discard the link content when it is a generated label, so that the link is rendered back without a label.
        JsonNode generatedLabel = linkBlock.path(PROPS).path(GENERATED_LABEL);
        if (generatedLabel.isMissingNode() || generatedLabel.isNull()) {
            visitInlineChildBlocks(linkBlock, CONTENT, contextStack);
        }

        contextStack.peek().listener().endLink(target, freeStanding, parameters);
    }
}

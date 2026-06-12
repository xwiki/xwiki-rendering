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
import java.util.function.Consumer;

import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base class for BlockNote embed block parsers.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public abstract class AbstractEmbedBlockParser extends AbstractBlockParser
{
    /**
     * The name of the property holding the caption of an embed block.
     */
    public static final String CAPTION = "caption";

    /**
     * The name of the property holding the URL of an embed block (indicates the resource that is being embedded).
     */
    public static final String URL = "url";

    /**
     * The parameter used to store the alternative text for an embed block.
     */
    public static final String ALT = "alt";

    /**
     * The name of the property holding the alternative text (or resource name) of an embed block.
     */
    public static final String NAME = "name";

    /**
     * The parameter used to store the width of an embed block.
     */
    public static final String WIDTH = "width";

    /**
     * The name of the property holding the width of an embed block preview.
     */
    public static final String PREVIEW_WIDTH = "previewWidth";

    @Override
    public void parse(ObjectNode embedBlock, Deque<Context> contextStack) throws ParseException
    {
        JsonNode caption = embedBlock.path(PROPS).path(CAPTION);
        if (caption.isTextual() && !contextStack.peek().inline()) {
            // Avoid storing the caption in the embed parameters, as we do for inline embeds.
            ((ObjectNode) embedBlock.path(PROPS)).remove(CAPTION);
            Map<String, String> figureParameters = Map.of("class", getEmbedType());
            Listener listener = contextStack.peek().listener();
            listener.beginFigure(figureParameters);
            contextStack.push(contextStack.peek().withInline(true));
            visitEmbedBlock(embedBlock, contextStack);
            listener.beginFigureCaption(Listener.EMPTY_PARAMETERS);
            parsePlainText(caption.asText(), contextStack);
            listener.endFigureCaption(Listener.EMPTY_PARAMETERS);
            contextStack.pop();
            listener.endFigure(figureParameters);
        } else {
            visitEmbedBlock(embedBlock, contextStack);
        }
    }

    protected abstract String getEmbedType();

    protected void visitEmbedBlock(ObjectNode embedBlock, Deque<Context> contextStack)
    {
        Map<String, String> parameters = getEmbedParameters(embedBlock);
        parameters.put("reference", embedBlock.path(PROPS).path(URL).asText(""));

        // We use a macro block because XDOM doesn't have dedicated blocks for file, video and audio embeds.
        contextStack.peek().listener().onMacro(getEmbedType(), parameters, null, contextStack.peek().inline());
    }

    @Override
    public void traverse(ObjectNode embedBlock, Consumer<ObjectNode> blockConsumer)
    {
        // Embed blocks don't have child blocks, so we don't need to traverse them.
        blockConsumer.accept(embedBlock);
    }

    protected Map<String, String> getEmbedParameters(ObjectNode embedBlock)
    {
        Map<String, String> parameters = getBlockParameters(embedBlock);
        JsonNode embedProperties = embedBlock.path(PROPS);

        JsonNode name = embedProperties.path(NAME);
        if (name.isTextual()) {
            parameters.put(ALT, name.asText());
        }

        JsonNode width = embedProperties.path(PREVIEW_WIDTH);
        if (width.isNumber()) {
            parameters.put(WIDTH, width.asText());
        }

        JsonNode caption = embedProperties.path(CAPTION);
        if (caption.isTextual()) {
            parameters.put(CAPTION, caption.asText());
        }

        return parameters;
    }
}

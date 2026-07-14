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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.FREE_STANDING;

/**
 * Image block parser.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named(ImageBlockParser.IMAGE)
@Singleton
public class ImageBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String IMAGE = "image";

    /**
     * The mapping between BlockNote image alignments and XWiki Rendering image alignment styles.
     */
    public static final Map<String, String> IMAGE_ALIGNMENT = Map.of("left", "start", "right", "end");

    /**
     * The parameter used to denote the alignment style of an image.
     */
    public static final String IMAGE_ALIGNMENT_PARAMETER = "data-xwiki-image-style-alignment";

    /**
     * The parameter used to denote the label (caption) of an inline image.
     */
    public static final String IMAGE_LABEL_PARAMETER = "data-xwiki-image-label";

    /**
     * The name of the property holding the URL of an image block.
     */
    public static final String URL = "url";

    /**
     * The name of the property holding the caption of an image block.
     */
    public static final String CAPTION = "caption";

    /**
     * The parameter used to store the alternative text for an image block.
     */
    public static final String ALT = "alt";

    /**
     * The name of the property holding the alternative text (or resource name) of an image block.
     */
    public static final String NAME = "name";

    /**
     * The parameter used to store the width of an image block.
     */
    public static final String WIDTH = "width";

    /**
     * The name of the property holding the width of an image block preview.
     */
    public static final String PREVIEW_WIDTH = "previewWidth";

    @Override
    public void parse(ObjectNode imageBlock, Deque<Context> contextStack) throws ParseException
    {
        JsonNode caption = imageBlock.path(PROPS).path(CAPTION);
        if (isNonEmptyTextual(caption) && !contextStack.peek().inline()) {
            // Avoid storing the caption in the image parameters, as we do for inline images.
            ((ObjectNode) imageBlock.path(PROPS)).remove(CAPTION);
            Map<String, String> figureParameters = Map.of("class", IMAGE);
            Listener listener = contextStack.peek().listener();
            listener.beginFigure(figureParameters);
            contextStack.push(contextStack.peek().withInline(true));
            visitImageBlock(imageBlock, contextStack);
            listener.beginFigureCaption(Listener.EMPTY_PARAMETERS);
            parsePlainText(caption.asText(), contextStack);
            listener.endFigureCaption(Listener.EMPTY_PARAMETERS);
            contextStack.pop();
            listener.endFigure(figureParameters);
        } else {
            visitImageBlock(imageBlock, contextStack);
        }
    }

    private void visitImageBlock(ObjectNode imageBlock, Deque<Context> contextStack)
    {
        JsonNode xwikiReference = imageBlock.path(PROPS).path(REFERENCE);
        ResourceReference imageReference = asResourceReference(xwikiReference.isMissingNode() || xwikiReference.isNull()
            ? imageBlock.path(PROPS).path(URL) : xwikiReference);
        String id = contextStack.peek().idGenerator().generateUniqueId("I", imageReference.getReference());
        Map<String, String> parameters = getImageParameters(imageBlock, contextStack);
        boolean inline = contextStack.peek().inline();
        boolean freeStanding = imageBlock.path(PROPS).path(FREE_STANDING).asBoolean(false);

        if (!inline) {
            // Wrap the image in a paragraph when not inline, because XDOM treats images as inline content.
            contextStack.peek().listener().beginParagraph(Listener.EMPTY_PARAMETERS);
        }

        contextStack.peek().listener().onImage(imageReference, freeStanding, id, parameters);

        if (!inline) {
            contextStack.peek().listener().endParagraph(Listener.EMPTY_PARAMETERS);
        }
    }

    @Override
    protected Map<String, String> getBlockStyles()
    {
        Map<String, String> imageStyles = new LinkedHashMap<>(super.getBlockStyles());
        // We handle text alignment separately, as an image parameter.
        imageStyles.remove(TEXT_ALIGNMENT);
        return imageStyles;
    }

    private Map<String, String> getImageParameters(ObjectNode imageBlock, Deque<Context> contextStack)
    {
        Map<String, String> parameters = getBlockParameters(imageBlock);
        JsonNode imageProperties = imageBlock.path(PROPS);

        JsonNode name = imageProperties.path(NAME);
        if (isNonEmptyTextual(name)) {
            parameters.put(ALT, name.asText());
        }

        JsonNode width = imageProperties.path(PREVIEW_WIDTH);
        if (width.isNumber()) {
            parameters.put(WIDTH, width.asText());
        }

        JsonNode caption = imageProperties.path(CAPTION);
        if (isNonEmptyTextual(caption)) {
            parameters.put(CAPTION, caption.asText());
        }

        JsonNode alignment = imageProperties.path(TEXT_ALIGNMENT);
        if (isNonEmptyTextual(alignment)) {
            String value = IMAGE_ALIGNMENT.getOrDefault(alignment.asText().toLowerCase(), alignment.asText());
            parameters.put(IMAGE_ALIGNMENT_PARAMETER, value);
        }

        if (parameters.containsKey(CAPTION) && contextStack.peek().inline()) {
            parameters.put(IMAGE_LABEL_PARAMETER, parameters.remove(CAPTION));
        }

        return parameters;
    }

    @Override
    public void traverse(ObjectNode embedBlock, Consumer<ObjectNode> blockConsumer)
    {
        // Image blocks don't have child blocks, so we don't need to traverse them.
        blockConsumer.accept(embedBlock);
    }

    private boolean isNonEmptyTextual(JsonNode node)
    {
        return node.isTextual() && !node.asText().isBlank();
    }
}

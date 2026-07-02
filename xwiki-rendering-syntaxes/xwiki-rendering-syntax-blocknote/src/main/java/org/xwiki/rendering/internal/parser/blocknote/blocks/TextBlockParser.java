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

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.internal.parser.blocknote.FormatContext;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Text block parser.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named(TextBlockParser.TEXT)
@Singleton
public class TextBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String TEXT = "text";

    /**
     * The mapping between BlockNote text styles and XWiki Rendering text formats.
     */
    public static final Map<String, Format> TEXT_STYLES;

    /**
     * The property that holds the text styles.
     */
    public static final String STYLES = "styles";

    /**
     * The text "style" used to mark verbatim text (text that should be rendered as-is, without interpreting any of its
     * content as wiki syntax).
     */
    public static final String VERBATIM = "xwikiVerbatim";

    static {
        Map<String, Format> textStyles = new LinkedHashMap<>();
        textStyles.put("bold", Format.BOLD);
        textStyles.put("italic", Format.ITALIC);
        textStyles.put("underline", Format.UNDERLINED);
        textStyles.put("strike", Format.STRIKEDOUT);
        textStyles.put("superscript", Format.SUPERSCRIPT);
        textStyles.put("subscript", Format.SUBSCRIPT);
        textStyles.put("code", Format.MONOSPACE);
        // Verbatim is a special "style" that doesn't have a corresponding format. We use a dedicated XDOM block for it.
        textStyles.put(VERBATIM, null);
        TEXT_STYLES = Collections.unmodifiableMap(textStyles);
    }

    @Override
    public void parse(ObjectNode textBlock, Deque<Context> contextStack) throws ParseException
    {
        Deque<FormatContext> formatStack = new LinkedList<>();
        for (ObjectNode siblingTextBlock : getConsecutiveTextBlocks(textBlock, contextStack)) {
            // Make sure we don't process this text block again.
            siblingTextBlock.put(SKIP, true);

            JsonNode textStyles = siblingTextBlock.path(STYLES);
            Map<String, String> parameters = new LinkedHashMap<>();
            maybeSetCustomParameters(parameters, textStyles);
            maybeSetStyleParameter(parameters, textStyles);
            List<Format> formats = getFormats(textStyles);
            StringBuilder plainText = new StringBuilder(siblingTextBlock.path(TEXT).asText(""));

            while (!formatStack.isEmpty()) {
                FormatContext newFormat = formatStack.peek().maybeNest(formats, parameters);
                if (newFormat != null) {
                    if (newFormat != formatStack.peek()) {
                        // Open a nested format block.
                        formatStack.push(newFormat);
                        beginFormat(newFormat, contextStack, plainText);
                    }
                    break;
                } else {
                    // Close the current format block.
                    endFormat(formatStack.pop(), contextStack);
                }
            }

            if (formatStack.isEmpty()) {
                formatStack.push(new FormatContext(formats, parameters, formats, parameters));
                beginFormat(formatStack.peek(), contextStack, plainText);
            }

            parsePlainText(plainText.toString(), contextStack);
        }

        while (!formatStack.isEmpty()) {
            endFormat(formatStack.pop(), contextStack);
        }
    }

    @Override
    public void traverse(ObjectNode textBlock, Consumer<ObjectNode> blockConsumer)
    {
        // Text blocks don't have child blocks, so we don't need to traverse them.
        blockConsumer.accept(textBlock);
    }

    private List<ObjectNode> getConsecutiveTextBlocks(ObjectNode startBlock, Deque<Context> contextStack)
    {
        List<ObjectNode> textBlocks = new LinkedList<>();
        textBlocks.add(startBlock);
        int i = contextStack.peek().indexOf(startBlock);
        if (i >= 0) {
            while (++i < contextStack.peek().siblings().size()) {
                JsonNode sibling = contextStack.peek().siblings().get(i);
                if (TEXT.equals(sibling.path(TYPE).asText())) {
                    textBlocks.add((ObjectNode) sibling);
                } else {
                    break;
                }
            }
        }
        return textBlocks;
    }

    private void beginFormat(FormatContext formatContext, Deque<Context> contextStack, StringBuilder plainText)
    {
        List<Format> formats = formatContext.actualOwnFormats();
        for (int i = 0; i < formats.size(); i++) {
            Format format = formats.get(i);
            Map<String, String> parameters = i == 0 ? formatContext.ownParameters() : Listener.EMPTY_PARAMETERS;
            if (format == null) {
                contextStack.peek().listener().onVerbatim(plainText.toString(), true, parameters);
                plainText.setLength(0);
            } else {
                contextStack.peek().listener().beginFormat(formats.get(i), parameters);
            }
        }
    }

    private void endFormat(FormatContext formatContext, Deque<Context> contextStack)
    {
        List<Format> formats = formatContext.actualOwnFormats();
        for (int i = formats.size() - 1; i >= 0; i--) {
            Format format = formats.get(i);
            if (format != null) {
                contextStack.peek().listener().endFormat(format,
                    i == 0 ? formatContext.ownParameters() : Listener.EMPTY_PARAMETERS);
            }
        }
    }

    private List<Format> getFormats(JsonNode textStyles)
    {
        List<Format> formats = new LinkedList<>();
        for (Map.Entry<String, Format> entry : TEXT_STYLES.entrySet()) {
            JsonNode styleValue = textStyles.path(entry.getKey());
            if (styleValue.isBoolean() && styleValue.asBoolean()) {
                formats.add(entry.getValue());
            }
        }
        return formats;
    }
}

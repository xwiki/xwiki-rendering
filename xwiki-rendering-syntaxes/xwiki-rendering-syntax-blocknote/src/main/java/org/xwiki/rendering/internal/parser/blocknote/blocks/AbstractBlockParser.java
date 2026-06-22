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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.jspecify.annotations.NonNull;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.internal.parser.blocknote.PlainTextWrappingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.TEXT;

/**
 * Base class for BlockNote block parsers.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public abstract class AbstractBlockParser implements BlockParser, Disposable
{
    @FunctionalInterface
    private interface BlockVisitor
    {
        Optional<Consumer<Deque<Context>>> visit(ObjectNode block, Deque<Context> contextStack) throws ParseException;
    }

    /**
     * The property used to denote the type of a BlockNote block.
     */
    public static final String TYPE = "type";

    /**
     * The key used to store the block content. This is normally an array of inline blocks.
     */
    public static final String CONTENT = "content";

    /**
     * The key used to store the nested blocks. This is normally an array of block-level blocks.
     */
    public static final String CHILDREN = "children";

    /**
     * The key used to store the block properties.
     */
    public static final String PROPS = "props";

    /**
     * Mapping between BlockNote block style properties and CSS properties.
     */
    public static final Map<String, String> BLOCK_STYLES;

    /**
     * The property used to denote the text alignment within a block.
     */
    public static final String TEXT_ALIGNMENT = "textAlignment";

    /**
     * The property used to store the block custom parameters, that are not editable through the BlockNote editor.
     */
    public static final String PARAMETERS = "xwiki:parameters";

    /**
     * The parameter used to store the block style, that is a CSS declaration string.
     */
    public static final String STYLE = "style";

    /**
     * The property used to mark a block as already processed.
     */
    protected static final String SKIP = "skip";

    static {
        Map<String, String> blockStyles = new LinkedHashMap<>();
        blockStyles.put("textColor", "color");
        blockStyles.put("backgroundColor", "background-color");
        blockStyles.put(TEXT_ALIGNMENT, "text-align");
        BLOCK_STYLES = Collections.unmodifiableMap(blockStyles);
    }

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    @Named("plain/1.0")
    private StreamParser plainTextStreamParser;

    private ThreadLocal<@NonNull ObjectMapper> objectMapper = ThreadLocal.withInitial(ObjectMapper::new);

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        this.objectMapper.remove();
    }

    protected void visitChildBlocks(ObjectNode parentBlock, String childBlocksKey, Deque<Context> contextStack)
        throws ParseException
    {
        visitChildBlocks(parentBlock, childBlocksKey, contextStack, this::visitBlock);
    }

    private void visitChildBlocks(ObjectNode parentBlock, String childBlocksKey, Deque<Context> contextStack,
        BlockVisitor visitor) throws ParseException
    {
        ArrayNode childBlocks = getChildBlocks(parentBlock, childBlocksKey);
        contextStack.push(contextStack.peek().withParentAndSiblings(parentBlock, childBlocks));
        List<Optional<Consumer<Deque<Context>>>> parentBlockEndCallbacks = new ArrayList<>();
        for (JsonNode childBlock : childBlocks) {
            if (!childBlock.isObject()) {
                throw new ParseException("Each BlockNote block must be a JSON object.");
            }
            parentBlockEndCallbacks.add(visitor.visit((ObjectNode) childBlock, contextStack));
        }
        parentBlockEndCallbacks.stream().filter(Optional::isPresent).map(Optional::get)
            .forEach(callback -> callback.accept(contextStack));
        contextStack.pop();
    }

    protected ArrayNode getChildBlocks(ObjectNode parentBlock, String childBlocksKey) throws ParseException
    {
        JsonNode childBlocks = parentBlock.path(childBlocksKey);
        if (childBlocks.isMissingNode() || childBlocks.isNull()) {
            childBlocks = createArrayBlock();
        } else if (childBlocks.isTextual()) {
            childBlocks = createArrayBlock().add(childBlocks);
        } else if (!childBlocks.isArray()) {
            throw new ParseException("The '" + childBlocksKey + "' array is missing.");
        }
        ArrayNode childBlocksArray = (ArrayNode) childBlocks;
        for (int i = 0; i < childBlocksArray.size(); i++) {
            JsonNode childBlock = childBlocksArray.get(i);
            if (childBlock.isTextual()) {
                childBlocksArray.set(i, createTextBlock(childBlock.asText()));
            }
        }
        return childBlocksArray;
    }

    protected void visitInlineChildBlocks(ObjectNode parent, String childBlocksKey, Deque<Context> contextStack)
        throws ParseException
    {
        contextStack.push(contextStack.peek().withInline(true));
        visitChildBlocks(parent, childBlocksKey, contextStack);
        contextStack.pop();
    }

    protected Optional<Consumer<Deque<Context>>> visitBlock(ObjectNode block, Deque<Context> contextStack)
        throws ParseException
    {
        if (block.path(SKIP).asBoolean()) {
            return Optional.empty();
        } else {
            BlockParser blockParser = getBlockParser(getBlockType(block));
            blockParser.parse(block, contextStack);
            return Optional.of(blockParser::onParentBlockEnd);
        }
    }

    private String getBlockType(ObjectNode block) throws ParseException
    {
        JsonNode typeNode = block.path(TYPE);
        if (!typeNode.isTextual()) {
            throw new ParseException("Each BlockNote block must have a textual 'type' property.");
        }
        return typeNode.asText();
    }

    private BlockParser getBlockParser(String blockType) throws ParseException
    {
        try {
            return this.componentManagerProvider.get().getInstance(BlockParser.class, blockType);
        } catch (Exception e) {
            throw new ParseException("Failed to get the parser for BlockNote block type: " + blockType, e);
        }
    }

    private ObjectNode createTextBlock(String text)
    {
        ObjectNode textBlock = getObjectMapper().createObjectNode();
        textBlock.put(TYPE, TEXT);
        textBlock.put(TEXT, text);
        return textBlock;
    }

    private ArrayNode createArrayBlock()
    {
        return getObjectMapper().createArrayNode();
    }

    protected ObjectMapper getObjectMapper()
    {

        return this.objectMapper.get();
    }

    protected Map<String, String> getBlockParameters(JsonNode block)
    {
        Map<String, String> parameters = new LinkedHashMap<>();
        JsonNode properties = block.path(PROPS);
        maybeSetCustomParameters(parameters, properties);
        maybeSetStyleParameter(parameters, properties, getBlockStyles());
        return parameters;
    }

    protected void maybeSetCustomParameters(Map<String, String> parameters, JsonNode properties)
    {
        JsonNode customParameters = properties.path(PARAMETERS);
        if (customParameters.isObject()) {
            customParameters.forEachEntry((key, value) -> parameters.put(key, value.asText()));
        }
    }

    protected void maybeSetStyleParameter(Map<String, String> parameters, JsonNode styleNode)
    {
        maybeSetStyleParameter(parameters, styleNode, getBlockStyles());
    }

    protected void maybeSetStyleParameter(Map<String, String> parameters, JsonNode styleNode,
        Map<String, String> styleMappings)
    {
        if (styleNode.isObject()) {
            String value = toCSSDeclarations((ObjectNode) styleNode, styleMappings);
            if (!value.isEmpty()) {
                if (parameters.containsKey(STYLE)) {
                    parameters.put(STYLE, parameters.get(STYLE) + ';' + value);
                } else {
                    parameters.put(STYLE, value);
                }
            }
        }
    }

    private String toCSSDeclarations(ObjectNode blockStyles, Map<String, String> styleMappings)
    {
        List<String> declarations = new LinkedList<>();

        for (Map.Entry<String, String> entry : styleMappings.entrySet()) {
            JsonNode styleValue = blockStyles.path(entry.getKey());
            if (styleValue.isTextual()) {
                declarations.add(entry.getValue() + ":" + styleValue.asText());
            }
        }

        return String.join(";", declarations);
    }

    protected Map<String, String> getBlockStyles()
    {
        return BLOCK_STYLES;
    }

    @Override
    public void traverse(ObjectNode block, Consumer<ObjectNode> blockConsumer) throws ParseException
    {
        traverse(block, CONTENT, blockConsumer);
    }

    protected void traverse(ObjectNode parentBlock, String childBlocksKey, Consumer<ObjectNode> blockConsumer)
        throws ParseException
    {
        blockConsumer.accept(parentBlock);
        ArrayNode childBlocks = getChildBlocks(parentBlock, childBlocksKey);
        for (JsonNode childBlock : childBlocks) {
            if (childBlock.isObject()) {
                ObjectNode childBlockObject = (ObjectNode) childBlock;
                BlockParser blockParser = getBlockParser(getBlockType(childBlockObject));
                blockParser.traverse(childBlockObject, blockConsumer);
            }
        }
    }

    protected String getTextContent(ObjectNode block) throws ParseException
    {
        StringBuilder textContent = new StringBuilder();
        traverse(block, descendantBlock -> {
            if (TextBlockParser.TEXT.equals(descendantBlock.path(TYPE).asText())) {
                textContent.append(descendantBlock.path(TextBlockParser.TEXT).asText(""));
            }
        });
        return textContent.toString();
    }

    protected void parsePlainText(String text, Deque<Context> contextStack) throws ParseException
    {
        // Parse the text using the Plain Text parser, ignoring the document and paragraph begin/end events.
        this.plainTextStreamParser.parse(new StringReader(text),
            new PlainTextWrappingListener(contextStack.peek().listener()));
    }

    protected ResourceReference asResourceReference(JsonNode jsonNode)
    {
        if (jsonNode.isObject()) {
            return getObjectMapper().convertValue(jsonNode, ResourceReference.class);
        } else if (jsonNode.isTextual()) {
            ResourceReference linkTarget = new ResourceReference(jsonNode.asText(), ResourceType.URL);
            linkTarget.setTyped(false);
            return linkTarget;
        } else {
            return new ResourceReference(jsonNode.asText(""), ResourceType.UNKNOWN);
        }
    }
}

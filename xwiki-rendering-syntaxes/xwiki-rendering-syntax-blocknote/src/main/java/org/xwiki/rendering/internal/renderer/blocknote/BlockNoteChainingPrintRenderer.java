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

import java.io.StringReader;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.internal.blocknote.BlockNoteObjectMapper;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.BLOCK_STYLES;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CHILDREN;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CONTENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PARAMETERS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.STYLE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.TEXT_ALIGNMENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.TYPE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.RootBlockParser.ROOT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.STYLES;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.TEXT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.TEXT_STYLES;

/**
 * Used to render the XDOM to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class BlockNoteChainingPrintRenderer extends AbstractChainingPrintRenderer
{
    /**
     * The attribute used to denote whether a block is editable (non-generated content) or not.
     */
    private static final String EDITABLE = "editable";

    /**
     * The mapping between XWiki Rendering text formats and BlockNote text styles. We simply reverse the mapping used by
     * the text block parser.
     */
    private static final Map<Format, String> FORMAT_TO_STYLE;

    static {
        Map<Format, String> formatToStyle = new LinkedHashMap<>();
        for (Map.Entry<String, Format> entry : TEXT_STYLES.entrySet()) {
            formatToStyle.put(entry.getValue(), entry.getKey());
        }
        FORMAT_TO_STYLE = Collections.unmodifiableMap(formatToStyle);
    }

    private final Deque<JsonNode> blockNotePath = new LinkedList<>();

    private final ObjectMapper objectMapper = BlockNoteObjectMapper.create();

    private final Context context;

    /**
     * The object used to parse the style attribute. Explicitly specify the parser to use, since otherwise cssparser
     * overrides the default parser used in the JVM, breaking css4j.
     */
    private final CSSOMParser cssParser = new CSSOMParser(new SACParserCSS3());

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public BlockNoteChainingPrintRenderer(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    //
    // Events
    //

    @Override
    public void beginDocument(MetaData metadata)
    {
        beginBlock(ROOT, false, false, true, false);
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        JsonNode root = endBlock();
        try {
            getPrinter()
                .print(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root.get(CHILDREN)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize the BlockNote tree as JSON.", e);
        }
    }

    //
    // Utility methods to build the BlockNote JSON tree
    //

    /**
     * Begins a block of the specified type, with the specified characteristics, and adds it to the current block note
     * path.
     * 
     * @param type the block type
     * @param hasProperties {@code true} if the block has properties, or {@code false} otherwise
     * @param hasContent {@code true} if the block has (inline) content, or {@code false} otherwise
     * @param hasChildren {@code true} if the block has (indented) children, or {@code false} otherwise
     * @param endInlineContent {@code true} if we need to switch from (inline) content to (block-level) children, or
     *            {@code false} otherwise; this is needed for blocks, like list items, that support both (inline)
     *            content and (indented, block-level) children
     * @return the block that was created and added to the block note path, or {@code null} if plain text rendering is
     *         active
     */
    public ObjectNode beginBlock(String type, boolean hasProperties, boolean hasContent, boolean hasChildren,
        boolean endInlineContent)
    {
        return beginBlock(type, hasProperties, hasContent ? this.objectMapper::createArrayNode : null, hasChildren,
            endInlineContent);
    }

    /**
     * Begins a block of the specified type, with the specified characteristics, and adds it to the current block note
     * path.
     * 
     * @param type the block type
     * @param styleMapping the mapping between CSS styles and block properties for this block type
     * @param hasContent {@code true} if the block has (inline) content, or {@code false} otherwise
     * @param hasChildren {@code true} if the block has (indented) children, or {@code false} otherwise
     * @param endInlineContent {@code true} if we need to switch from (inline) content to (block-level) children, or
     *            {@code false} otherwise; this is needed for blocks, like list items, that support both (inline)
     *            content and (indented, block-level) children
     * @return the block that was created and added to the block note path, or {@code null} if plain text rendering is
     *         active
     */
    public ObjectNode beginBlock(String type, Map<String, String> styleMapping, boolean hasContent, boolean hasChildren,
        boolean endInlineContent)
    {
        return beginBlock(type, styleMapping, hasContent ? this.objectMapper::createArrayNode : null, hasChildren,
            endInlineContent);
    }

    /**
     * Begins a block of the specified type, with the specified characteristics, and adds it to the current block note
     * path.
     * 
     * @param type the block type
     * @param hasProperties {@code true} if the block has properties, or {@code false} otherwise
     * @param contentSupplier allows customizing how the content node is created (most of the time the content is an
     *            array node, but in some cases we need an object node)
     * @param hasChildren {@code true} if the block has (indented) children, or {@code false} otherwise
     * @param endInlineContent {@code true} if we need to switch from (inline) content to (block-level) children, or
     *            {@code false} otherwise; this is needed for blocks, like list items, that support both (inline)
     *            content and (indented, block-level) children
     * @return the block that was created and added to the block note path, or {@code null} if plain text rendering is
     *         active
     */
    public ObjectNode beginBlock(String type, boolean hasProperties, Supplier<JsonNode> contentSupplier,
        boolean hasChildren, boolean endInlineContent)
    {
        return beginBlock(type, hasProperties ? BLOCK_STYLES : null, contentSupplier, hasChildren, endInlineContent);
    }

    /**
     * Begins a block of the specified type, with the specified characteristics, and adds it to the current block note
     * path.
     * 
     * @param type the block type
     * @param styleMapping the mapping between CSS styles and block properties for this block type
     * @param contentSupplier allows customizing how the content node is created (most of the time the content is an
     *            array node, but in some cases we need an object node)
     * @param hasChildren {@code true} if the block has (indented) children, or {@code false} otherwise
     * @param endInlineContent {@code true} if we need to switch from (inline) content to (block-level) children, or
     *            {@code false} otherwise; this is needed for blocks, like list items, that support both (inline)
     *            content and (indented, block-level) children
     * @return the block that was created and added to the block note path, or {@code null} if plain text rendering is
     *         active
     */
    public ObjectNode beginBlock(String type, Map<String, String> styleMapping, Supplier<JsonNode> contentSupplier,
        boolean hasChildren, boolean endInlineContent)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return null;
        }

        ObjectNode block = addBlock(type, endInlineContent);
        this.blockNotePath.push(block);

        if (styleMapping != null) {
            block.set(PROPS, getBlockProperties(this.context.getParameters(), styleMapping));
        }

        if (contentSupplier != null) {
            JsonNode content = contentSupplier.get();
            block.set(CONTENT, content);
            this.blockNotePath.push(content);
        }

        if (hasChildren) {
            ArrayNode children = this.objectMapper.createArrayNode();
            block.set(CHILDREN, children);
            if (contentSupplier == null) {
                this.blockNotePath.push(children);
            }
        }

        return block;
    }

    private ObjectNode addBlock(String type, boolean endInlineContent)
    {
        if (!TEXT.equals(type)) {
            endTextBlock();
            if (endInlineContent) {
                this.endInlineContent();
            }
        }

        ObjectNode block = this.objectMapper.createObjectNode();
        block.put(TYPE, type);

        JsonNode siblings = this.blockNotePath.peek();
        if (siblings != null) {
            if (siblings.isArray()) {
                ((ArrayNode) siblings).add(block);
            } else {
                throw new IllegalStateException("Unexpected %s start.".formatted(type));
            }
        }

        return block;
    }

    /**
     * Ends the current text block if there is one, by adding a text block with the plain text collected so far and
     * merging the active formats. This is normally called before starting a new child block.
     */
    public void endTextBlock()
    {
        JsonNode siblings = this.blockNotePath.peek();
        if (siblings != null && siblings.isArray()) {
            maybeAddTextBlock(false);
        }
    }

    private void endInlineContent()
    {
        if (this.blockNotePath.size() > 1) {
            JsonNode siblings = this.blockNotePath.pop();
            JsonNode parent = this.blockNotePath.peek();
            JsonNode children = parent.path(CHILDREN);
            if (children.isArray()) {
                // Start collecting blocks in the children array.
                this.blockNotePath.push(children);
                // Simplify the content array (siblings) if it has a single textual node.
                maybeSimplifyContentArray(parent, (ArrayNode) siblings);
            } else {
                // Put back the siblings.
                this.blockNotePath.push(siblings);
            }
        }
    }

    private ObjectNode getBlockProperties(Map<String, String> parameters, Map<String, String> styleMapping)
    {
        ObjectNode properties = this.objectMapper.createObjectNode();
        String unknownStyles = "";
        try {
            CSSStyleDeclaration cssStyleDeclaration = this.cssParser
                .parseStyleDeclaration(new InputSource(new StringReader(parameters.getOrDefault(STYLE, ""))));
            for (Map.Entry<String, String> entry : styleMapping.entrySet()) {
                String value = cssStyleDeclaration.getPropertyValue(entry.getValue());
                if (StringUtils.isNotEmpty(value)) {
                    properties.put(entry.getKey(), value);
                    cssStyleDeclaration.removeProperty(entry.getValue());
                }
            }
            unknownStyles = cssStyleDeclaration.getCssText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the style parameter.", e);
        }

        ObjectNode unknownParameters = this.objectMapper.valueToTree(parameters);
        if (StringUtils.isEmpty(unknownStyles)) {
            unknownParameters.remove(STYLE);
        } else {
            unknownParameters.put(STYLE, unknownStyles);
        }
        properties.set(PARAMETERS, unknownParameters);

        return properties;
    }

    /**
     * Adds a text block with the plain text collected so far, merging the active formats.
     *
     * @param currentContainerBlockEnded {@code true} if the current container block just ended, or {@code false}
     *            otherwise. This is used to decide whether to add the text block even if empty, in case there is active
     *            formatting or block parameters
     */
    public void maybeAddTextBlock(boolean currentContainerBlockEnded)
    {
        Map<String, String> mergedParameters = new LinkedHashMap<>();
        List<@NonNull Block> formatBlocks = getFormats();
        for (Block formatBlock : formatBlocks) {
            mergedParameters.putAll(formatBlock.getParameters());
        }

        StringBuilder text = this.context.getTextState().getPlainText();
        if (!text.isEmpty() || isEmptyTextBlockNeeded(currentContainerBlockEnded, mergedParameters)) {
            ObjectNode styles = getTextStyles(mergedParameters, formatBlocks);
            JsonNode unknownParameters = styles.path(PARAMETERS);
            if (unknownParameters.size() == 0) {
                styles.remove(PARAMETERS);
            }

            if (styles.isEmpty()) {
                ((ArrayNode) this.blockNotePath.peek()).add(text.toString());
            } else {
                ObjectNode textBlock = addBlock(TEXT, false);
                textBlock.set(STYLES, styles);
                textBlock.put(TEXT, text.toString());
            }

            text.setLength(0);
        }
    }

    private ObjectNode getTextStyles(Map<String, String> mergedParameters, List<@NonNull Block> formatBlocks)
    {
        Map<String, String> styleMapping = new LinkedHashMap<>(BLOCK_STYLES);
        styleMapping.remove(TEXT_ALIGNMENT);
        ObjectNode styles = getBlockProperties(mergedParameters, styleMapping);
        for (Block block : formatBlocks) {
            Format format = block instanceof FormatBlock formatBlock ? formatBlock.getFormat() : null;
            String styleName = FORMAT_TO_STYLE.get(format);
            if (styleName != null) {
                styles.put(styleName, true);
            }
        }
        return styles;
    }

    private boolean isEmptyTextBlockNeeded(boolean currentContainerBlockEnded, Map<String, String> parameters)
    {
        // If we are at the end of an empty block and there are active formats or parameters.
        return currentContainerBlockEnded && this.context.isCurrentContainerBlockEmpty()
            && !(parameters.isEmpty() && this.context.getXDOMPath().stream().noneMatch(FormatBlock.class::isInstance));
    }

    private List<@NonNull Block> getFormats()
    {
        markEditableBlocksFromXDOMPath();
        Deque<Block> xdomPath = this.context.getXDOMPath();
        if (isEditableBlock(xdomPath.peek())) {
            // Return only the editable formats (from non-generated content).
            return xdomPath.stream().filter(this::isFormatBlock).filter(this::isEditableBlock).filter(Objects::nonNull)
                .toList();
        } else {
            // Return only the non-editable formats from the inner-most macro output (generated content).
            return xdomPath.stream().takeWhile(block -> !(block instanceof MacroMarkerBlock))
                .filter(this::isFormatBlock).filter(Objects::nonNull).toList();
        }
    }

    private boolean isFormatBlock(Block block)
    {
        return block instanceof FormatBlock || block instanceof VerbatimBlock;
    }

    private void markEditableBlocksFromXDOMPath()
    {
        boolean editable = true;
        // Iterate the XDOM path in reverse order, from the root to the current block, marking those that are editable
        // (non-generated content).
        var pathIterator = this.context.getXDOMPath().descendingIterator();
        while (pathIterator.hasNext()) {
            Block block = pathIterator.next();
            block.setAttribute(EDITABLE, editable);
            if (block instanceof MacroMarkerBlock) {
                // We enter the macro output, which is read-only.
                editable = false;
            } else if (block instanceof MetaDataBlock metaDataBlock
                && metaDataBlock.getMetaData().contains(MetaData.NON_GENERATED_CONTENT)) {
                // We enter a nested editable area inside the macro output.
                editable = true;
            }
        }
    }

    private boolean isEditableBlock(Block block)
    {
        return block != null && Boolean.TRUE.equals(block.getAttribute(EDITABLE));
    }

    /**
     * Ends the current block and returns it. The block note path is updated accordingly (the block that ended, and its
     * content / children are popped).
     * 
     * @return the block that ended, or {@code null} if the rendering of content as plain text is active
     */
    public JsonNode endBlock()
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return null;
        }

        JsonNode children = this.blockNotePath.peek();
        if (children != null && children.isArray()) {
            maybeAddTextBlock(true);
            // Pop the children array.
            this.blockNotePath.pop();
            // Simplify the children array if it is mapped to the content property and it has a single textual node.
            maybeSimplifyContentArray(this.blockNotePath.peek(), (ArrayNode) children);
        }

        // Pop the block itself.
        JsonNode block = this.blockNotePath.pop();

        // Remove the property that stores the unknown parameters if there are no unknown parameters.
        JsonNode blockProperties = block.path(PROPS);
        JsonNode blockUnknownParameters = blockProperties.path(PARAMETERS);
        if (blockUnknownParameters.isObject() && blockUnknownParameters.size() == 0) {
            ((ObjectNode) blockProperties).remove(PARAMETERS);
        }

        return block;
    }

    private void maybeSimplifyContentArray(JsonNode parent, ArrayNode content)
    {
        if (parent != null && content.equals(parent.get(CONTENT)) && content.size() == 1
            && content.get(0).isTextual()) {
            ((ObjectNode) parent).put(CONTENT, content.get(0).asText());
        }
    }

    /**
     * @return the current path in the BlockNote tree, with the closest ancestor at the top of the stack and the root at
     *         the bottom of the stack
     */
    public Deque<JsonNode> getBlockNotePath()
    {
        return this.blockNotePath;
    }

    /**
     * @return the object mapper used to build the BlockNote JSON tree
     */
    public ObjectMapper getObjectMapper()
    {
        return this.objectMapper;
    }

    /**
     * Converts the given resource reference to a JSON object node.
     * 
     * @param reference the resource reference to convert
     * @return the JSON object node representing the given resource reference
     */
    public ObjectNode toJSON(ResourceReference reference)
    {
        ObjectNode objectNode = this.objectMapper.valueToTree(reference);
        if (reference.getBaseReferences().isEmpty()) {
            // ResourceReference initializes "baseReferences" with null but its getter returns an empty list. We remove
            // the empty "baseReferences" property in order to match the automatic XML serialization of
            // ResourceReference from CTS tests.
            objectNode.remove("baseReferences");
        }
        return objectNode;
    }
}

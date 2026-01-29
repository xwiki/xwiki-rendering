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
package org.xwiki.rendering.internal.renderer.uniast;

import java.io.StringReader;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.xwiki.rendering.internal.parser.uniast.UniAstStreamParser;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.reference.ResourceReferenceTypeSerializer;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

/**
 * Used to render the XDOM to UniAst JSON format.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
public class UniAstChainingRenderer extends AbstractChainingPrintRenderer
{
    private static final Map<String, String> IMAGE_ALIGNMENT = Map.of("start", "left", "end", "right");

    private static final Map<Format, String> TEXT_STYLES;

    private static final List<ResourceType> INTERNAL_RESOURCE_TYPES =
        List.of(ResourceType.ATTACHMENT, ResourceType.DOCUMENT, ResourceType.ICON, ResourceType.PAGE,
            ResourceType.PAGE_ATTACHMENT, ResourceType.SPACE, ResourceType.USER);

    private static final String HEADER_CELL = "headerCell";

    private static final String HEIGHT = "height";

    private static final String ROWS = "rows";

    private static final String COL_SPAN = "colSpan";

    private static final String ROW_SPAN = "rowSpan";

    static {
        Map<Format, String> textStyles = new LinkedHashMap<>();
        textStyles.put(Format.BOLD, "bold");
        textStyles.put(Format.ITALIC, "italic");
        textStyles.put(Format.UNDERLINED, "underline");
        textStyles.put(Format.STRIKEDOUT, "strikethrough");
        textStyles.put(Format.SUPERSCRIPT, "superscript");
        textStyles.put(Format.SUBSCRIPT, "subscript");
        textStyles.put(Format.MONOSPACE, UniAstStreamParser.CODE);
        TEXT_STYLES = Collections.unmodifiableMap(textStyles);
    }

    private ResourceReferenceTypeSerializer resourceReferenceTypeSerializer;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Deque<JsonNode> path = new LinkedList<>();

    private Deque<ListType> listTypes = new LinkedList<>();

    private Map<String, String> figureParameters;

    private String figureCaption;

    private Deque<Map.Entry<Format, Map<String, String>>> formats = new LinkedList<>();

    private StringBuilder plainText = new StringBuilder();

    private int plainTextRenderingNestingLevel;

    /**
     * The object used to parse the style attribute. Explicitly specify the parser to use, since otherwise cssparser
     * overrides the default parser used in the JVM, breaking css4j.
     */
    private final CSSOMParser cssParser = new CSSOMParser(new SACParserCSS3());

    /**
     * Creates a new instance using the provided link label generator and listener chain.
     * 
     * @param resourceReferenceTypeSerializer the resource reference serializer
     * @param listenerChain the listener chain
     */
    public UniAstChainingRenderer(ResourceReferenceTypeSerializer resourceReferenceTypeSerializer,
        ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);

        this.resourceReferenceTypeSerializer = resourceReferenceTypeSerializer;
    }

    // State

    /**
     * @return the {@link BlockStateChainingListener} from the listeners chain
     */
    private BlockStateChainingListener getBlockState()
    {
        return (BlockStateChainingListener) getListenerChain().getListener(BlockStateChainingListener.class);
    }

    /**
     * @return the {@link EmptyBlockChainingListener} from the listeners chain
     */
    private EmptyBlockChainingListener getEmptyBlockState()
    {
        return (EmptyBlockChainingListener) getListenerChain().getListener(EmptyBlockChainingListener.class);
    }

    // Events

    @Override
    public void beginDocument(MetaData metadata)
    {
        beginBlock(null, EMPTY_PARAMETERS, false, "blocks");
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        JsonNode root = endBlock();
        try {
            getPrinter().print(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize the UniAst tree as JSON.", e);
        }
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        beginBlock("paragraph", parameters, true, UniAstStreamParser.CONTENT);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        JsonNode paragraph = endBlock();
        JsonNode children = paragraph.path(UniAstStreamParser.CONTENT);
        if (parameters.isEmpty() && children.size() == 1
            && UniAstStreamParser.IMAGE.equals(children.path(0).path(UniAstStreamParser.TYPE).asText())) {
            // We have a paragraph with no parameters that contains a single image. UniAst supports block-level images,
            // so we can unwrap the image from the paragraph.
            ArrayNode siblings = (ArrayNode) this.path.peek();
            siblings.set(siblings.size() - 1, children.get(0));
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        beginBlock("heading", parameters, true, UniAstStreamParser.CONTENT).put("level", level.getAsInt());
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        beginBlock("break", parameters, false, null);
        endBlock();
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        beginBlock("list", parameters, true, "items");
        this.listTypes.push(type);
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        endBlock();
        this.listTypes.pop();
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        ObjectNode listItem = beginBlock(null, parameters, true, UniAstStreamParser.CONTENT);
        if (listItem != null && this.listTypes.peek() == ListType.NUMBERED) {
            listItem.put("number", 1);
        }
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        ObjectNode listItem = (ObjectNode) endBlock();
        // UniAst, unlike XWiki syntax, doesn't allow inline content directly under list items.
        wrapInlineContent((ArrayNode) listItem.path(UniAstStreamParser.CONTENT));
    }

    private void wrapInlineContent(ArrayNode children)
    {
        ArrayNode siblings = (ArrayNode) this.path.peek();

        beginParagraph(EMPTY_PARAMETERS);
        ArrayNode paragraphContent = (ArrayNode) this.path.pop();
        ObjectNode paragraph = (ObjectNode) this.path.pop();
        siblings.remove(siblings.size() - 1);

        int i = 0;
        while (i < children.size()) {
            JsonNode child = children.get(i);
            if (isInlineBlock(child)) {
                // Add the inline content the current paragraph.
                children.remove(i);
                paragraphContent.add(child);
            } else if (!paragraphContent.isEmpty()) {
                // Close the current paragraph and open a new one.
                children.insert(i, paragraph);

                beginParagraph(EMPTY_PARAMETERS);
                paragraphContent = (ArrayNode) this.path.pop();
                paragraph = (ObjectNode) this.path.pop();
                siblings.remove(siblings.size() - 1);

                i += 2;
            } else {
                i++;
            }
        }

        if (!paragraphContent.isEmpty()) {
            // Add the last paragraph if not empty.
            children.add(paragraph);
        }
    }

    private boolean isInlineBlock(JsonNode block)
    {
        String blockType = block.path(UniAstStreamParser.TYPE).asText();
        return List.of(UniAstStreamParser.TEXT, UniAstStreamParser.LINK, UniAstStreamParser.INLINE_MACRO).contains(
            blockType) || (UniAstStreamParser.IMAGE.equals(blockType) && !block.has(UniAstStreamParser.CAPTION));
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        // UniAst doesn't support definition lists, so we use bulleted lists instead.
        beginList(ListType.BULLETED, parameters);
    }

    @Override
    public void beginDefinitionTerm()
    {
        beginListItem(EMPTY_PARAMETERS);
    }

    @Override
    public void endDefinitionTerm()
    {
        endListItem(EMPTY_PARAMETERS);
    }

    @Override
    public void beginDefinitionDescription()
    {
        beginListItem(EMPTY_PARAMETERS);
    }

    @Override
    public void endDefinitionDescription()
    {
        endListItem(EMPTY_PARAMETERS);
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        endList(ListType.BULLETED, parameters);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        beginBlock("quote", parameters, true, UniAstStreamParser.CONTENT);
    }

    @Override
    public void beginQuotationLine()
    {
        // Nothing to do here.
    }

    @Override
    public void endQuotationLine()
    {
        // UniAst doesn't support the concept of quotation lines, so we add the content directly under the quotation.
        // But UniAst doesn't allow inline content directly under quotation, so we need to wrap the inline content. We
        // do this after each quotation line in order to make sure that inline content from different lines are properly
        // separated and displayed.
        wrapInlineContent((ArrayNode) this.path.peek());
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            addText(content, inline);
        } else if (inline) {
            maybeAddTextBlock(false);
            ObjectNode textBlock = beginTextBlock(parameters);
            textBlock.put(UniAstStreamParser.CONTENT, content);
        } else {
            ObjectNode code = beginBlock(UniAstStreamParser.CODE, parameters, false, null);
            if (parameters.containsKey(UniAstStreamParser.VERBATIM_LANGUAGE)) {
                code.put("language", parameters.get(UniAstStreamParser.VERBATIM_LANGUAGE));
            }
            code.put(UniAstStreamParser.CONTENT, content);
        }
        endBlock();
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        this.figureParameters = parameters;
        this.figureCaption = null;
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        onImage(reference, freestanding, null, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            return;
        }

        Map<String, String> imageParameters = new LinkedHashMap<>();
        if (this.figureParameters != null) {
            imageParameters.putAll(this.figureParameters);
        }
        imageParameters.putAll(parameters);

        // Note that we prepare the style object but we don't fill it (we pass empty parameters) because the image block
        // supports only the alignment style.
        ObjectNode image = beginBlock("image", EMPTY_PARAMETERS, true, null);
        image.set(UniAstStreamParser.TARGET, getLinkTarget(reference));
        if (imageParameters.containsKey(UniAstStreamParser.ALT)) {
            image.put(UniAstStreamParser.ALT, imageParameters.get(UniAstStreamParser.ALT));
        }
        if (imageParameters.containsKey(UniAstStreamParser.WIDTH)) {
            image.put(UniAstStreamParser.WIDTH_PX, Integer.parseInt(imageParameters.get(UniAstStreamParser.WIDTH)));
        }
        if (imageParameters.containsKey(HEIGHT)) {
            image.put("heightPx", Integer.parseInt(imageParameters.get(HEIGHT)));
        }
        if (imageParameters.containsKey(UniAstStreamParser.IMAGE_ALIGNMENT_PARAMETER)) {
            String alignment = imageParameters.get(UniAstStreamParser.IMAGE_ALIGNMENT_PARAMETER);
            alignment = IMAGE_ALIGNMENT.getOrDefault(alignment, alignment);
            ObjectNode styles = (ObjectNode) image.path(UniAstStreamParser.STYLES);
            styles.put("alignment", alignment);
            image.set(UniAstStreamParser.STYLES, styles);
        }

        if (this.figureParameters == null) {
            if (imageParameters.containsKey(UniAstStreamParser.IMAGE_LABEL_PARAMETER)) {
                // Inline image with caption.
                image.put(UniAstStreamParser.CAPTION, imageParameters.get(UniAstStreamParser.IMAGE_LABEL_PARAMETER));
            }

            // No figure caption, so we can add the image right away.
            endBlock();
        }
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        this.figureParameters = null;
        this.figureCaption = null;
        beginPlainTextRendering();
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        this.figureCaption = endPlainTextRendering();
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        if (!isPlainTextRendering()) {
            ObjectNode image = (ObjectNode) this.path.peek();
            image.put(UniAstStreamParser.CAPTION, this.figureCaption != null ? this.figureCaption : "");
        }

        endBlock();

        this.figureParameters = null;
        this.figureCaption = null;
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        // Treat macros that have not been executed as macros without output.
        beginMacroMarker(id, parameters, content, inline);
        endMacroMarker(id, parameters, content, inline);
    }

    @Override
    public void beginMacroMarker(String id, Map<String, String> parameters, String content, boolean inline)
    {
        if (isPlainTextRendering()) {
            addText(content, inline);
        } else {
            ObjectNode macro = beginBlock(inline ? UniAstStreamParser.INLINE_MACRO : UniAstStreamParser.MACRO_BLOCK,
                parameters, false, UniAstStreamParser.OUTPUT);
            ObjectNode macroCall = getMacroCall(id, parameters, content);
            macro.set(UniAstStreamParser.CALL, macroCall);
        }
    }

    private ObjectNode getMacroCall(String id, Map<String, String> parameters, String content)
    {
        ObjectNode macroParams = this.objectMapper.createObjectNode();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            macroParams.put(entry.getKey(), entry.getValue());
        }

        ObjectNode macroBody = this.objectMapper.createObjectNode();
        if (content != null) {
            macroBody.put(UniAstStreamParser.TYPE, UniAstStreamParser.RAW);
            macroBody.put(UniAstStreamParser.CONTENT, content);
        } else {
            macroBody.put(UniAstStreamParser.TYPE, "none");
        }

        ObjectNode macroCall = this.objectMapper.createObjectNode();
        macroCall.put("id", id);
        macroCall.set(UniAstStreamParser.PARAMS, macroParams);
        macroCall.set(UniAstStreamParser.BODY, macroBody);

        return macroCall;
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        ObjectNode macroBlock = (ObjectNode) endBlock();
        if (macroBlock != null) {
            // If the macro has no output, we remove the output property.
            ArrayNode output = (ArrayNode) macroBlock.path(UniAstStreamParser.OUTPUT);
            if (output.isEmpty()) {
                macroBlock.remove(UniAstStreamParser.OUTPUT);
            }
        }
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        if (!isPlainTextRendering() && metadata.contains(MetaData.NON_GENERATED_CONTENT)) {
            ObjectNode macroBlock = getMacroBlock();
            if (macroBlock == null) {
                throw new IllegalStateException("Unexpected non-generated content outside macro markers.");
            }
            ObjectNode editableArea =
                beginBlock(isInlineBlock(macroBlock) ? "inlineMacroEditableArea" : "macroBlockEditableArea",
                    EMPTY_PARAMETERS, false, UniAstStreamParser.CONTENT);
            if (metadata.contains(MetaData.PARAMETER_NAME)) {
                editableArea.put("name", (String) metadata.getMetaData(MetaData.PARAMETER_NAME));
            }
            if (!UniAstStreamParser.BLOCK_LIST.equals(metadata.getMetaData(MetaData.NON_GENERATED_CONTENT))) {
                beginPlainTextRendering();
            }
        }
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        if (!isPlainTextRendering() && metadata.contains(MetaData.NON_GENERATED_CONTENT)) {
            ObjectNode editableArea = (ObjectNode) endBlock();

            Object content = editableArea.remove(UniAstStreamParser.CONTENT);
            if (!UniAstStreamParser.BLOCK_LIST.equals(metadata.getMetaData(MetaData.NON_GENERATED_CONTENT))) {
                content = endPlainTextRendering();
            }

            ObjectNode macroBlock = getMacroBlock();
            if (metadata.contains(MetaData.PARAMETER_NAME)) {
                // Inplace editable macro parameter.
                String parameterName = (String) metadata.getMetaData(MetaData.PARAMETER_NAME);
                setMacroParameter(macroBlock, parameterName, content);
            } else {
                // Inplace editable macro content.
                setMacroContent(macroBlock, content);
            }
        }
    }

    private ObjectNode getMacroBlock()
    {
        for (JsonNode node : this.path) {
            String type = node.path(UniAstStreamParser.TYPE).asText();
            if (List.of(UniAstStreamParser.MACRO_BLOCK, UniAstStreamParser.INLINE_MACRO).contains(type)) {
                return (ObjectNode) node;
            }
        }
        return null;
    }

    private void setMacroParameter(ObjectNode macroBlock, String name, Object value)
    {
        ObjectNode macroCall = (ObjectNode) macroBlock.path(UniAstStreamParser.CALL);
        ObjectNode parameters = (ObjectNode) macroCall.path(UniAstStreamParser.PARAMS);
        if (value instanceof String stringValue) {
            parameters.put(name, stringValue);
        } else {
            parameters.set(name, (JsonNode) value);
        }
    }

    private void setMacroContent(ObjectNode macroBlock, Object value)
    {
        ObjectNode macroCall = (ObjectNode) macroBlock.path(UniAstStreamParser.CALL);
        ObjectNode macroBody = (ObjectNode) macroCall.path(UniAstStreamParser.BODY);
        macroBody.remove(UniAstStreamParser.CONTENT);
        if (value instanceof String stringValue) {
            macroBody.put(UniAstStreamParser.TYPE, UniAstStreamParser.RAW);
            macroBody.put(UniAstStreamParser.CONTENT, stringValue);
        } else if (isInlineBlock(macroBlock)) {
            macroBody.put(UniAstStreamParser.TYPE, UniAstStreamParser.INLINE_CONTENT);
            ArrayNode children = (ArrayNode) value;
            // We have to keep only the first child, as inline content can contain only one block.
            macroBody.set(UniAstStreamParser.INLINE_CONTENT, children.get(0));
        } else {
            macroBody.put(UniAstStreamParser.TYPE, UniAstStreamParser.INLINE_CONTENTS);
            macroBody.set(UniAstStreamParser.INLINE_CONTENTS, (JsonNode) value);
        }
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        if (SyntaxType.HTML_FAMILY_TYPES.contains(syntax.getType())) {
            beginBlock("rawHtml", EMPTY_PARAMETERS, false, null);
            ObjectNode rawHtml = (ObjectNode) endBlock();
            if (rawHtml != null) {
                rawHtml.put("html", text);
            }
        }
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        beginBlock("table", parameters, true, ROWS);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            return;
        }

        ArrayNode row = this.objectMapper.createArrayNode();
        JsonNode rows = this.path.peek();
        if (rows != null && rows.isArray()) {
            ((ArrayNode) rows).add(row);
            this.path.push(row);
        }
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            return;
        } else if (getBlockState().getCellRow() > 0) {
            // UniAst allows header cells only in the first row.
            beginTableCell(parameters);
            return;
        }

        ArrayNode row = (ArrayNode) this.path.peek();
        beginTableCell(parameters);
        JsonNode cellContent = this.path.pop();
        ObjectNode cell = (ObjectNode) this.path.pop();

        ObjectNode column = this.objectMapper.createObjectNode();
        column.set(HEADER_CELL, cell);
        if (parameters.containsKey(UniAstStreamParser.WIDTH)) {
            column.put(UniAstStreamParser.WIDTH_PX, Integer.parseInt(parameters.get(UniAstStreamParser.WIDTH)));
        }
        // Replace the last added cell with the header cell.
        row.set(row.size() - 1, column);

        this.path.push(column);
        this.path.push(cellContent);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            return;
        }

        ObjectNode cell = beginBlock(null, parameters, true, UniAstStreamParser.CONTENT);
        if (parameters.containsKey(COL_SPAN.toLowerCase())) {
            cell.put(COL_SPAN, Integer.parseInt(parameters.get(COL_SPAN.toLowerCase())));
        }
        if (parameters.containsKey(ROW_SPAN.toLowerCase())) {
            cell.put(ROW_SPAN, Integer.parseInt(parameters.get(ROW_SPAN.toLowerCase())));
        }
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        if (!isPlainTextRendering()) {
            this.path.pop();
        }
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            return;
        }

        ObjectNode table = (ObjectNode) endBlock();
        ArrayNode columns = this.objectMapper.createArrayNode();
        table.set("columns", columns);
        ArrayNode rows = (ArrayNode) table.path(ROWS);
        if (!rows.isEmpty()) {
            // Extract the columns information from the first row.
            extractColumnsFromFirstRow(columns, rows);
        }
    }

    private void extractColumnsFromFirstRow(ArrayNode columns, ArrayNode rows)
    {
        ArrayNode firstRow = (ArrayNode) rows.get(0);
        boolean hasHeaderCells =
            StreamSupport.stream(firstRow.spliterator(), false).anyMatch(cell -> cell.has(HEADER_CELL));
        if (hasHeaderCells) {
            // Convert the first table row to columns.
            rows.remove(0);
            for (JsonNode cell : firstRow) {
                if (cell.has(HEADER_CELL)) {
                    columns.add(cell);
                } else {
                    // Convert to header cell.
                    ObjectNode column = this.objectMapper.createObjectNode();
                    column.set(HEADER_CELL, cell);
                    columns.add(column);
                }
            }
        } else {
            // No header cells, just create empty columns.
            int columnCount = StreamSupport.stream(firstRow.spliterator(), false).mapToInt(cell -> {
                if (cell.has(COL_SPAN)) {
                    return cell.get(COL_SPAN).asInt();
                } else {
                    return 1;
                }
            }).reduce(0, Integer::sum);
            for (int i = 0; i < columnCount; i++) {
                columns.add(this.objectMapper.createObjectNode());
            }
        }
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (!isPlainTextRendering()) {
            ObjectNode link = beginBlock(UniAstStreamParser.LINK, parameters, false, UniAstStreamParser.CONTENT);
            link.set(UniAstStreamParser.TARGET, getLinkTarget(reference));
        }
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        endBlock();
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        if (!isPlainTextRendering()) {
            maybeAddTextBlock(false);
            this.formats.push(Map.entry(format, parameters));
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        if (!isPlainTextRendering()) {
            maybeAddTextBlock(true);
            this.formats.pop();
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        // Generate empty paragraphs.
        for (int i = 0; i < count; i++) {
            beginParagraph(EMPTY_PARAMETERS);
            endParagraph(EMPTY_PARAMETERS);
        }
    }

    @Override
    public void onNewLine()
    {
        this.plainText.append('\n');
    }

    @Override
    public void onSpace()
    {
        this.plainText.append(' ');
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.plainText.append(symbol);
    }

    @Override
    public void onWord(String word)
    {
        this.plainText.append(word);
    }

    private ObjectNode beginBlock(String type, Map<String, String> parameters, boolean hasStyles, String childBlocksKey)
    {
        if (isPlainTextRendering()) {
            return null;
        }

        ObjectNode block = this.objectMapper.createObjectNode();
        if (type != null) {
            block.put(UniAstStreamParser.TYPE, type);
        }
        if (hasStyles) {
            block.set(UniAstStreamParser.STYLES, getBlockStyles(parameters));
        }

        JsonNode siblings = this.path.peek();
        if (siblings != null) {
            if (siblings.isArray()) {
                if (!UniAstStreamParser.TEXT.equals(type)) {
                    maybeAddTextBlock(false);
                }

                ((ArrayNode) siblings).add(block);
            } else {
                throw new IllegalStateException("Unexpected %s start.".formatted(type));
            }
        }
        this.path.push(block);

        if (childBlocksKey != null) {
            ArrayNode childBlocks = this.objectMapper.createArrayNode();
            block.set(childBlocksKey, childBlocks);
            this.path.push(childBlocks);
        }

        return block;
    }

    private JsonNode endBlock()
    {
        if (isPlainTextRendering()) {
            return null;
        }

        JsonNode children = this.path.peek();
        if (children != null && children.isArray()) {
            maybeAddTextBlock(true);
            // Pop the children array.
            this.path.pop();
        }
        // Pop the block itself.
        return this.path.pop();
    }

    private ObjectNode getBlockStyles(Map<String, String> parameters)
    {
        ObjectNode styles = this.objectMapper.createObjectNode();
        try {
            CSSStyleDeclaration cssStyleDeclaration = this.cssParser
                .parseStyleDeclaration(new InputSource(new StringReader(parameters.getOrDefault("style", ""))));
            for (Map.Entry<String, String> entry : UniAstStreamParser.BLOCK_STYLES.entrySet()) {
                String value = cssStyleDeclaration.getPropertyValue(entry.getValue());
                if (StringUtils.isNotEmpty(value)) {
                    styles.put(entry.getKey(), value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the style parameter.", e);
        }
        return styles;
    }

    private ObjectNode getLinkTarget(ResourceReference reference)
    {
        ObjectNode linkTarget = this.objectMapper.createObjectNode();
        if (INTERNAL_RESOURCE_TYPES.contains(reference.getType())) {
            linkTarget.put(UniAstStreamParser.TYPE, "internal");
            linkTarget.put("rawReference", this.resourceReferenceTypeSerializer.serialize(reference));
        } else {
            linkTarget.put(UniAstStreamParser.TYPE, "external");
            linkTarget.put("url", this.resourceReferenceTypeSerializer.serialize(reference));
        }
        return linkTarget;
    }

    private void maybeAddTextBlock(boolean currentContainerBlockEnded)
    {
        Map<String, String> mergedParameters = new LinkedHashMap<>();
        for (Map.Entry<Format, Map<String, String>> formatEntry : this.formats) {
            mergedParameters.putAll(formatEntry.getValue());
        }
        if (!this.plainText.isEmpty() || isEmptyTextBlockNeeded(currentContainerBlockEnded, mergedParameters)) {
            ObjectNode textBlock = beginTextBlock(mergedParameters);
            textBlock.put(UniAstStreamParser.CONTENT, this.plainText.toString());
            this.plainText.setLength(0);

            // Set text styles.
            ObjectNode styles = (ObjectNode) textBlock.path(UniAstStreamParser.STYLES);
            for (Map.Entry<Format, Map<String, String>> formatEntry : this.formats) {
                String styleName = TEXT_STYLES.get(formatEntry.getKey());
                if (styleName != null) {
                    styles.put(styleName, true);
                }
            }

            endBlock();
        }
    }

    private boolean isEmptyTextBlockNeeded(boolean currentContainerBlockEnded, Map<String, String> parameters)
    {
        // If we are at the end of an empty block and there are active formats or parameters.
        return currentContainerBlockEnded && getEmptyBlockState().isCurrentContainerBlockEmpty()
            && !(parameters.isEmpty() && this.formats.isEmpty());
    }

    private ObjectNode beginTextBlock(Map<String, String> parameters)
    {
        return beginBlock(UniAstStreamParser.TEXT, parameters, true, null);
    }

    private void addText(String text, boolean inline)
    {
        if (StringUtils.isNotEmpty(text)) {
            if (inline) {
                this.plainText.append(text);
            } else {
                this.plainText.append('\n').append(text).append('\n');
            }
        }
    }

    private void beginPlainTextRendering()
    {
        this.plainTextRenderingNestingLevel++;
    }

    private boolean isPlainTextRendering()
    {
        return this.plainTextRenderingNestingLevel > 0;
    }

    private String endPlainTextRendering()
    {
        this.plainTextRenderingNestingLevel--;
        if (this.plainTextRenderingNestingLevel == 0) {
            String result = this.plainText.toString();
            this.plainText.setLength(0);
            return result;
        }
        return null;
    }
}

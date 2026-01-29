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
package org.xwiki.rendering.internal.parser.uniast;

import java.io.StringReader;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * UniAst parser to convert a JSON source into events.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
@Component
@Named("uniast/1.0")
@Singleton
public class UniAstStreamParser extends AbstractUniAstStreamParser
{
    //
    // Block types
    //

    /**
     * The paragraph block type.
     */
    public static final String PARAGRAPH = "paragraph";

    /**
     * The list block type.
     */
    public static final String LIST = "list";

    /**
     * The quote block type.
     */
    public static final String QUOTE = "quote";

    /**
     * The code block type.
     */
    public static final String CODE = "code";

    /**
     * The image block type.
     */
    public static final String IMAGE = "image";

    /**
     * The text block type.
     */
    public static final String TEXT = "text";

    /**
     * The link block type.
     */
    public static final String LINK = "link";

    /**
     * The inline macro block type.
     */
    public static final String INLINE_MACRO = "inlineMacro";

    /**
     * The macro block type.
     */
    public static final String MACRO_BLOCK = "macroBlock";

    //
    // Block properties
    //

    /**
     * The property used to denote the type of a UniAst block.
     */
    public static final String TYPE = "type";

    /**
     * The key used by some of the blocks to store their child blocks.
     */
    public static final String CONTENT = "content";

    /**
     * The key used by some of the blocks to store their styles.
     */
    public static final String STYLES = "styles";

    /**
     * The target property for links and images.
     */
    public static final String TARGET = "target";

    /**
     * The alternative text property for images.
     */
    public static final String ALT = "alt";

    /**
     * The caption property for image blocks.
     */
    public static final String CAPTION = "caption";

    /**
     * The property holding the width in pixels for images and table columns.
     */
    public static final String WIDTH_PX = "widthPx";

    /**
     * The property of the macro block that contains the macro call.
     */
    public static final String CALL = "call";

    /**
     * The property of the macro call that contains the macro parameters.
     */
    public static final String PARAMS = "params";

    /**
     * The property of the macro call that contains the macro body (content).
     */
    public static final String BODY = "body";

    /**
     * The type of macro body that contains raw content.
     */
    public static final String RAW = "raw";

    /**
     * The type of macro body that contains a single inline content block.
     */
    public static final String INLINE_CONTENT = "inlineContent";

    /**
     * The type of macro body that contains multiple inline content blocks.
     */
    public static final String INLINE_CONTENTS = "inlineContents";

    /**
     * The property used to denote the output of a macro.
     */
    public static final String OUTPUT = "output";

    /**
     * The property used to denote a header cell in a table column.
     */
    public static final String HEADER_CELL = "headerCell";

    /**
     * The property used to mark a block as already processed.
     */
    public static final String SKIP = "skip";

    //
    // XDOM block parameters.
    //

    /**
     * The width parameter for images and table columns.
     */
    public static final String WIDTH = "width";

    /**
     * The parameter used to denote the label (caption) of an inline image.
     */
    public static final String IMAGE_LABEL_PARAMETER = "data-xwiki-image-label";

    /**
     * The parameter used to denote the alignment style of an image.
     */
    public static final String IMAGE_ALIGNMENT_PARAMETER = "data-xwiki-image-style-alignment";

    /**
     * The parameter used on verbatim blocks to store the language of the verbatim content. This is mapped to the
     * language of the code block from UniAst.
     */
    public static final String VERBATIM_LANGUAGE = "data-xwiki-verbatim-language";

    /**
     * The type representing a list of blocks.
     */
    public static final String BLOCK_LIST = "java.util.List<org.xwiki.rendering.block.Block>";

    //
    // Mapping between UniAst and XDOM.
    //

    /**
     * Mapping between UniAst block style properties and CSS properties.
     */
    public static final Map<String, String> BLOCK_STYLES;

    private static final Map<String, Format> TEXT_STYLES;

    private static final Map<String, String> IMAGE_ALIGNMENT = Map.of("left", "start", "right", "end");

    @FunctionalInterface
    private interface BlockVisitor
    {
        void visit(ObjectNode block, Deque<Context> contextStack) throws ParseException;
    }

    static {
        Map<String, String> blockStyles = new LinkedHashMap<>();
        blockStyles.put("textColor", "color");
        blockStyles.put("backgroundColor", "background-color");
        blockStyles.put("textAlignment", "text-align");
        BLOCK_STYLES = Collections.unmodifiableMap(blockStyles);

        Map<String, Format> textStyles = new LinkedHashMap<>();
        textStyles.put("bold", Format.BOLD);
        textStyles.put("italic", Format.ITALIC);
        textStyles.put("underline", Format.UNDERLINED);
        textStyles.put("strikethrough", Format.STRIKEDOUT);
        textStyles.put("superscript", Format.SUPERSCRIPT);
        textStyles.put("subscript", Format.SUBSCRIPT);
        textStyles.put(CODE, Format.MONOSPACE);
        TEXT_STYLES = Collections.unmodifiableMap(textStyles);
    }

    @Inject
    @Named("plain/1.0")
    private StreamParser plainTextStreamParser;

    @Inject
    private UniAstResourceReferenceParser resourceReferenceParser;

    @Inject
    private FragmentRenderer fragmentRenderer;

    protected void visitRoot(ObjectNode root, Deque<Context> contextStack) throws ParseException
    {
        MetaData metadata = new MetaData();
        metadata.addMetaData(MetaData.SYNTAX, getSyntax());
        Context context = contextStack.peek();
        context.listener().beginDocument(metadata);

        visitChildBlocks(root, "blocks", contextStack);

        context.listener().endDocument(metadata);
    }

    private void visitChildBlocks(ObjectNode parent, String childBlocksKey, Deque<Context> contextStack)
        throws ParseException
    {
        visitChildBlocks(parent, childBlocksKey, contextStack, this::visitBlock);
    }

    private void visitChildBlocks(ObjectNode parent, String childBlocksKey, Deque<Context> contextStack,
        BlockVisitor visitor) throws ParseException
    {
        ArrayNode childBlocks = getChildBlocks(parent, childBlocksKey);
        contextStack.push(contextStack.peek().withParentAndSiblings(parent, childBlocks));
        for (JsonNode childBlock : childBlocks) {
            if (!childBlock.isObject()) {
                throw new ParseException("Each UniAst block must be a JSON object.");
            }
            visitor.visit((ObjectNode) childBlock, contextStack);
        }
        endSections(contextStack, HeaderLevel.LEVEL1);
        contextStack.pop();
    }

    private ArrayNode getChildBlocks(ObjectNode parent, String childBlocksKey) throws ParseException
    {
        JsonNode childBlocks = parent.path(childBlocksKey);
        if (!childBlocks.isArray()) {
            throw new ParseException("The '" + childBlocksKey + "' array is missing.");
        }
        return (ArrayNode) childBlocks;
    }

    private void visitInlineChildBlocks(ObjectNode parent, String childBlocksKey, Deque<Context> contextStack)
        throws ParseException
    {
        contextStack.push(contextStack.peek().withInline(true));
        visitChildBlocks(parent, childBlocksKey, contextStack);
        contextStack.pop();
    }

    private void visitBlock(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        JsonNode typeNode = block.path(TYPE);
        if (!typeNode.isTextual()) {
            throw new ParseException("Each UniAst block must have a textual 'type' property.");
        }
        String blockType = typeNode.asText();
        if (!block.path(SKIP).asBoolean() && !maybeVisitStandaloneBlock(blockType, block, contextStack)
            && !maybeVisitInlineBlock(blockType, block, contextStack)) {
            throw new ParseException("Unsupported UniAst block type: " + blockType);
        }
    }

    private boolean maybeVisitStandaloneBlock(String blockType, ObjectNode block, Deque<Context> contextStack)
        throws ParseException
    {
        switch (blockType) {
            case PARAGRAPH:
                visitParagraph(block, contextStack);
                break;
            case "heading":
                visitHeading(block, contextStack);
                break;
            case LIST:
                visitList(block, contextStack);
                break;
            case QUOTE:
                visitQuote(block, contextStack);
                break;
            case CODE:
                visitCode(block, contextStack);
                break;
            case "table":
                visitTable(block, contextStack);
                break;
            case IMAGE:
                visitImage(block, contextStack);
                break;
            case "break":
                contextStack.peek().listener().onHorizontalLine(Listener.EMPTY_PARAMETERS);
                break;
            case MACRO_BLOCK:
                visitMacro(block, contextStack, false);
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean maybeVisitInlineBlock(String blockType, ObjectNode block, Deque<Context> contextStack)
        throws ParseException
    {
        switch (blockType) {
            case TEXT:
                visitText(block, contextStack);
                break;
            case LINK:
                visitLink(block, contextStack);
                break;
            case INLINE_MACRO:
                visitMacro(block, contextStack, true);
                break;
            default:
                return false;
        }
        return true;
    }

    private void visitParagraph(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        if (isEmptyLine(block, contextStack)) {
            contextStack.peek().listener().onEmptyLines(getEmptyLinesCount(block, contextStack));
        } else {
            Map<String, String> parameters = getBlockParameters(block);
            contextStack.peek().listener().beginParagraph(parameters);

            visitInlineChildBlocks(block, CONTENT, contextStack);

            contextStack.peek().listener().endParagraph(parameters);
        }
    }

    private boolean isEmptyLine(JsonNode block, Deque<Context> contextStack)
    {
        JsonNode children = block.path(CONTENT);
        Map<String, String> parameters = getBlockParameters(block);
        // UniAst doesn't support the concept of empty lines so we map empty lines to empty paragraphs. Empty lines are
        // used to separate blocks of content so normally they are not the first or the last child.
        return PARAGRAPH.equals(block.path(TYPE).asText()) && children.isEmpty() && parameters.isEmpty()
            && !contextStack.peek().isFirstOrLastChild(block);
    }

    private int getEmptyLinesCount(ObjectNode emptyLine, Deque<Context> contextStack)
    {
        int count = 1;
        ArrayNode siblings = contextStack.peek().siblings();
        int startIndex = contextStack.peek().indexOf(emptyLine);
        if (startIndex >= 0) {
            while ((startIndex + count) < siblings.size()
                && isEmptyLine(siblings.get(startIndex + count), contextStack)) {
                // Make sure this empty line is not processed again.
                ((ObjectNode) siblings.get(startIndex + count)).put(SKIP, true);
                count++;
            }
        }
        return count;
    }

    private void visitHeading(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        HeaderLevel level = getHeaderLevel(block);
        beginSection(level, contextStack);

        Map<String, String> parameters = getBlockParameters(block);
        Context context = contextStack.peek();
        // Generate the id from the plain text content of the heading.
        String id = context.idGenerator().generateUniqueId("H", getTextContent(block));
        context.listener().beginHeader(level, id, parameters);

        visitInlineChildBlocks(block, CONTENT, contextStack);

        context.listener().endHeader(level, id, parameters);
    }

    private HeaderLevel getHeaderLevel(ObjectNode headingBlock) throws ParseException
    {
        JsonNode level = headingBlock.path("level");
        if (!level.isInt()) {
            throw new ParseException("The 'level' property of a 'heading' block must have an integer value.");
        }
        try {
            return HeaderLevel.parseInt(level.asInt());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid value for the 'level' property of a 'heading' block: " + level.asInt(),
                e);
        }
    }

    private String getTextContent(JsonNode block)
    {
        return block.findParents(TYPE).stream().filter(descendant -> TEXT.equals(descendant.path(TYPE).asText()))
            .map(textBlock -> textBlock.path(CONTENT).asText("")).reduce("", (a, b) -> a + b);
    }

    private void beginSection(HeaderLevel level, Deque<Context> contextStack)
    {
        endSections(contextStack, level);
        contextStack.push(contextStack.peek().withSectionLevel(level));
        contextStack.peek().listener().beginSection(Listener.EMPTY_PARAMETERS);
    }

    private void endSections(Deque<Context> contextStack, HeaderLevel upToLevel)
    {
        while (!contextStack.isEmpty() && contextStack.peek().sectionLevel() != null
            && contextStack.peek().sectionLevel().ordinal() >= upToLevel.ordinal()) {
            Context context = contextStack.pop();
            context.listener().endSection(Listener.EMPTY_PARAMETERS);
        }
    }

    private void visitList(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = getBlockParameters(block);
        ArrayNode items = getChildBlocks(block, "items");
        contextStack.push(contextStack.peek().withParentAndSiblings(block, items));
        Listener listener = contextStack.peek().listener();
        ListType currentListType = null;
        for (JsonNode item : items) {
            if (!item.isObject()) {
                throw new ParseException("Each list item must be a JSON object.");
            }
            JsonNode number = item.path("number");
            ListType listType = number.isNumber() ? ListType.NUMBERED : ListType.BULLETED;
            if (!listType.equals(currentListType)) {
                if (currentListType != null) {
                    listener.endList(currentListType, parameters);
                }
                listener.beginList(listType, parameters);
                currentListType = listType;
            }
            visitListItem((ObjectNode) item, contextStack);
        }
        if (currentListType != null) {
            listener.endList(currentListType, parameters);
        }
        contextStack.pop();
    }

    private void visitListItem(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = getBlockParameters(block);
        contextStack.peek().listener().beginListItem(parameters);

        maybeUnwrapSingleParagraphListItem(block);
        visitChildBlocks(block, CONTENT, contextStack);

        contextStack.peek().listener().endListItem(parameters);
    }

    private void maybeUnwrapSingleParagraphListItem(ObjectNode listItem) throws ParseException
    {
        ArrayNode childBlocks = getChildBlocks(listItem, CONTENT);
        JsonNode firstChild = childBlocks.path(0);
        if (PARAGRAPH.equals(firstChild.path(TYPE).asText()) && (childBlocks.size() == 1
            || (childBlocks.size() == 2 && LIST.equals(childBlocks.path(1).path(TYPE).asText())))) {
            // Replace the paragraph block with its child blocks.
            childBlocks.remove(0);
            ArrayNode paragraphChildren = getChildBlocks((ObjectNode) firstChild, CONTENT);
            for (int i = paragraphChildren.size() - 1; i >= 0; i--) {
                childBlocks.insert(0, paragraphChildren.get(i));
            }
        }
    }

    private void visitQuote(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = getBlockParameters(block);
        contextStack.peek().listener().beginQuotation(parameters);

        final boolean[] inQuotationLine = {false};
        visitChildBlocks(block, CONTENT, contextStack, (childBlock, sameContextStack) -> {
            // Keep nested quote blocks in the same quotation line, to match the XDOM expected by the compatibility
            // tests.
            if (!QUOTE.equals(childBlock.path(TYPE).asText()) || !inQuotationLine[0]) {
                if (inQuotationLine[0]) {
                    // End previous quotation line.
                    sameContextStack.peek().listener().endQuotationLine();
                }

                // Start new quotation line.
                sameContextStack.peek().listener().beginQuotationLine();
                inQuotationLine[0] = true;
            }

            if (PARAGRAPH.equals(childBlock.path(TYPE).asText())) {
                // Unwrap the paragraph block because XWiki syntax expects inline content inside a quotation line.
                visitInlineChildBlocks(childBlock, CONTENT, sameContextStack);
            } else {
                visitBlock(childBlock, sameContextStack);
            }
        });

        if (inQuotationLine[0]) {
            // End last quotation line.
            contextStack.peek().listener().endQuotationLine();
        }

        contextStack.peek().listener().endQuotation(parameters);
    }

    private void visitCode(ObjectNode block, Deque<Context> contextStack)
    {
        Map<String, String> parameters = new LinkedHashMap<>();
        JsonNode language = block.path("language");
        if (language.isTextual()) {
            parameters.put(VERBATIM_LANGUAGE, language.asText());
        }
        String code = block.path(CONTENT).asText("");
        contextStack.peek().listener().onVerbatim(code, false, parameters);
    }

    private void visitTable(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = getBlockParameters(block);
        contextStack.peek().listener().beginTable(parameters);

        ArrayNode columns = getChildBlocks(block, "columns");
        if (!columns.isEmpty() && columns.get(0).path(HEADER_CELL).isObject()) {
            // We have a header row.
            contextStack.push(contextStack.peek().withParentAndSiblings(block, columns));
            visitTableColumns(columns, contextStack);
            contextStack.pop();
        }

        ArrayNode rows = getChildBlocks(block, "rows");
        contextStack.push(contextStack.peek().withParentAndSiblings(block, rows));
        visitTableRows(rows, columns, contextStack);
        contextStack.pop();

        contextStack.peek().listener().endTable(parameters);
    }

    private void visitTableColumns(ArrayNode columns, Deque<Context> contextStack) throws ParseException
    {
        contextStack.peek().listener().beginTableRow(Listener.EMPTY_PARAMETERS);

        for (JsonNode column : columns) {
            if (!column.isObject()) {
                throw new ParseException("Each table column must be a JSON object.");
            }
            visitTableColumn((ObjectNode) column, contextStack);
        }

        contextStack.peek().listener().endTableRow(Listener.EMPTY_PARAMETERS);
    }

    private void visitTableColumn(ObjectNode column, Deque<Context> contextStack) throws ParseException
    {
        ObjectNode cell = (ObjectNode) column.path(HEADER_CELL);
        visitTableCell(cell, column, contextStack);
    }

    private void visitTableRows(ArrayNode rows, ArrayNode columns, Deque<Context> contextStack) throws ParseException
    {
        for (int i = 0; i < rows.size(); i++) {
            JsonNode row = rows.path(i);
            if (!row.isArray()) {
                throw new ParseException("Each table row must be a JSON array.");
            }
            visitTableRow((ArrayNode) row, i, columns, contextStack);
        }
    }

    private void visitTableRow(ArrayNode row, int rowIndex, ArrayNode columns, Deque<Context> contextStack)
        throws ParseException
    {
        contextStack.peek().listener().beginTableRow(Listener.EMPTY_PARAMETERS);

        for (int i = 0; i < row.size(); i++) {
            JsonNode cell = row.get(i);
            if (!cell.isObject()) {
                throw new ParseException("Each table cell must be a JSON object.");
            }
            JsonNode column = columns.path(rowIndex == 0 ? i : -1);
            visitTableCell((ObjectNode) cell, column, contextStack);
        }

        contextStack.peek().listener().endTableRow(Listener.EMPTY_PARAMETERS);
    }

    private void visitTableCell(ObjectNode cell, JsonNode column, Deque<Context> contextStack) throws ParseException
    {
        JsonNode headerCell = column.path(HEADER_CELL);
        Map<String, String> parameters = getTableCellParameters(cell);

        JsonNode widthPx = column.path(WIDTH_PX);
        if (widthPx.isNumber() && (cell == headerCell || headerCell.isMissingNode())) {
            parameters.put(WIDTH, widthPx.asText());
        }

        if (cell == headerCell) {
            contextStack.peek().listener().beginTableHeadCell(parameters);
        } else {
            contextStack.peek().listener().beginTableCell(parameters);
        }

        visitInlineChildBlocks(cell, CONTENT, contextStack);

        if (cell == headerCell) {
            contextStack.peek().listener().endTableHeadCell(parameters);
        } else {
            contextStack.peek().listener().endTableCell(parameters);
        }
    }

    private Map<String, String> getTableCellParameters(ObjectNode cell)
    {
        Map<String, String> parameters = getBlockParameters(cell);
        JsonNode colSpan = cell.path("colSpan");
        if (colSpan.isInt()) {
            parameters.put("colspan", colSpan.asText());
        }
        JsonNode rowSpan = cell.path("rowSpan");
        if (rowSpan.isInt()) {
            parameters.put("rowspan", rowSpan.asText());
        }
        return parameters;
    }

    private void visitImage(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        JsonNode caption = block.path(CAPTION);
        if (caption.isTextual() && !contextStack.peek().inline()) {
            // Avoid storing the caption in the image parameters, as we do for inline images.
            block.remove(CAPTION);
            Map<String, String> figureParameters = Map.of("class", IMAGE);
            Listener listener = contextStack.peek().listener();
            listener.beginFigure(figureParameters);
            contextStack.push(contextStack.peek().withInline(true));
            visitImageWithoutCaption(block, contextStack);
            listener.beginFigureCaption(Listener.EMPTY_PARAMETERS);
            parsePlainText(caption.asText(), contextStack);
            listener.endFigureCaption(Listener.EMPTY_PARAMETERS);
            contextStack.pop();
            listener.endFigure(figureParameters);
        } else {
            visitImageWithoutCaption(block, contextStack);
        }
    }

    private void visitImageWithoutCaption(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        ResourceReference reference = this.resourceReferenceParser.parse(block.path(TARGET));
        String id = contextStack.peek().idGenerator().generateUniqueId("I", reference.getReference());
        Map<String, String> parameters = getImageParameters(block, contextStack);
        boolean inline = contextStack.peek().inline();
        boolean freestanding = parameters.isEmpty() && (!inline || contextStack.peek().siblings().size() == 1);

        if (!inline) {
            // Wrap the image in a paragraph when not inline, because XDOM treats images as inline content.
            contextStack.peek().listener().beginParagraph(Listener.EMPTY_PARAMETERS);
        }

        contextStack.peek().listener().onImage(reference, freestanding, id, parameters);

        if (!inline) {
            contextStack.peek().listener().endParagraph(Listener.EMPTY_PARAMETERS);
        }
    }

    private Map<String, String> getImageParameters(ObjectNode block, Deque<Context> contextStack)
    {
        Map<String, String> parameters = new LinkedHashMap<>();

        JsonNode altText = block.path(ALT);
        if (altText.isTextual()) {
            parameters.put(ALT, altText.asText());
        }

        JsonNode widthPx = block.path(WIDTH_PX);
        if (widthPx.isNumber()) {
            parameters.put(WIDTH, widthPx.asText());
        }

        JsonNode heightPx = block.path("heightPx");
        if (heightPx.isNumber()) {
            parameters.put("height", heightPx.asText());
        }

        JsonNode styles = block.path(STYLES);
        if (styles.isObject()) {
            JsonNode alignment = styles.path("alignment");
            if (alignment.isTextual()) {
                String value = IMAGE_ALIGNMENT.getOrDefault(alignment.asText().toLowerCase(), alignment.asText());
                parameters.put(IMAGE_ALIGNMENT_PARAMETER, value);
            }
        }

        JsonNode caption = block.path(CAPTION);
        if (caption.isTextual() && contextStack.peek().inline()) {
            parameters.put(IMAGE_LABEL_PARAMETER, caption.asText());
        }

        return parameters;
    }

    private void visitMacro(ObjectNode block, Deque<Context> contextStack, boolean inline) throws ParseException
    {
        JsonNode macroCall = block.path(CALL);
        JsonNode macroIdNode = macroCall.path("id");
        if (!macroIdNode.isTextual()) {
            throw new ParseException("The macro id is missing or its value is unexpected.");
        }
        JsonNode body = macroCall.path(BODY);
        if (!body.isObject()) {
            throw new ParseException("The 'body' property of a macro block must be a JSON object.");
        }

        String macroId = macroIdNode.asText();
        Map<String, String> macroParameters = getMacroParameters(macroCall.path(PARAMS), contextStack);
        String macroContent = getMacroContent((ObjectNode) body, contextStack);

        contextStack.peek().listener().onMacro(macroId, macroParameters, macroContent, inline);
    }

    private String getMacroContent(ObjectNode macroBody, Deque<Context> contextStack) throws ParseException
    {
        String contentKey = CONTENT;
        JsonNode bodyType = macroBody.path(TYPE);
        switch (bodyType.asText()) {
            case "none", RAW:
                break;
            case INLINE_CONTENT:
                contentKey = INLINE_CONTENT;
                break;
            case INLINE_CONTENTS:
                contentKey = INLINE_CONTENTS;
                break;
            default:
                throw new ParseException("Unsupported macro body type: " + bodyType.asText());
        }
        return renderFragment(macroBody, contentKey, true, contextStack);
    }

    private String renderFragment(ObjectNode parent, String fragmentKey, boolean inline, Deque<Context> contextStack)
        throws ParseException
    {
        JsonNode fragment = parent.path(fragmentKey);
        if (fragment.isValueNode()) {
            return fragment.asText("");
        } else if (fragment.isObject()) {
            this.fragmentRenderer.beginFragment(contextStack);
            contextStack.push(contextStack.peek().withInline(inline));
            visitBlock((ObjectNode) fragment, contextStack);
            endSections(contextStack, HeaderLevel.LEVEL1);
            contextStack.pop();
            return this.fragmentRenderer.endFragment(contextStack);
        } else if (fragment.isArray()) {
            this.fragmentRenderer.beginFragment(contextStack);
            contextStack.push(contextStack.peek().withInline(inline));
            visitChildBlocks(parent, fragmentKey, contextStack);
            contextStack.pop();
            return this.fragmentRenderer.endFragment(contextStack);
        }
        return null;
    }

    private Map<String, String> getMacroParameters(JsonNode parameters, Deque<Context> contextStack)
        throws ParseException
    {
        if (!parameters.isObject()) {
            throw new ParseException("The 'params' property of a macro block must be a JSON object.");
        }
        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : parameters.properties()) {
            params.put(entry.getKey(), renderFragment((ObjectNode) parameters, entry.getKey(), true, contextStack));
        }
        return params;
    }

    private void visitText(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        Deque<FormatContext> formatStack = new LinkedList<>();
        for (ObjectNode textBlock : getConsecutiveTextBlocks(block, contextStack)) {
            // Make sure we don't process this text block again.
            textBlock.put(SKIP, true);

            Map<String, String> parameters = getBlockParameters(textBlock);
            List<Format> formats = getFormats(textBlock.path(STYLES));

            while (!formatStack.isEmpty()) {
                FormatContext newFormat = formatStack.peek().maybeNest(formats, parameters);
                if (newFormat != null) {
                    if (newFormat != formatStack.peek()) {
                        // Open a nested format block.
                        formatStack.push(newFormat);
                        beginFormat(newFormat, contextStack);
                    }
                    break;
                } else {
                    // Close the current format block.
                    endFormat(formatStack.pop(), contextStack);
                }
            }

            if (formatStack.isEmpty()) {
                formatStack.push(new FormatContext(formats, parameters, formats, parameters));
                beginFormat(formatStack.peek(), contextStack);
            }

            parsePlainText(textBlock.path(CONTENT).asText(""), contextStack);
        }

        while (!formatStack.isEmpty()) {
            endFormat(formatStack.pop(), contextStack);
        }
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

    private void beginFormat(FormatContext formatContext, Deque<Context> contextStack)
    {
        List<Format> formats = formatContext.actualOwnFormats();
        for (int i = 0; i < formats.size(); i++) {
            contextStack.peek().listener().beginFormat(formats.get(i),
                i == 0 ? formatContext.ownParameters() : Listener.EMPTY_PARAMETERS);
        }
    }

    private void endFormat(FormatContext formatContext, Deque<Context> contextStack)
    {
        List<Format> formats = formatContext.actualOwnFormats();
        for (int i = formats.size() - 1; i >= 0; i--) {
            contextStack.peek().listener().endFormat(formats.get(i),
                i == 0 ? formatContext.ownParameters() : Listener.EMPTY_PARAMETERS);
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

    private void visitLink(ObjectNode block, Deque<Context> contextStack) throws ParseException
    {
        ResourceReference target = this.resourceReferenceParser.parse(block.path(TARGET));
        contextStack.peek().listener().beginLink(target, false, Listener.EMPTY_PARAMETERS);

        visitInlineChildBlocks(block, CONTENT, contextStack);

        contextStack.peek().listener().endLink(target, false, Listener.EMPTY_PARAMETERS);
    }

    private void parsePlainText(String text, Deque<Context> contextStack) throws ParseException
    {
        // Parse the text using the Plain Text parser, ignoring the document and paragraph begin/end events.
        this.plainTextStreamParser.parse(new StringReader(text),
            new PlainTextWrappingListener(contextStack.peek().listener()));
    }

    private Map<String, String> getBlockParameters(JsonNode block)
    {
        Map<String, String> parameters = new LinkedHashMap<>();
        JsonNode styles = block.path(STYLES);
        if (styles.isObject()) {
            String value = toCSSDeclarations((ObjectNode) styles);
            if (!value.isEmpty()) {
                parameters.put("style", value);
            }
        }
        return parameters;
    }

    private String toCSSDeclarations(ObjectNode blockStyles)
    {
        List<String> declarations = new LinkedList<>();

        for (Map.Entry<String, String> entry : BLOCK_STYLES.entrySet()) {
            JsonNode styleValue = blockStyles.path(entry.getKey());
            if (styleValue.isTextual()) {
                declarations.add(entry.getValue() + ":" + styleValue.asText());
            }
        }

        return String.join(";", declarations);
    }
}

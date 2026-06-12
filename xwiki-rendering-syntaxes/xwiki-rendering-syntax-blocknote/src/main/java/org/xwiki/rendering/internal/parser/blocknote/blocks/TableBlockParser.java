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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.TEXT;

/**
 * Table block parser.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named("table")
@Singleton
public class TableBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String TABLE = "table";

    /**
     * The table content property that holds the array of columns widths (column width is either a number, the pixel
     * count, or null if not defined).
     */
    public static final String COLUMN_WIDTHS = "columnWidths";

    /**
     * The table content property that holds the number of header rows (e.g. a value of 2 means the first two rows have
     * table header cells).
     */
    public static final String HEADER_ROWS = "headerRows";

    /**
     * The table content property that holds the number of header columns (e.g. a value of 2 means the first two columns
     * have table header cells).
     */
    public static final String HEADER_COLS = "headerCols";

    /**
     * The table content property that holds the array of table rows.
     */
    public static final String ROWS = "rows";

    /**
     * The table row property that holds the array of table cells.
     */
    public static final String CELLS = "cells";

    /**
     * The table cell block type.
     */
    public static final String TABLE_CELL = "tableCell";

    /**
     * The table cell property that holds the column span.
     */
    public static final String COLSPAN = "colspan";

    /**
     * The table cell property that holds the row span.
     */
    public static final String ROWSPAN = "rowspan";

    /**
     * The parameter used to set the table cell width.
     */
    public static final String WIDTH = "width";

    @Override
    public void parse(ObjectNode tableBlock, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = getBlockParameters(tableBlock);
        contextStack.peek().listener().beginTable(parameters);

        JsonNode tableContent = tableBlock.path(CONTENT);
        if (!tableContent.isObject()) {
            throw new ParseException("Table content is expected to be a JSON object");
        }
        visitTableContent((ObjectNode) tableContent, contextStack);

        contextStack.peek().listener().endTable(parameters);
    }

    private void visitTableContent(ObjectNode tableContent, Deque<Context> contextStack) throws ParseException
    {
        int headerRows = tableContent.path(HEADER_ROWS).asInt(0);
        int headerCols = tableContent.path(HEADER_COLS).asInt(0);

        List<Integer> columnWidths = new ArrayList<>();
        JsonNode columnWidthsNode = tableContent.path(COLUMN_WIDTHS);
        for (JsonNode widthNode : columnWidthsNode) {
            columnWidths.add(widthNode.isNumber() ? widthNode.asInt() : null);
        }

        JsonNode rowsNode = tableContent.path(ROWS);
        if (!rowsNode.isArray()) {
            throw new ParseException("Table content is expected to have a 'rows' property which is a JSON array.");
        }

        ArrayNode rows = (ArrayNode) rowsNode;
        contextStack.push(contextStack.peek().withParentAndSiblings(tableContent, rows));
        visitTableRows(rows, headerRows, headerCols, columnWidths, contextStack);
        contextStack.pop();
    }

    private void visitTableRows(ArrayNode rows, int headerRows, int headerCols, List<Integer> columnWidths,
        Deque<Context> contextStack) throws ParseException
    {
        for (int i = 0; i < rows.size(); i++) {
            JsonNode row = rows.path(i);
            if (!row.isObject()) {
                throw new ParseException("Each table row must be a JSON object.");
            }
            boolean isHeaderRow = i < headerRows;
            // Apply the column widths only to the first row.
            List<Integer> cellWidths = i == 0 ? columnWidths : List.of();
            visitTableRow((ObjectNode) row, isHeaderRow, headerCols, cellWidths, contextStack);
        }
    }

    private void visitTableRow(ObjectNode row, boolean isHeaderRow, int headerCols, List<Integer> columnWidths,
        Deque<Context> contextStack) throws ParseException
    {
        contextStack.peek().listener().beginTableRow(Listener.EMPTY_PARAMETERS);

        ArrayNode cells = getCells(row);
        contextStack.push(contextStack.peek().withParentAndSiblings(row, cells));
        for (int i = 0; i < cells.size(); i++) {
            ObjectNode cell = (ObjectNode) cells.path(i);
            boolean isHeaderCell = isHeaderRow || i < headerCols;
            Integer columnWidth = columnWidths.size() > i ? columnWidths.get(i) : null;
            visitTableCell(cell, isHeaderCell, columnWidth, contextStack);
        }
        contextStack.pop();

        contextStack.peek().listener().endTableRow(Listener.EMPTY_PARAMETERS);
    }

    private ArrayNode getCells(ObjectNode row) throws ParseException
    {
        ArrayNode cells = getChildBlocks(row, CELLS);
        for (int i = 0; i < cells.size(); i++) {
            JsonNode cell = cells.path(i);
            String type = cell.path(TYPE).asText(null);
            if (TEXT.equals(type)) {
                // Wrap the text block in a table cell block.
                cells.set(i, createTableCell(cells.arrayNode().add(cell)));
            } else if (!TABLE_CELL.equals(type)) {
                throw new ParseException("Invalid table cell");
            }
        }
        return cells;
    }

    private ObjectNode createTableCell(ArrayNode content)
    {
        ObjectNode tableCell = content.objectNode();
        tableCell.put(TYPE, TABLE_CELL);
        tableCell.set(PROPS, content.objectNode());
        tableCell.set(CONTENT, content);
        return tableCell;
    }

    private void visitTableCell(ObjectNode cell, boolean isHeaderCell, Integer columnWidth, Deque<Context> contextStack)
        throws ParseException
    {
        if (columnWidth != null) {
            // Save the column width in the cell properties so that it gets picked up when computing the cell styles.
            ((ObjectNode) cell.path(PROPS)).put(WIDTH, columnWidth + "px");
        }
        Map<String, String> parameters = getTableCellParameters(cell);

        if (isHeaderCell) {
            contextStack.peek().listener().beginTableHeadCell(parameters);
        } else {
            contextStack.peek().listener().beginTableCell(parameters);
        }

        visitInlineChildBlocks(cell, CONTENT, contextStack);

        if (isHeaderCell) {
            contextStack.peek().listener().endTableHeadCell(parameters);
        } else {
            contextStack.peek().listener().endTableCell(parameters);
        }
    }

    private Map<String, String> getTableCellParameters(ObjectNode cell)
    {
        Map<String, String> parameters = getBlockParameters(cell);
        JsonNode colSpan = cell.path(PROPS).path(COLSPAN);
        if (colSpan.isInt()) {
            parameters.put(COLSPAN, colSpan.asText());
        }
        JsonNode rowSpan = cell.path(PROPS).path(ROWSPAN);
        if (rowSpan.isInt()) {
            parameters.put(ROWSPAN, rowSpan.asText());
        }
        return parameters;
    }

    @Override
    protected Map<String, String> getBlockStyles()
    {
        Map<String, String> styles = new LinkedHashMap<>(super.getBlockStyles());
        // Treat the width property of a table cell block as a CSS style.
        styles.put(WIDTH, WIDTH);
        return styles;
    }
}

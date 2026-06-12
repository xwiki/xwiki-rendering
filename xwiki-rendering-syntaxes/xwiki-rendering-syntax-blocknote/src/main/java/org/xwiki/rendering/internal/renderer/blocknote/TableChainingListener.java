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

import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.BLOCK_STYLES;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CONTENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.TYPE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.CELLS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.COLSPAN;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.COLUMN_WIDTHS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.HEADER_COLS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.HEADER_ROWS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.ROWS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.ROWSPAN;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.TABLE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.TABLE_CELL;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TableBlockParser.WIDTH;

/**
 * Listener that handles table-related events and updates the BlockNote state accordingly.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public class TableChainingListener extends AbstractChainingListener
{
    private final Context context;

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public TableChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return;
        }

        ObjectNode table = this.context.getBlockNoteState().beginBlock(TABLE, true,
            this.context.getBlockNoteState().getObjectMapper()::createObjectNode, false, true);

        ObjectNode tableContent = (ObjectNode) table.get(CONTENT);
        tableContent.put(TYPE, "tableContent");
        tableContent.set(COLUMN_WIDTHS, this.context.getBlockNoteState().getObjectMapper().createArrayNode());
        tableContent.put(HEADER_ROWS, Integer.MAX_VALUE);
        tableContent.put(HEADER_COLS, Integer.MAX_VALUE);
        tableContent.set(ROWS, this.context.getBlockNoteState().getObjectMapper().createArrayNode());
        getPath().pop();
        getPath().push(tableContent.get(ROWS));
    }

    private Deque<JsonNode> getPath()
    {
        return this.context.getBlockNoteState().getBlockNotePath();
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return;
        }

        ObjectNode row = this.context.getBlockNoteState().getObjectMapper().createObjectNode();
        row.set(CELLS, this.context.getBlockNoteState().getObjectMapper().createArrayNode());
        JsonNode rows = getPath().peek();
        if (rows != null && rows.isArray()) {
            ((ArrayNode) rows).add(row);
            getPath().push(row.get(CELLS));
        }
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        beginTableCell(true, parameters);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        endTableCell(parameters);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        beginTableCell(false, parameters);
    }

    private void beginTableCell(boolean header, Map<String, String> parameters)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return;
        }

        if (!header) {
            ObjectNode table = (ObjectNode) getPath().stream().skip(2).findFirst().orElseThrow();
            ObjectNode tableContent = (ObjectNode) table.get(CONTENT);
            tableContent.put(HEADER_ROWS,
                Math.min(tableContent.get(HEADER_ROWS).asInt(), this.context.getBlockState().getCellRow()));
            tableContent.put(HEADER_COLS,
                Math.min(tableContent.get(HEADER_COLS).asInt(), this.context.getBlockState().getCellCol()));
        }

        Map<String, String> styleMapping = new LinkedHashMap<>(BLOCK_STYLES);
        styleMapping.put(WIDTH, WIDTH);
        ObjectNode cell = this.context.getBlockNoteState().beginBlock(TABLE_CELL, styleMapping, true, false, true);
        ObjectNode cellProperties = (ObjectNode) cell.get(PROPS);
        Integer cellWidth = getCellWidth(cellProperties.remove(WIDTH));

        if (parameters.containsKey(COLSPAN)) {
            cellProperties.put(COLSPAN, Integer.parseInt(parameters.get(COLSPAN)));
        }
        if (parameters.containsKey(ROWSPAN)) {
            cellProperties.put(ROWSPAN, Integer.parseInt(parameters.get(ROWSPAN)));
        }

        if (cellWidth != null && this.context.getBlockState().getCellRow() == 0) {
            // In order to reach the table node we need to skip over cell content, cell, row and rows.
            ObjectNode table = (ObjectNode) getPath().stream().skip(4).findFirst().orElseThrow();
            ObjectNode tableContent = (ObjectNode) table.get(CONTENT);
            ArrayNode columnWidths = (ArrayNode) tableContent.get(COLUMN_WIDTHS);
            for (int i = columnWidths.size(); i < this.context.getBlockState().getCellCol(); i++) {
                columnWidths.addNull();
            }
            columnWidths.add(cellWidth);
        }
    }

    private Integer getCellWidth(JsonNode widthNode)
    {
        if (widthNode != null) {
            if (widthNode.isNumber()) {
                return widthNode.asInt();
            } else if (widthNode.isTextual()) {
                String widthValue = widthNode.asText();
                if (widthValue.endsWith("px")) {
                    try {
                        return Integer.parseInt(widthValue.substring(0, widthValue.length() - 2).trim());
                    } catch (NumberFormatException e) {
                        // Ignore invalid width value.
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        ObjectNode cell = (ObjectNode) this.context.getBlockNoteState().endBlock();
        if (cell.path(PROPS).isEmpty() && cell.path(CONTENT).isTextual()) {
            // Simplify the cell block by replacing it with its plain text content.
            ArrayNode cells = (ArrayNode) getPath().peek();
            cells.set(cells.size() - 1, cell.get(CONTENT));
        }
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            getPath().pop();
        }
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        JsonNode table = this.context.getBlockNoteState().endBlock();
        if (table != null) {
            ObjectNode tableContent = (ObjectNode) table.get(CONTENT);

            int rowCount = tableContent.get(ROWS).size();
            int headerRows = Math.min(tableContent.get(HEADER_ROWS).asInt(), rowCount);
            if (headerRows == 0) {
                tableContent.remove(HEADER_ROWS);
            } else {
                tableContent.put(HEADER_ROWS, headerRows);
            }

            int columnCount = tableContent.get(ROWS).elements().hasNext()
                ? tableContent.get(ROWS).elements().next().get(CELLS).size() : 0;
            int headerCols = Math.min(tableContent.get(HEADER_COLS).asInt(), columnCount);
            if (headerCols == 0) {
                tableContent.remove(HEADER_COLS);
            } else {
                tableContent.put(HEADER_COLS, headerCols);
            }
        }
    }
}

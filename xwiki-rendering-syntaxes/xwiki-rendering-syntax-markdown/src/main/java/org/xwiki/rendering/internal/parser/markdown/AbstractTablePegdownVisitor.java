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
package org.xwiki.rendering.internal.parser.markdown;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Implements Pegdown Visitor's table events.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractTablePegdownVisitor extends AbstractListPegdownVisitor
{
    private Stack<Boolean> isInTableHeaderStack = new Stack<Boolean>();

    private Stack<TableNode> currentTableStack = new Stack<TableNode>();

    private Stack<Integer> currentTableColumnPositionStack = new Stack<Integer>();

    @Override
    public void visit(TableBodyNode tableBodyNode)
    {
        this.isInTableHeaderStack.push(false);
        visitChildren(tableBodyNode);
        this.isInTableHeaderStack.pop();
    }

    @Override
    public void visit(TableCellNode tableCellNode)
    {
        boolean isInHeader = this.isInTableHeaderStack.peek();

        List<TableColumnNode> columns = this.currentTableStack.peek().getColumns();
        TableColumnNode column = columns.get(Math.min(this.currentTableColumnPositionStack.peek(), columns.size() - 1));

        // Compute cell parameters
        Map<String, String> parameters = new HashMap<String, String>();
        if (tableCellNode.getColSpan() > 1) {
            parameters.put("colspan", "" + tableCellNode.getColSpan());
        }

        switch (column.getAlignment()) {
            case Left:
                parameters.put("align", "left");
                break;
            case Right:
                parameters.put("align", "right");
                break;
            case Center:
                parameters.put("align", "center");
                break;
        }

        if (isInHeader) {
            getListener().beginTableHeadCell(parameters);
        } else {
            getListener().beginTableCell(parameters);
        }

        visitChildren(tableCellNode);

        if (isInHeader) {
            getListener().endTableHeadCell(parameters);
        } else {
            getListener().endTableCell(parameters);
        }

        int currentTableColumn = this.currentTableColumnPositionStack.pop();
        this.currentTableColumnPositionStack.push(currentTableColumn + tableCellNode.getColSpan());
    }

    @Override
    public void visit(TableColumnNode tableColumnNode)
    {
        // No need to do anything here, it's handled already in visit(TableCellNode tableCellNode)
    }

    @Override
    public void visit(TableHeaderNode tableHeaderNode)
    {
        this.isInTableHeaderStack.push(true);
        visitChildren(tableHeaderNode);
        this.isInTableHeaderStack.pop();
    }

    @Override
    public void visit(TableNode tableNode)
    {
        this.currentTableStack.push(tableNode);
        getListener().beginTable(Collections.EMPTY_MAP);
        visitChildren(tableNode);
        getListener().endTable(Collections.EMPTY_MAP);
        this.currentTableStack.pop();
    }

    @Override
    public void visit(TableRowNode tableRowNode)
    {
        this.currentTableColumnPositionStack.push(0);
        getListener().beginTableRow(Collections.EMPTY_MAP);
        visitChildren(tableRowNode);
        getListener().endTableRow(Collections.EMPTY_MAP);
        this.currentTableColumnPositionStack.pop();
    }

    @Override
    public void visit(TableCaptionNode tableCaptionNode)
    {
        // TODO: XWiki Rendering doesn't support Caption in tables ATM. Add proper support. Also note that the
        // HTML caption tag is supposed to be sent just after the <table> tag and thus the limited solution we have
        // below is probably wrong...
        String captionText = extractText(tableCaptionNode);
        getListener().onRawText(String.format("<caption>%s</caption>", captionText), Syntax.HTML_4_01);
    }
}

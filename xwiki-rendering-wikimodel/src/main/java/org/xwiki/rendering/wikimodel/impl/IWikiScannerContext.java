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
package org.xwiki.rendering.wikimodel.impl;

import org.xwiki.rendering.wikimodel.IWemConstants;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.WikiStyle;

/**
 * @version $Id$
 * @since 4.0M1
 */
public interface IWikiScannerContext extends IWemConstants
{
    void beginDocument();

    void beginDocument(WikiParameters params);

    void beginHeader(int level);

    void beginHeader(int level, WikiParameters params);

    void beginInfo(String type, WikiParameters params);

    void beginList();

    void beginList(WikiParameters params);

    void beginListItem(String item);

    void beginListItem(String item, WikiParameters listParams);

    default void beginListItem(String item, WikiParameters listParams, WikiParameters itemParams)
    {
        beginListItem(item, listParams);
    }

    void beginParagraph();

    void beginParagraph(WikiParameters params);

    void beginPropertyBlock(String property, boolean doc);

    void beginPropertyInline(String str);

    void beginQuot();

    void beginQuot(WikiParameters params);

    void beginQuotLine(int depth);

    void beginTable();

    void beginTable(WikiParameters params);

    void beginTableCell(boolean headCell);

    void beginTableCell(boolean headCell, WikiParameters params);

    /**
     * Starts a new table row and adds the first cell to the table.
     *
     * @param headCell if this parameter is <code>true</code> then this method
     * starts the header cell at the beginning of the line
     */
    void beginTableRow(boolean headCell);

    /**
     * Starts a new table row and adds the first cell to the table.
     *
     * @param head if this parameter is <code>true</code> then this method
     * starts the header cell at the beginning of the line
     * @param rowParams parameters of the row
     * @param cellParams parameters of the first row cell
     */
    void beginTableRow(
        boolean head,
        WikiParameters rowParams,
        WikiParameters cellParams);

    /**
     * Starts a new table row (but doesn't add a cell).
     */
    void beginTableRow(WikiParameters rowParams);

    boolean canApplyDefintionSplitter();

    boolean checkFormatStyle(WikiStyle style);

    void closeBlock();

    void endDocument();

    void endHeader();

    void endInfo();

    void endList();

    void endListItem();

    void endParagraph();

    void endPropertyBlock();

    void endPropertyInline();

    void endQuot();

    void endQuotLine();

    void endTable();

    void endTableCell();

    void endTableExplicit();

    void endTableRow();

    /**
     * Starts a new figure.
     *
     * @param params Parameters of the figure.
     * @since 14.1RC1
     */
    default void beginFigure(WikiParameters params)
    {
        // do nothing by default
    }

    /**
     * Ends a figure.
     *
     * @since 14.1RC1
     */
    default void endFigure()
    {
        // do nothing by default
    }

    /**
     * Starts a figure caption.
     *
     * This must only be used inside a figure.
     *
     * @param params The parameters of the caption.
     * @since 14.1RC1
     */
    default void beginFigureCaption(WikiParameters params)
    {
        // do nothing by default
    }

    /**
     * Ends a figure caption.
     * @since 14.1RC1
     */
    default void endFigureCaption()
    {
        // do nothing by default
    }

    InlineState getInlineState();

    int getTableCellCounter();

    int getTableRowCounter();

    boolean isInDefinitionList();

    boolean isInDefinitionTerm();

    boolean isInHeader();

    boolean isInInlineProperty();

    boolean isInList();

    boolean isInTable();

    boolean isInTableCell();

    boolean isInTableRow();

    void onDefinitionListItemSplit();

    void onEmptyLines(int count);

    void onEscape(String str);

    void onExtensionBlock(String extensionName, WikiParameters params);

    void onExtensionInline(String extensionName, WikiParameters params);

    void onFormat(WikiParameters params);

    void onFormat(WikiStyle wikiStyle);

    void beginFormat(WikiParameters params);

    void beginFormat(WikiStyle wikiStyle);

    void endFormat(WikiParameters params);

    void endFormat(WikiStyle wikiStyle);

    /**
     * @see WikiScannerContext#onFormat(org.xwiki.rendering.wikimodel.WikiStyle,
     *      boolean)
     */
    void onFormat(WikiStyle wikiStyle, boolean forceClose);

    void onHorizontalLine();

    void onHorizontalLine(WikiParameters params);

    void onImage(String ref);

    void onImage(WikiReference ref);

    void onLineBreak();

    void onMacro(String name, WikiParameters params, String content);

    void onMacro(
        String macroName,
        WikiParameters params,
        String content,
        boolean inline);

    void onMacroBlock(String macroName, WikiParameters params, String content);

    void onMacroInline(String macroName, WikiParameters params, String content);

    void onNewLine();

    void onQuotLine(int depth);

    void onReference(String ref);

    void onReference(WikiReference ref);

    void onSpace(String str);

    void onSpecialSymbol(String str);

    void onTableCaption(String str);

    void onTableCell(boolean headCell);

    void onTableCell(boolean head, WikiParameters cellParams);

    /**
     * Explicitly starts a new table row. This method should not create a new
     * cell at the beginning of the line. To automatically create the first row
     * cell the methods {@link #beginTableCell(boolean)} or
     * {@link #beginTableRow(boolean, WikiParameters, WikiParameters)} should be
     * used.
     */
    void onTableRow(WikiParameters params);

    void onVerbatim(String str, WikiParameters params);

    /**
     * @see WikiScannerContext#onVerbatim(java.lang.String,
     *      boolean)
     */
    void onVerbatim(String str, boolean inline);

    void onVerbatim(String str, boolean inline, WikiParameters params);

    void onWord(String str);
}

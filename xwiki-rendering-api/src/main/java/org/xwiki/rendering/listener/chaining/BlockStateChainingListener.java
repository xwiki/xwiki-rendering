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
package org.xwiki.rendering.listener.chaining;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Indicates block element for which we are inside and previous blocks.
 *
 * @version $Id$
 * @since 1.8RC1
 */
public class BlockStateChainingListener extends AbstractChainingListener implements StackableChainingListener
{
    public enum Event
    {
        NONE,
        DEFINITION_DESCRIPTION,
        DEFINITION_TERM,
        DEFINITION_LIST,
        DOCUMENT,
        FORMAT,
        HEADER,
        LINK,
        LIST,
        LIST_ITEM,
        MACRO_MARKER,
        PARAGRAPH,
        QUOTATION,
        QUOTATION_LINE,
        SECTION,
        TABLE,
        TABLE_CELL,
        TABLE_HEAD_CELL,
        TABLE_ROW,
        RAW_TEXT,
        EMPTY_LINES,
        HORIZONTAL_LINE,
        ID,
        IMAGE,
        NEW_LINE,
        SPACE,
        SPECIAL_SYMBOL,
        MACRO,
        VERBATIM_INLINE,
        VERBATIM_STANDALONE,
        WORD,
        FIGURE,
        FIGURE_CAPTION,
        META_DATA,
        GROUP
    }

    private Event previousEvent = Event.NONE;

    private int inlineDepth;

    private boolean isInParagraph;

    private boolean isInHeader;

    private int linkDepth;

    private boolean isInTable;

    private boolean isInTableCell;

    private Deque<DefinitionListState> definitionListDepth = new ArrayDeque<>();

    private Deque<ListState> listDepth = new ArrayDeque<>();

    private int quotationDepth;

    private int quotationLineDepth;

    private int quotationLineIndex = -1;

    private int macroDepth;

    private int cellRow = -1;

    private int cellCol = -1;

    public BlockStateChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    @Override
    public StackableChainingListener createChainingListenerInstance()
    {
        return new BlockStateChainingListener(getListenerChain());
    }

    public Event getPreviousEvent()
    {
        return this.previousEvent;
    }

    public int getInlineDepth()
    {
        return this.inlineDepth;
    }

    public boolean isInLine()
    {
        return getInlineDepth() > 0;
    }

    public boolean isInParagraph()
    {
        return this.isInParagraph;
    }

    public boolean isInHeader()
    {
        return this.isInHeader;
    }

    public boolean isInTable()
    {
        return this.isInTable;
    }

    public boolean isInTableCell()
    {
        return this.isInTableCell;
    }

    public int getCellCol()
    {
        return this.cellCol;
    }

    public int getCellRow()
    {
        return this.cellRow;
    }

    public int getDefinitionListDepth()
    {
        return this.definitionListDepth.size();
    }

    public boolean isInDefinitionList()
    {
        return getDefinitionListDepth() > 0;
    }

    public int getDefinitionListItemIndex()
    {
        return isInDefinitionList() ? this.definitionListDepth.peek().definitionListItemIndex : -1;
    }

    public int getListDepth()
    {
        return this.listDepth.size();
    }

    public boolean isInList()
    {
        return getListDepth() > 0;
    }

    public int getListItemIndex()
    {
        return isInList() ? this.listDepth.peek().listItemIndex : -1;
    }

    public void pushLinkDepth()
    {
        ++this.linkDepth;
    }

    public void popLinkDepth()
    {
        --this.linkDepth;
    }

    public int getLinkDepth()
    {
        return this.linkDepth;
    }

    public boolean isInLink()
    {
        return getLinkDepth() > 0;
    }

    public int getQuotationDepth()
    {
        return this.quotationDepth;
    }

    public boolean isInQuotation()
    {
        return getQuotationDepth() > 0;
    }

    public int getQuotationLineDepth()
    {
        return this.quotationLineDepth;
    }

    public boolean isInQuotationLine()
    {
        return getQuotationLineDepth() > 0;
    }

    public int getQuotationLineIndex()
    {
        return this.quotationLineIndex;
    }

    public int getMacroDepth()
    {
        return this.macroDepth;
    }

    public boolean isInMacro()
    {
        return getMacroDepth() > 0;
    }

    // Events

    @Override
    public void beginDefinitionDescription()
    {
        ++this.inlineDepth;
        ++this.definitionListDepth.peek().definitionListItemIndex;

        super.beginDefinitionDescription();
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        this.definitionListDepth.push(new DefinitionListState());

        super.beginDefinitionList(parameters);
    }

    @Override
    public void beginDefinitionTerm()
    {
        ++this.inlineDepth;
        ++this.definitionListDepth.peek().definitionListItemIndex;

        super.beginDefinitionTerm();
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        ++this.linkDepth;

        super.beginLink(reference, freestanding, parameters);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        this.listDepth.push(new ListState());

        super.beginList(type, parameters);
    }

    @Override
    public void beginListItem()
    {
        ++this.inlineDepth;
        ++this.listDepth.peek().listItemIndex;

        super.beginListItem();
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        ++this.inlineDepth;
        ++this.listDepth.peek().listItemIndex;

        super.beginListItem(parameters);
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        ++this.macroDepth;

        super.beginMacroMarker(name, parameters, content, isInline);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.isInParagraph = true;
        ++this.inlineDepth;

        super.beginParagraph(parameters);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        ++this.quotationDepth;

        super.beginQuotation(parameters);
    }

    @Override
    public void beginQuotationLine()
    {
        ++this.quotationLineDepth;
        ++this.inlineDepth;
        ++this.quotationLineIndex;

        super.beginQuotationLine();
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.isInHeader = true;
        ++this.inlineDepth;

        super.beginHeader(level, id, parameters);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        this.isInTable = true;

        super.beginTable(parameters);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        ++this.cellRow;

        super.beginTableRow(parameters);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        this.isInTableCell = true;
        ++this.inlineDepth;
        ++this.cellCol;

        super.beginTableCell(parameters);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.isInTableCell = true;
        ++this.inlineDepth;
        ++this.cellCol;

        super.beginTableHeadCell(parameters);
    }

    @Override
    public void endDefinitionDescription()
    {
        super.endDefinitionDescription();

        --this.inlineDepth;
        this.previousEvent = Event.DEFINITION_DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        super.endDefinitionList(parameters);

        this.definitionListDepth.pop();

        this.previousEvent = Event.DEFINITION_LIST;
    }

    @Override
    public void endDefinitionTerm()
    {
        super.endDefinitionTerm();

        --this.inlineDepth;
        this.previousEvent = Event.DEFINITION_TERM;
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        super.endDocument(metadata);

        this.previousEvent = Event.DOCUMENT;
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        super.endFormat(format, parameters);

        this.previousEvent = Event.FORMAT;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        super.endLink(reference, freestanding, parameters);

        --this.linkDepth;
        this.previousEvent = Event.LINK;
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        super.endList(type, parameters);

        this.listDepth.pop();

        this.previousEvent = Event.LIST;
    }

    @Override
    public void endListItem()
    {
        super.endListItem();

        --this.inlineDepth;
        this.previousEvent = Event.LIST_ITEM;
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        super.endListItem(parameters);

        --this.inlineDepth;
        this.previousEvent = Event.LIST_ITEM;
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        super.endMacroMarker(name, parameters, content, isInline);

        this.previousEvent = Event.MACRO_MARKER;
        --this.macroDepth;
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        super.endMetaData(metadata);

        this.previousEvent = Event.META_DATA;
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void endGroup(Map<String, String> parameters)
    {
        super.endGroup(parameters);

        this.previousEvent = Event.GROUP;
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        super.endParagraph(parameters);

        this.isInParagraph = false;
        --this.inlineDepth;
        this.previousEvent = Event.PARAGRAPH;
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        super.endQuotation(parameters);

        --this.quotationDepth;
        if (this.quotationDepth == 0) {
            this.quotationLineIndex = -1;
        }
        this.previousEvent = Event.QUOTATION;
    }

    @Override
    public void endQuotationLine()
    {
        super.endQuotationLine();

        --this.quotationLineDepth;
        --this.inlineDepth;
        this.previousEvent = Event.QUOTATION_LINE;
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        super.endSection(parameters);

        this.previousEvent = Event.SECTION;
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        super.endHeader(level, id, parameters);

        this.isInHeader = false;
        --this.inlineDepth;
        this.previousEvent = Event.HEADER;
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        super.endTable(parameters);

        this.isInTable = false;
        this.cellRow = -1;
        this.previousEvent = Event.TABLE;
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        super.endTableCell(parameters);

        this.isInTableCell = false;
        --this.inlineDepth;
        this.previousEvent = Event.TABLE_CELL;
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        super.endTableHeadCell(parameters);

        this.isInTableCell = false;
        --this.inlineDepth;
        this.previousEvent = Event.TABLE_HEAD_CELL;
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        super.endTableRow(parameters);

        this.previousEvent = Event.TABLE_ROW;
        this.cellCol = -1;
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        super.endFigure(parameters);

        this.previousEvent = Event.FIGURE;
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        ++this.inlineDepth;

        super.beginFigureCaption(parameters);
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        super.endFigureCaption(parameters);

        --this.inlineDepth;
        this.previousEvent = Event.FIGURE_CAPTION;
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        super.onRawText(text, syntax);

        this.previousEvent = Event.RAW_TEXT;
    }

    @Override
    public void onEmptyLines(int count)
    {
        super.onEmptyLines(count);

        this.previousEvent = Event.EMPTY_LINES;
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        super.onHorizontalLine(parameters);

        this.previousEvent = Event.HORIZONTAL_LINE;
    }

    @Override
    public void onId(String name)
    {
        super.onId(name);

        this.previousEvent = Event.ID;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        super.onImage(reference, freestanding, parameters);

        this.previousEvent = Event.IMAGE;
    }

    @Override
    public void onNewLine()
    {
        super.onNewLine();

        this.previousEvent = Event.NEW_LINE;
    }

    @Override
    public void onSpace()
    {
        super.onSpace();

        this.previousEvent = Event.SPACE;
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        super.onSpecialSymbol(symbol);

        this.previousEvent = Event.SPECIAL_SYMBOL;
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        super.onVerbatim(content, inline, parameters);

        if (inline) {
            this.previousEvent = Event.VERBATIM_INLINE;
        } else {
            this.previousEvent = Event.VERBATIM_STANDALONE;
        }
    }

    @Override
    public void onWord(String word)
    {
        super.onWord(word);

        this.previousEvent = Event.WORD;
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        super.onMacro(id, parameters, content, inline);

        this.previousEvent = Event.MACRO;
    }

    private static class ListState
    {
        public int listItemIndex = -1;
    }

    private static class DefinitionListState
    {
        public int definitionListItemIndex = -1;
    }
}

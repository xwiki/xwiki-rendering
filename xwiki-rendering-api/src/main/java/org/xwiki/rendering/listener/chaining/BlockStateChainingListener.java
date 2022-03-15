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
import org.xwiki.stability.Unstable;

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

    private final Deque<Event> eventStack = new ArrayDeque<>();

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

    /**
     * @return The event that encloses the current event.
     * @since 14.0RC1
     */
    @Unstable
    public Event getParentEvent()
    {
        return this.eventStack.peek();
    }

    // Events

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void beginDocument(MetaData metadata)
    {
        super.beginDocument(metadata);

        this.eventStack.push(Event.DOCUMENT);
    }

    @Override
    public void beginDefinitionDescription()
    {
        ++this.inlineDepth;
        ++this.definitionListDepth.peek().definitionListItemIndex;

        super.beginDefinitionDescription();

        this.eventStack.push(Event.DEFINITION_DESCRIPTION);
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

        this.eventStack.push(Event.DEFINITION_LIST);
    }

    @Override
    public void beginDefinitionTerm()
    {
        ++this.inlineDepth;
        ++this.definitionListDepth.peek().definitionListItemIndex;

        super.beginDefinitionTerm();

        this.eventStack.push(Event.DEFINITION_TERM);
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

        this.eventStack.push(Event.LINK);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        this.listDepth.push(new ListState());

        super.beginList(type, parameters);

        this.eventStack.push(Event.LIST);
    }

    @Override
    public void beginListItem()
    {
        ++this.inlineDepth;
        ++this.listDepth.peek().listItemIndex;

        super.beginListItem();

        this.eventStack.push(Event.LIST_ITEM);
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        ++this.inlineDepth;
        ++this.listDepth.peek().listItemIndex;

        super.beginListItem(parameters);

        this.eventStack.push(Event.LIST_ITEM);
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        ++this.macroDepth;

        super.beginMacroMarker(name, parameters, content, isInline);

        this.eventStack.push(Event.MACRO_MARKER);
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        super.beginMetaData(metadata);

        this.eventStack.push(Event.META_DATA);
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        super.beginGroup(parameters);

        this.eventStack.push(Event.GROUP);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.isInParagraph = true;
        ++this.inlineDepth;

        super.beginParagraph(parameters);

        this.eventStack.push(Event.PARAGRAPH);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        ++this.quotationDepth;

        super.beginQuotation(parameters);

        this.eventStack.push(Event.QUOTATION);
    }

    @Override
    public void beginQuotationLine()
    {
        ++this.quotationLineDepth;
        ++this.inlineDepth;
        ++this.quotationLineIndex;

        super.beginQuotationLine();

        this.eventStack.push(Event.QUOTATION_LINE);
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.isInHeader = true;
        ++this.inlineDepth;

        super.beginHeader(level, id, parameters);

        this.eventStack.push(Event.HEADER);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        this.isInTable = true;

        super.beginTable(parameters);

        this.eventStack.push(Event.TABLE);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        ++this.cellRow;

        super.beginTableRow(parameters);

        this.eventStack.push(Event.TABLE_ROW);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        this.isInTableCell = true;
        ++this.inlineDepth;
        ++this.cellCol;

        super.beginTableCell(parameters);

        this.eventStack.push(Event.TABLE_CELL);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.isInTableCell = true;
        ++this.inlineDepth;
        ++this.cellCol;

        super.beginTableHeadCell(parameters);

        this.eventStack.push(Event.TABLE_HEAD_CELL);
    }

    /**
     * Removes an event from the stack if it matches the passed event.
     *
     * @param event The event to remove.
     * @since 14.0RC1
     */
    private void removeEventFromStack(Event event)
    {
        if (this.eventStack.peek() == event) {
            this.eventStack.pop();
        }
    }

    @Override
    public void endDefinitionDescription()
    {
        removeEventFromStack(Event.DEFINITION_DESCRIPTION);

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
        removeEventFromStack(Event.DEFINITION_LIST);

        super.endDefinitionList(parameters);

        this.definitionListDepth.pop();

        this.previousEvent = Event.DEFINITION_LIST;
    }

    @Override
    public void endDefinitionTerm()
    {
        removeEventFromStack(Event.DEFINITION_TERM);

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
        removeEventFromStack(Event.DOCUMENT);

        super.endDocument(metadata);

        this.previousEvent = Event.DOCUMENT;
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        super.beginFormat(format, parameters);

        this.eventStack.push(Event.FORMAT);
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        removeEventFromStack(Event.FORMAT);

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
        removeEventFromStack(Event.LINK);

        super.endLink(reference, freestanding, parameters);

        --this.linkDepth;
        this.previousEvent = Event.LINK;
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        removeEventFromStack(Event.LIST);

        super.endList(type, parameters);

        this.listDepth.pop();

        this.previousEvent = Event.LIST;
    }

    @Override
    public void endListItem()
    {
        removeEventFromStack(Event.LIST_ITEM);

        super.endListItem();

        --this.inlineDepth;
        this.previousEvent = Event.LIST_ITEM;
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        removeEventFromStack(Event.LIST_ITEM);

        super.endListItem(parameters);

        --this.inlineDepth;
        this.previousEvent = Event.LIST_ITEM;
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        removeEventFromStack(Event.MACRO_MARKER);

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
        removeEventFromStack(Event.META_DATA);

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
        removeEventFromStack(Event.GROUP);

        super.endGroup(parameters);

        this.previousEvent = Event.GROUP;
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        removeEventFromStack(Event.PARAGRAPH);

        super.endParagraph(parameters);

        this.isInParagraph = false;
        --this.inlineDepth;
        this.previousEvent = Event.PARAGRAPH;
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        removeEventFromStack(Event.QUOTATION);

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
        removeEventFromStack(Event.QUOTATION_LINE);

        super.endQuotationLine();

        --this.quotationLineDepth;
        --this.inlineDepth;
        this.previousEvent = Event.QUOTATION_LINE;
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void beginSection(Map<String, String> parameters)
    {
        super.beginSection(parameters);

        this.eventStack.push(Event.SECTION);
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        removeEventFromStack(Event.SECTION);

        super.endSection(parameters);

        this.previousEvent = Event.SECTION;
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        removeEventFromStack(Event.HEADER);

        super.endHeader(level, id, parameters);

        this.isInHeader = false;
        --this.inlineDepth;
        this.previousEvent = Event.HEADER;
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        removeEventFromStack(Event.TABLE);

        super.endTable(parameters);

        this.isInTable = false;
        this.cellRow = -1;
        this.previousEvent = Event.TABLE;
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        removeEventFromStack(Event.TABLE_CELL);

        super.endTableCell(parameters);

        this.isInTableCell = false;
        --this.inlineDepth;
        this.previousEvent = Event.TABLE_CELL;
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        removeEventFromStack(Event.TABLE_HEAD_CELL);

        super.endTableHeadCell(parameters);

        this.isInTableCell = false;
        --this.inlineDepth;
        this.previousEvent = Event.TABLE_HEAD_CELL;
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        removeEventFromStack(Event.TABLE_ROW);

        super.endTableRow(parameters);

        this.previousEvent = Event.TABLE_ROW;
        this.cellCol = -1;
    }

    /**
     * {@inheritDoc}
     *
     * @since 14.0RC1
     */
    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        super.beginFigure(parameters);

        this.eventStack.push(Event.FIGURE);
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        removeEventFromStack(Event.FIGURE);

        super.endFigure(parameters);

        this.previousEvent = Event.FIGURE;
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        super.beginFigureCaption(parameters);

        this.eventStack.push(Event.FIGURE_CAPTION);
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        removeEventFromStack(Event.FIGURE_CAPTION);

        super.endFigureCaption(parameters);

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
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        super.onImage(reference, freestanding, id, parameters);

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

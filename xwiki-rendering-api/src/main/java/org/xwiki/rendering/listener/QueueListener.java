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
package org.xwiki.rendering.listener;

import java.util.LinkedList;
import java.util.Map;

import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Manage a {@link java.util.Queue} of events.
 *
 * @version $Id$
 * @since 2.1M1
 */
public class QueueListener extends LinkedList<QueueListener.Event> implements Listener
{
    /**
     * Class ID for serialization.
     */
    private static final long serialVersionUID = 2869508092440006345L;

    /**
     * An event.
     *
     * @version $Id$
     */
    public class Event
    {
        /**
         * The type of the event.
         */
        public EventType eventType;

        /**
         * The parameters of the event.
         */
        public Object[] eventParameters;

        /**
         * @param eventType the type of the event
         * @param eventParameters the parameters of the event
         */
        public Event(EventType eventType, Object... eventParameters)
        {
            this.eventType = eventType;
            this.eventParameters = eventParameters;
        }
    }

    /**
     * Returns the event at the specified position in this queue.
     *
     * @param depth index of event to return.
     * @return the evnet at the specified position in this queue.
     */
    public Event getEvent(int depth)
    {
        Event event = null;

        if (depth > 0 && size() > depth - 1) {
            event = get(depth - 1);
        }

        return event;
    }

    /**
     * Send all stored events to provided {@link Listener}.
     *
     * @param listener the {@link Listener} on which to send events
     */
    public void consumeEvents(Listener listener)
    {
        while (!isEmpty()) {
            Event event = remove();
            event.eventType.fireEvent(listener, event.eventParameters);
        }
    }

    /**
     * Store provided event.
     *
     * @param eventType the type of the event
     * @param objects the parameters of the event
     */
    private void saveEvent(EventType eventType, Object... objects)
    {
        offer(new Event(eventType, objects));
    }

    @Override
    public void beginDefinitionDescription()
    {
        saveEvent(EventType.BEGIN_DEFINITION_DESCRIPTION);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_DEFINITION_LIST, parameters);
    }

    @Override
    public void beginDefinitionTerm()
    {
        saveEvent(EventType.BEGIN_DEFINITION_TERM);
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginDocument(MetaData metadata)
    {
        saveEvent(EventType.BEGIN_DOCUMENT, metadata);
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_GROUP, parameters);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_FORMAT, format, parameters);
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_HEADER, level, id, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_LINK, reference, freestanding, parameters);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_LIST, type, parameters);
    }

    @Override
    public void beginListItem()
    {
        saveEvent(EventType.BEGIN_LIST_ITEM);
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_LIST_ITEM, parameters);
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        saveEvent(EventType.BEGIN_MACRO_MARKER, name, parameters, content, isInline);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_PARAGRAPH, parameters);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_QUOTATION, parameters);
    }

    @Override
    public void beginQuotationLine()
    {
        saveEvent(EventType.BEGIN_QUOTATION_LINE);
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_SECTION, parameters);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_TABLE, parameters);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_TABLE_CELL, parameters);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_TABLE_HEAD_CELL, parameters);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_TABLE_ROW, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        saveEvent(EventType.BEGIN_METADATA, metadata);
    }

    @Override
    public void endDefinitionDescription()
    {
        saveEvent(EventType.END_DEFINITION_DESCRIPTION);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        saveEvent(EventType.END_DEFINITION_LIST, parameters);
    }

    @Override
    public void endDefinitionTerm()
    {
        saveEvent(EventType.END_DEFINITION_TERM);
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        saveEvent(EventType.END_DOCUMENT, metadata);
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        saveEvent(EventType.END_GROUP, parameters);
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        saveEvent(EventType.END_FORMAT, format, parameters);
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        saveEvent(EventType.END_HEADER, level, id, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        saveEvent(EventType.END_LINK, reference, freestanding, parameters);
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        saveEvent(EventType.END_LIST, type, parameters);
    }

    @Override
    public void endListItem()
    {
        saveEvent(EventType.END_LIST_ITEM);
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        saveEvent(EventType.END_LIST_ITEM, parameters);
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        saveEvent(EventType.END_MACRO_MARKER, name, parameters, content, isInline);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        saveEvent(EventType.END_PARAGRAPH, parameters);
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        saveEvent(EventType.END_QUOTATION, parameters);
    }

    @Override
    public void endQuotationLine()
    {
        saveEvent(EventType.END_QUOTATION_LINE);
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_FIGURE, parameters);
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        saveEvent(EventType.END_FIGURE, parameters);
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        saveEvent(EventType.BEGIN_FIGURE_CAPTION, parameters);
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        saveEvent(EventType.END_FIGURE_CAPTION, parameters);
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        saveEvent(EventType.END_SECTION, parameters);
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        saveEvent(EventType.END_TABLE, parameters);
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        saveEvent(EventType.END_TABLE_CELL, parameters);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        saveEvent(EventType.END_TABLE_HEAD_CELL, parameters);
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        saveEvent(EventType.END_TABLE_ROW, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        saveEvent(EventType.END_METADATA, metadata);
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        saveEvent(EventType.ON_RAW_TEXT, text, syntax);
    }

    @Override
    public void onEmptyLines(int count)
    {
        saveEvent(EventType.ON_EMPTY_LINES, count);
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        saveEvent(EventType.ON_HORIZONTAL_LINE, parameters);
    }

    @Override
    public void onId(String name)
    {
        saveEvent(EventType.ON_ID, name);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        saveEvent(EventType.ON_IMAGE, reference, freestanding, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        saveEvent(EventType.ON_IMAGE, reference, freestanding, id, parameters);
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        saveEvent(EventType.ON_MACRO, id, parameters, content, inline);
    }

    @Override
    public void onNewLine()
    {
        saveEvent(EventType.ON_NEW_LINE);
    }

    @Override
    public void onSpace()
    {
        saveEvent(EventType.ON_SPACE);
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        saveEvent(EventType.ON_SPECIAL_SYMBOL, symbol);
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        saveEvent(EventType.ON_VERBATIM, content, inline, parameters);
    }

    @Override
    public void onWord(String word)
    {
        saveEvent(EventType.ON_WORD, word);
    }
}

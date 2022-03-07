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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Send events to a list of {@link Listener}s.
 *
 * @version $Id$
 * @since 2.1M1
 */
public class CompositeListener implements Listener
{
    /**
     * The listeners.
     */
    private List<Listener> listeners = new ArrayList<Listener>();

    /**
     * Add a nex listener to the list.
     *
     * @param listener a listener
     */
    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }

    /**
     * Get listener at the provided position in the list.
     *
     * @param i the index of the listener in the list
     * @return the listener
     */
    public Listener getListener(int i)
    {
        return this.listeners.get(i);
    }

    @Override
    public void beginDefinitionDescription()
    {
        for (Listener listener : this.listeners) {
            listener.beginDefinitionDescription();
        }
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginDefinitionList(parameters);
        }
    }

    @Override
    public void beginDefinitionTerm()
    {
        for (Listener listener : this.listeners) {
            listener.beginDefinitionTerm();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginDocument(MetaData metadata)
    {
        for (Listener listener : this.listeners) {
            listener.beginDocument(metadata);
        }
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginFormat(format, parameters);
        }
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginGroup(parameters);
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginHeader(level, id, parameters);
        }
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginList(type, parameters);
        }
    }

    @Override
    public void beginListItem()
    {
        for (Listener listener : this.listeners) {
            listener.beginListItem();
        }
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginListItem(parameters);
        }
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        for (Listener listener : this.listeners) {
            listener.beginMacroMarker(name, macroParameters, content, isInline);
        }
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginParagraph(parameters);
        }
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginQuotation(parameters);
        }
    }

    @Override
    public void beginQuotationLine()
    {
        for (Listener listener : this.listeners) {
            listener.beginQuotationLine();
        }
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginSection(parameters);
        }
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginTable(parameters);
        }
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginTableCell(parameters);
        }
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginTableHeadCell(parameters);
        }
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginTableRow(parameters);
        }
    }

    @Override
    public void endDefinitionDescription()
    {
        for (Listener listener : this.listeners) {
            listener.endDefinitionDescription();
        }
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endDefinitionList(parameters);
        }
    }

    @Override
    public void endDefinitionTerm()
    {
        for (Listener listener : this.listeners) {
            listener.endDefinitionTerm();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        for (Listener listener : this.listeners) {
            listener.endDocument(metadata);
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endFormat(format, parameters);
        }
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endGroup(parameters);
        }
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endHeader(level, id, parameters);
        }
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endList(type, parameters);
        }
    }

    @Override
    public void endListItem()
    {
        for (Listener listener : this.listeners) {
            listener.endListItem();
        }
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endListItem(parameters);
        }
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        for (Listener listener : this.listeners) {
            listener.endMacroMarker(name, macroParameters, content, isInline);
        }
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endParagraph(parameters);
        }
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endQuotation(parameters);
        }
    }

    @Override
    public void endQuotationLine()
    {
        for (Listener listener : this.listeners) {
            listener.endQuotationLine();
        }
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginFigure(parameters);
        }
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endFigure(parameters);
        }
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginFigureCaption(parameters);
        }
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endFigureCaption(parameters);
        }
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endSection(parameters);
        }
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endTable(parameters);
        }
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endTableCell(parameters);
        }
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endTableHeadCell(parameters);
        }
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endTableRow(parameters);
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        for (Listener listener : this.listeners) {
            listener.onEmptyLines(count);
        }
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.onHorizontalLine(parameters);
        }
    }

    @Override
    public void onId(String name)
    {
        for (Listener listener : this.listeners) {
            listener.onId(name);
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        for (Listener listener : this.listeners) {
            listener.onMacro(id, parameters, content, inline);
        }
    }

    @Override
    public void onNewLine()
    {
        for (Listener listener : this.listeners) {
            listener.onNewLine();
        }
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        for (Listener listener : this.listeners) {
            listener.onRawText(text, syntax);
        }
    }

    @Override
    public void onSpace()
    {
        for (Listener listener : this.listeners) {
            listener.onSpace();
        }
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        for (Listener listener : this.listeners) {
            listener.onSpecialSymbol(symbol);
        }
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.onVerbatim(content, inline, parameters);
        }
    }

    @Override
    public void onWord(String word)
    {
        for (Listener listener : this.listeners) {
            listener.onWord(word);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.beginLink(reference, freestanding, parameters);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.endLink(reference, freestanding, parameters);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.onImage(reference, freestanding, parameters);
        }
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        for (Listener listener : this.listeners) {
            listener.onImage(reference, freestanding, id, parameters);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        for (Listener listener : this.listeners) {
            listener.beginMetaData(metadata);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        for (Listener listener : this.listeners) {
            listener.endMetaData(metadata);
        }
    }
}

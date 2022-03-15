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

import java.util.Map;

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * A Listener wrapping another Listener.
 *
 * @version $Id$
 */
public class WrappingListener implements Listener
{
    /**
     * The Listener to wrap.
     */
    private Listener listener;

    /**
     * @param listener the Listener to wrap
     */
    public void setWrappedListener(Listener listener)
    {
        this.listener = listener;
    }

    /**
     * @return the Listener to wrap
     */
    public Listener getWrappedListener()
    {
        return this.listener;
    }

    @Override
    public void beginDocument(MetaData metadata)
    {
        if (this.listener != null) {
            this.listener.beginDocument(metadata);
        }
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginFigure(parameters);
        }
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginFigureCaption(parameters);
        }
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        if (this.listener != null) {
            this.listener.endDocument(metadata);
        }
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endFigure(parameters);
        }
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endFigureCaption(parameters);
        }
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginGroup(parameters);
        }
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endGroup(parameters);
        }
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginFormat(format, parameters);
        }
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginList(type, parameters);
        }
    }

    @Override
    public void beginListItem()
    {
        if (this.listener != null) {
            this.listener.beginListItem();
        }
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginListItem(parameters);
        }
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        if (this.listener != null) {
            this.listener.beginMacroMarker(name, parameters, content, isInline);
        }
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginParagraph(parameters);
        }
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginSection(parameters);
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginHeader(level, id, parameters);
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endFormat(format, parameters);
        }
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endList(type, parameters);
        }
    }

    @Override
    public void endListItem()
    {
        if (this.listener != null) {
            this.listener.endListItem();
        }
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endListItem(parameters);
        }
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        if (this.listener != null) {
            this.listener.endMacroMarker(name, parameters, content, isInline);
        }
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endParagraph(parameters);
        }
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endSection(parameters);
        }
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endHeader(level, id, parameters);
        }
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginLink(reference, freestanding, parameters);
        }
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endLink(reference, freestanding, parameters);
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        if (this.listener != null) {
            this.listener.onMacro(id, parameters, content, inline);
        }
    }

    @Override
    public void onNewLine()
    {
        if (this.listener != null) {
            this.listener.onNewLine();
        }
    }

    @Override
    public void onSpace()
    {
        if (this.listener != null) {
            this.listener.onSpace();
        }
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        if (this.listener != null) {
            this.listener.onSpecialSymbol(symbol);
        }
    }

    @Override
    public void onWord(String word)
    {
        if (this.listener != null) {
            this.listener.onWord(word);
        }
    }

    @Override
    public void onId(String name)
    {
        if (this.listener != null) {
            this.listener.onId(name);
        }
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.onHorizontalLine(parameters);
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        if (this.listener != null) {
            this.listener.onEmptyLines(count);
        }
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.onVerbatim(content, inline, parameters);
        }
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        if (this.listener != null) {
            this.listener.onRawText(text, syntax);
        }
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginDefinitionList(parameters);
        }
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endDefinitionList(parameters);
        }
    }

    @Override
    public void beginDefinitionTerm()
    {
        if (this.listener != null) {
            this.listener.beginDefinitionTerm();
        }
    }

    @Override
    public void beginDefinitionDescription()
    {
        if (this.listener != null) {
            this.listener.beginDefinitionDescription();
        }
    }

    @Override
    public void endDefinitionTerm()
    {
        if (this.listener != null) {
            this.listener.endDefinitionTerm();
        }
    }

    @Override
    public void endDefinitionDescription()
    {
        if (this.listener != null) {
            this.listener.endDefinitionDescription();
        }
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginQuotation(parameters);
        }
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endQuotation(parameters);
        }
    }

    @Override
    public void beginQuotationLine()
    {
        if (this.listener != null) {
            this.listener.beginQuotationLine();
        }
    }

    @Override
    public void endQuotationLine()
    {
        if (this.listener != null) {
            this.listener.endQuotationLine();
        }
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginTable(parameters);
        }
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginTableCell(parameters);
        }
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginTableHeadCell(parameters);
        }
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.beginTableRow(parameters);
        }
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endTable(parameters);
        }
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endTableCell(parameters);
        }
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endTableHeadCell(parameters);
        }
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.endTableRow(parameters);
        }
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.onImage(reference, freestanding, parameters);
        }
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        if (this.listener != null) {
            this.listener.onImage(reference, freestanding, id, parameters);
        }
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        if (this.listener != null) {
            this.listener.beginMetaData(metadata);
        }
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        if (this.listener != null) {
            this.listener.endMetaData(metadata);
        }
    }
}

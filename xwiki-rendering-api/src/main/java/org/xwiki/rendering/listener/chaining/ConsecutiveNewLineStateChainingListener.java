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

import java.util.Map;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Counts consecutive new lines.
 *
 * @version $Id; $
 * @since 1.8RC1
 */
public class ConsecutiveNewLineStateChainingListener extends AbstractChainingListener
    implements StackableChainingListener
{
    /**
     * Number of found new lines.
     */
    private int newLineCount;

    /**
     * @param listenerChain see {@link #getListenerChain()}
     */
    public ConsecutiveNewLineStateChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    @Override
    public StackableChainingListener createChainingListenerInstance()
    {
        return new ConsecutiveNewLineStateChainingListener(getListenerChain());
    }

    /**
     * @return the number of found new lines.
     */
    public int getNewLineCount()
    {
        return this.newLineCount;
    }

    @Override
    public void endDefinitionDescription()
    {
        this.newLineCount = 0;
        super.endDefinitionDescription();
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endDefinitionList(parameters);
    }

    @Override
    public void endDefinitionTerm()
    {
        this.newLineCount = 0;
        super.endDefinitionTerm();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        this.newLineCount = 0;
        super.endDocument(metadata);
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endGroup(parameters);
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endFormat(format, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endLink(reference, freestanding, parameters);
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endList(type, parameters);
    }

    @Override
    public void endListItem()
    {
        this.newLineCount = 0;
        super.endListItem();
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endListItem(parameters);
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        this.newLineCount = 0;
        super.endMacroMarker(name, parameters, content, isInline);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endParagraph(parameters);
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endQuotation(parameters);
    }

    @Override
    public void endQuotationLine()
    {
        this.newLineCount = 0;
        super.endQuotationLine();
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endHeader(level, id, parameters);
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endTable(parameters);
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endTableCell(parameters);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endTableHeadCell(parameters);
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endTableRow(parameters);
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endFigure(parameters);
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.endFigureCaption(parameters);
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        this.newLineCount = 0;
        super.onRawText(text, syntax);
    }

    @Override
    public void onEmptyLines(int count)
    {
        this.newLineCount = 0;
        super.onEmptyLines(count);
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.onHorizontalLine(parameters);
    }

    @Override
    public void onId(String name)
    {
        this.newLineCount = 0;
        super.onId(name);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.onImage(reference, freestanding, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.onImage(reference, freestanding, id, parameters);
    }

    @Override
    public void onNewLine()
    {
        this.newLineCount++;
        super.onNewLine();
    }

    @Override
    public void onSpace()
    {
        this.newLineCount = 0;
        super.onSpace();
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.newLineCount = 0;
        super.onSpecialSymbol(symbol);
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        this.newLineCount = 0;
        super.onMacro(id, parameters, content, inline);
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        this.newLineCount = 0;
        super.onVerbatim(content, inline, parameters);
    }

    @Override
    public void onWord(String word)
    {
        this.newLineCount = 0;
        super.onWord(word);
    }
}

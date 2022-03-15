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
 * Allow knowing if a container block (a block which can have children) has children or not.
 *
 * @version $Id$
 * @since 2.0M3
 */
public class EmptyBlockChainingListener extends AbstractChainingListener
{
    private Deque<Boolean> containerBlockStates = new ArrayDeque<Boolean>();

    public EmptyBlockChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    public boolean isCurrentContainerBlockEmpty()
    {
        return this.containerBlockStates.peek();
    }

    // Events

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginDocument(MetaData metadata)
    {
        startContainerBlock();
        super.beginDocument(metadata);
    }

    @Override
    public void beginDefinitionDescription()
    {
        markNotEmpty();
        startContainerBlock();
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
        markNotEmpty();
        startContainerBlock();
        super.beginDefinitionList(parameters);
    }

    @Override
    public void beginDefinitionTerm()
    {
        markNotEmpty();
        startContainerBlock();
        super.beginDefinitionTerm();
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginGroup(parameters);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginFormat(format, parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginLink(reference, freestanding, parameters);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginList(type, parameters);
    }

    @Override
    public void beginListItem()
    {
        markNotEmpty();
        startContainerBlock();
        super.beginListItem();
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginListItem(parameters);
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginMacroMarker(name, parameters, content, isInline);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginParagraph(parameters);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginQuotation(parameters);
    }

    @Override
    public void beginQuotationLine()
    {
        markNotEmpty();
        startContainerBlock();
        super.beginQuotationLine();
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginHeader(level, id, parameters);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginTable(parameters);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginTableRow(parameters);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginTableCell(parameters);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginTableHeadCell(parameters);
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginSection(parameters);
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginFigure(parameters);
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        markNotEmpty();
        startContainerBlock();
        super.beginFigureCaption(parameters);
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
        stopContainerBlock();
    }

    @Override
    public void endDefinitionDescription()
    {
        super.endDefinitionDescription();
        stopContainerBlock();
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
        stopContainerBlock();
    }

    @Override
    public void endDefinitionTerm()
    {
        super.endDefinitionTerm();
        stopContainerBlock();
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        super.endFormat(format, parameters);
        stopContainerBlock();
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        super.endGroup(parameters);
        stopContainerBlock();
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
        stopContainerBlock();
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        super.endList(type, parameters);
        stopContainerBlock();
    }

    @Override
    public void endListItem()
    {
        super.endListItem();
        stopContainerBlock();
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        super.endListItem(parameters);
        stopContainerBlock();
    }
    
    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        super.endMacroMarker(name, parameters, content, isInline);
        stopContainerBlock();
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        super.endParagraph(parameters);
        stopContainerBlock();
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        super.endQuotation(parameters);
        stopContainerBlock();
    }

    @Override
    public void endQuotationLine()
    {
        super.endQuotationLine();
        stopContainerBlock();
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        super.endSection(parameters);
        stopContainerBlock();
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        super.endHeader(level, id, parameters);
        stopContainerBlock();
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        super.endTable(parameters);
        stopContainerBlock();
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        super.endTableCell(parameters);
        stopContainerBlock();
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        super.endTableHeadCell(parameters);
        stopContainerBlock();
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        super.endTableRow(parameters);
        stopContainerBlock();
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        super.endFigure(parameters);
        stopContainerBlock();
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        super.endFigureCaption(parameters);
        stopContainerBlock();
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        super.onRawText(text, syntax);
        markNotEmpty();
    }

    @Override
    public void onEmptyLines(int count)
    {
        super.onEmptyLines(count);
        markNotEmpty();
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        super.onHorizontalLine(parameters);
        markNotEmpty();
    }

    @Override
    public void onId(String name)
    {
        super.onId(name);
        markNotEmpty();
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
        markNotEmpty();
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        super.onImage(reference, freestanding, id, parameters);
        markNotEmpty();
    }

    @Override
    public void onNewLine()
    {
        super.onNewLine();
        markNotEmpty();
    }

    @Override
    public void onSpace()
    {
        super.onSpace();
        markNotEmpty();
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        super.onSpecialSymbol(symbol);
        markNotEmpty();
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        super.onVerbatim(content, inline, parameters);
        markNotEmpty();
    }

    @Override
    public void onWord(String word)
    {
        super.onWord(word);
        markNotEmpty();
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        super.onMacro(id, parameters, content, inline);
        markNotEmpty();
    }

    private void startContainerBlock()
    {
        this.containerBlockStates.push(Boolean.TRUE);
    }

    private void stopContainerBlock()
    {
        this.containerBlockStates.pop();
    }

    private void markNotEmpty()
    {
        if (!this.containerBlockStates.isEmpty() && this.containerBlockStates.peek()) {
            this.containerBlockStates.pop();
            this.containerBlockStates.push(Boolean.FALSE);
        }
    }

}

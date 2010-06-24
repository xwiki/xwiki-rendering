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
package org.xwiki.rendering.xdomxml.internal.renderer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.XMLWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxml.internal.XMLEntities;
import org.xwiki.rendering.xdomxml.internal.parameters.ParameterManager;

/**
 * Current version of the XDOM+XML stream based renderer.
 * 
 * @version $Id$
 */
public class XMLChainingStreamRenderer extends AbstractChainingPrintRenderer implements XMLEntities
{
    private ParameterManager parameterManager;

    private XMLWikiPrinter xmlWikiPrinter;

    public XMLChainingStreamRenderer(ListenerChain listenerChain, ParameterManager parameterManager)
    {
        setListenerChain(listenerChain);

        this.parameterManager = parameterManager;
    }

    // Printer

    protected XMLWikiPrinter getXMLWikiPrinter()
    {
        if (this.xmlWikiPrinter == null) {
            this.xmlWikiPrinter = new XMLWikiPrinter(getPrinter());
        }

        return this.xmlWikiPrinter;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#pushPrinter(org.xwiki.rendering.renderer.printer.WikiPrinter)
     */
    @Override
    protected void pushPrinter(WikiPrinter wikiPrinter)
    {
        super.pushPrinter(wikiPrinter);

        getXMLWikiPrinter().setWikiPrinter(getPrinter());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#popPrinter()
     */
    @Override
    protected void popPrinter()
    {
        super.popPrinter();

        getXMLWikiPrinter().setWikiPrinter(getPrinter());
    }

    // Events

    @Override
    public void beginDocument(Map<String, String> parameters)
    {
        if (parameters.isEmpty()) {
            beginEvent(EventType.BEGIN_DOCUMENT);
        } else {
            beginEvent(EventType.BEGIN_DOCUMENT, new LinkedHashMap<String, String>(parameters));
        }
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        if (parameters.isEmpty()) {
            beginEvent(EventType.BEGIN_GROUP);
        } else {
            beginEvent(EventType.BEGIN_GROUP, new LinkedHashMap<String, String>(parameters));
        }
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_FORMAT, format, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        if (parameters.isEmpty()) {
            beginEvent(EventType.BEGIN_PARAGRAPH);
        } else {
            beginEvent(EventType.BEGIN_PARAGRAPH, new LinkedHashMap<String, String>(parameters));
        }
    }

    @Override
    public void beginLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_LINK, link, isFreeStandingURI, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        if (parameters.isEmpty()) {
            beginEvent(EventType.BEGIN_SECTION);
        } else {
            beginEvent(EventType.BEGIN_SECTION, new LinkedHashMap<String, String>(parameters));
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_HEADER, level, id, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_LIST, listType, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginListItem()
    {
        beginEvent(EventType.BEGIN_LIST_ITEM);
    }

    @Override
    public void beginDefinitionTerm()
    {
        beginEvent(EventType.BEGIN_DEFINITION_TERM);
    }

    @Override
    public void beginDefinitionDescription()
    {
        beginEvent(EventType.BEGIN_DEFINITION_DESCRIPTION);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        if (parameters.isEmpty()) {
            beginEvent(EventType.BEGIN_TABLE);
        } else {
            beginEvent(EventType.BEGIN_TABLE, new LinkedHashMap<String, String>(parameters));
        }
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_TABLE_CELL, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_TABLE_HEAD_CELL, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_TABLE_ROW, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_QUOTATION, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        beginEvent(EventType.BEGIN_MACRO_MARKER, name, new LinkedHashMap<String, String>(parameters), content, isInline);
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        beginEvent(EventType.BEGIN_DEFINITION_LIST, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void beginQuotationLine()
    {
        beginEvent(EventType.BEGIN_QUOTATION_LINE);
    }

    @Override
    public void endDocument(Map<String, String> parameters)
    {
        endEvent(EventType.END_DOCUMENT);
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        endEvent(EventType.END_GROUP);
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        endEvent(EventType.END_FORMAT);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        endEvent(EventType.END_PARAGRAPH);
    }

    @Override
    public void endLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        endEvent(EventType.END_LINK);
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        endEvent(EventType.END_SECTION);
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        endEvent(EventType.END_HEADER);
    }

    @Override
    public void endList(ListType listType, Map<String, String> parameters)
    {
        endEvent(EventType.END_LIST);
    }

    @Override
    public void endListItem()
    {
        endEvent(EventType.END_LIST_ITEM);
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        endEvent(EventType.END_MACRO_MARKER);
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        endEvent(EventType.END_DEFINITION_LIST);
    }

    @Override
    public void endDefinitionTerm()
    {
        endEvent(EventType.END_DEFINITION_TERM);
    }

    @Override
    public void endDefinitionDescription()
    {
        endEvent(EventType.END_DEFINITION_DESCRIPTION);
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        endEvent(EventType.END_QUOTATION);
    }

    @Override
    public void endQuotationLine()
    {
        endEvent(EventType.END_QUOTATION_LINE);
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        endEvent(EventType.END_TABLE);
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        endEvent(EventType.END_TABLE_CELL);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        endEvent(EventType.END_TABLE_HEAD_CELL);
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        endEvent(EventType.END_TABLE_ROW);
    }

    @Override
    public void onNewLine()
    {
        onEvent(EventType.ON_NEW_LINE);
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        onEvent(EventType.ON_MACRO, id, new LinkedHashMap<String, String>(parameters), content, isInline);
    }

    @Override
    public void onWord(String word)
    {
        onEvent(EventType.ON_WORD, word);
    }

    @Override
    public void onSpace()
    {
        onEvent(EventType.ON_SPACE);
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        onEvent(EventType.ON_SPECIAL_SYMBOL, symbol);
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        onEvent(EventType.ON_RAW_TEXT, text, syntax);
    }

    @Override
    public void onId(String name)
    {
        onEvent(EventType.ON_ID, name);
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        if (parameters.isEmpty()) {
            onEvent(EventType.ON_HORIZONTAL_LINE);
        } else {
            onEvent(EventType.ON_HORIZONTAL_LINE, new LinkedHashMap<String, String>(parameters));
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        onEvent(EventType.ON_EMPTY_LINES, count);
    }

    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        onEvent(EventType.ON_VERBATIM, protectedString, isInline, new LinkedHashMap<String, String>(parameters));
    }

    @Override
    public void onImage(Image image, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        onEvent(EventType.ON_IMAGE, image, isFreeStandingURI, new LinkedHashMap<String, String>(parameters));
    }

    // Tools

    private void beginEvent(EventType eventType, Object... parameters)
    {
        getXMLWikiPrinter().printXMLStartElement(ELEM_BLOCK,
            new String[][] {{ATT_BLOCK_NAME, eventType.toString().substring("BEGIN_".length()).toLowerCase()}});

        printParameters(parameters);
    }

    private void endEvent(EventType eventType)
    {
        getXMLWikiPrinter().printXMLEndElement(ELEM_BLOCK);
    }

    private void onEvent(EventType eventType, Object... parameters)
    {
        if (parameters.length > 0) {
            getXMLWikiPrinter().printXMLStartElement(ELEM_BLOCK,
                new String[][] {{ATT_BLOCK_NAME, eventType.toString().substring("ON_".length()).toLowerCase()}});
            printParameters(parameters);
            endEvent(eventType);
        } else {
            getXMLWikiPrinter().printXMLElement(ELEM_BLOCK,
                new String[][] {{ATT_BLOCK_NAME, eventType.toString().substring("ON_".length()).toLowerCase()}});
        }
    }

    private void printParameters(Object[] parameters)
    {
        if (parameters.length > 0) {
            getXMLWikiPrinter().printXMLStartElement(ELEM_PARAMETERS);
            for (Object parameter : parameters) {
                this.parameterManager.serialize(parameter, getXMLWikiPrinter().getXMLWriter());
            }
            getXMLWikiPrinter().printXMLEndElement(ELEM_PARAMETERS);
        }
    }
}

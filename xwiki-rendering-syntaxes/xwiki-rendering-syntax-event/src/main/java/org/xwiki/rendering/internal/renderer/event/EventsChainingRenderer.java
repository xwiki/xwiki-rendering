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
package org.xwiki.rendering.internal.renderer.event;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Prints listener event names in a format useful for testing and debugging.
 *
 * @version $Id$
 * @since 1.8RC1
 */
public class EventsChainingRenderer extends AbstractChainingPrintRenderer
{
    public EventsChainingRenderer(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
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
        getPrinter().println("beginDocument" + serializeParameters(metadata.getMetaData()));
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        getPrinter().print("endDocument" + serializeParameters(metadata.getMetaData()));
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        getPrinter().println("beginGroup" + serializeParameters(parameters));
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        getPrinter().println("endGroup" + serializeParameters(parameters));
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        getPrinter().println("beginFormat [" + format + "]" + serializeParameters(parameters));
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        getPrinter().println("endFormat [" + format + "]" + serializeParameters(parameters));
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        getPrinter().println("beginParagraph" + serializeParameters(parameters));
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        getPrinter().println("endParagraph" + serializeParameters(parameters));
    }

    @Override
    public void onNewLine()
    {
        getPrinter().println("onNewLine");
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        getPrinter().println(
            "beginLink [" + reference + "] [" + freestanding + "]" + serializeParameters(parameters));
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        getPrinter().println(
            "endLink [" + reference + "] [" + freestanding + "]" + serializeParameters(parameters));
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        printMacroData("onMacro", id, parameters, content, inline);
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        getPrinter().println("beginSection" + serializeParameters(parameters));
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        getPrinter().println("beginHeader [" + level + ", " + id + "]" + serializeParameters(parameters));
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        getPrinter().println("endSection" + serializeParameters(parameters));
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        getPrinter().println("endHeader [" + level + ", " + id + "]" + serializeParameters(parameters));
    }

    @Override
    public void onWord(String word)
    {
        getPrinter().println("onWord [" + getEscaped(word) + "]");
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        getPrinter().println("beginList [" + type + "]" + serializeParameters(parameters));
    }

    @Override
    public void beginListItem()
    {
        getPrinter().println("beginListItem");
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        getPrinter().println("endList [" + type + "]" + serializeParameters(parameters));
    }

    @Override
    public void endListItem()
    {
        getPrinter().println("endListItem");
    }

    @Override
    public void onSpace()
    {
        getPrinter().println("onSpace");
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        getPrinter().println("onSpecialSymbol [" + getEscaped(Character.toString(symbol)) + "]");
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        getPrinter().println("onRawText [" + text + "] [" + syntax.toIdString() + "]");
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        printMacroData("beginMacroMarker", name, parameters, content, isInline);
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        printMacroData("endMacroMarker", name, parameters, content, isInline);
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        getPrinter().println("beginMetaData" + serializeParameters(metadata.getMetaData()));
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        getPrinter().println("endMetaData" + serializeParameters(metadata.getMetaData()));
    }

    @Override
    public void onId(String name)
    {
        getPrinter().println("onId [" + name + "]");
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        getPrinter().println("onHorizontalLine" + serializeParameters(parameters));
    }

    @Override
    public void onEmptyLines(int count)
    {
        getPrinter().println("onEmptyLines [" + count + "]");
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        getPrinter().println(
            "onVerbatim [" + content + "] [" + inline + "]" + serializeParameters(parameters));
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        getPrinter().println("beginDefinitionList" + serializeParameters(parameters));
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        getPrinter().println("endDefinitionList" + serializeParameters(parameters));
    }

    @Override
    public void beginDefinitionTerm()
    {
        getPrinter().println("beginDefinitionTerm");
    }

    @Override
    public void beginDefinitionDescription()
    {
        getPrinter().println("beginDefinitionDescription");
    }

    @Override
    public void endDefinitionTerm()
    {
        getPrinter().println("endDefinitionTerm");
    }

    @Override
    public void endDefinitionDescription()
    {
        getPrinter().println("endDefinitionDescription");
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        getPrinter().println("beginQuotation" + serializeParameters(parameters));
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        getPrinter().println("endQuotation" + serializeParameters(parameters));
    }

    @Override
    public void beginQuotationLine()
    {
        getPrinter().println("beginQuotationLine");
    }

    @Override
    public void endQuotationLine()
    {
        getPrinter().println("endQuotationLine");
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        getPrinter().println("beginTable" + serializeParameters(parameters));
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        getPrinter().println("beginTableCell" + serializeParameters(parameters));
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        getPrinter().println("beginTableHeadCell" + serializeParameters(parameters));
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        getPrinter().println("beginTableRow" + serializeParameters(parameters));
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        getPrinter().println("endTable" + serializeParameters(parameters));
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        getPrinter().println("endTableCell" + serializeParameters(parameters));
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        getPrinter().println("endTableHeadCell" + serializeParameters(parameters));
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        getPrinter().println("endTableRow" + serializeParameters(parameters));
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        getPrinter().println(
            "onImage [" + reference + "] [" + freestanding + "]" + serializeParameters(parameters));
    }

    public String getEscaped(String str)
    {
        String printableStr;

        if (str == null) {
            printableStr = null;
        } else if (StringUtils.isAsciiPrintable(str)) {
            printableStr = str;
        } else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c > 126) {
                    buffer.append("(((").append((int) c).append(")))");
                } else {
                    buffer.append(c);
                }
            }
            printableStr = buffer.toString();
        }

        return printableStr;
    }

    private void printMacroData(String eventName, String name, Map<String, String> parameters, String content,
        boolean isInline)
    {
        StringBuffer parametersBuffer = new StringBuffer();
        for (Iterator<Map.Entry<String, String>> paramsIt = parameters.entrySet().iterator(); paramsIt.hasNext();) {
            Map.Entry<String, String> entry = paramsIt.next();
            parametersBuffer.append(entry.getKey()).append("=").append(entry.getValue());
            if (paramsIt.hasNext()) {
                parametersBuffer.append("|");
            }
        }

        StringBuffer macroBuffer = new StringBuffer();

        macroBuffer.append(eventName);
        macroBuffer.append(isInline ? "Inline" : "Standalone");

        macroBuffer.append(" [");
        macroBuffer.append(name);
        macroBuffer.append("]");

        macroBuffer.append(" [");
        macroBuffer.append(parametersBuffer);
        macroBuffer.append("]");

        if (content != null) {
            macroBuffer.append(" [");
            macroBuffer.append(content);
            macroBuffer.append("]");
        }

        getPrinter().println(macroBuffer.toString());
    }

    private String serializeParameters(Map<String, ? extends Object> parameters)
    {
        // Start by sorting the parameters so that when comparing the output the parameter are listed in a fixed order.
        Map<String, ? extends Object> sortedParameters = new TreeMap<>(parameters);

        StringBuffer parametersStr = new StringBuffer();
        for (Map.Entry<String, ? extends Object> entry : sortedParameters.entrySet()) {
            String value = entry.getValue() == null ? null : entry.getValue().toString();
            String key = entry.getKey();

            if (key != null && value != null) {
                parametersStr.append('[').append(getEscaped(entry.getKey())).append(']').append('=').append('[')
                    .append(getEscaped(entry.getValue().toString())).append(']');
            }
        }

        if (parametersStr.length() > 0) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(' ').append('[');
            buffer.append(parametersStr);
            buffer.append(']');
            return buffer.toString();
        } else {
            return "";
        }
    }
}

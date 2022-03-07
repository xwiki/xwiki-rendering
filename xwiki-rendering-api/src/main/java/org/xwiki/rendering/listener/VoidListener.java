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
 * A Listener that does nothing.
 *
 * @version $Id$
 * @since 3.2RC1
 */
public class VoidListener implements Listener
{
    @Override
    public void beginDefinitionDescription()
    {
        // Do nothing.
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginDefinitionTerm()
    {
        // Do nothing.
    }

    @Override
    public void beginDocument(MetaData metadata)
    {
        // Do nothing.
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginListItem()
    {
        // Do nothing.
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        // Do nothing.
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        // Do nothing.
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginQuotationLine()
    {
        // Do nothing.
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endDefinitionDescription()
    {
        // Do nothing.
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endDefinitionTerm()
    {
        // Do nothing.
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        // Do nothing.
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endListItem()
    {
        // Do nothing.
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        // Do nothing.
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        // Do nothing.
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endQuotationLine()
    {
        // Do nothing.
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void onEmptyLines(int count)
    {
        // Do nothing.
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void onId(String name)
    {
        // Do nothing.
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        // Do nothing.
    }

    @Override
    public void onNewLine()
    {
        // Do nothing.
    }

    @Override
    public void onRawText(String content, Syntax syntax)
    {
        // Do nothing.
    }

    @Override
    public void onSpace()
    {
        // Do nothing.
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        // Do nothing.
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void onWord(String word)
    {
        // Do nothing.
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // Do nothing.
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        // Do nothing.
    }
}

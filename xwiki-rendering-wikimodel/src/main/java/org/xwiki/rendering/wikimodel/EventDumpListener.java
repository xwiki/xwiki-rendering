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
package org.xwiki.rendering.wikimodel;

/**
 * This is a dump listener generating text traces of events for every listener
 * call.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class EventDumpListener extends PrintTextListener
{
    private int fDepth;

    public EventDumpListener(IWikiPrinter printer)
    {
        super(printer);
    }

    @Override
    public void beginDefinitionDescription()
    {
        println("beginDefinitionDescription()");
        inc();
    }

    @Override
    public void beginDefinitionList(WikiParameters params)
    {
        println("beginDefinitionList([" + params + "])");
        inc();
    }

    @Override
    public void beginDefinitionTerm()
    {
        println("beginDefinitionTerm()");
        inc();
    }

    @Override
    public void beginDocument(WikiParameters params)
    {
        println("beginDocument([" + params + "])");
        inc();
    }

    @Override
    public void beginFormat(WikiFormat format)
    {
        println("beginFormat(" + format + ")");
        inc();
    }

    @Override
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        println("beginHeader(" + headerLevel + ",[" + params + "])");
        inc();
    }

    @Override
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        println("beginInfoBlock(" + infoType + ",[" + params + "])");
        inc();
    }

    @Override
    public void beginList(WikiParameters params, boolean ordered)
    {
        println("beginList([" + params + "], ordered=" + ordered + ")");
        inc();
    }

    @Override
    public void beginListItem()
    {
        println("beginListItem()");
        inc();
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        println("beginListItem([" + params + "])");
        inc();
    }

    @Override
    public void beginParagraph(WikiParameters params)
    {
        println("beginParagraph([" + params + "])");
        inc();
    }

    @Override
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        println("beginPropertyBlock('" + propertyUri + "',doc=" + doc + ")");
        inc();
    }

    @Override
    public void beginPropertyInline(String str)
    {
        println("beginPropertyInline('" + str + "')");
        inc();
    }

    @Override
    public void beginQuotation(WikiParameters params)
    {
        println("beginQuotation([" + params + "])");
        inc();
    }

    @Override
    public void beginQuotationLine()
    {
        println("beginQuotationLine()");
        inc();
    }

    @Override
    public void beginSection(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        println("beginSection([" + docLevel + "])");
        inc();
    }

    @Override
    public void beginSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        println("beginSectionContent([" + docLevel + "])");
        inc();
    }

    @Override
    public void beginTable(WikiParameters params)
    {
        println("beginTable([" + params + "])");
        inc();
    }

    @Override
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        println("beginTableCell(" + tableHead + ", [" + params + "])");
        inc();
    }

    @Override
    public void beginTableRow(WikiParameters params)
    {
        println("beginTableRow([" + params + "])");
        inc();
    }

    private void dec()
    {
        fDepth--;
    }

    @Override
    protected void endBlock()
    {
        dec();
        println("endBlock()");
    }

    @Override
    public void endDefinitionDescription()
    {
        dec();
        println("endDefinitionDescription()");
    }

    @Override
    public void endDefinitionList(WikiParameters params)
    {
        dec();
        println("endDefinitionList([" + params + "])");
    }

    @Override
    public void endDefinitionTerm()
    {
        dec();
        println("endDefinitionTerm()");
    }

    @Override
    public void endDocument(WikiParameters params)
    {
        dec();
        println("endDocument([" + params + "])");
    }

    @Override
    public void endFormat(WikiFormat format)
    {
        dec();
        println("endFormat(" + format + ")");
    }

    @Override
    public void endHeader(int headerLevel, WikiParameters params)
    {
        dec();
        println("endHeader(" + headerLevel + ", [" + params + "])");
    }

    @Override
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        dec();
        println("endInfoBlock(" + infoType + ", [" + params + "])");
    }

    @Override
    public void endList(WikiParameters params, boolean ordered)
    {
        dec();
        println("endList([" + params + "], ordered=" + ordered + ")");
    }

    @Override
    public void endListItem()
    {
        dec();
        println("endListItem()");
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        dec();
        println("endListItem()[" + params + "]");
    }

    @Override
    public void endParagraph(WikiParameters params)
    {
        dec();
        println("endParagraph([" + params + "])");
    }

    @Override
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        dec();
        println("endPropertyBlock('" + propertyUri + "', doc=" + doc + ")");
    }

    @Override
    public void endPropertyInline(String inlineProperty)
    {
        dec();
        println("endPropertyInline('" + inlineProperty + "')");
    }

    @Override
    public void endQuotation(WikiParameters params)
    {
        dec();
        println("endQuotation([" + params + "])");
    }

    @Override
    public void endQuotationLine()
    {
        dec();
        println("endQuotationLine()");
    }

    @Override
    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        dec();
        println("endSection([" + docLevel + "])");
    }

    @Override
    public void endSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        dec();
        println("endSectionContent([" + docLevel + "])");
    }

    @Override
    public void endTable(WikiParameters params)
    {
        dec();
        println("endTable([" + params + "])");
    }

    @Override
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        dec();
        println("endTableCell(" + tableHead + ", [" + params + "])");
    }

    @Override
    public void endTableRow(WikiParameters params)
    {
        dec();
        println("endTableRow([" + params + "])");
    }

    private void inc()
    {
        fDepth++;
    }

    @Override
    public void onEmptyLines(int count)
    {
        println("onEmptyLines(" + count + ")");
    }

    @Override
    public void onEscape(String str)
    {
        println("onEscape('" + str + "')");
    }

    @Override
    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        println("onExtensionBlock('" + extensionName + "', [" + params + "])");
    }

    @Override
    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        println("onExtensionInline('" + extensionName + "', [" + params + "])");
    }

    @Override
    public void onHorizontalLine(WikiParameters params)
    {
        println("onHorizontalLine([" + params + "])");
    }

    @Override
    public void onLineBreak()
    {
        println("onLineBreak()");
    }

    @Override
    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        println("onMacroBlock('"
            + macroName
            + "', "
            + params
            + ", '"
            + content
            + "')");
    }

    @Override
    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        println("onMacroInline('"
            + macroName
            + "', "
            + params
            + ", '"
            + content
            + "')");
    }

    @Override
    public void onNewLine()
    {
        println("onNewLine()");
    }

    @Override
    public void onReference(String ref)
    {
        println("onReference('" + ref + "')");
    }

    @Override
    public void onSpace(String str)
    {
        println("onSpace('" + str + "')");
    }

    @Override
    public void onSpecialSymbol(String str)
    {
        println("onSpecialSymbol('" + str + "')");
    }

    @Override
    public void onTableCaption(String str)
    {
        println("onTableCaption('" + str + "')");
    }

    @Override
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        println("onVerbatimBlock('" + str + "')");
    }

    @Override
    public void onVerbatimInline(String str, WikiParameters params)
    {
        println("onVerbatimInline('" + str + "')");
    }

    @Override
    public void onWord(String str)
    {
        println("onWord('" + str + "')");
    }

    @Override
    protected void println(String str)
    {
        for (int i = 0; i < fDepth; i++) {
            super.print("    ");
        }
        super.println(str);
    }
}

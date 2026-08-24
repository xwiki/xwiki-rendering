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
 * @version $Id$
 * @since 4.0M1
 */
public class PrintTextListener implements IWemListener
{
    private final IWikiPrinter fPrinter;

    protected ReferenceHandler fRefHandler;

    private boolean supportImage;

    private boolean supportDownload;

    public PrintTextListener(IWikiPrinter printer)
    {
        this(printer, false, false);
    }

    public PrintTextListener(IWikiPrinter printer, boolean supportImage, boolean supportDownload)
    {
        this.supportImage = supportImage;
        this.supportDownload = supportDownload;

        fPrinter = printer;
        fRefHandler = newReferenceHandler();
    }

    public boolean isSupportImage()
    {
        return supportImage;
    }

    public boolean isSupportDownload()
    {
        return supportDownload;
    }

    /**
     * @see IWemListener#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginDefinitionList(WikiParameters)
     */
    public void beginDefinitionList(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListenerDocument#beginDocument(WikiParameters)
     */
    public void beginDocument(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginFormat(WikiFormat)
     */
    public void beginFormat(WikiFormat format)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginHeader(int, WikiParameters)
     */
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginInfoBlock(String,
     *      WikiParameters)
     */
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginList(WikiParameters,
     *      boolean)
     */
    public void beginList(WikiParameters params, boolean ordered)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginListItem()
     */
    public void beginListItem()
    {
        // Nothing to print for this event.
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginParagraph(WikiParameters)
     */
    public void beginParagraph(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginPropertyBlock(java.lang.String,
     *      boolean)
     */
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginPropertyInline(java.lang.String)
     */
    public void beginPropertyInline(String str)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginQuotation(WikiParameters)
     */
    public void beginQuotation(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        //
    }

    /**
     * @see IWemListenerDocument#beginSection(int, int,
     *      WikiParameters)
     */
    public void beginSection(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        // 
    }

    /**
     * @see IWemListenerDocument#beginSectionContent(int, int,
     *      WikiParameters)
     */
    public void beginSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        // 
    }

    /**
     * @see IWemListener#beginTable(WikiParameters)
     */
    public void beginTable(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginTableCell(boolean,
     *      WikiParameters)
     */
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#beginTableRow(WikiParameters)
     */
    public void beginTableRow(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * This method is called at the end of each block element. It can be
     * overloaded in subclasses.
     */
    protected void endBlock()
    {
        //
    }

    /**
     * @see IWemListener#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        endBlock();
    }

    /**
     * @see IWemListener#endDefinitionList(WikiParameters)
     */
    public void endDefinitionList(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListenerDocument#endDocument(WikiParameters)
     */
    public void endDocument(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endFormat(WikiFormat)
     */
    public void endFormat(WikiFormat format)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#endHeader(int, WikiParameters)
     */
    public void endHeader(int headerLevel, WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endInfoBlock(String,
     *      WikiParameters)
     */
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endList(WikiParameters,
     *      boolean)
     */
    public void endList(WikiParameters params, boolean ordered)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endListItem()
     */
    public void endListItem()
    {
        endBlock();
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endParagraph(WikiParameters)
     */
    public void endParagraph(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endPropertyBlock(java.lang.String,
     *      boolean)
     */
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endPropertyInline(java.lang.String)
     */
    public void endPropertyInline(String inlineProperty)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#endQuotation(WikiParameters)
     */
    public void endQuotation(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endQuotationLine()
     */
    public void endQuotationLine()
    {
        //
    }

    /**
     * @see IWemListenerDocument#endSection(int, int,
     *      WikiParameters)
     */
    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        // 
    }

    /**
     * @see IWemListenerDocument#endSectionContent(int, int,
     *      WikiParameters)
     */
    public void endSectionContent(
        int docLevel,
        int headerLevel,
        WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endTable(WikiParameters)
     */
    public void endTable(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endTableCell(boolean, WikiParameters)
     */
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#endTableRow(WikiParameters)
     */
    public void endTableRow(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    protected ReferenceHandler newReferenceHandler()
    {
        return new ReferenceHandler(supportImage, supportDownload)
        {
            @Override
            protected void handleImage(
                String ref,
                String label,
                WikiParameters params)
            {
                handleReference(ref, label, params);
            }

            @Override
            protected void handleReference(
                String ref,
                String label,
                WikiParameters params)
            {
                print(label);
                print("<" + ref + ">");
            }
        };
    }

    public void onEmptyLines(int count)
    {
        //
    }

    /**
     * @see IWemListener#onEscape(java.lang.String)
     */
    public void onEscape(String str)
    {
        print(str);
    }

    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        //
    }

    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#onHorizontalLine(WikiParameters
     *      params)
     */
    public void onHorizontalLine(WikiParameters params)
    {
        // Nothing to print for this event.
    }

    public void onImage(String ref)
    {
        print("<img");
        print(" src='" + ref + "'");
        print(" class='wikimodel-freestanding'/>");
    }

    public void onImage(WikiReference ref)
    {
        print("<img");
        String link = ref.getLink();
        link = WikiPageUtil.escapeXmlAttribute(link);
        print(" src='" + link + "'");
        WikiParameters params = ref.getParameters();
        String label = ref.getLabel();
        if (label != null && params.getParameter("title") == null) {
            params = params.addParameter("title", label);
        }
        print(params + "/>");
    }

    /**
     * @see IWemListener#onLineBreak()
     */
    public void onLineBreak()
    {
        println("");
    }

    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        // Nothing to print for this event.
    }

    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#onNewLine()
     */
    public void onNewLine()
    {
        println("");
    }

    /**
     * @see IWemListener#onReference(java.lang.String)
     */
    public void onReference(String ref)
    {
        WikiReference reference = new WikiReference(ref);
        onReference(reference);
    }

    public void onReference(WikiReference ref)
    {
        fRefHandler.handle(ref);
    }

    /**
     * @see IWemListener#onSpace(java.lang.String)
     */
    public void onSpace(String str)
    {
        print(str);
    }

    /**
     * @see IWemListener#onSpecialSymbol(java.lang.String)
     */
    public void onSpecialSymbol(String str)
    {
        print(str);
    }

    /**
     * @see IWemListener#onTableCaption(java.lang.String)
     */
    public void onTableCaption(String str)
    {
        // Nothing to print for this event.
    }

    /**
     * @see IWemListener#onVerbatimBlock(String,
     *      WikiParameters)
     */
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        print(str);
    }

    /**
     * @see IWemListener#onVerbatimInline(java.lang.String,
     *      WikiParameters)
     */
    public void onVerbatimInline(String str, WikiParameters params)
    {
        print(str);
    }

    /**
     * @see IWemListener#onWord(java.lang.String)
     */
    public void onWord(String str)
    {
        print(str);
    }

    protected void print(String str)
    {
        fPrinter.print(str);
    }

    protected void println()
    {
        fPrinter.println("");
    }

    protected void println(String str)
    {
        fPrinter.println(str);
    }
}

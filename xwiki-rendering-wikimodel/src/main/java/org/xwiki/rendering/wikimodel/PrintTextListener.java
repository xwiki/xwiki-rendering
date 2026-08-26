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
public class PrintTextListener extends EmptyWemListener
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
    @Override
    public void endDefinitionDescription()
    {
        endBlock();
    }

    /**
     * @see IWemListener#endDefinitionList(WikiParameters)
     */
    @Override
    public void endDefinitionList(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListenerDocument#beginDocument(WikiParameters)
     */
    @Override
    public void beginDocument(WikiParameters params)
    {
        // Overridden to stay a no-op. EmptyWemListener#beginDocument(WikiParameters) forwards to the no-arg
        // beginDocument(), which would invoke a subclass override of it; nothing is printed for this event.
    }

    /**
     * @see IWemListenerDocument#endDocument(WikiParameters)
     */
    @Override
    public void endDocument(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endHeader(int, WikiParameters)
     */
    @Override
    public void endHeader(int headerLevel, WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endInfoBlock(String,
     *      WikiParameters)
     */
    @Override
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endList(WikiParameters,
     *      boolean)
     */
    @Override
    public void endList(WikiParameters params, boolean ordered)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endListItem()
     */
    @Override
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
    @Override
    public void endParagraph(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endPropertyBlock(java.lang.String,
     *      boolean)
     */
    @Override
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endQuotation(WikiParameters)
     */
    @Override
    public void endQuotation(WikiParameters params)
    {
        endBlock();
    }

    /**
     * @see IWemListener#endTable(WikiParameters)
     */
    @Override
    public void endTable(WikiParameters params)
    {
        endBlock();
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

    /**
     * @see IWemListener#onEscape(java.lang.String)
     */
    @Override
    public void onEscape(String str)
    {
        print(str);
    }

    @Override
    public void onImage(String ref)
    {
        print("<img");
        print(" src='" + ref + "'");
        print(" class='wikimodel-freestanding'/>");
    }

    @Override
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
    @Override
    public void onLineBreak()
    {
        println("");
    }

    /**
     * @see IWemListener#onNewLine()
     */
    @Override
    public void onNewLine()
    {
        println("");
    }

    /**
     * @see IWemListener#onReference(java.lang.String)
     */
    @Override
    public void onReference(String ref)
    {
        WikiReference reference = new WikiReference(ref);
        onReference(reference);
    }

    @Override
    public void onReference(WikiReference ref)
    {
        fRefHandler.handle(ref);
    }

    /**
     * @see IWemListener#onSpace(java.lang.String)
     */
    @Override
    public void onSpace(String str)
    {
        print(str);
    }

    /**
     * @see IWemListener#onSpecialSymbol(java.lang.String)
     */
    @Override
    public void onSpecialSymbol(String str)
    {
        print(str);
    }

    /**
     * @see IWemListener#onVerbatimBlock(String,
     *      WikiParameters)
     */
    @Override
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        print(str);
    }

    /**
     * @see IWemListener#onVerbatimInline(java.lang.String,
     *      WikiParameters)
     */
    @Override
    public void onVerbatimInline(String str, WikiParameters params)
    {
        print(str);
    }

    /**
     * @see IWemListener#onWord(java.lang.String)
     */
    @Override
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

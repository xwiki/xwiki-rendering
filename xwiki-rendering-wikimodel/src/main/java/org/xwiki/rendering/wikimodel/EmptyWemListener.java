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
public class EmptyWemListener implements IWemListener
{
    public EmptyWemListener()
    {
        //
    }

    /**
     * @see IWemListener#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        //
    }

    /**
     * @see IWemListener#beginDefinitionList(WikiParameters)
     */
    public void beginDefinitionList(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        //
    }

    /**
     * @see IWemListener#beginDocument(WikiParameters)
     */
    public void beginDocument()
    {
        //
    }

    /**
     * @see IWemListenerDocument#beginDocument(WikiParameters)
     */
    public void beginDocument(WikiParameters params)
    {
        beginDocument();
    }

    /**
     * @see IWemListener#beginFormat(WikiFormat)
     */
    public void beginFormat(WikiFormat format)
    {
        //
    }

    /**
     * @see IWemListener#beginHeader(int, WikiParameters)
     */
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginInfoBlock(String, WikiParameters)
     */
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginList(WikiParameters, boolean)
     */
    public void beginList(WikiParameters params, boolean ordered)
    {
        //
    }

    /**
     * @see IWemListener#beginListItem()
     */
    public void beginListItem()
    {
        //
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        //    
    }

    /**
     * @see IWemListener#beginParagraph(WikiParameters)
     */
    public void beginParagraph(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginPropertyBlock(java.lang.String, boolean)
     */
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        //
    }

    /**
     * @see IWemListener#beginPropertyInline(java.lang.String)
     */
    public void beginPropertyInline(String str)
    {
        //
    }

    /**
     * @see IWemListener#beginQuotation(WikiParameters)
     */
    public void beginQuotation(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        //
    }

    /**
     * @see IWemListenerDocument#beginSection(int, int, WikiParameters)
     */
    public void beginSection(int docLevel, int headerLevel, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListenerDocument#beginSectionContent(int, int, WikiParameters)
     */
    public void beginSectionContent(int docLevel, int headerLevel, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginTable(WikiParameters)
     */
    public void beginTable(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginTableCell(boolean, WikiParameters)
     */
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#beginTableRow(WikiParameters)
     */
    public void beginTableRow(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        //
    }

    /**
     * @see IWemListener#endDefinitionList(WikiParameters)
     */
    public void endDefinitionList(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        //
    }

    /**
     * @see IWemListener#endDocument(WikiParameters)
     */
    public void endDocument()
    {
        //
    }

    /**
     * @see IWemListenerDocument#endDocument(WikiParameters)
     */
    public void endDocument(WikiParameters params)
    {
        endDocument();
    }

    /**
     * @see IWemListener#endFormat(WikiFormat)
     */
    public void endFormat(WikiFormat format)
    {
        //
    }

    /**
     * @see IWemListener#endHeader(int, WikiParameters)
     */
    public void endHeader(int headerLevel, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endInfoBlock(String, WikiParameters)
     */
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endList(WikiParameters, boolean)
     */
    public void endList(WikiParameters params, boolean ordered)
    {
        //
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        // 
    }
    
    /**
     * @see IWemListener#endListItem()
     */
    public void endListItem()
    {
        //
    }

    /**
     * @see IWemListener#endParagraph(WikiParameters)
     */
    public void endParagraph(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endPropertyBlock(java.lang.String, boolean)
     */
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        //
    }

    /**
     * @see IWemListener#endPropertyInline(java.lang.String)
     */
    public void endPropertyInline(String inlineProperty)
    {
        //
    }

    /**
     * @see IWemListener#endQuotation(WikiParameters)
     */
    public void endQuotation(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endQuotationLine()
     */
    public void endQuotationLine()
    {
        //
    }

    /**
     * @see IWemListenerDocument#endSection(int, int, WikiParameters)
     */
    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListenerDocument#endSectionContent(int, int, WikiParameters)
     */
    public void endSectionContent(int docLevel, int headerLevel, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endTable(WikiParameters)
     */
    public void endTable(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endTableCell(boolean, WikiParameters)
     */
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#endTableRow(WikiParameters)
     */
    public void endTableRow(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#onEmptyLines(int)
     */
    public void onEmptyLines(int count)
    {
        //
    }

    /**
     * @see IWemListener#onEscape(java.lang.String)
     */
    public void onEscape(String str)
    {
        //
    }

    /**
     * @see IWemListener#onExtensionBlock(java.lang.String, WikiParameters)
     */
    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#onExtensionInline(java.lang.String, WikiParameters)
     */
    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#onHorizontalLine(WikiParameters params)
     */
    public void onHorizontalLine(WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListenerInline#onImage(java.lang.String)
     */
    public void onImage(String ref)
    {
        //
    }

    /**
     * @see IWemListenerInline#onImage(WikiReference)
     */
    public void onImage(WikiReference ref)
    {
        //
    }

    /**
     * @see IWemListener#onLineBreak()
     */
    public void onLineBreak()
    {
        //
    }

    /**
     * @see IWemListener#onMacroBlock(java.lang.String, WikiParameters, java.lang.String)
     */
    public void onMacroBlock(String macroName, WikiParameters params, String content)
    {
        //
    }

    /**
     * @see IWemListener#onMacroInline(java.lang.String, WikiParameters, java.lang.String)
     */
    public void onMacroInline(String macroName, WikiParameters params, String content)
    {
        //
    }

    /**
     * @see IWemListener#onNewLine()
     */
    public void onNewLine()
    {
        //
    }

    /**
     * @see IWemListener#onReference(java.lang.String)
     */
    public void onReference(String ref)
    {
        //
    }

    public void onReference(WikiReference ref)
    {
        //
    }

    /**
     * @see IWemListener#onSpace(java.lang.String)
     */
    public void onSpace(String str)
    {
        //
    }

    /**
     * @see IWemListener#onSpecialSymbol(java.lang.String)
     */
    public void onSpecialSymbol(String str)
    {
        //
    }

    /**
     * @see IWemListener#onTableCaption(java.lang.String)
     */
    public void onTableCaption(String str)
    {
        //
    }

    /**
     * @see IWemListener#onVerbatimBlock(String, WikiParameters)
     */
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#onVerbatimInline(java.lang.String, WikiParameters)
     */
    public void onVerbatimInline(String str, WikiParameters params)
    {
        //
    }

    /**
     * @see IWemListener#onWord(java.lang.String)
     */
    public void onWord(String str)
    {
        //
    }
}

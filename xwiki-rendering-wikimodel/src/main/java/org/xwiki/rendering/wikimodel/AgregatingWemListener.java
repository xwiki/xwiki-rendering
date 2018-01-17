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
public class AgregatingWemListener implements IWemListener
{
    protected IWemListenerSimpleBlocks fBlockListener;

    protected IWemListenerDocument fDocumentListener;

    protected IWemListenerInline fInlineListener;

    protected IWemListenerList fListListener;

    protected IWemListenerProgramming fProgrammingListener;

    protected IWemListenerSemantic fSemanticListener;

    protected IWemListenerTable fTableListener;

    public AgregatingWemListener()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see IWemListenerList#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        if (fListListener != null) {
            fListListener.beginDefinitionDescription();
        }
    }

    /**
     * @see IWemListenerList#beginDefinitionList(WikiParameters)
     */
    public void beginDefinitionList(WikiParameters params)
    {
        if (fListListener != null) {
            fListListener.beginDefinitionList(params);
        }
    }

    /**
     * @see IWemListenerList#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        if (fListListener != null) {
            fListListener.beginDefinitionTerm();
        }
    }

    /**
     * @see IWemListenerDocument#beginDocument(WikiParameters)
     */
    public void beginDocument(WikiParameters params)
    {
        if (fDocumentListener != null) {
            fDocumentListener.beginDocument(params);
        }
    }

    /**
     * @see IWemListenerInline#beginFormat(WikiFormat)
     */
    public void beginFormat(WikiFormat format)
    {
        if (fInlineListener != null) {
            fInlineListener.beginFormat(format);
        }
    }

    /**
     * @see IWemListenerDocument#beginHeader(int,
     *      WikiParameters)
     */
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        if (fDocumentListener != null) {
            fDocumentListener.beginHeader(headerLevel, params);
        }
    }

    /**
     * @see IWemListenerSimpleBlocks#beginInfoBlock(java.lang.String,
     *      WikiParameters)
     */
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        if (fBlockListener != null) {
            fBlockListener.beginInfoBlock(infoType, params);
        }
    }

    /**
     * @see IWemListenerList#beginList(WikiParameters,
     *      boolean)
     */
    public void beginList(WikiParameters params, boolean ordered)
    {
        if (fListListener != null) {
            fListListener.beginList(params, ordered);
        }
    }

    /**
     * @see IWemListenerList#beginListItem()
     */
    public void beginListItem()
    {
        if (fListListener != null) {
            fListListener.beginListItem();
        }
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        if (fListListener != null) {
            fListListener.beginListItem(params);
        }
    }
    
    /**
     * @see IWemListenerSimpleBlocks#beginParagraph(WikiParameters)
     */
    public void beginParagraph(WikiParameters params)
    {
        if (fBlockListener != null) {
            fBlockListener.beginParagraph(params);
        }
    }

    /**
     * @see IWemListenerSemantic#beginPropertyBlock(java.lang.String,
     *      boolean)
     */
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        if (fSemanticListener != null) {
            fSemanticListener.beginPropertyBlock(propertyUri, doc);
        }
    }

    /**
     * @see IWemListenerSemantic#beginPropertyInline(java.lang.String)
     */
    public void beginPropertyInline(String propertyUri)
    {
        if (fSemanticListener != null) {
            fSemanticListener.beginPropertyInline(propertyUri);
        }
    }

    /**
     * @see IWemListenerList#beginQuotation(WikiParameters)
     */
    public void beginQuotation(WikiParameters params)
    {
        if (fListListener != null) {
            fListListener.beginQuotation(params);
        }
    }

    /**
     * @see IWemListenerList#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        if (fListListener != null) {
            fListListener.beginQuotationLine();
        }
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
        if (fDocumentListener != null) {
            fDocumentListener.beginSection(docLevel, headerLevel, params);
        }
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
        if (fDocumentListener != null) {
            fDocumentListener.beginSectionContent(
                docLevel,
                headerLevel,
                params);
        }
    }

    /**
     * @see IWemListenerTable#beginTable(WikiParameters)
     */
    public void beginTable(WikiParameters params)
    {
        if (fTableListener != null) {
            fTableListener.beginTable(params);
        }
    }

    /**
     * @see IWemListenerTable#beginTableCell(boolean,
     *      WikiParameters)
     */
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        if (fTableListener != null) {
            fTableListener.beginTableCell(tableHead, params);
        }
    }

    /**
     * @see IWemListenerTable#beginTableRow(WikiParameters)
     */
    public void beginTableRow(WikiParameters params)
    {
        if (fTableListener != null) {
            fTableListener.beginTableRow(params);
        }
    }

    /**
     * @see IWemListenerList#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        if (fListListener != null) {
            fListListener.endDefinitionDescription();
        }
    }

    /**
     * @see IWemListenerList#endDefinitionList(WikiParameters)
     */
    public void endDefinitionList(WikiParameters params)
    {
        if (fListListener != null) {
            fListListener.beginDefinitionList(params);
        }
    }

    /**
     * @see IWemListenerList#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        if (fListListener != null) {
            fListListener.endDefinitionTerm();
        }
    }

    /**
     * @see IWemListenerDocument#endDocument(WikiParameters)
     */
    public void endDocument(WikiParameters params)
    {
        if (fDocumentListener != null) {
            fDocumentListener.endDocument(params);
        }
    }

    /**
     * @see IWemListenerInline#endFormat(WikiFormat)
     */
    public void endFormat(WikiFormat format)
    {
        if (fInlineListener != null) {
            fInlineListener.endFormat(format);
        }
    }

    /**
     * @see IWemListenerDocument#endHeader(int,
     *      WikiParameters)
     */
    public void endHeader(int headerLevel, WikiParameters params)
    {
        if (fDocumentListener != null) {
            fDocumentListener.endHeader(headerLevel, params);
        }
    }

    /**
     * @see IWemListenerSimpleBlocks#endInfoBlock(java.lang.String,
     *      WikiParameters)
     */
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        if (fBlockListener != null) {
            fBlockListener.endInfoBlock(infoType, params);
        }
    }

    /**
     * @see IWemListenerList#endList(WikiParameters,
     *      boolean)
     */
    public void endList(WikiParameters params, boolean ordered)
    {
        if (fListListener != null) {
            fListListener.endList(params, ordered);
        }
    }

    /**
     * @see IWemListenerList#endListItem()
     */
    public void endListItem()
    {
        if (fListListener != null) {
            fListListener.endListItem();
        }
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        if (fListListener != null) {
            fListListener.endListItem(params);
        }
    }

    /**
     * @see IWemListenerSimpleBlocks#endParagraph(WikiParameters)
     */
    public void endParagraph(WikiParameters params)
    {
        if (fBlockListener != null) {
            fBlockListener.endParagraph(params);
        }
    }

    /**
     * @see IWemListenerSemantic#endPropertyBlock(java.lang.String,
     *      boolean)
     */
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        if (fSemanticListener != null) {
            fSemanticListener.endPropertyBlock(propertyUri, doc);
        }
    }

    /**
     * @see IWemListenerSemantic#endPropertyInline(java.lang.String)
     */
    public void endPropertyInline(String propertyUri)
    {
        if (fSemanticListener != null) {
            fSemanticListener.endPropertyInline(propertyUri);
        }
    }

    /**
     * @see IWemListenerList#endQuotation(WikiParameters)
     */
    public void endQuotation(WikiParameters params)
    {
        if (fListListener != null) {
            fListListener.endQuotation(params);
        }
    }

    /**
     * @see IWemListenerList#endQuotationLine()
     */
    public void endQuotationLine()
    {
        if (fListListener != null) {
            fListListener.endQuotationLine();
        }
    }

    /**
     * @see IWemListenerDocument#endSection(int, int,
     *      WikiParameters)
     */
    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        if (fDocumentListener != null) {
            fDocumentListener.endSection(docLevel, headerLevel, params);
        }
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
        if (fDocumentListener != null) {
            fDocumentListener.endSectionContent(docLevel, headerLevel, params);
        }
    }

    /**
     * @see IWemListenerTable#endTable(WikiParameters)
     */
    public void endTable(WikiParameters params)
    {
        if (fTableListener != null) {
            fTableListener.endTable(params);
        }
    }

    /**
     * @see IWemListenerTable#endTableCell(boolean,
     *      WikiParameters)
     */
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        if (fTableListener != null) {
            fTableListener.endTableCell(tableHead, params);
        }
    }

    /**
     * @see IWemListenerTable#endTableRow(WikiParameters)
     */
    public void endTableRow(WikiParameters params)
    {
        if (fTableListener != null) {
            fTableListener.endTableRow(params);
        }
    }

    /**
     * @return the blockListener
     */
    public IWemListenerSimpleBlocks getBlockListener()
    {
        return fBlockListener;
    }

    /**
     * @return the documentListener
     */
    public IWemListenerDocument getDocumentListener()
    {
        return fDocumentListener;
    }

    /**
     * @return the inlineListener
     */
    public IWemListenerInline getInlineListener()
    {
        return fInlineListener;
    }

    /**
     * @return the listListener
     */
    public IWemListenerList getListListener()
    {
        return fListListener;
    }

    /**
     * @return the programmingListener
     */
    public IWemListenerProgramming getProgrammingListener()
    {
        return fProgrammingListener;
    }

    /**
     * @return the semanticListener
     */
    public IWemListenerSemantic getSemanticListener()
    {
        return fSemanticListener;
    }

    /**
     * @return the tableListener
     */
    public IWemListenerTable getTableListener()
    {
        return fTableListener;
    }

    /**
     * @see IWemListenerSimpleBlocks#onEmptyLines(int)
     */
    public void onEmptyLines(int count)
    {
        if (fBlockListener != null) {
            fBlockListener.onEmptyLines(count);
        }
    }

    /**
     * @see IWemListenerInline#onEscape(java.lang.String)
     */
    public void onEscape(String str)
    {
        if (fInlineListener != null) {
            fInlineListener.onEscape(str);
        }
    }

    /**
     * @see IWemListenerProgramming#onExtensionBlock(java.lang.String,
     *      WikiParameters)
     */
    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        if (fProgrammingListener != null) {
            fProgrammingListener.onExtensionBlock(extensionName, params);
        }
    }

    /**
     * @see IWemListenerProgramming#onExtensionInline(java.lang.String,
     *      WikiParameters)
     */
    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        if (fProgrammingListener != null) {
            fProgrammingListener.onExtensionInline(extensionName, params);
        }
    }

    /**
     * @see IWemListenerSimpleBlocks#onHorizontalLine(WikiParameters)
     */
    public void onHorizontalLine(WikiParameters params)
    {
        if (fBlockListener != null) {
            fBlockListener.onHorizontalLine(params);
        }
    }

    /**
     * @see IWemListenerInline#onImage(java.lang.String)
     */
    public void onImage(String ref)
    {
        if (fInlineListener != null) {
            fInlineListener.onImage(ref);
        }
    }

    /**
     * @see IWemListenerInline#onImage(WikiReference)
     */
    public void onImage(WikiReference ref)
    {
        if (fInlineListener != null) {
            fInlineListener.onImage(ref);
        }
    }

    /**
     * @see IWemListenerInline#onLineBreak()
     */
    public void onLineBreak()
    {
        if (fInlineListener != null) {
            fInlineListener.onLineBreak();
        }
    }

    /**
     * @see IWemListenerProgramming#onMacroBlock(java.lang.String,
     *      WikiParameters, java.lang.String)
     */
    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        if (fProgrammingListener != null) {
            fProgrammingListener.onMacroBlock(macroName, params, content);
        }
    }

    /**
     * @see IWemListenerProgramming#onMacroInline(java.lang.String,
     *      WikiParameters, java.lang.String)
     */
    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        if (fProgrammingListener != null) {
            fProgrammingListener.onMacroInline(macroName, params, content);
        }
    }

    /* èèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèèè */

    /**
     * @see IWemListenerInline#onNewLine()
     */
    public void onNewLine()
    {
        if (fInlineListener != null) {
            fInlineListener.onNewLine();
        }
    }

    /**
     * @see IWemListenerInline#onReference(java.lang.String)
     */
    public void onReference(String ref)
    {
        if (fInlineListener != null) {
            fInlineListener.onReference(ref);
        }
    }

    /**
     * @see IWemListenerInline#onReference(WikiReference)
     */
    public void onReference(WikiReference ref)
    {
        if (fInlineListener != null) {
            fInlineListener.onReference(ref);
        }
    }

    /**
     * @see IWemListenerInline#onSpace(java.lang.String)
     */
    public void onSpace(String str)
    {
        if (fInlineListener != null) {
            fInlineListener.onSpace(str);
        }
    }

    /**
     * @see IWemListenerInline#onSpecialSymbol(java.lang.String)
     */
    public void onSpecialSymbol(String str)
    {
        if (fInlineListener != null) {
            fInlineListener.onSpecialSymbol(str);
        }
    }

    /**
     * @see IWemListenerTable#onTableCaption(java.lang.String)
     */
    public void onTableCaption(String str)
    {
        if (fTableListener != null) {
            fTableListener.onTableCaption(str);
        }
    }

    /**
     * @see IWemListenerSimpleBlocks#onVerbatimBlock(java.lang.String,
     *      WikiParameters)
     */
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        if (fBlockListener != null) {
            fBlockListener.onVerbatimBlock(str, params);
        }
    }

    /**
     * @see IWemListenerInline#onVerbatimInline(java.lang.String,
     *      WikiParameters)
     */
    public void onVerbatimInline(String str, WikiParameters params)
    {
        if (fInlineListener != null) {
            fInlineListener.onVerbatimInline(str, params);
        }
    }

    /**
     * @see IWemListenerInline#onWord(java.lang.String)
     */
    public void onWord(String str)
    {
        if (fInlineListener != null) {
            fInlineListener.onWord(str);
        }
    }

    /**
     * @param blockListener the blockListener to set
     */
    public void setBlockListener(IWemListenerSimpleBlocks blockListener)
    {
        fBlockListener = blockListener;
    }

    /**
     * @param documentListener the documentListener to set
     */
    public void setDocumentListener(IWemListenerDocument documentListener)
    {
        fDocumentListener = documentListener;
    }

    /**
     * @param inlineListener the inlineListener to set
     */
    public void setInlineListener(IWemListenerInline inlineListener)
    {
        fInlineListener = inlineListener;
    }

    /**
     * @param listListener the listListener to set
     */
    public void setListListener(IWemListenerList listListener)
    {
        fListListener = listListener;
    }

    /**
     * @param programmingListener the programmingListener to set
     */
    public void setProgrammingListener(
        IWemListenerProgramming programmingListener)
    {
        fProgrammingListener = programmingListener;
    }

    /**
     * @param semanticListener the semanticListener to set
     */
    public void setSemanticListener(IWemListenerSemantic semanticListener)
    {
        fSemanticListener = semanticListener;
    }

    /**
     * @param tableListener the tableListener to set
     */
    public void setTableListener(IWemListenerTable tableListener)
    {
        fTableListener = tableListener;
    }
}

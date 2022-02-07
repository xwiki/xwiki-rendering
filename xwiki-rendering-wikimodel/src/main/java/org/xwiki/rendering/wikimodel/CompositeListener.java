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
 * A composite listener which delegates each listener method call to multiple
 * listeners registered in this composite listener.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class CompositeListener implements IWemListener
{
    /**
     * An internal list of listeners to which each method call will be
     * delegated.
     */
    private final IWemListener[] fListeners;

    /**
     * @param listeners an array of listeners to which all method calls will be
     * delegated
     */
    public CompositeListener(IWemListener... listeners)
    {
        fListeners = listeners;
    }

    /**
     * @see IWemListener#beginDefinitionDescription()
     */
    public void beginDefinitionDescription()
    {
        for (IWemListener listener : fListeners) {
            listener.beginDefinitionDescription();
        }
    }

    /**
     * @see IWemListener#beginDefinitionList(WikiParameters)
     */
    public void beginDefinitionList(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginDefinitionList(params);
        }
    }

    /**
     * @see IWemListener#beginDefinitionTerm()
     */
    public void beginDefinitionTerm()
    {
        for (IWemListener listener : fListeners) {
            listener.beginDefinitionTerm();
        }
    }

    /**
     * @see IWemListenerDocument#beginDocument(WikiParameters)
     */
    public void beginDocument(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginDocument(params);
        }
    }

    /**
     * @see IWemListener#beginFormat(WikiFormat)
     */
    public void beginFormat(WikiFormat format)
    {
        for (IWemListener listener : fListeners) {
            listener.beginFormat(format);
        }
    }

    /**
     * @see IWemListener#beginHeader(int, WikiParameters)
     */
    public void beginHeader(int headerLevel, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginHeader(headerLevel, params);
        }
    }

    /**
     * @see IWemListener#beginInfoBlock(String,
     *      WikiParameters)
     */
    public void beginInfoBlock(String infoType, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginInfoBlock(infoType, params);
        }
    }

    /**
     * @see IWemListener#beginList(WikiParameters,
     *      boolean)
     */
    public void beginList(WikiParameters params, boolean ordered)
    {
        for (IWemListener listener : fListeners) {
            listener.beginList(params, ordered);
        }
    }

    /**
     * @see IWemListener#beginListItem()
     */
    public void beginListItem()
    {
        for (IWemListener listener : fListeners) {
            listener.beginListItem();
        }
    }

    @Override
    public void beginListItem(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginListItem(params);
        }
    }

    /**
     * @see IWemListener#beginParagraph(WikiParameters)
     */
    public void beginParagraph(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginParagraph(params);
        }
    }

    /**
     * @see IWemListener#beginPropertyBlock(java.lang.String,
     *      boolean)
     */
    public void beginPropertyBlock(String propertyUri, boolean doc)
    {
        for (IWemListener listener : fListeners) {
            listener.beginPropertyBlock(propertyUri, doc);
        }
    }

    /**
     * @see IWemListener#beginPropertyInline(java.lang.String)
     */
    public void beginPropertyInline(String str)
    {
        for (IWemListener listener : fListeners) {
            listener.beginPropertyInline(str);
        }
    }

    /**
     * @see IWemListener#beginQuotation(WikiParameters)
     */
    public void beginQuotation(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginQuotation(params);
        }
    }

    /**
     * @see IWemListener#beginQuotationLine()
     */
    public void beginQuotationLine()
    {
        for (IWemListener listener : fListeners) {
            listener.beginQuotationLine();
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
        for (IWemListener listener : fListeners) {
            listener.beginSection(docLevel, headerLevel, params);
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
        for (IWemListener listener : fListeners) {
            listener.beginSectionContent(docLevel, headerLevel, params);
        }
    }

    /**
     * @see IWemListener#beginTable(WikiParameters)
     */
    public void beginTable(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginTable(params);
        }
    }

    /**
     * @see IWemListener#beginTableCell(boolean,
     *      WikiParameters)
     */
    public void beginTableCell(boolean tableHead, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginTableCell(tableHead, params);
        }
    }

    /**
     * @see IWemListener#beginTableRow(WikiParameters)
     */
    public void beginTableRow(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginTableRow(params);
        }
    }

    /**
     * @see IWemListener#endDefinitionDescription()
     */
    public void endDefinitionDescription()
    {
        for (IWemListener listener : fListeners) {
            listener.endDefinitionDescription();
        }
    }

    /**
     * @see IWemListener#endDefinitionList(WikiParameters)
     */
    public void endDefinitionList(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endDefinitionList(params);
        }
    }

    /**
     * @see IWemListener#endDefinitionTerm()
     */
    public void endDefinitionTerm()
    {
        for (IWemListener listener : fListeners) {
            listener.endDefinitionTerm();
        }
    }

    /**
     * @see IWemListenerDocument#endDocument(WikiParameters)
     */
    public void endDocument(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endDocument(params);
        }
    }

    /**
     * @see IWemListener#endFormat(WikiFormat)
     */
    public void endFormat(WikiFormat format)
    {
        for (IWemListener listener : fListeners) {
            listener.endFormat(format);
        }
    }

    /**
     * @see IWemListener#endHeader(int, WikiParameters)
     */
    public void endHeader(int headerLevel, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endHeader(headerLevel, params);
        }
    }

    /**
     * @see IWemListener#endInfoBlock(String,
     *      WikiParameters)
     */
    public void endInfoBlock(String infoType, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endInfoBlock(infoType, params);
        }
    }

    /**
     * @see IWemListener#endList(WikiParameters,
     *      boolean)
     */
    public void endList(WikiParameters params, boolean ordered)
    {
        for (IWemListener listener : fListeners) {
            listener.endList(params, ordered);
        }
    }

    /**
     * @see IWemListener#endListItem()
     */
    public void endListItem()
    {
        for (IWemListener listener : fListeners) {
            listener.endListItem();
        }
    }

    @Override
    public void endListItem(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endListItem(params);
        }
    }

    /**
     * @see IWemListener#endParagraph(WikiParameters)
     */
    public void endParagraph(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endParagraph(params);
        }
    }

    /**
     * @see IWemListener#endPropertyBlock(java.lang.String,
     *      boolean)
     */
    public void endPropertyBlock(String propertyUri, boolean doc)
    {
        for (IWemListener listener : fListeners) {
            listener.endPropertyBlock(propertyUri, doc);
        }
    }

    /**
     * @see IWemListener#endPropertyInline(java.lang.String)
     */
    public void endPropertyInline(String inlineProperty)
    {
        for (IWemListener listener : fListeners) {
            listener.endPropertyInline(inlineProperty);
        }
    }

    /**
     * @see IWemListener#endQuotation(WikiParameters)
     */
    public void endQuotation(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endQuotation(params);
        }
    }

    /**
     * @see IWemListener#endQuotationLine()
     */
    public void endQuotationLine()
    {
        for (IWemListener listener : fListeners) {
            listener.endQuotationLine();
        }
    }

    /**
     * @see IWemListenerDocument#endSection(int, int,
     *      WikiParameters)
     */
    public void endSection(int docLevel, int headerLevel, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endSection(docLevel, headerLevel, params);
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
        for (IWemListener listener : fListeners) {
            listener.endSectionContent(docLevel, headerLevel, params);
        }
    }

    /**
     * @see IWemListener#endTable(WikiParameters)
     */
    public void endTable(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endTable(params);
        }
    }

    /**
     * @see IWemListener#endTableCell(boolean, WikiParameters)
     */
    public void endTableCell(boolean tableHead, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endTableCell(tableHead, params);
        }
    }

    /**
     * @see IWemListener#endTableRow(WikiParameters)
     */
    public void endTableRow(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endTableRow(params);
        }
    }

    public void onEmptyLines(int count)
    {
        for (IWemListener listener : fListeners) {
            listener.onEmptyLines(count);
        }
    }

    /**
     * @see IWemListener#onEscape(java.lang.String)
     */
    public void onEscape(String str)
    {
        for (IWemListener listener : fListeners) {
            listener.onEscape(str);
        }
    }

    /**
     * @see IWemListener#onExtensionBlock(java.lang.String,
     *      WikiParameters)
     */
    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.onExtensionBlock(extensionName, params);
        }
    }

    /**
     * @see IWemListener#onExtensionInline(java.lang.String,
     *      WikiParameters)
     */
    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.onExtensionInline(extensionName, params);
        }
    }

    /**
     * @see IWemListener#onHorizontalLine(WikiParameters
     *      params)
     */
    public void onHorizontalLine(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.onHorizontalLine(params);
        }
    }

    /**
     * @see IWemListenerInline#onImage(java.lang.String)
     */
    public void onImage(String ref)
    {
        for (IWemListener listener : fListeners) {
            listener.onImage(ref);
        }
    }

    /**
     * @see IWemListenerInline#onImage(WikiReference)
     */
    public void onImage(WikiReference ref)
    {
        for (IWemListener listener : fListeners) {
            listener.onImage(ref);
        }
    }

    /**
     * @see IWemListener#onLineBreak()
     */
    public void onLineBreak()
    {
        for (IWemListener listener : fListeners) {
            listener.onLineBreak();
        }
    }

    public void onMacroBlock(
        String macroName,
        WikiParameters params,
        String content)
    {
        for (IWemListener listener : fListeners) {
            listener.onMacroBlock(macroName, params, content);
        }
    }

    public void onMacroInline(
        String macroName,
        WikiParameters params,
        String content)
    {
        for (IWemListener listener : fListeners) {
            listener.onMacroInline(macroName, params, content);
        }
    }

    /**
     * @see IWemListener#onNewLine()
     */
    public void onNewLine()
    {
        for (IWemListener listener : fListeners) {
            listener.onNewLine();
        }
    }

    /**
     * @see IWemListener#onReference(java.lang.String)
     */
    public void onReference(String ref)
    {
        for (IWemListener listener : fListeners) {
            listener.onReference(ref);
        }
    }

    public void onReference(WikiReference ref)
    {
        for (IWemListener listener : fListeners) {
            listener.onReference(ref);
        }
    }

    /**
     * @see IWemListener#onSpace(java.lang.String)
     */
    public void onSpace(String str)
    {
        for (IWemListener listener : fListeners) {
            listener.onSpace(str);
        }
    }

    /**
     * @see IWemListener#onSpecialSymbol(java.lang.String)
     */
    public void onSpecialSymbol(String str)
    {
        for (IWemListener listener : fListeners) {
            listener.onSpecialSymbol(str);
        }
    }

    /**
     * @see IWemListener#onTableCaption(java.lang.String)
     */
    public void onTableCaption(String str)
    {
        for (IWemListener listener : fListeners) {
            listener.onTableCaption(str);
        }
    }

    /**
     * @see IWemListener#onVerbatimBlock(String,
     *      WikiParameters)
     */
    public void onVerbatimBlock(String str, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.onVerbatimBlock(str, params);
        }
    }

    /**
     * @see IWemListener#onVerbatimInline(java.lang.String,
     *      WikiParameters)
     */
    public void onVerbatimInline(String str, WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.onVerbatimInline(str, params);
        }
    }

    /**
     * @see IWemListener#onWord(java.lang.String)
     */
    public void onWord(String str)
    {
        for (IWemListener listener : fListeners) {
            listener.onWord(str);
        }
    }

    @Override
    public void beginFigure(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginFigure(params);
        }
    }

    @Override
    public void endFigure(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endFigure(params);
        }
    }

    @Override
    public void beginFigureCaption(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.beginFigureCaption(params);
        }
    }

    @Override
    public void endFigureCaption(WikiParameters params)
    {
        for (IWemListener listener : fListeners) {
            listener.endFigureCaption(params);
        }
    }
}

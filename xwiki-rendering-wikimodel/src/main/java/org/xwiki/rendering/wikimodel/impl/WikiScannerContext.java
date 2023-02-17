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
package org.xwiki.rendering.wikimodel.impl;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.WikiStyle;
import org.xwiki.rendering.wikimodel.util.SectionBuilder;
import org.xwiki.rendering.wikimodel.util.SectionListener;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WikiScannerContext implements IWikiScannerContext
{
    protected final IWemListener fListener;

    protected SectionBuilder<WikiParameters> fSectionBuilder;

    protected final Deque<IWikiScannerContext> fStack = new ArrayDeque<IWikiScannerContext>();

    private class DefaultSectionListener extends SectionListener<WikiParameters>
    {
        @Override
        public void beginDocument(IPos<WikiParameters> pos)
        {
            org.xwiki.rendering.wikimodel.WikiParameters params = pos.getData();
            fListener.beginDocument(params);
            beginSection(pos);
            beginSectionContent(pos);
        }

        @Override
        public void beginSection(IPos<WikiParameters > pos)
        {
            org.xwiki.rendering.wikimodel.WikiParameters params = pos.getData();
            int docLevel = pos.getDocumentLevel();
            int headerLevel = pos.getHeaderLevel();
            fListener.beginSection(docLevel, headerLevel, params);
        }

        @Override
        public void beginSectionContent(IPos<WikiParameters> pos)
        {
            fListener.beginSectionContent(pos.getDocumentLevel(), pos
                .getHeaderLevel(), pos.getData());
        }

        @Override
        public void beginSectionHeader(IPos<WikiParameters> pos)
        {
            fListener.beginHeader(pos.getHeaderLevel(), pos.getData());
        }

        @Override
        public void endDocument(IPos<WikiParameters> pos)
        {
            endSectionContent(pos);
            endSection(pos);
            org.xwiki.rendering.wikimodel.WikiParameters params = pos.getData();
            fListener.endDocument(params);
        }

        @Override
        public void endSection(IPos<WikiParameters> pos)
        {
            org.xwiki.rendering.wikimodel.WikiParameters params = pos.getData();
            int docLevel = pos.getDocumentLevel();
            int headerLevel = pos.getHeaderLevel();
            fListener.endSection(docLevel, headerLevel, params);
        }

        @Override
        public void endSectionContent(IPos<WikiParameters> pos)
        {
            fListener.endSectionContent(pos.getDocumentLevel(), pos
                .getHeaderLevel(), pos.getData());
        }

        @Override
        public void endSectionHeader(IPos<WikiParameters> pos)
        {
            fListener.endHeader(pos.getHeaderLevel(), pos.getData());
        }
    }

    public WikiScannerContext(IWemListener listener)
    {
        fListener = listener;
        fSectionBuilder = new SectionBuilder<>(new DefaultSectionListener());
    }

    public IWemListener getfListener()
    {
        return this.fListener;
    }

    public void beginDocument()
    {
        InternalWikiScannerContext context = pushContext();
        context.beginDocument();
    }

    public void beginDocument(WikiParameters params)
    {
        InternalWikiScannerContext context = pushContext();
        context.beginDocument(params);
    }

    @Override
    public void beginFigure(WikiParameters params)
    {
        InternalWikiScannerContext context = (InternalWikiScannerContext) getContext();
        if (context != null) {
            context.checkBlockContainer();
            context.closeFormat();
        }

        context = new InternalWikiScannerContext(
            new SectionBuilder<>(new DefaultSectionListener() {
                @Override
                public void beginDocument(IPos<WikiParameters> pos)
                {
                    WikiParameters params = pos.getData();
                    WikiScannerContext.this.fListener.beginFigure(params);
                    beginSection(pos);
                    beginSectionContent(pos);
                }

                @Override
                public void endDocument(IPos<WikiParameters> pos)
                {
                    endSectionContent(pos);
                    endSection(pos);
                    WikiParameters params = pos.getData();
                    WikiScannerContext.this.fListener.endFigure(params);
                }
            }),
            this.fListener);
        this.fStack.push(context);
        context.beginFigure(params);
    }

    @Override
    public void endFigure()
    {
        getContext().endFigure();
        this.fStack.pop();
    }

    @Override
    public void beginFigureCaption(WikiParameters params)
    {
        InternalWikiScannerContext context = (InternalWikiScannerContext) getContext();
        if (context != null) {
            context.checkBlockContainer();
            context.closeFormat();
        }

        context = new InternalWikiScannerContext(
            new SectionBuilder<>(new DefaultSectionListener() {
                @Override
                public void beginDocument(IPos<WikiParameters> pos)
                {
                    WikiParameters params = pos.getData();
                    WikiScannerContext.this.fListener.beginFigureCaption(params);
                    beginSection(pos);
                    beginSectionContent(pos);
                }

                @Override
                public void endDocument(IPos<WikiParameters> pos)
                {
                    endSectionContent(pos);
                    endSection(pos);
                    WikiParameters params = pos.getData();
                    WikiScannerContext.this.fListener.endFigureCaption(params);
                }
            }),
            this.fListener);
        this.fStack.push(context);
        getContext().beginFigureCaption(params);
    }

    @Override
    public void endFigureCaption()
    {
        getContext().endFigureCaption();
        this.fStack.pop();
    }

    public void beginFormat(WikiParameters params)
    {
        getContext().beginFormat(params);
    }

    public void beginFormat(WikiStyle wikiStyle)
    {
        getContext().beginFormat(wikiStyle);
    }

    public void beginHeader(int level)
    {
        getContext().beginHeader(level);
    }

    public void beginHeader(int level, WikiParameters params)
    {
        getContext().beginHeader(level, params);
    }

    public void beginInfo(String type, WikiParameters params)
    {
        getContext().beginInfo(type, params);
    }

    public void beginList()
    {
        getContext().beginList();
    }

    public void beginList(WikiParameters params)
    {
        getContext().beginList(params);
    }

    public void beginListItem(String item)
    {
        getContext().beginListItem(item);
    }

    public void beginListItem(String item, WikiParameters listParams)
    {
        getContext().beginListItem(item, listParams);
    }

    @Override
    public void beginListItem(String item, WikiParameters listParams, WikiParameters itemParams)
    {
        getContext().beginListItem(item, listParams, itemParams);
    }

    public void beginParagraph()
    {
        getContext().beginParagraph();
    }

    public void beginParagraph(WikiParameters params)
    {
        getContext().beginParagraph(params);
    }

    public void beginPropertyBlock(String property, boolean doc)
    {
        getContext().beginPropertyBlock(property, doc);
    }

    public void beginPropertyInline(String str)
    {
        getContext().beginPropertyInline(str);
    }

    public void beginQuot()
    {
        getContext().beginQuot();
    }

    public void beginQuot(WikiParameters params)
    {
        getContext().beginQuot(params);
    }

    public void beginQuotLine(int depth)
    {
        getContext().beginQuotLine(depth);
    }

    public void beginTable()
    {
        getContext().beginTable();
    }

    public void beginTable(WikiParameters params)
    {
        getContext().beginTable(params);
    }

    public void beginTableCell(boolean headCell)
    {
        getContext().beginTableCell(headCell);
    }

    public void beginTableCell(boolean headCell, WikiParameters params)
    {
        getContext().beginTableCell(headCell, params);
    }

    public void beginTableRow(boolean headCell)
    {
        getContext().beginTableRow(headCell);
    }

    public void beginTableRow(boolean head, WikiParameters rowParams,
        WikiParameters cellParams)
    {
        getContext().beginTableRow(head, rowParams, cellParams);
    }

    public void beginTableRow(WikiParameters rowParams)
    {
        getContext().beginTableRow(rowParams);
    }

    public boolean canApplyDefintionSplitter()
    {
        return getContext().canApplyDefintionSplitter();
    }

    public boolean checkFormatStyle(WikiStyle style)
    {
        return getContext().checkFormatStyle(style);
    }

    public void closeBlock()
    {
        getContext().closeBlock();
    }

    public void endDocument()
    {
        getContext().endDocument();
        fStack.pop();
    }

    public void endFormat(WikiParameters params)
    {
        getContext().endFormat(params);
    }

    public void endFormat(WikiStyle wikiStyle)
    {
        getContext().endFormat(wikiStyle);
    }

    public void endHeader()
    {
        getContext().endHeader();
    }

    public void endInfo()
    {
        getContext().endInfo();
    }

    public void endList()
    {
        getContext().endList();
    }

    public void endListItem()
    {
        getContext().endListItem();
    }

    public void endParagraph()
    {
        getContext().endParagraph();
    }

    public void endPropertyBlock()
    {
        getContext().endPropertyBlock();
    }

    public void endPropertyInline()
    {
        getContext().endPropertyInline();
    }

    public void endQuot()
    {
        getContext().endQuot();
    }

    public void endQuotLine()
    {
        getContext().endQuotLine();
    }

    public void endTable()
    {
        getContext().endTable();
    }

    public void endTableCell()
    {
        getContext().endTableCell();
    }

    public void endTableExplicit()
    {
        getContext().endTableExplicit();
    }

    public void endTableRow()
    {
        getContext().endTableRow();
    }

    public IWikiScannerContext getContext()
    {
        if (!fStack.isEmpty()) {
            return fStack.peek();
        }
        InternalWikiScannerContext context = newInternalContext();
        fStack.push(context);
        return context;
    }

    public InlineState getInlineState()
    {
        return getContext().getInlineState();
    }

    public int getTableCellCounter()
    {
        return getContext().getTableCellCounter();
    }

    public int getTableRowCounter()
    {
        return getContext().getTableRowCounter();
    }

    public boolean isInDefinitionList()
    {
        return getContext().isInDefinitionList();
    }

    public boolean isInDefinitionTerm()
    {
        return getContext().isInDefinitionTerm();
    }

    public boolean isInHeader()
    {
        return getContext().isInHeader();
    }

    public boolean isInInlineProperty()
    {
        return getContext().isInInlineProperty();
    }

    public boolean isInList()
    {
        return getContext().isInList();
    }

    public boolean isInTable()
    {
        return getContext().isInTable();
    }

    public boolean isInTableCell()
    {
        return getContext().isInTableCell();
    }

    public boolean isInTableRow()
    {
        return getContext().isInTableRow();
    }

    /**
     * @return
     */
    protected InternalWikiScannerContext newInternalContext()
    {
        InternalWikiScannerContext context = new InternalWikiScannerContext(
            fSectionBuilder,
            fListener);
        return context;
    }

    public void onDefinitionListItemSplit()
    {
        getContext().onDefinitionListItemSplit();
    }

    public void onEmptyLines(int count)
    {
        getContext().onEmptyLines(count);
    }

    public void onEscape(String str)
    {
        getContext().onEscape(str);
    }

    public void onExtensionBlock(String extensionName, WikiParameters params)
    {
        getContext().onExtensionBlock(extensionName, params);
    }

    public void onExtensionInline(String extensionName, WikiParameters params)
    {
        getContext().onExtensionInline(extensionName, params);
    }

    public void onFormat(WikiParameters params)
    {
        getContext().onFormat(params);
    }

    public void onFormat(WikiStyle wikiStyle)
    {
        getContext().onFormat(wikiStyle);
    }

    /**
     * @see WikiScannerContext#onFormat(org.xwiki.rendering.wikimodel.WikiStyle,
     *      boolean)
     */
    public void onFormat(WikiStyle wikiStyle, boolean forceClose)
    {
        getContext().onFormat(wikiStyle, forceClose);
    }

    public void onHorizontalLine()
    {
        getContext().onHorizontalLine();
    }

    public void onHorizontalLine(WikiParameters params)
    {
        getContext().onHorizontalLine(params);
    }

    public void onImage(String ref)
    {
        getContext().onImage(ref);
    }

    public void onImage(WikiReference ref)
    {
        getContext().onImage(ref);
    }

    public void onLineBreak()
    {
        getContext().onLineBreak();
    }

    public void onMacro(String name, WikiParameters params, String content)
    {
        getContext().onMacro(name, params, content);
    }

    public void onMacro(String macroName, WikiParameters params,
        String content, boolean inline)
    {
        if (inline) {
            onMacroInline(macroName, params, content);
        } else {
            onMacroBlock(macroName, params, content);
        }
    }

    public void onMacroBlock(String macroName, WikiParameters params,
        String content)
    {
        getContext().onMacroBlock(macroName, params, content);
    }

    public void onMacroInline(String macroName, WikiParameters params,
        String content)
    {
        getContext().onMacroInline(macroName, params, content);
    }

    public void onNewLine()
    {
        getContext().onNewLine();
    }

    public void onQuotLine(int depth)
    {
        getContext().onQuotLine(depth);
    }

    public void onReference(String ref)
    {
        getContext().onReference(ref);
    }

    public void onReference(WikiReference ref)
    {
        getContext().onReference(ref);
    }

    public void onSpace(String str)
    {
        getContext().onSpace(str);
    }

    public void onSpecialSymbol(String str)
    {
        // Extract white spaces from special characters
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ' ') {
                getContext().onSpace(" ");
            } else {
                getContext().onSpecialSymbol(String.valueOf(c));
            }
        }
    }

    public void onTableCaption(String str)
    {
        getContext().onTableCaption(str);
    }

    public void onTableCell(boolean headCell)
    {
        getContext().onTableCell(headCell);
    }

    public void onTableCell(boolean head, WikiParameters cellParams)
    {
        getContext().onTableCell(head, cellParams);
    }

    /**
     * @see WikiScannerContext#onTableRow(org.xwiki.rendering.wikimodel.WikiParameters)
     */
    public void onTableRow(WikiParameters params)
    {
        getContext().onTableRow(params);
    }

    /**
     * @see WikiScannerContext#onVerbatim(java.lang.String,
     *      boolean)
     */
    public void onVerbatim(String str, boolean inline)
    {
        getContext().onVerbatim(str, inline);
    }

    public void onVerbatim(String str, boolean inline, WikiParameters params)
    {
        getContext().onVerbatim(str, inline, params);
    }

    public void onVerbatim(String str, WikiParameters params)
    {
        getContext().onVerbatim(str, params);
    }

    public void onWord(String str)
    {
        getContext().onWord(str);
    }

    private InternalWikiScannerContext pushContext()
    {
        InternalWikiScannerContext context = (InternalWikiScannerContext) getContext();
        if (context != null) {
            context.checkBlockContainer();
            context.closeFormat();
        }
        context = newInternalContext();
        fStack.push(context);

        return context;
    }
}

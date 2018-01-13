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
package org.xwiki.rendering.internal.renderer.wikimodel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.wikimodel.IWemConstants;
import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * Map XWiki Listener events on to WikiModel events.
 *
 * @version $Id$
 * @since 1.5RC1
 */
public class WikiModelGeneratorListener implements Listener
{
    private IWemListener wikimodelListener;

    private int docLevel = 1;

    private Deque<Context> context = new ArrayDeque<Context>();

    private class Context
    {
        int headerLevel;
    }

    public WikiModelGeneratorListener(IWemListener wikimodelListener)
    {
        this.wikimodelListener = wikimodelListener;
    }

    private Context getContext()
    {
        return this.context.peek();
    }

    private Context pushContext()
    {
        Context ctx = new Context();
        this.context.push(ctx);
        return ctx;
    }

    private Context popContext()
    {
        return this.context.pop();
    }

    @Override
    public void beginDocument(MetaData metadata)
    {
        pushContext();

        this.wikimodelListener.beginDocument(WikiParameters.EMPTY);
        this.wikimodelListener.beginSection(this.docLevel, getContext().headerLevel, WikiParameters.EMPTY);
        this.docLevel++;
        getContext().headerLevel++;
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        this.wikimodelListener.endSection(this.docLevel, getContext().headerLevel, WikiParameters.EMPTY);
        this.docLevel--;
        this.wikimodelListener.endDocument(WikiParameters.EMPTY);

        popContext();
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        this.wikimodelListener.beginDocument(createWikiParameters(parameters));
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        this.wikimodelListener.endDocument(createWikiParameters(parameters));
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        switch (format) {
            case BOLD:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.STRONG,
                    createWikiParameters(parameters).toList()));
                break;
            case ITALIC:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.EM, createWikiParameters(parameters)
                    .toList()));
                break;
            case STRIKEDOUT:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.STRIKE,
                    createWikiParameters(parameters).toList()));
                break;
            case UNDERLINED:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.INS, createWikiParameters(parameters)
                    .toList()));
                break;
            case MONOSPACE:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.MONO, createWikiParameters(parameters)
                    .toList()));
                break;
            case SUBSCRIPT:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.SUB, createWikiParameters(parameters)
                    .toList()));
                break;
            case SUPERSCRIPT:
                this.wikimodelListener.beginFormat(new WikiFormat(IWemConstants.SUP, createWikiParameters(parameters)
                    .toList()));
                break;
            case NONE:
                this.wikimodelListener.beginFormat(new WikiFormat(createWikiParameters(parameters).toList()));
                break;
            //Unsupported format
            default: 
                break;
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        switch (format) {
            case BOLD:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.STRONG, createWikiParameters(parameters)
                    .toList()));
                break;
            case ITALIC:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.EM, createWikiParameters(parameters)
                    .toList()));
                break;
            case STRIKEDOUT:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.STRIKE, createWikiParameters(parameters)
                    .toList()));
                break;
            case UNDERLINED:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.INS, createWikiParameters(parameters)
                    .toList()));
                break;
            case MONOSPACE:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.MONO, createWikiParameters(parameters)
                    .toList()));
                break;
            case SUBSCRIPT:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.SUB, createWikiParameters(parameters)
                    .toList()));
                break;
            case SUPERSCRIPT:
                this.wikimodelListener.endFormat(new WikiFormat(IWemConstants.SUP, createWikiParameters(parameters)
                    .toList()));
                break;
            case NONE:
                this.wikimodelListener.endFormat(new WikiFormat(createWikiParameters(parameters).toList()));
                break;
            //Unsupported format
            default: 
                break;
        }
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        this.wikimodelListener.beginList(createWikiParameters(parameters), type == ListType.NUMBERED);
    }

    @Override
    public void beginListItem()
    {
        this.wikimodelListener.beginListItem();
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Don't do anything since there's no notion of Macro marker in WikiModel and anyway
        // there's nothing to render for a marker...
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.wikimodelListener.beginParagraph(createWikiParameters(parameters));
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        this.wikimodelListener.beginSection(this.docLevel, getContext().headerLevel, createWikiParameters(parameters));
        getContext().headerLevel++;
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.wikimodelListener.beginHeader(level.getAsInt(), createWikiParameters(parameters));
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        this.wikimodelListener.endList(createWikiParameters(parameters), type == ListType.NUMBERED);
    }

    @Override
    public void endListItem()
    {
        this.wikimodelListener.endListItem();
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Don't do anything since there's no notion of Macro marker in WikiModel and anyway
        // there's nothing to render for a marker...
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        this.wikimodelListener.endParagraph(createWikiParameters(parameters));
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        this.wikimodelListener.beginSection(this.docLevel, getContext().headerLevel, createWikiParameters(parameters));
        getContext().headerLevel--;
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.wikimodelListener.endHeader(level.getAsInt(), createWikiParameters(parameters));
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // TODO wait for WikiModel to support wiki syntax in links
        // See http://code.google.com/p/wikimodel/issues/detail?id=87
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // TODO wait for WikiModel to support wiki syntax in links
        // See http://code.google.com/p/wikimodel/issues/detail?id=87
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        if (inline) {
            this.wikimodelListener.onMacroInline(id, createWikiParameters(parameters), content);
        } else {
            this.wikimodelListener.onMacroBlock(id, createWikiParameters(parameters), content);
        }
    }

    @Override
    public void onNewLine()
    {
        // TODO: Decide when to generate a line break and when to generate a new line
        this.wikimodelListener.onNewLine();
    }

    @Override
    public void onSpace()
    {
        this.wikimodelListener.onSpace(" ");
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.wikimodelListener.onSpecialSymbol(String.valueOf(symbol));
    }

    @Override
    public void onWord(String word)
    {
        this.wikimodelListener.onWord(word);
    }

    @Override
    public void onId(String name)
    {
        this.wikimodelListener.onExtensionBlock(DefaultXWikiGeneratorListener.EXT_ID, createWikiParameters(Collections
            .singletonMap("name", name)));
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        // Nothing to do since wikimodel doesn't support raw content.
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.wikimodelListener.onHorizontalLine(createWikiParameters(parameters));
    }

    @Override
    public void onEmptyLines(int count)
    {
        this.wikimodelListener.onEmptyLines(count);
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (inline) {
            // TODO: we're currently not handling any inline verbatim parameters (we don't have support for this in
            // XWiki Blocks for now).
            this.wikimodelListener.onVerbatimInline(content, WikiParameters.EMPTY);
        } else {
            this.wikimodelListener.onVerbatimBlock(content, createWikiParameters(parameters));
        }
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        this.wikimodelListener.beginDefinitionList(createWikiParameters(parameters));
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        this.wikimodelListener.endDefinitionList(createWikiParameters(parameters));
    }

    @Override
    public void beginDefinitionTerm()
    {
        this.wikimodelListener.beginDefinitionTerm();
    }

    @Override
    public void beginDefinitionDescription()
    {
        this.wikimodelListener.beginDefinitionDescription();
    }

    @Override
    public void endDefinitionTerm()
    {
        this.wikimodelListener.endDefinitionTerm();
    }

    @Override
    public void endDefinitionDescription()
    {
        this.wikimodelListener.endDefinitionDescription();
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        this.wikimodelListener.beginQuotation(createWikiParameters(parameters));
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        this.wikimodelListener.endQuotation(createWikiParameters(parameters));
    }

    @Override
    public void beginQuotationLine()
    {
        this.wikimodelListener.beginQuotationLine();
    }

    @Override
    public void endQuotationLine()
    {
        this.wikimodelListener.endQuotationLine();
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        this.wikimodelListener.beginTable(createWikiParameters(parameters));
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        this.wikimodelListener.beginTableCell(false, createWikiParameters(parameters));
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.wikimodelListener.beginTableCell(true, createWikiParameters(parameters));
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        this.wikimodelListener.beginTableRow(createWikiParameters(parameters));
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        this.wikimodelListener.endTable(createWikiParameters(parameters));
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        this.wikimodelListener.endTableCell(false, createWikiParameters(parameters));
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        this.wikimodelListener.endTableCell(true, createWikiParameters(parameters));
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        this.wikimodelListener.endTableRow(createWikiParameters(parameters));
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // Note: This means that any WikiModel listener needs to be overridden with a XWiki specific
        // version that knows how to handle XWiki image location format.
        // TODO this.wikimodelListener.onReference("image:" + imageLocation);
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        // WikiModel has a notion of Property but it's different from XWiki's notion of MetaData. We could map some
        // specific metadata as WikiModel's property but it's not important since it would be useful only to benefit
        // from WikiModel's Renderer implementations and such implementation won't use XWiki's metadata anyway.
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        // WikiModel has a notion of Property but it's different from XWiki's notion of MetaData. We could map some
        // specific metadata as WikiModel's property but it's not important since it would be useful only to benefit
        // from WikiModel's Renderer implementations and such implementation won't use XWiki's metadata anyway.
    }

    private WikiParameters createWikiParameters(Map<String, String> parameters)
    {
        List<WikiParameter> wikiParams = new ArrayList<WikiParameter>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            wikiParams.add(new WikiParameter(entry.getKey(), entry.getValue()));
        }

        return new WikiParameters(wikiParams);
    }
}

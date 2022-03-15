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
package org.xwiki.rendering.internal.parser.doxia;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkAdapter;
import org.xwiki.rendering.listener.CompositeListener;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.VoidListener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;

/**
 * Transforms Doxia events into XWiki Rendering events.
 *
 * @version $Id$
 * @since 2.1RC1
 */
public class XWikiGeneratorSink extends SinkAdapter
{
    private Deque<Listener> listener = new ArrayDeque<>();

    private Deque<Object> parameters = new ArrayDeque<>();

    private ResourceReferenceParser linkReferenceParser;

    private IdGenerator idGenerator;

    private PrintRendererFactory plainRendererFactory;

    private StreamParser plainParser;

    private int lineBreaks;

    private int inlineDepth;

    private Syntax syntax;

    private MetaData documentMetadata;

    private boolean isInVerbatim;

    private StringBuilder accumulatedText = new StringBuilder();

    /**
     * @since 3.0M3
     */
    public XWikiGeneratorSink(Listener listener, ResourceReferenceParser linkReferenceParser,
        PrintRendererFactory plainRendererFactory, IdGenerator idGenerator, StreamParser plainParser, Syntax syntax)
    {
        pushListener(listener);

        this.linkReferenceParser = linkReferenceParser;
        this.idGenerator = idGenerator != null ? idGenerator : new IdGenerator();
        this.plainRendererFactory = plainRendererFactory;
        this.plainParser = plainParser;
        this.syntax = syntax;
        this.documentMetadata = new MetaData();
        this.documentMetadata.addMetaData(MetaData.SYNTAX, this.syntax);
    }

    public Listener getListener()
    {
        return this.listener.peek();
    }

    private Listener pushListener(Listener listener)
    {
        this.listener.push(listener);
        return listener;
    }

    private Listener popListener()
    {
        return this.listener.pop();
    }

    private boolean isInline()
    {
        return this.inlineDepth > 0;
    }

    private void flushEmptyLines()
    {
        if (this.lineBreaks > 0) {
            if (isInline()) {
                for (int i = 0; i < this.lineBreaks; ++i) {
                    getListener().onNewLine();
                }
            } else {
                if (this.lineBreaks >= 2) {
                    getListener().onEmptyLines(this.lineBreaks - 1);
                } else {
                    getListener().onNewLine();
                }
            }
        }
    }

    @Override
    public void flush()
    {
        flushEmptyLines();
    }

    @Override
    public void anchor(String name, SinkEventAttributes attributes)
    {
        flushEmptyLines();

        getListener().onId(name);
    }

    @Override
    public void anchor(String name)
    {
        anchor(name, null);
    }

    @Override
    public void anchor_()
    {
        // Nothing to do since for XWiki anchors don't have children and thus the XWiki Block is generated in the Sink
        // anchor start event
    }

    @Override
    public void author(SinkEventAttributes attributes)
    {
        // XWiki's Listener model doesn't support authors. Don't do anything.
    }

    @Override
    public void author()
    {
        // XWiki's Listener model doesn't support authors. Don't do anything.
    }

    @Override
    public void author_()
    {
        // XWiki's Listener model doesn't support authors. Don't do anything.
    }

    @Override
    public void body(SinkEventAttributes attributes)
    {
        body();
    }

    @Override
    public void body()
    {
        getListener().beginDocument(this.documentMetadata);
    }

    @Override
    public void body_()
    {
        getListener().endDocument(this.documentMetadata);
    }

    @Override
    public void bold()
    {
        flushEmptyLines();

        getListener().beginFormat(Format.BOLD, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void bold_()
    {
        flushEmptyLines();

        getListener().endFormat(Format.BOLD, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void close()
    {
        // Not used.
    }

    @Override
    public void comment(String comment)
    {
        // TODO: Not supported yet by the XDOM.
    }

    @Override
    public void date(SinkEventAttributes attributes)
    {
        // XWiki's Listener model doesn't support dates. Don't do anything.
    }

    @Override
    public void date()
    {
        // XWiki's Listener model doesn't support dates. Don't do anything.
    }

    @Override
    public void date_()
    {
        // XWiki's Listener model doesn't support dates. Don't do anything.
    }

    @Override
    public void definedTerm(SinkEventAttributes attributes)
    {
        getListener().beginDefinitionTerm();

        ++this.inlineDepth;
    }

    @Override
    public void definedTerm()
    {
        definedTerm(null);
    }

    @Override
    public void definedTerm_()
    {
        flushEmptyLines();

        // Limitation: XWiki doesn't use parameters on this Block.
        getListener().endDefinitionTerm();

        --this.inlineDepth;
    }

    @Override
    public void definition(SinkEventAttributes attributes)
    {
        getListener().beginDefinitionDescription();

        ++this.inlineDepth;
    }

    @Override
    public void definition()
    {
        definition(null);
    }

    @Override
    public void definition_()
    {
        flushEmptyLines();

        // Limitation: XWiki doesn't use parameters on this Block.
        getListener().endDefinitionDescription();

        --this.inlineDepth;
    }

    @Override
    public void definitionList(SinkEventAttributes attributes)
    {
        flushEmptyLines();

        getListener().beginDefinitionList(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void definitionList()
    {
        definitionList(null);
    }

    @Override
    public void definitionList_()
    {
        // TODO: Handle parameters
        getListener().endDefinitionList(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void definitionListItem(SinkEventAttributes attributes)
    {
        // Nothing to do since for XWiki the definition list items are the definition term/descriptions.
    }

    @Override
    public void definitionListItem()
    {
        // Nothing to do since for XWiki the definition list items are the definition term/descriptions.
    }

    @Override
    public void definitionListItem_()
    {
        // Nothing to do since for XWiki the definition list items are the definition term/descriptions.
    }

    @Override
    public void figureCaption(SinkEventAttributes attributes)
    {
        // TODO: Handle caption as parameters in the future
    }

    @Override
    public void figureCaption()
    {
        figureCaption(null);
    }

    @Override
    public void figureCaption_()
    {
        // TODO: Handle caption as parameters in the future
    }

    @Override
    public void figureGraphics(String source, SinkEventAttributes attributes)
    {
        flushEmptyLines();
        String id = this.idGenerator.generateUniqueId("I", source);

        // TODO: Handle image to attachments. For now we only handle URLs.
        getListener().onImage(new ResourceReference(source, ResourceType.URL), false, id, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void figureGraphics(String source)
    {
        figureGraphics(source, null);
    }

    @Override
    public void head(SinkEventAttributes sinkEventAttributes)
    {
        head();
    }

    @Override
    public void head()
    {
        // When in head don't output anything
        pushListener(new VoidListener());
    }

    @Override
    public void head_()
    {
        // Start generating stuff again...
        popListener();
    }

    @Override
    public void horizontalRule(SinkEventAttributes attributes)
    {
        flushEmptyLines();

        // TODO: Handle parameters
        getListener().onHorizontalLine(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void horizontalRule()
    {
        horizontalRule(null);
    }

    @Override
    public void italic()
    {
        flushEmptyLines();

        getListener().beginFormat(Format.ITALIC, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void italic_()
    {
        flushEmptyLines();

        getListener().endFormat(Format.ITALIC, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void lineBreak(SinkEventAttributes attributes)
    {
        ++this.lineBreaks;
    }

    @Override
    public void lineBreak()
    {
        lineBreak(null);
    }

    @Override
    public void link(String name, SinkEventAttributes attributes)
    {
        flushEmptyLines();

        ResourceReference resourceReference = this.linkReferenceParser.parse(name);

        getListener().beginLink(resourceReference, false, Listener.EMPTY_PARAMETERS);

        this.parameters.push(resourceReference);
    }

    @Override
    public void link(String name)
    {
        link(name, null);
    }

    @Override
    public void link_()
    {
        flushEmptyLines();

        getListener().endLink((ResourceReference) this.parameters.pop(), false, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void list(SinkEventAttributes attributes)
    {
        flushEmptyLines();

        // TODO: Handle parameters
        getListener().beginList(ListType.BULLETED, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void list()
    {
        list(null);
    }

    @Override
    public void list_()
    {
        getListener().endList(ListType.BULLETED, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void listItem(SinkEventAttributes attributes)
    {
        // TODO: Handle parameters
        getListener().beginListItem();

        ++this.inlineDepth;
    }

    @Override
    public void listItem()
    {
        listItem(null);
    }

    @Override
    public void listItem_()
    {
        flushEmptyLines();

        getListener().endListItem();

        --this.inlineDepth;
    }

    @Override
    public void monospaced()
    {
        flushEmptyLines();

        getListener().beginFormat(Format.MONOSPACE, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void monospaced_()
    {
        flushEmptyLines();

        getListener().endFormat(Format.MONOSPACE, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void nonBreakingSpace()
    {
        flushEmptyLines();

        getListener().onSpace();
    }

    @Override
    public void numberedList(int numbering, SinkEventAttributes sinkEventAttributes)
    {
        flushEmptyLines();

        getListener().beginList(ListType.NUMBERED, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void numberedList(int numbering)
    {
        numberedList(numbering, null);
    }

    @Override
    public void numberedList_()
    {
        getListener().endList(ListType.NUMBERED, Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void numberedListItem(SinkEventAttributes attributes)
    {
        getListener().beginListItem();

        ++this.inlineDepth;
    }

    @Override
    public void numberedListItem()
    {
        numberedListItem(null);
    }

    @Override
    public void numberedListItem_()
    {
        flushEmptyLines();

        getListener().endListItem();

        --this.inlineDepth;
    }

    @Override
    public void pageBreak()
    {
        // Not supported in XWiki.
    }

    @Override
    public void paragraph(SinkEventAttributes attributes)
    {
        flushEmptyLines();

        // TODO: handle parameters
        getListener().beginParagraph(Listener.EMPTY_PARAMETERS);

        ++this.inlineDepth;
    }

    @Override
    public void paragraph()
    {
        paragraph(null);
    }

    @Override
    public void paragraph_()
    {
        flushEmptyLines();

        getListener().endParagraph(Listener.EMPTY_PARAMETERS);

        --this.inlineDepth;
    }

    @Override
    public void rawText(String text)
    {
        flushEmptyLines();

        getListener().onVerbatim(text, isInline(), Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void section(int level, SinkEventAttributes attributes)
    {
        flushEmptyLines();

        getListener().beginSection(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void section_(int level)
    {
        flushEmptyLines();

        getListener().endSection(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void section1()
    {
        section(1, null);
    }

    @Override
    public void section1_()
    {
        section_(1);
    }

    @Override
    public void section2()
    {
        section(2, null);
    }

    @Override
    public void section2_()
    {
        section_(2);
    }

    @Override
    public void section3()
    {
        section(3, null);
    }

    @Override
    public void section3_()
    {
        section_(3);
    }

    @Override
    public void section4()
    {
        section(4, null);
    }

    @Override
    public void section4_()
    {
        section_(4);
    }

    @Override
    public void section5()
    {
        section(5, null);
    }

    @Override
    public void section5_()
    {
        section_(5);
    }

    @Override
    public void section6()
    {
        section(6, null);
    }

    @Override
    public void section6_()
    {
        section_(6);
    }

    @Override
    public void sectionTitle(int level, SinkEventAttributes attributes)
    {
        flushEmptyLines();

        CompositeListener composite = new CompositeListener();

        composite.addListener(new QueueListener());
        composite.addListener(this.plainRendererFactory.createRenderer(new DefaultWikiPrinter()));

        pushListener(composite);

        ++this.inlineDepth;
    }

    @Override
    public void sectionTitle()
    {
        // Should be deprecated in Doxia
    }

    @Override
    public void sectionTitle_(int level)
    {
        flushEmptyLines();

        CompositeListener composite = (CompositeListener) getListener();

        QueueListener queue = (QueueListener) composite.getListener(0);
        PrintRenderer renderer = (PrintRenderer) composite.getListener(1);

        popListener();

        HeaderLevel headerLevel = HeaderLevel.parseInt(level);
        String id = this.idGenerator.generateUniqueId("H", renderer.getPrinter().toString());

        getListener().beginHeader(headerLevel, id, Listener.EMPTY_PARAMETERS);
        queue.consumeEvents(getListener());
        getListener().endHeader(headerLevel, id, Listener.EMPTY_PARAMETERS);

        --this.inlineDepth;
    }

    @Override
    public void sectionTitle_()
    {
        // Should be deprecated in Doxia
    }

    @Override
    public void sectionTitle1()
    {
        sectionTitle(1, null);
    }

    @Override
    public void sectionTitle1_()
    {
        sectionTitle_(1);
    }

    @Override
    public void sectionTitle2()
    {
        sectionTitle(2, null);
    }

    @Override
    public void sectionTitle2_()
    {
        sectionTitle_(2);
    }

    @Override
    public void sectionTitle3()
    {
        sectionTitle(3, null);
    }

    @Override
    public void sectionTitle3_()
    {
        sectionTitle_(3);
    }

    @Override
    public void sectionTitle4()
    {
        sectionTitle(4, null);
    }

    @Override
    public void sectionTitle4_()
    {
        sectionTitle_(4);
    }

    @Override
    public void sectionTitle5()
    {
        sectionTitle(5, null);
    }

    @Override
    public void sectionTitle5_()
    {
        sectionTitle_(5);
    }

    @Override
    public void sectionTitle6()
    {
        sectionTitle(6, null);
    }

    @Override
    public void sectionTitle6_()
    {
        sectionTitle_(6);
    }

    @Override
    public void table(SinkEventAttributes attributes)
    {
        flushEmptyLines();

        // TODO: Handle parameters
        getListener().beginTable(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void table()
    {
        table(null);
    }

    @Override
    public void table_()
    {
        getListener().endTable(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void tableCaption(SinkEventAttributes attributes)
    {
        // TODO: Handle this
    }

    @Override
    public void tableCaption()
    {
        tableCaption(null);
    }

    @Override
    public void tableCaption_()
    {
        // TODO: Handle this
    }

    @Override
    public void tableCell(SinkEventAttributes attributes)
    {
        // TODO: Handle parameters
        getListener().beginTableCell(Listener.EMPTY_PARAMETERS);

        ++this.inlineDepth;
    }

    @Override
    public void tableCell()
    {
        tableCell((SinkEventAttributes) null);
    }

    @Override
    public void tableCell(String width)
    {
        // TODO: Handle width
        tableCell((SinkEventAttributes) null);
    }

    @Override
    public void tableCell_()
    {
        flushEmptyLines();

        getListener().endTableCell(Listener.EMPTY_PARAMETERS);

        --this.inlineDepth;
    }

    @Override
    public void tableHeaderCell(SinkEventAttributes attributes)
    {
        // TODO: Handle parameters
        getListener().beginTableHeadCell(Listener.EMPTY_PARAMETERS);

        ++this.inlineDepth;
    }

    @Override
    public void tableHeaderCell()
    {
        tableHeaderCell((SinkEventAttributes) null);
    }

    @Override
    public void tableHeaderCell(String width)
    {
        // TODO: Handle width
        tableHeaderCell((SinkEventAttributes) null);
    }

    @Override
    public void tableHeaderCell_()
    {
        flushEmptyLines();

        getListener().endTableHeadCell(Listener.EMPTY_PARAMETERS);

        --this.inlineDepth;
    }

    @Override
    public void tableRow(SinkEventAttributes attributes)
    {
        // TODO: Handle parameters
        getListener().beginTableRow(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void tableRow()
    {
        tableRow(null);
    }

    @Override
    public void tableRow_()
    {
        getListener().endTableRow(Listener.EMPTY_PARAMETERS);
    }

    @Override
    public void tableRows(int[] arg0, boolean arg1)
    {
        // Not supported by XWiki.
    }

    @Override
    public void tableRows_()
    {
        // Not supported by XWiki.
    }

    @Override
    public void text(String text, SinkEventAttributes attributes)
    {
        flushEmptyLines();

        if (this.isInVerbatim) {
            this.accumulatedText.append(text);
            return;
        }

        // TODO Handle parameters
        // Since Doxia doesn't generate events at the word level we need to reparse the
        // text to extract spaces, special symbols and words.

        // TODO: Use an inline parser. See https://jira.xwiki.org/browse/XWIKI-2748
        WrappingListener inlineFilterListener = new InlineFilterListener();
        inlineFilterListener.setWrappedListener(getListener());

        // Parse the text using the plain text parser
        try {
            this.plainParser.parse(new StringReader(text), inlineFilterListener);
        } catch (ParseException e) {
            // Shouldn't happen since we use a StringReader which shouldn't generate any IO.
            throw new RuntimeException("Failed to parse raw text [" + text + "]", e);
        }
    }

    @Override
    public void text(String text)
    {
        text(text, null);
    }

    @Override
    public void title(SinkEventAttributes attributes)
    {
        // XWiki's Listener model doesn't support titles. Don't do anything.
    }

    @Override
    public void title()
    {
        // XWiki's Listener model doesn't support titles. Don't do anything.
    }

    @Override
    public void title_()
    {
        // XWiki's Listener model doesn't support titles. Don't do anything.
    }

    @Override
    public void verbatim(SinkEventAttributes attributes)
    {
        this.isInVerbatim = true;
    }

    @Override
    public void verbatim(boolean boxed)
    {
        this.isInVerbatim = true;
    }

    @Override
    public void verbatim_()
    {
        // TODO: Handle inline or not inline for verbatim
        getListener().onVerbatim(this.accumulatedText.toString(), true, Collections.<String, String>emptyMap());
        this.accumulatedText.setLength(0);
        this.isInVerbatim = false;
    }

    @Override
    public void unknown(String arg0, Object[] arg1, SinkEventAttributes arg2)
    {
        // TODO: Not supported yet by the XDOM.
    }
}

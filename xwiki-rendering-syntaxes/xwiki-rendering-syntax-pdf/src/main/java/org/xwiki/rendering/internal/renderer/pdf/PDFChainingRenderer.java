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
package org.xwiki.rendering.internal.renderer.pdf;

import java.util.Map;
import java.util.Stack;

import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;

import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Convert listener events to PDF.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class PDFChainingRenderer extends AbstractChainingPrintRenderer
{
    /**
     * The iText document representing the PDF document to generate.
     */
    private Document document;

    /**
     * The current iText Chunk (portion of text that can be styled) being handled.
     */
    private Chunk currentChunk;

    /**
     * The current iText Paragraph being handled.
     */
    private Paragraph currentParagraph;

    /**
     * The current iText Chapter.
     * @todo add support for passing metadata to the Renderer and when a title is passed use it as the Chapter title
     */
    private Chapter currentChapter = new Chapter(1);

    /**
     * The current iText Section being handled. A Chapter can have several Sections.
     */
    private Section currentSection;

    /**
     * The iText List elements that have been started and that are being handled.
     */
    private Stack<List> currentLists = new Stack<List>();

    /**
     * The current iText List Item element being handled.
     */
    private ListItem currentListItem;

    /**
     * @param listenerChain see {@link PDFRenderer}
     */
    public PDFChainingRenderer(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    @Override
    public void beginDocument(MetaData metaData)
    {
        this.document = new Document();

        try {
            PdfWriter.getInstance(this.document, new OutputStreamPrinterAdapter(getPrinter()));
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to prepare PDF output stream", e);
        }

        this.document.open();
        addElement(this.currentChapter);
    }

    @Override
    public void endDocument(MetaData metaData)
    {
        flushSection();
        this.document.close();
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        flushParagraph();
        this.currentParagraph = new Paragraph();
    }

    @Override
    public void onWord(String word)
    {
        addToChunk(word);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        // Close the current chunk and open a new one
        flushChunk();
        Chunk chunk = new Chunk();
        if (format.equals(Format.BOLD)) {
            chunk.getFont().setStyle(Font.BOLD);
        } else if (format.equals(Format.ITALIC)) {
            chunk.getFont().setStyle(Font.ITALIC);
        } else if (format.equals(Format.STRIKEDOUT)) {
            chunk.getFont().setStyle(Font.STRIKETHRU);
        } else if (format.equals(Format.UNDERLINED)) {
            chunk.getFont().setStyle(Font.UNDERLINE);
        }
        this.currentChunk = chunk;
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        flushChunk();
    }

    @Override
    public void onSpace()
    {
        addToChunk(" ");
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        addToChunk("" + symbol);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
    }
    
    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        flushSection();
        this.currentParagraph = new Paragraph();
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        flushChunk();
        this.currentSection = this.currentChapter.addSection(this.currentParagraph, level.getAsInt());
        this.currentParagraph = null;
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
    }

    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        // If we're inside a list already then flush the list item
        if (this.currentLists.size() > 0) {
            this.currentListItem.add(this.currentChunk);
            this.currentChunk = null;
            this.currentLists.peek().add(this.currentListItem);
        }

        this.currentLists.push(new List(listType.equals(ListType.NUMBERED), 20));
    }

    @Override
    public void endList(ListType listType, Map<String, String> parameters)
    {
        List list = this.currentLists.pop();
        if (this.currentLists.size() > 0) {
            this.currentLists.peek().add(list);
        } else {
            addElement(list);
        }
    }

    @Override
    public void beginListItem()
    {
        this.currentListItem = new ListItem();
    }

    @Override
    public void endListItem()
    {
        if (this.currentChunk != null) {
            this.currentListItem.add(this.currentChunk);
            this.currentChunk = null;

            this.currentLists.peek().add(this.currentListItem);
            this.currentListItem = null;
        }
    }

    /**
     * @param content the text content to add to the current Chunk
     */
    private void addToChunk(String content)
    {
        if (this.currentChunk == null) {
            this.currentChunk = new Chunk();
        }
        this.currentChunk.append(content);
    }

    /**
     * Add the current Chunk (if not null) to the current Paragraph.
     */
    private void flushChunk()
    {
        if (this.currentChunk != null) {
            this.currentParagraph.add(this.currentChunk);
            this.currentChunk = null;
        }
    }

    /**
     * Flush the current Chunk (see {@link #flushChunk()} and add the current Paragraph (if not null) to the current
     * Section.
     */
    private void flushParagraph()
    {
        flushChunk();
        if (this.currentParagraph != null) {
            if (this.currentSection != null) {
                this.currentSection.add(this.currentParagraph);
            } else {
                addElement(this.currentParagraph);
            }
            this.currentParagraph = null;
        }
    }

    /**
     * Flush the current Paragraph (see {@link #flushParagraph()} and add the current Section (if not null) to the
     * Document.
     */
    private void flushSection()
    {
        flushParagraph();
        if (this.currentSection != null) {
            addElement(this.currentSection);
        }
        this.currentSection = null;
    }

    /**
     * Helper method to avoid having to catch exceptions everywhere when adding an Element to the Document.
     *
     * @param element the iText element to add to the Document
     */
    private void addElement(Element element)
    {
        try {
            this.document.add(element);
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to add element [" + element + "]", e);
        }
    }
}

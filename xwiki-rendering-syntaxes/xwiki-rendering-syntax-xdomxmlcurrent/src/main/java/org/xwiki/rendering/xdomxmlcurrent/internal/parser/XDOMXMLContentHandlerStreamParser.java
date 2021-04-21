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
package org.xwiki.rendering.xdomxmlcurrent.internal.parser;

import javax.inject.Inject;
import javax.inject.Named;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.filter.xml.parser.XMLParserFactory;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.syntax.Syntax;

import static org.xwiki.rendering.xdomxmlcurrent.internal.XDOMXMLCurrentSyntaxProvider.XDOMXML_CURRENT;

/**
 * Generic XML based events parser.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Component
@Named("xdom+xml/current")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XDOMXMLContentHandlerStreamParser implements ContentHandlerStreamParser
{
    /**
     * The actual parser factory.
     */
    @Inject
    private XMLParserFactory parserFactory;

    /**
     * The content handler to send SAX events to.
     */
    private ContentHandler handler;

    @Override
    public Syntax getSyntax()
    {
        return XDOMXML_CURRENT;
    }

    @Override
    public void setListener(Listener listener)
    {
        this.handler = this.parserFactory.createContentHandler(listener, null);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        this.handler.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        this.handler.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        this.handler.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        this.handler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
        this.handler.skippedEntity(name);
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
        this.handler.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException
    {
        this.handler.startDocument();
    }

    @Override
    public void endDocument() throws SAXException
    {
        this.handler.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        this.handler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
        this.handler.endPrefixMapping(prefix);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        this.handler.processingInstruction(target, data);
    }
}

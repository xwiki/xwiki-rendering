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
package org.xwiki.rendering.xml.internal.parser;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;
import org.xwiki.rendering.parser.xml.ContentHandlerBlockParser;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Proxy parser which is using stream parser to produce XDOM.
 *
 * @version $Id$
 * @since 5.2M1
 */
public class ProxyContentHandlerBlockParser implements ContentHandlerBlockParser
{
    /**
     * The actual parser.
     */
    private ContentHandlerStreamParser parser;

    /**
     * The proxy listener which produce the XDOM.
     */
    private XDOMGeneratorListener listener;

    /**
     * @param parser the actual parser
     * @param listener the listener which produce the XDOM
     */
    public ProxyContentHandlerBlockParser(ContentHandlerStreamParser parser, XDOMGeneratorListener listener)
    {
        this.parser = parser;
        this.listener = listener;
    }

    @Override
    public void setDocumentLocator(Locator locator)
    {
        this.parser.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException
    {
        this.parser.startDocument();
    }

    @Override
    public void endDocument() throws SAXException
    {
        this.parser.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        this.parser.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException
    {
        this.parser.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        this.parser.startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        this.parser.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        this.parser.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        this.parser.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        this.parser.processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
        this.parser.skippedEntity(name);
    }

    @Override
    public Syntax getSyntax()
    {
        return this.parser.getSyntax();
    }

    @Override
    public XDOM getXDOM()
    {
        return this.listener.getXDOM();
    }
}

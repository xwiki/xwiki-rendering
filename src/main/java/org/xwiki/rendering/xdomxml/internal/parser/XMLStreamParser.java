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
package org.xwiki.rendering.xdomxml.internal.parser;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParserFactory;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxml.internal.XMLEntities;

/**
 * XDOM+XML stream based parser.
 * 
 * @version $Id$
 */
@Component("xml/1.0")
public class XMLStreamParser implements StreamParser, Initializable
{
    /**
     * Used to lookup the {@link PrintRenderer}.
     */
    @Requirement
    private ContentHandlerStreamParserFactory contentHandlerStreamParserFactory;

    private SAXParserFactory parserFactory;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        this.parserFactory = SAXParserFactory.newInstance();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.parser.Parser#getSyntax()
     */
    public Syntax getSyntax()
    {
        return XMLEntities.XML_1_0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.parser.StreamParser#parse(java.io.Reader, org.xwiki.rendering.listener.Listener)
     */
    public void parse(Reader source, Listener listener) throws ParseException
    {
        try {
            parseXML(source, listener);
        } catch (Exception e) {
            throw new ParseException("Failed to parse input source", e);
        }
    }

    public void parseXML(Reader reader, Listener listener) throws ParserConfigurationException, SAXException,
        IOException
    {
        SAXParser parser = this.parserFactory.newSAXParser();
        XMLReader xmlReader = parser.getXMLReader();

        xmlReader.setContentHandler(this.contentHandlerStreamParserFactory.createParser(listener));

        xmlReader.parse(new InputSource(reader));
    }
}

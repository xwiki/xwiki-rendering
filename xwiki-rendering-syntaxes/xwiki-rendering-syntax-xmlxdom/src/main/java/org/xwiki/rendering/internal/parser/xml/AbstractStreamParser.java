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
package org.xwiki.rendering.internal.parser.xml;

import java.io.IOException;
import java.io.Reader;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParserFactory;

public abstract class AbstractStreamParser implements ContentHandlerStreamParserFactory, StreamParser, Initializable
{
    @Inject
    private ComponentManager componentManager;

    private SAXParserFactory parserFactory;

    @Override
    public void initialize() throws InitializationException
    {
        this.parserFactory = SAXParserFactory.newInstance();
    }

    @Override
    public ContentHandlerStreamParser createParser(Listener listener)
    {
        ContentHandlerStreamParser parser;
        try {
            parser = this.componentManager.lookup(ContentHandlerStreamParser.class, getSyntax().toIdString());
        } catch (ComponentLookupException e) {
            throw new RuntimeException(
                "Failed to create [" + getSyntax().toString() + "] ContentHandler stream parser", e);
        }

        parser.setListener(listener);

        return parser;
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        try {
            parseXML(source, listener);
        } catch (Exception e) {
            throw new ParseException("Failed to parse input source", e);
        }
    }

    public void parseXML(Reader source, Listener listener) throws ParserConfigurationException, SAXException,
        IOException
    {
        SAXParser saxParser = this.parserFactory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();

        ContentHandlerStreamParser parser = createParser(listener);
        xmlReader.setContentHandler(parser);

        xmlReader.parse(new InputSource(source));
    }
}

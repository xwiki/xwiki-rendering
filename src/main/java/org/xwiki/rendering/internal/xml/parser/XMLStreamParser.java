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
package org.xwiki.rendering.internal.xml.parser;

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
import org.xwiki.rendering.internal.xml.parameters.ParameterManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

@Component("xml/1.0")
public class XMLStreamParser implements StreamParser, Initializable
{
    public static final Syntax XML_1_0 = new Syntax(new SyntaxType("xml", "XML"), "1.0");

    @Requirement
    private ParameterManager parameterManager;

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
        return XML_1_0;
    }

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

        xmlReader.setContentHandler(new XMLHandler(listener, this.parameterManager));

        xmlReader.parse(new InputSource(reader));
    }
}

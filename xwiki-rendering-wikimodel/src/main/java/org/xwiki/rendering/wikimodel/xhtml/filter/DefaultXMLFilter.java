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
package org.xwiki.rendering.wikimodel.xhtml.filter;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultXMLFilter extends XMLFilterImpl implements LexicalHandler
{
    public static final String SAX_LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

    private LexicalHandler lexicalHandler;

    public DefaultXMLFilter()
    {
        super();
    }

    public DefaultXMLFilter(XMLReader reader)
    {
        super(reader);
    }

    @Override
    public void parse(InputSource input) throws SAXException, IOException
    {
        if (getParent() != null) {
            getParent().setProperty(SAX_LEXICAL_HANDLER_PROPERTY, this);
        }
        super.parse(input);
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        // We save the lexical handler so that we can use it in the
        // implementation of the LexicalHandler interface methods.
        if (SAX_LEXICAL_HANDLER_PROPERTY.equals(name)) {
            this.lexicalHandler = (LexicalHandler) value;
        } else {
            super.setProperty(name, value);
        }
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (SAX_LEXICAL_HANDLER_PROPERTY.equals(name)) {
            return this.lexicalHandler;
        } else {
            return super.getProperty(name);
        }
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.comment(ch, start, length);
        }
    }

    @Override
    public void endCDATA() throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.endCDATA();
        }
    }

    @Override
    public void endDTD() throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.endDTD();
        }
    }

    @Override
    public void endEntity(String name) throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.endEntity(name);
        }
    }

    @Override
    public void startCDATA() throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.startCDATA();
        }
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.startDTD(name, publicId, systemId);
        }
    }

    @Override
    public void startEntity(String name) throws SAXException
    {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.startEntity(name);
        }
    }
}

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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Skip all callbacks when parsing the DTD.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class DTDXMLFilter extends DefaultXMLFilter
{
    /**
     * We want to accumulate characters only when not parsing the DTD.
     */
    private boolean fIsInDTD;

    public DTDXMLFilter()
    {
        super();
    }

    public DTDXMLFilter(XMLReader reader)
    {
        super(reader);
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] array, int start, int length)
        throws SAXException
    {
        if (!fIsInDTD) {
            super.characters(array, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(
        String uri,
        String localName,
        String qName,
        Attributes attributes) throws SAXException
    {
        if (!fIsInDTD) {
            super.startElement(uri, localName, qName, attributes);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        if (!fIsInDTD) {
            super.endElement(uri, localName, qName);
        }
    }

    @Override
    public void comment(char[] array, int start, int length)
        throws SAXException
    {
        if (!fIsInDTD) {
            super.comment(array, start, length);
        }
    }

    @Override
    public void startCDATA() throws SAXException
    {
        if (!fIsInDTD) {
            super.startCDATA();
        }
    }

    @Override
    public void endCDATA() throws SAXException
    {
        if (!fIsInDTD) {
            super.endCDATA();
        }
    }

    @Override
    public void startDTD(String name, String publicId, String systemId)
        throws SAXException
    {
        fIsInDTD = true;
        super.startDTD(name, publicId, systemId);
    }

    @Override
    public void endDTD() throws SAXException
    {
        fIsInDTD = false;
        super.endDTD();
    }
}

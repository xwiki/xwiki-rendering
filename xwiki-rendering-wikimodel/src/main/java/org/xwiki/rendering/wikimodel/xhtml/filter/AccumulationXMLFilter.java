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
 * SAX parsers are allowed to call the characters() method several times in a
 * row. Some parsers have a buffer of 8K (Crimson), others of 16K (Xerces) and
 * others can even call onCharacters() for every single characters! Thus we need
 * to accumulate the characters in a buffer before we process them. This filter
 * does exactly this.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class AccumulationXMLFilter extends DefaultXMLFilter
{
    private StringBuffer fAccumulationBuffer = new StringBuffer();

    public AccumulationXMLFilter()
    {
        super();
    }

    public AccumulationXMLFilter(XMLReader reader)
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
        if (fAccumulationBuffer != null) {
            fAccumulationBuffer.append(array, start, length);
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
        flushAccumulationBuffer();
        super.startElement(uri, localName, qName, attributes);
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        flushAccumulationBuffer();
        super.endElement(uri, localName, qName);
    }

    @Override
    public void comment(char[] array, int start, int length)
        throws SAXException
    {
        flushAccumulationBuffer();
        super.comment(array, start, length);
    }

    @Override
    public void startCDATA() throws SAXException
    {
        flushAccumulationBuffer();
        super.startCDATA();
    }

    @Override
    public void endCDATA() throws SAXException
    {
        flushAccumulationBuffer();
        super.endCDATA();
    }

    private void flushAccumulationBuffer() throws SAXException
    {
        if (fAccumulationBuffer.length() > 0) {
            super.characters(
                fAccumulationBuffer.toString().toCharArray(),
                0,
                fAccumulationBuffer.length());
        }
        fAccumulationBuffer.setLength(0);
    }
}

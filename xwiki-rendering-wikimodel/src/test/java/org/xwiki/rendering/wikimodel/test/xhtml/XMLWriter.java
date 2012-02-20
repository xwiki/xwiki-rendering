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
package org.xwiki.rendering.wikimodel.test.xhtml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.rendering.wikimodel.xhtml.filter.DefaultXMLFilter;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XMLWriter extends DefaultXMLFilter
{
    StringBuffer fBuffer = new StringBuffer();

    public String getBuffer()
    {
        return fBuffer.toString();
    }

    public void reset()
    {
        fBuffer.setLength(0);
    }

    @Override
    public void characters(char[] array, int start, int length)
        throws SAXException
    {
        fBuffer.append(array, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
        Attributes atts) throws SAXException
    {
        fBuffer.append("<" + localName + ">");
    }

    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        fBuffer.append("</" + localName + ">");
    }

    @Override
    public void startCDATA() throws SAXException
    {
        fBuffer.append("<![CDATA[");
    }

    @Override
    public void endCDATA() throws SAXException
    {
        fBuffer.append("]]>");
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        fBuffer.append("<!--");
        fBuffer.append(ch, start, length);
        fBuffer.append("-->");
    }
}

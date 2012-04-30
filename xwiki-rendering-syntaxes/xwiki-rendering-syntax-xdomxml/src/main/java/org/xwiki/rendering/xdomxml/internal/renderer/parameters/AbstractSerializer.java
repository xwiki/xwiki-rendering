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
package org.xwiki.rendering.xdomxml.internal.renderer.parameters;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;

public abstract class AbstractSerializer
{
    public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    public void serializeParameter(String name, Map<String, String> map, ContentHandler contentHandler)
    {
        startElement(name, EMPTY_ATTRIBUTES, contentHandler);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            serializeParameter(entry.getKey(), entry.getValue(), contentHandler);
        }
        endElement(name, contentHandler);
    }

    public void serializeParameter(String name, char value, ContentHandler contentHandler)
    {
        serializeParameter(name, String.valueOf(value), contentHandler);
    }

    public void serializeParameter(String name, int value, ContentHandler contentHandler)
    {
        serializeParameter(name, String.valueOf(value), contentHandler);
    }

    public void serializeParameter(String name, boolean value, ContentHandler contentHandler)
    {
        serializeParameter(name, String.valueOf(value), contentHandler);
    }

    public void serializeParameter(String name, String value, ContentHandler contentHandler)
    {
        String nodeName;
        Attributes attributes;

        if (isValidNodeName(name)) {
            nodeName = name;
            attributes = EMPTY_ATTRIBUTES;
        } else {
            nodeName = "entry";
            AttributesImpl attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute(null, null, "name", null, name);
            attributes = attributesImpl;
        }

        startElement(nodeName, attributes, contentHandler);
        characters(value, contentHandler);
        endElement(nodeName, contentHandler);
    }

    public boolean isValidNodeName(String name)
    {
        return XDOMXMLConstants.VALID_ELEMENTNAME.matcher(name).matches();
    }

    public void startElement(String elementName, Attributes attributes, ContentHandler contentHandler)
    {
        try {
            contentHandler.startElement("", elementName, elementName, attributes);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }

    public void characters(String str, ContentHandler contentHandler)
    {
        try {
            contentHandler.characters(str.toCharArray(), 0, str.length());
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }

    public void endElement(String elementName, ContentHandler contentHandler)
    {
        try {
            contentHandler.endElement("", elementName, elementName);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }
}

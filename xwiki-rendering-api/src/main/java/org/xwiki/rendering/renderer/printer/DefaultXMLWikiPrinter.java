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
package org.xwiki.rendering.renderer.printer;

import java.io.IOException;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultComment;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultEntity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.rendering.internal.renderer.printer.WikiWriter;
import org.xwiki.rendering.internal.renderer.printer.XHTMLWriter;
import org.xwiki.xml.XMLUtils;

/**
 * Base toolkit class for all XML-based printers.
 * 
 * @version $Id$
 * @since 1.9M1
 */
public class DefaultXMLWikiPrinter implements XMLWikiPrinter
{
    /** XWikiWriter. */
    protected WikiWriter wikiWriter;

    /** XMLWriter. */
    protected XMLWriter xmlWriter;
    
    /**
     * @param printer the object to which to write the XHTML output to
     */
    public DefaultXMLWikiPrinter(WikiPrinter printer)
    {
        this.wikiWriter = new WikiWriter(printer);

        this.xmlWriter = new XHTMLWriter(this.wikiWriter);
    }

    @Override
    public XMLWriter getXMLWriter()
    {
        return this.xmlWriter;
    }

    @Override
    public void setWikiPrinter(WikiPrinter printer)
    {
        this.wikiWriter.setWikiPrinter(printer);
    }

    @Override
    public void printXML(String str)
    {
        try {
            this.xmlWriter.write(str);
        } catch (IOException e) {
            // TODO: add error log here
        }
    }

    @Override
    public void printXMLElement(String name)
    {
        printXMLElement(name, (String[][]) null);
    }

    @Override
    public void printXMLElement(String name, String[][] attributes)
    {
        Element element = new DefaultElement(name);

        if (attributes != null && attributes.length > 0) {
            for (String[] entry : attributes) {
                element.addAttribute(entry[0], entry[1]);
            }
        }

        try {
            this.xmlWriter.write(element);
        } catch (IOException e) {
            // TODO: add error log here
        }
    }

    @Override
    public void printXMLElement(String name, Map<String, String> attributes)
    {
        Element element = new DefaultElement(name);

        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                element.addAttribute(entry.getKey(), entry.getValue());
            }
        }

        try {
            this.xmlWriter.write(element);
        } catch (IOException e) {
            // TODO: add error log here
        }
    }

    @Override
    public void printXMLStartElement(String name)
    {
        printXMLStartElement(name, new AttributesImpl());
    }

    @Override
    public void printXMLStartElement(String name, String[][] attributes)
    {
        printXMLStartElement(name, createAttributes(attributes));
    }

    @Override
    public void printXMLStartElement(String name, Map<String, String> attributes)
    {
        printXMLStartElement(name, createAttributes(attributes));
    }

    @Override
    public void printXMLStartElement(String name, Attributes attributes)
    {
        try {
            this.xmlWriter.startElement("", name, name, attributes);
        } catch (SAXException e) {
            // TODO: add error log here
        }
    }

    @Override
    public void printXMLEndElement(String name)
    {
        try {
            this.xmlWriter.endElement("", name, name);
        } catch (SAXException e) {
            // TODO: add error log here
        }
    }

    @Override
    public void printXMLComment(String content)
    {
        printXMLComment(content, false);
    }

    @Override
    public void printXMLComment(String content, boolean escape)
    {
        try {
            this.xmlWriter.write(new DefaultComment(escape ? XMLUtils.escapeXMLComment(content) : content));
        } catch (IOException e) {
            // TODO: add error log here
        }
    }

    @Override
    public void printXMLStartCData()
    {
        try {
            this.xmlWriter.startCDATA();
            // Ensure that characters inside CDATA sections are not escaped
            this.xmlWriter.setEscapeText(false);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void printXMLEndCData()
    {
        try {
            this.xmlWriter.setEscapeText(true);
            this.xmlWriter.endCDATA();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void printEntity(String entity)
    {
        try {
            this.xmlWriter.write(new DefaultEntity(entity, entity));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void printRaw(String row)
    {
        try {
            this.wikiWriter.write(row);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * Convert provided table into {@link Attributes} to use in xml writer.
     * 
     * @param parameters attributes as a table
     * @return corresponding attributes
     */
    private Attributes createAttributes(String[][] parameters)
    {
        AttributesImpl attributes = new AttributesImpl();

        if (parameters != null && parameters.length > 0) {
            for (String[] entry : parameters) {
                attributes.addAttribute(null, null, entry[0], null, entry[1]);
            }
        }

        return attributes;
    }

    /**
     * Convert provided map into {@link Attributes} to use in xml writer.
     * 
     * @param parameters attributes as a map key, value
     * @return corresponding attributes
     */
    private Attributes createAttributes(Map<String, String> parameters)
    {
        AttributesImpl attributes = new AttributesImpl();

        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String value = entry.getValue();
                String key = entry.getKey();

                if (key != null && value != null) {
                    attributes.addAttribute(null, null, key, null, value);
                }
            }
        }

        return attributes;
    }
    
}


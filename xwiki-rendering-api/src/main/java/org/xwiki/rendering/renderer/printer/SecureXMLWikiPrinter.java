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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * Secure XML printer, preventing XSS attacks by allowing only safe attributes (white list) and for {@code href} and
 * {@code src} by only allowing content that's safe.
 * 
 * @version $Id$
 * @since 5.1RC1
 */
public class SecureXMLWikiPrinter extends DefaultXMLWikiPrinter implements XMLWikiPrinter
{
    /**
     * List of authorized attributes.
     */
    private static final List<String> ATTRIBUTES_WHITELIST = 
        Arrays.asList("alt", "class", "height", "id", "name", "rel", "scope", "style", "target", "title", "width");
    
    /**
     * Attributes that should be authorized only if their value is safe.
     */
    private static final List<String> VULNERABLE_ATTRIBUTES = Arrays.asList("href", "src");
    
    /**
     * Constructor.
     * 
     * @param printer the object to which to write the XHTML output to
     */
    public SecureXMLWikiPrinter(WikiPrinter printer)
    {
        super(printer);
    }
    
    @Override
    public void printXMLElement(String name, String[][] attributes)
    {
        Element element = new DefaultElement(name);

        if (attributes != null && attributes.length > 0) {
            for (String[] entry : attributes) {
                if (isAttributeClean(entry[0], entry[1])) {
                    // We add this attribute if and only if it is safe.
                    element.addAttribute(entry[0], entry[1]);
                }
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
                if (isAttributeClean(entry.getKey(), entry.getValue())) {
                    element.addAttribute(entry.getKey(), entry.getValue());
                }
            }
        }

        try {
            this.xmlWriter.write(element);
        } catch (IOException e) {
            // TODO: add error log here
        }
    }
    
    @Override
    public void printXMLStartElement(String name, Attributes attributes)
    {
        try {
            this.xmlWriter.startElement("", name, name, cleanAttributes(attributes));
        } catch (SAXException e) {
            // TODO: add error log here
        }
    }
    
    /**
     * Clean attributes to prevent XSS.
     * 
     * @param attributes Attributes to clean
     * @return clean attributes 
     */
    private Attributes cleanAttributes(Attributes attributes)
    {
        AttributesImpl cleanAttributes = new AttributesImpl();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            String qName = attributes.getQName(i);
            String value = attributes.getValue(i);
            if (isAttributeClean(qName, value)) {
                cleanAttributes.addAttribute(attributes.getURI(i), attributes.getLocalName(i), 
                    qName, attributes.getType(i), value);
            }
        }
        return cleanAttributes;
    }

    /**
     * Determine whether an attribute is safe or not.
     * 
     * @param key Name of the attribute
     * @param value Value of the attribute
     * @return true if the attribute is in the attributes whitelist or if its value is safe 
     */
    private boolean isAttributeClean(String key, String value)
    {
        // Let's trim the attribute value to make sure that leading whitespaces won't create any issue.
        String tValue = value.trim();
        if (ATTRIBUTES_WHITELIST.contains(key)) {
            return true;
        } else if (VULNERABLE_ATTRIBUTES.contains(key)) {
            boolean isURL = Pattern.matches("^[a-zA-Z0-9+.-]*://.*$", tValue);
            if (isURL || tValue.startsWith("/") || tValue.startsWith("mailto") || tValue.startsWith("#")) {
                return true;
            }
        }
        return false;
    }
    
}

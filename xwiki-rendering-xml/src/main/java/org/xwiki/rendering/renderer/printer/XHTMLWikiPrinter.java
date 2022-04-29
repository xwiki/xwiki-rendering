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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.stability.Unstable;
import org.xwiki.xml.html.HTMLElementSanitizer;

/**
 * Base toolkit class for all XHTML-based renderers. This printer handles whitespaces so that it prints "&nbsp;" when
 * needed (i.e. when the spaces are at the beginning or at the end of an element's content or when there are more than 1
 * contiguous spaces, except for CDATA sections and inside PRE elements. It also knows how to handle XHTML comments).
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XHTMLWikiPrinter extends XMLWikiPrinter
{
    /**
     * Prefix that is used for invalid/disallowed attributes.
     *
     * @since 14.6RC1
     */
    @Unstable
    public static final String TRANSLATED_ATTRIBUTE_PREFIX = "data-xwiki-translated-attribute-";

    /**
     * The sanitizer used to restrict allowed elements and attributes, can be null (no restrictions).
     *
     * @since 14.6RC1
     */
    protected final HTMLElementSanitizer htmlElementSanitizer;

    private int spaceCount;

    private boolean isInCData;

    private boolean isInPreserveElement;

    private boolean elementEnded;

    private boolean hasTextBeenPrinted;

    private boolean isStandalone;

    /**
     * @param printer the object to which to write the XHTML output to
     */
    public XHTMLWikiPrinter(WikiPrinter printer)
    {
        this(printer, null);
    }

    /**
     * @param printer the object to which to write the XHTML output to
     * @param htmlElementSanitizer the sanitizer to use for sanitizing elements and attributes
     */
    public XHTMLWikiPrinter(WikiPrinter printer, HTMLElementSanitizer htmlElementSanitizer)
    {
        super(printer);
        this.htmlElementSanitizer = htmlElementSanitizer;
    }

    /**
     * Use it to specify that the current element to print is standalone. This value might be used to know if the first
     * space should be printed with a simple space or a {@code &nbsp;} entity. Note that the standalone value is
     * automatically reset after first printing of a space, or when a text is printed.
     *
     * @since 12.2
     */
    public void setStandalone()
    {
        this.isStandalone = true;
    }

    @Override
    public void printXML(String str)
    {
        handleSpaceWhenInText();
        super.printXML(str);
        this.hasTextBeenPrinted = true;
        this.isStandalone = false;
    }

    @Override
    public void printXMLElement(String name)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLElement(name);
        }
    }

    @Override
    public void printXMLElement(String name, String[][] attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLElement(String name, Map<String, String> attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLStartElement(String name)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name);
        }
    }

    @Override
    public void printXMLStartElement(String name, String[][] attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLStartElement(String name, Map<String, String> attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLStartElement(String name, Attributes attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLEndElement(String name)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenEndlement();
            super.printXMLEndElement(name);
            this.elementEnded = true;
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
        handleSpaceWhenStartElement();
        super.printXMLComment(content, escape);
        this.elementEnded = true;
    }

    @Override
    public void printXMLStartCData()
    {
        handleSpaceWhenStartElement();
        super.printXMLStartCData();
    }

    @Override
    public void printXMLEndCData()
    {
        handleSpaceWhenEndlement();
        super.printXMLEndCData();
    }

    /**
     * This method should be used to print a space rather than calling <code>printXML(" ")</code>.
     */
    public void printSpace()
    {
        this.spaceCount++;
    }

    @Override
    public void printRaw(String raw)
    {
        handleSpaceWhenStartElement();
        super.printRaw(raw);
        this.elementEnded = true;
    }

    private void handleSpaceWhenInText()
    {
        if (this.elementEnded || this.hasTextBeenPrinted) {
            handleSpaceWhenStartElement();
        } else {
            handleSpaceWhenEndlement();
        }
    }

    private Map<String, String> cleanAttributes(String elementName, Map<String, String> attributes)
    {
        Map<String, String> cleanAttributes;

        if (this.htmlElementSanitizer == null || attributes == null) {
            cleanAttributes = attributes;
        } else {
            cleanAttributes = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                if (this.htmlElementSanitizer.isAttributeAllowed(elementName, e.getKey(), e.getValue())) {
                    cleanAttributes.put(e.getKey(), e.getValue());
                } else {
                    cleanAttributes.put(TRANSLATED_ATTRIBUTE_PREFIX + e.getKey(), e.getValue());
                }
            }
        }

        return cleanAttributes;
    }

    private String[][] cleanAttributes(String elementName, String[][] attributes)
    {
        String[][] allowedAttributes;
        if (this.htmlElementSanitizer == null || attributes == null) {
            allowedAttributes = attributes;
        } else {
            allowedAttributes = Arrays.stream(attributes)
                .map(entry -> {
                    if (this.htmlElementSanitizer.isAttributeAllowed(elementName, entry[0], entry[1])) {
                        return entry;
                    } else {
                        return new String[] { TRANSLATED_ATTRIBUTE_PREFIX + entry[0], entry[1] };
                    }
                })
                .toArray(String[][]::new);
        }

        return allowedAttributes;
    }

    private Attributes cleanAttributes(String elementName, Attributes attributes)
    {
        Attributes allowedAttribute;

        if (this.htmlElementSanitizer == null || attributes == null) {
            allowedAttribute = attributes;
        } else {
            allowedAttribute = new AttributesImpl();

            for (int i = 0; i < attributes.getLength(); ++i) {
                if (this.htmlElementSanitizer.isAttributeAllowed(elementName, attributes.getQName(i),
                    attributes.getValue(i)))
                {
                    ((AttributesImpl) allowedAttribute).addAttribute(null, null, attributes.getQName(i),
                        null, attributes.getValue(i));
                } else {
                    ((AttributesImpl) allowedAttribute).addAttribute(null, null,
                        TRANSLATED_ATTRIBUTE_PREFIX + attributes.getQName(i), null, attributes.getValue(i));
                }
            }
        }

        return allowedAttribute;
    }

    private void handleSpaceWhenStartElement()
    {
        // Use case: <tag1>something <tag2>...
        // Use case: <tag1>something <!--...
        if (this.spaceCount > 0) {
            if (!this.isInCData && !this.isInPreserveElement) {
                // We print a single space as a normal space, except if we are at the beginning of a standalone element
                // in that case we want a non-breaking space so it won't be stripped.
                // Any supplementary space will be printed as non-breaking spaces so we keep them too.
                if (this.isStandalone && !this.hasTextBeenPrinted) {
                    printEntity("&nbsp;");
                } else {
                    super.printXML(" ");
                }
                for (int i = 0; i < this.spaceCount - 1; i++) {
                    printEntity("&nbsp;");
                }
            } else {
                super.printXML(StringUtils.repeat(' ', this.spaceCount));
            }
            this.isStandalone = false;
        }
        this.spaceCount = 0;
        this.elementEnded = false;
        this.hasTextBeenPrinted = false;
    }

    private void handleSpaceWhenEndlement()
    {
        // Use case: <tag1>something </tag1>...
        // All spaces are &nbsp; spaces since otherwise they'll be all stripped by browsers
        if (!this.isInCData && !this.isInPreserveElement) {
            for (int i = 0; i < this.spaceCount; i++) {
                printEntity("&nbsp;");
            }
        } else {
            super.printXML(StringUtils.repeat(' ', this.spaceCount));
        }
        this.spaceCount = 0;
        this.elementEnded = false;
        this.hasTextBeenPrinted = false;
    }
}

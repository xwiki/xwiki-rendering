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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;

/**
 * Secure version of the XHTMLWikiPrinter, preventing XSS attacks.
 * 
 * @version $Id$
 * @since 5.1RC1
 */
public class SecureXHTMLWikiPrinter extends SecureXMLWikiPrinter implements XHTMLWikiPrinter
{
    /** String representing non-breaking spaces. */
    private static final String NBSP = "&nbsp;";
    
    /** For counting spaces printed.*/
    private int spaceCount;

    /** For CData sections. */
    private boolean isInCData;

    /** Indicates whether the element should be preserved. */
    private boolean isInPreserveElement;

    /** Indicates whether the element has ended or not. */
    private boolean elementEnded;

    /** Indicates whether the element text has been printed or not.*/
    private boolean hasTextBeenPrinted;

    /**
     * Constructor.
     * 
     * @param printer the object to which to write the XHTML output to
     */
    public SecureXHTMLWikiPrinter(WikiPrinter printer)
    {
        super(printer);
    }

    @Override
    public void printXML(String str)
    {
        handleSpaceWhenInText();
        super.printXML(str);
        this.hasTextBeenPrinted = true;
    }

    @Override
    public void printXMLElement(String name)
    {
        handleSpaceWhenStartElement();
        super.printXMLElement(name);
    }

    @Override
    public void printXMLElement(String name, String[][] attributes)
    {
        handleSpaceWhenStartElement();
        super.printXMLElement(name, attributes);
    }

    @Override
    public void printXMLElement(String name, Map<String, String> attributes)
    {
        handleSpaceWhenStartElement();
        super.printXMLElement(name, attributes);
    }

    @Override
    public void printXMLStartElement(String name)
    {
        handleSpaceWhenStartElement();
        super.printXMLStartElement(name);
    }

    @Override
    public void printXMLStartElement(String name, String[][] attributes)
    {
        handleSpaceWhenStartElement();
        super.printXMLStartElement(name, attributes);
    }

    @Override
    public void printXMLStartElement(String name, Map<String, String> attributes)
    {
        handleSpaceWhenStartElement();
        super.printXMLStartElement(name, attributes);
    }

    @Override
    public void printXMLStartElement(String name, Attributes attributes)
    {
        handleSpaceWhenStartElement();
        super.printXMLStartElement(name, attributes);
    }

    @Override
    public void printXMLEndElement(String name)
    {
        handleSpaceWhenEndlement();
        super.printXMLEndElement(name);
        this.elementEnded = true;
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

    @Override
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

    /**
     * Handling space in text.
     */
    private void handleSpaceWhenInText()
    {
        if (this.elementEnded || this.hasTextBeenPrinted) {
            handleSpaceWhenStartElement();
        } else {
            handleSpaceWhenEndlement();
        }
    }

    /**
     * Handling space when starting element.
     */
    private void handleSpaceWhenStartElement()
    {
        // Use case: <tag1>something <tag2>...
        // Use case: <tag1>something <!--...
        if (this.spaceCount > 0) {
            if (!this.isInCData && !this.isInPreserveElement) {
                // The first space is a normal space
                super.printXML(" ");
                for (int i = 0; i < this.spaceCount - 1; i++) {
                    printEntity(NBSP);
                }
            } else {
                super.printXML(StringUtils.repeat(' ', this.spaceCount));
            }
        }
        this.spaceCount = 0;
        this.elementEnded = false;
        this.hasTextBeenPrinted = false;
    }

    /**
     * Handling space when ending element.
     */
    private void handleSpaceWhenEndlement()
    {
        // Use case: <tag1>something </tag1>...
        // All spaces are &nbsp; spaces since otherwise they'll be all stripped by browsers
        if (!this.isInCData && !this.isInPreserveElement) {
            for (int i = 0; i < this.spaceCount; i++) {
                printEntity(NBSP);
            }
        } else {
            super.printXML(StringUtils.repeat(' ', this.spaceCount));
        }
        this.spaceCount = 0;
        this.elementEnded = false;
        this.hasTextBeenPrinted = false;
    }
}

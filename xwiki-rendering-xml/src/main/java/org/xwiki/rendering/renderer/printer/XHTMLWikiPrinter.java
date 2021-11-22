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
 * Base toolkit class for all XHTML-based renderers. This printer handles whitespaces so that it prints "&nbsp;" when
 * needed (i.e. when the spaces are at the beginning or at the end of an element's content or when there are more than 1
 * contiguous spaces, except for CDATA sections and inside PRE elements. It also knows how to handle XHTML comments).
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XHTMLWikiPrinter extends XMLWikiPrinter
{
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
        super(printer);
    }

    /**
     * Use it to specify that the current element to print is standalone.
     * This value might be used to know if the first space should be printed with a simple space or a {@code &nbsp;}
     * entity. Note that the standalone value is automatically reset after first printing of a space, or when a text
     * is printed.
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

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
package org.xwiki.rendering.internal.renderer.xml;

import org.dom4j.io.XMLWriter;
import org.xwiki.rendering.internal.renderer.printer.WikiWriter;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRenderer;

/**
 * Convert SAX events into {@link WikiPrinter} calls.
 * 
 * @version $Id$
 * @since 3.3M1
 */
public class ContentHandlerPrintRenderer extends WrappingListener implements PrintRenderer
{
    /**
     * Used to convert {@link java.io.Writer} calls to {@link WikiPrinter} calls.
     */
    private WikiWriter wikiWriter;

    /**
     * Used to write String based on SAX events.
     */
    private XMLWriter xmlWriter;

    /**
     * @param renderer the wrapped renderer
     * @param printer the printer
     */
    public ContentHandlerPrintRenderer(ContentHandlerStreamRenderer renderer, WikiPrinter printer)
    {
        setWrappedListener(renderer);

        this.wikiWriter = new WikiWriter(printer);

        this.xmlWriter = new XMLWriter(this.wikiWriter)
        {
            @Override
            // FIXME: remove that when https://sourceforge.net/p/dom4j/bugs/202/ is fixed
            protected String escapeAttributeEntities(String text)
            {
                String escapedTest = super.escapeAttributeEntities(text);
                escapedTest = escapedTest.replace("\t", "&#9;");
                escapedTest = escapedTest.replace("\n", "&#10;");
                escapedTest = escapedTest.replace("\r", "&#13;");

                return escapedTest;
            }
        };
        // escape all non US-ASCII to have as less encoding problems as possible
        this.xmlWriter.setMaximumAllowedCharacter(127);

        renderer.setContentHandler(this.xmlWriter);
    }

    @Override
    public WikiPrinter getPrinter()
    {
        return this.wikiWriter.getWikiPrinter();
    }

    @Override
    public void setPrinter(WikiPrinter printer)
    {
        this.wikiWriter.setWikiPrinter(printer);
    }
}

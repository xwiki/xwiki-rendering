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

import java.io.UnsupportedEncodingException;

import org.dom4j.io.XMLWriter;
import org.xwiki.rendering.internal.renderer.printer.WikiWriter;
import org.xwiki.rendering.internal.renderer.printer.XHTMLWriter;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRenderer;

/**
 * @version $Id$
 */
public class ContentHandlerPrintRenderer extends WrappingListener implements PrintRenderer
{
    private WikiWriter wikiWriter;

    private XMLWriter xmlWriter;

    public ContentHandlerPrintRenderer(ContentHandlerStreamRenderer renderer, WikiPrinter printer)
    {
        setWrappedListener(renderer);

        this.wikiWriter = new WikiWriter(printer);

        try {
            this.xmlWriter = new XHTMLWriter(this.wikiWriter);
        } catch (UnsupportedEncodingException e) {
            // TODO: add error log "should not append"
        }

        renderer.setContentHandler(this.xmlWriter);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.PrintRenderer#getPrinter()
     */
    public WikiPrinter getPrinter()
    {
        return this.wikiWriter.getWikiPrinter();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.PrintRenderer#setPrinter(org.xwiki.rendering.renderer.printer.WikiPrinter)
     */
    public void setPrinter(WikiPrinter printer)
    {
        this.wikiWriter.setWikiPrinter(printer);
    }
}

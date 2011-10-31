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

import javax.inject.Inject;

import org.xml.sax.ContentHandler;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRenderer;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRendererFactory;

public abstract class AbstractStreamRendererFactory implements PrintRendererFactory,
    ContentHandlerStreamRendererFactory
{
    @Inject
    protected ComponentManager componentManager;

    protected ContentHandlerStreamRenderer createContentHandlerStreamRenderer()
    {
        ContentHandlerStreamRenderer renderer;
        try {
            renderer = this.componentManager.lookup(ContentHandlerStreamRenderer.class, getSyntax().toIdString());
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to create [" + getSyntax().toString() + "] renderer", e);
        }

        return renderer;
    }

    @Override
    public ContentHandlerStreamRenderer createRenderer(ContentHandler contentHandler)
    {
        ContentHandlerStreamRenderer renderer = createContentHandlerStreamRenderer();

        renderer.setContentHandler(contentHandler);

        return renderer;
    }

    @Override
    public PrintRenderer createRenderer(WikiPrinter printer)
    {
        return new ContentHandlerPrintRenderer(createContentHandlerStreamRenderer(), printer);
    }
}

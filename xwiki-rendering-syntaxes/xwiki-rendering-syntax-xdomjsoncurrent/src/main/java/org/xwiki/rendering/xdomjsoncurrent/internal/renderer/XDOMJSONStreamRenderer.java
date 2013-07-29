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
package org.xwiki.rendering.xdomjsoncurrent.internal.renderer;

import java.io.IOException;

import org.xwiki.filter.json.serializer.JSONSerializerFactory;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinterWriter;

/**
 * Current version of the XDOM+JSON stream based renderer.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class XDOMJSONStreamRenderer extends WrappingListener implements PrintRenderer
{
    /**
     * The actual JSON serializer factory.
     */
    private JSONSerializerFactory serializerFactory;

    private WikiPrinterWriter writer;

    /**
     * @param serializerFactory the actual JSON serializer factory
     */
    public XDOMJSONStreamRenderer(JSONSerializerFactory serializerFactory, WikiPrinter printer)
    {
        this.serializerFactory = serializerFactory;
        setPrinter(printer);
    }

    @Override
    public WikiPrinter getPrinter()
    {
        return this.writer.getPrinter();
    }

    @Override
    public void setPrinter(WikiPrinter printer)
    {
        this.writer = new WikiPrinterWriter(printer);

        try {
            setWrappedListener(this.serializerFactory.createSerializer(Listener.class, this.writer, null));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

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
package org.xwiki.rendering.xdomxmlcurrent.internal.renderer;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.ContentHandler;
import org.xwiki.filter.xml.serializer.XMLSerializerFactory;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRenderer;
import org.xwiki.rendering.syntax.Syntax;

import static org.xwiki.rendering.xdomxmlcurrent.internal.XDOMXMLCurrentSyntaxProvider.XDOMXML_CURRENT;

/**
 * Current version of the XDOM+XML stream based renderer.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class XDOMXMLChainingStreamRenderer extends WrappingListener implements ContentHandlerStreamRenderer
{
    /**
     * The actual XML serializer factory.
     */
    private XMLSerializerFactory serializerFactory;

    /**
     * The content handler to send SAX events to.
     */
    private ContentHandler contentHandler;

    /**
     * @param serializerFactory the actual XML serializer factory
     */
    public XDOMXMLChainingStreamRenderer(XMLSerializerFactory serializerFactory)
    {
        this.serializerFactory = serializerFactory;
    }

    @Override
    public Syntax getSyntax()
    {
        return XDOMXML_CURRENT;
    }

    @Override
    public ContentHandler getContentHandler()
    {
        return this.contentHandler;
    }

    @Override
    public void setContentHandler(ContentHandler contentHandler)
    {
        this.contentHandler = contentHandler;

        try {
            setWrappedListener(this.serializerFactory.createSerializer(Listener.class, new SAXResult(
                this.contentHandler), null));
        } catch (XMLStreamException e) {
            // Should never happen
            // TODO: log an error something
        }
    }
}

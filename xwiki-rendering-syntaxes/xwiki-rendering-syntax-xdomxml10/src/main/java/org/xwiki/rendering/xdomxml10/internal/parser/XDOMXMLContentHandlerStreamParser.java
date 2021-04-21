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
package org.xwiki.rendering.xdomxml10.internal.parser;

import javax.inject.Inject;
import javax.inject.Named;

import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxml10.internal.XDOMXMLConstants;

import static org.xwiki.rendering.xdomxml10.internal.XDOMXML10SyntaxProvider.XDOMXML_1_0;

@Component
@Named("xdom+xml/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XDOMXMLContentHandlerStreamParser extends DefaultHandler implements ContentHandlerStreamParser
{
    private Listener listener;

    private BlockParser documentParser;

    @Inject
    private ComponentManager componentManager;

    /**
     * Avoid create a new SAXContentHandler for each block when the same can be used for all.
     */
    public SAXContentHandler currentDOMBuilder = new SAXContentHandler();

    @Override
    public Syntax getSyntax()
    {
        return XDOMXML_1_0;
    }

    @Override
    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.documentParser != null) {
            this.documentParser.startElement(uri, localName, qName, attributes);
        } else if (XDOMXMLConstants.ELEM_BLOCK.equals(qName)) {
            try {
                this.documentParser = getDocumentBlockParser();

                this.documentParser.setListener(this.listener);
                this.documentParser.setVersion(XDOMXML_1_0.getVersion());

                this.documentParser.startElement(uri, localName, qName, attributes);
            } catch (ComponentLookupException e) {
                throw new SAXException("Failed to find a document block parser", e);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.documentParser != null) {
            this.documentParser.characters(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (this.documentParser != null) {
            this.documentParser.endElement(uri, localName, qName);
        }
    }

    protected BlockParser getDocumentBlockParser() throws ComponentLookupException
    {
        BlockParser blockParser;
        try {
            blockParser =
                this.componentManager.getInstance(BlockParser.class, "document/" + XDOMXML_1_0.getVersion());
        } catch (ComponentLookupException e1) {
            try {
                blockParser = this.componentManager.getInstance(BlockParser.class, "document");
            } catch (ComponentLookupException e2) {
                blockParser = this.componentManager.getInstance(BlockParser.class);
            }
        }

        return blockParser;
    }
}

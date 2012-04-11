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
package org.xwiki.rendering.xdomxml.internal.parser;

import javax.inject.Inject;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;

public abstract class AbstractBlockParser extends DefaultHandler implements BlockParser
{
    @Inject
    private ComponentManager componentManager;

    private String blockName;

    private ContentHandler currentHandler;

    private int currentHandlerLevel;

    private int level = 0;

    private String blockVersion;

    private boolean beginBlockFlushed;

    private Listener listener;

    public void setComponentManager(ComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    public ComponentManager getComponentManager()
    {
        return componentManager;
    }

    @Override
    public Listener getListener()
    {
        return this.listener;
    }

    @Override
    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    @Override
    public String getVersion()
    {
        return this.blockVersion;
    }

    @Override
    public void setVersion(String version)
    {
        this.blockVersion = version;
    }

    public String getBlockName()
    {
        return this.blockName;
    }

    public int getLevel()
    {
        return this.level;
    }

    // ContentHandler

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.currentHandler != null) {
            this.currentHandler.startElement(uri, localName, qName, attributes);
        } else {
            if (qName.equals(XDOMXMLConstants.ELEM_BLOCK)) {
                String name = attributes.getValue(XDOMXMLConstants.ATT_BLOCK_NAME);
                String version = attributes.getValue(XDOMXMLConstants.ATT_BLOCK_VERSION);

                if (this.level == 0 && this.blockName == null) {
                    this.blockName = name;
                    this.blockVersion = version;
                } else {
                    flushBeginBlock();

                    // start parsing new child block
                    try {
                        BlockParser blockParser = getBlockParser(name);

                        blockParser.setListener(getListener());
                        blockParser.setVersion(getVersion());

                        setCurrentHandler(blockParser);
                    } catch (ComponentLookupException e) {
                        throw new SAXException("Failed to find a block parser for [" + name + "]", e);
                    }
                }
            } else {
                startElementInternal(uri, localName, qName, attributes);
            }

            if (this.currentHandler != null) {
                this.currentHandler.startElement(uri, localName, qName, attributes);
            }
        }

        ++this.level;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.currentHandler != null) {
            this.currentHandler.characters(ch, start, length);
        } else {
            charactersInternal(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        --this.level;

        if (this.currentHandler != null) {
            this.currentHandler.endElement(uri, localName, qName);

            if (this.level == this.currentHandlerLevel) {
                endElementInternal(uri, localName, qName);
                this.currentHandler = null;
            }
        } else {
            if (this.level == 0) {
                flushBeginBlock();
                endElementInternal(uri, localName, qName);
                endBlock();
            } else {
                endElementInternal(uri, localName, qName);
            }
        }
    }

    // to override

    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        // no op
    }

    protected void charactersInternal(char[] ch, int start, int length) throws SAXException
    {
        // no op
    }

    protected void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        // no op
    }

    protected void beginBlock() throws SAXException
    {
        // no op
    }

    protected void endBlock() throws SAXException
    {
        // no op
    }

    // tools

    protected void flushBeginBlock() throws SAXException
    {
        if (!this.beginBlockFlushed) {
            beginBlock();
            this.beginBlockFlushed = true;
        }
    }

    protected void setCurrentHandler(ContentHandler currentHandler)
    {
        this.currentHandler = currentHandler;
        this.currentHandlerLevel = this.level;
    }

    public ContentHandler getCurrentHandler()
    {
        return this.currentHandler;
    }

    protected BlockParser getBlockParser(String name) throws ComponentLookupException
    {
        BlockParser blockParser;
        try {
            blockParser = this.componentManager.getInstance(BlockParser.class, name + "/" + getVersion());
        } catch (ComponentLookupException e1) {
            try {
                blockParser = this.componentManager.getInstance(BlockParser.class, name);
            } catch (ComponentLookupException e2) {
                blockParser = this.componentManager.getInstance(BlockParser.class);
            }
        }

        return blockParser;
    }
}

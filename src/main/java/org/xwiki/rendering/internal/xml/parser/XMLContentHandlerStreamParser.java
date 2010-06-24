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
package org.xwiki.rendering.internal.xml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dom4j.Element;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.rendering.internal.xml.XMLEntities;
import org.xwiki.rendering.internal.xml.parameters.ParameterManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * @version $Id$
 */
@Component
public class XMLContentHandlerStreamParser extends DefaultHandler implements ContentHandlerStreamParser, XMLEntities
{
    private static Object[] DEFAULT_PARAMETERS = new Object[] {Listener.EMPTY_PARAMETERS};

    @Requirement
    private ParameterManager parameterManager;
    
    private Listener listener;

    private Stack<Block> blockStack = new Stack<Block>();

    private Stack<String> currentElement = new Stack<String>();

    /**
     * Avoid create a new SAXContentHandler for each block when the same can be used for all.
     */
    public SAXContentHandler currentDOMBuilder = new SAXContentHandler();

    /**
     * A block.
     * 
     * @version $Id$
     */
    public static class Block
    {
        public EventType beginEventType;

        public EventType endEventType;

        public EventType onEventType;

        public boolean beginSent = false;

        public List<Object> parametersList = new ArrayList<Object>();

        private Object[] parametersTable;

        public SAXContentHandler parameterDOMBuilder;

        public Block(EventType beginEventType, EventType endEventType)
        {
            this.beginEventType = beginEventType;
            this.endEventType = endEventType;
        }

        public Block(EventType onEventType)
        {
            this.onEventType = onEventType;
        }

        public boolean isContainer()
        {
            return this.onEventType == null;
        }

        public void addParameter(Object parameter)
        {
            this.parametersList.add(parameter);
            this.parametersTable = null;
        }

        public Object[] getParametersTable()
        {
            if (this.parametersTable == null) {
                if (this.parametersList.isEmpty()) {
                    this.parametersTable = DEFAULT_PARAMETERS;
                }

                this.parametersTable = this.parametersList.toArray();
            }

            return this.parametersTable;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.parser.xml.ContentHandlerStreamParser#getSyntax()
     */
    public Syntax getSyntax()
    {
        return XML_1_0;
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        String parentElement = !this.currentElement.isEmpty() ? this.currentElement.peek() : null;

        if (ELEM_BLOCK.equals(qName)) {
            if (!this.blockStack.isEmpty()) {
                // send previous event
                Block block = this.blockStack.peek();

                if (block != null && !block.beginSent) {
                    block.beginEventType.fireEvent(this.listener, block.getParametersTable());
                    block.beginSent = true;
                }
            }

            // push new event
            Block block = getBlock(uri, localName, qName, attributes);

            this.blockStack.push(block);
        } else if (parentElement != null && parentElement.equals(ELEM_PARAMETERS)) {
            // starting a new block parameter
            Block block = this.blockStack.peek();

            block.parameterDOMBuilder = this.currentDOMBuilder;
            block.parameterDOMBuilder.startDocument();
        }

        if (!this.blockStack.isEmpty() && this.blockStack.peek().parameterDOMBuilder != null) {
            this.blockStack.peek().parameterDOMBuilder.startElement(uri, localName, qName, attributes);
        }

        this.currentElement.push(qName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parameterDOMBuilder != null) {
            this.blockStack.peek().parameterDOMBuilder.endElement(uri, localName, qName);
        }

        this.currentElement.pop();

        String parentElement = !this.currentElement.isEmpty() ? this.currentElement.peek() : null;

        if (ELEM_BLOCK.equals(qName)) {
            Block block = this.blockStack.pop();

            // Flush pending begin event and send end event or send on event
            if (block.isContainer()) {
                if (!block.beginSent) {
                    block.beginEventType.fireEvent(this.listener, block.getParametersTable());
                    block.beginSent = true;
                }

                block.endEventType.fireEvent(this.listener, block.getParametersTable());
            } else {
                block.onEventType.fireEvent(this.listener, block.getParametersTable());
            }
        } else if (ELEM_PARAMETERS.equals(qName)) {
            Block block = this.blockStack.peek();

            // Flush pending begin event
            if (block.isContainer()) {
                if (!block.beginSent) {
                    block.beginEventType.fireEvent(this.listener, block.getParametersTable());
                    block.beginSent = true;
                }
            }
        } else if (parentElement != null && parentElement.equals(ELEM_PARAMETERS)) {
            Block block = this.blockStack.peek();

            block.parameterDOMBuilder.endDocument();

            Element rootElement = block.parameterDOMBuilder.getDocument().getRootElement();
            block.addParameter(this.parameterManager.unSerialize(rootElement));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parameterDOMBuilder != null) {
            this.blockStack.peek().parameterDOMBuilder.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parameterDOMBuilder != null) {
            this.blockStack.peek().parameterDOMBuilder.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parameterDOMBuilder != null) {
            this.blockStack.peek().parameterDOMBuilder.skippedEntity(name);
        }
    }

    private Block getBlock(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        Block block;

        String name = attributes.getValue(ATT_BLOCK_NAME).toUpperCase();

        try {
            EventType onEventType = EventType.valueOf("ON_" + name);
            block = new Block(onEventType);
        } catch (IllegalArgumentException e) {
            // It's a container

            try {
                EventType beginEventType = EventType.valueOf("BEGIN_" + name);
                EventType endEventType = EventType.valueOf("END_" + name);

                block = new Block(beginEventType, endEventType);
            } catch (IllegalArgumentException e2) {
                throw new SAXException("Unknow block [" + qName + "]", e2);
            }
        }

        return block;
    }
}

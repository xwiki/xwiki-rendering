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
package org.xwiki.rendering.xdomxmlcurrent.internal.parser;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.properties.ConverterManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptor;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptorManager;
import org.xwiki.rendering.listener.descriptor.ListenerElement;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxmlcurrent.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxmlcurrent.internal.XDOMXMLCurrentUtils;
import org.xwiki.rendering.xdomxmlcurrent.internal.parameter.ParameterManager;
import org.xwiki.xml.Sax2Dom;

@Component("xdom+xml/current")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XDOMXMLContentHandlerStreamParser extends DefaultHandler implements ContentHandlerStreamParser
{
    private static final Object[] DEFAULT_PARAMETERS = new Object[] {Listener.EMPTY_PARAMETERS};

    @Inject
    private ParameterManager parameterManager;

    @Inject
    private ListenerDescriptorManager listenerDescriptorManager;

    @Inject
    private ConverterManager stringConverter;

    private ListenerDescriptor listenerDescriptor;

    private Listener listener;

    private Stack<Block> blockStack = new Stack<Block>();

    private int elementDepth = 0;

    private StringBuilder content;

    public static class Block
    {
        public ListenerElement listenerElement;

        public boolean beginSent = false;

        public List<Object> parametersList = new ArrayList<Object>();

        public Sax2Dom parameterDOMBuilder;

        public int elementDepth;

        private Object[] parametersTable;

        public Block(ListenerElement listenerElement, int elementDepth)
        {
            this.listenerElement = listenerElement;
            this.elementDepth = elementDepth;
        }

        public boolean isContainer()
        {
            return this.listenerElement.getOnMethod() == null;
        }

        public void setParameter(int index, Object parameter)
        {
            while (this.parametersList.size() <= index) {
                this.parametersList.add(null);
            }

            this.parametersList.set(index, parameter);
            this.parametersTable = null;
        }

        public List<Object> getParametersList()
        {
            return parametersList;
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

        public void fireBeginEvent(Listener listener, Object[] parameters) throws SAXException
        {
            fireEvent(this.listenerElement.getBeginMethod(), listener, parameters);
            this.beginSent = true;
        }

        public void fireEndEvent(Listener listener, Object[] parameters) throws SAXException
        {
            fireEvent(this.listenerElement.getEndMethod(), listener, parameters);
        }

        public void fireOnEvent(Listener listener, Object[] parameters) throws SAXException
        {
            fireEvent(this.listenerElement.getOnMethod(), listener, parameters);
        }

        private void fireEvent(Method eventMethod, Listener listener, Object[] parameters) throws SAXException
        {
            Object[] properParameters = parameters;
            Class< ? >[] methodParameters = eventMethod.getParameterTypes();

            // Missing parameters
            if (methodParameters.length > parameters.length) {
                properParameters = new Object[methodParameters.length];
                for (int i = 0; i < methodParameters.length; ++i) {
                    if (i < parameters.length) {
                        properParameters[i] = parameters[i];
                    } else {
                        properParameters[i] = null;
                    }
                }
            }

            // Invalid primitive
            for (int i = 0; i < properParameters.length; ++i) {
                Object parameter = properParameters[i];

                if (parameter == null) {
                    Class< ? > methodParameter = methodParameters[i];

                    if (methodParameter.isPrimitive()) {
                        properParameters[i] = XDOMXMLCurrentUtils.defaultValue(methodParameter);
                    }
                }
            }

            // Send event
            try {
                eventMethod.invoke(listener, properParameters);
            } catch (Exception e) {
                throw new SAXException("Failed to invoke event [" + eventMethod + "]", e);
            }
        }
    }

    @Override
    public Syntax getSyntax()
    {
        return Syntax.XDOMXML_CURRENT;
    }

    @Override
    public void setListener(Listener listener)
    {
        this.listener = listener;

        this.listenerDescriptor = this.listenerDescriptorManager.getListenerDescriptor(Listener.class);
    }

    private boolean onBlockChild()
    {
        boolean result;

        if (!this.blockStack.isEmpty()) {
            Block currentBlock = this.blockStack.peek();

            return currentBlock.elementDepth == (this.elementDepth - 1);
        } else {
            result = false;
        }

        return result;
    }

    private boolean onBlockElement(String elementName)
    {
        boolean result;

        if (!this.blockStack.isEmpty()) {
            Block currentBlock = this.blockStack.peek();

            result =
                (this.elementDepth - currentBlock.elementDepth <= 1)
                    && !XDOMXMLConstants.PATTERN_ELEM_PARAMETER.matcher(elementName).matches();
        } else {
            result = true;
        }

        return result;
    }

    private boolean onParameterElement(String elementName)
    {
        return onBlockChild() && XDOMXMLConstants.PATTERN_ELEM_PARAMETER.matcher(elementName).matches();
    }

    private int extractParameterIndex(String elementName)
    {
        Matcher matcher = XDOMXMLConstants.PATTERN_ELEM_PARAMETER.matcher(elementName);
        matcher.find();

        return Integer.valueOf(matcher.group(1));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        Block currentBlock = this.blockStack.isEmpty() ? null : this.blockStack.peek();

        if (onBlockElement(qName)) {
            if (currentBlock != null) {
                // send previous event
                if (currentBlock.listenerElement != null && !currentBlock.beginSent) {
                    currentBlock.fireBeginEvent(this.listener, currentBlock.getParametersTable());
                }
            }

            // push new event
            Block block = getBlock(uri, localName, qName, attributes);

            currentBlock = this.blockStack.push(block);

            if (!block.isContainer() && block.listenerElement.getParameters().size() == 1
                && XDOMXMLCurrentUtils.isSimpleType(block.listenerElement.getParameters().get(0))) {
                this.content = new StringBuilder();
            }

            // Extract simple parameters from attributes
            for (int i = 0; i < attributes.getLength(); ++i) {
                String attributeName = attributes.getQName(i);

                if (XDOMXMLConstants.PATTERN_ELEM_PARAMETER.matcher(attributeName).matches()) {
                    int parameterIndex = extractParameterIndex(attributeName);

                    Type type = block.listenerElement.getParameters().get(parameterIndex);
                    Class< ? > typeClass = ReflectionUtils.getTypeClass(type);

                    if (XDOMXMLCurrentUtils.isSimpleType(typeClass)) {
                        block.setParameter(parameterIndex, this.stringConverter.convert(type, attributes.getValue(i)));
                    } else {
                        block.setParameter(parameterIndex, XDOMXMLCurrentUtils.defaultValue(typeClass));
                    }
                }
            }
        } else {
            if (onParameterElement(qName)) {
                // starting a new block parameter
                if (currentBlock.listenerElement != null) {
                    try {
                        currentBlock.parameterDOMBuilder = new Sax2Dom();
                    } catch (ParserConfigurationException e) {
                        throw new SAXException("Failed to create new Sax2Dom handler", e);
                    }
                    currentBlock.parameterDOMBuilder.startDocument();
                }
            }

            if (currentBlock.parameterDOMBuilder != null) {
                currentBlock.parameterDOMBuilder.startElement(uri, localName, qName, attributes);
            }
        }

        ++this.elementDepth;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        Block currentBlock = this.blockStack.isEmpty() ? null : this.blockStack.peek();

        --this.elementDepth;

        if (onBlockElement(qName)) {
            Block block = this.blockStack.pop();

            if (block.listenerElement != null) {
                // Flush pending begin event and send end event or send on event
                if (block.isContainer()) {
                    if (!block.beginSent) {
                        block.fireBeginEvent(this.listener, block.getParametersTable());
                    }

                    block.fireEndEvent(this.listener, block.getParametersTable());
                } else {
                    if (block.getParametersList().size() == 0
                        && this.listenerDescriptor.getElements().get(qName.toLowerCase()).getParameters().size() == 1) {
                        block.setParameter(
                            0,
                            this.stringConverter.convert(this.listenerDescriptor.getElements().get(qName.toLowerCase())
                                .getParameters().get(0), this.content.toString()));
                        this.content = null;
                    }

                    block.fireOnEvent(this.listener, block.getParametersTable());
                }
            }
        } else if (currentBlock.parameterDOMBuilder != null) {
            currentBlock.parameterDOMBuilder.endElement(uri, localName, qName);

            if (onParameterElement(qName)) {
                if (currentBlock.listenerElement != null) {
                    currentBlock.parameterDOMBuilder.endDocument();

                    ListenerElement listenerElement = currentBlock.listenerElement;
                    Type parameterType = listenerElement.getParameters().get(extractParameterIndex(qName));
                    Element rootElement = currentBlock.parameterDOMBuilder.getRootElement();

                    int parameterIndex = extractParameterIndex(qName);
                    currentBlock.setParameter(parameterIndex,
                        this.parameterManager.unSerialize(parameterType, rootElement));
                }

                currentBlock.parameterDOMBuilder = null;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parameterDOMBuilder != null) {
            this.blockStack.peek().parameterDOMBuilder.characters(ch, start, length);
        } else if (this.content != null) {
            this.content.append(ch, start, length);
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

    private Block getBlock(String uri, String localName, String qName, Attributes attributes)
    {
        String blockName;
        if (XDOMXMLConstants.ELEM_BLOCK.equals(qName)) {
            blockName = attributes.getValue(XDOMXMLConstants.ATT_BLOCK_NAME);
        } else {
            blockName = qName;
        }

        ListenerElement element = this.listenerDescriptor.getElements().get(blockName.toLowerCase());

        return new Block(element, this.elementDepth);
    }
}

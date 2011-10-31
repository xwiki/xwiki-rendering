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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml.internal.parser.parameters.CustomParametersParser;

@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultBlockParser extends AbstractBlockParser
{
    private Set<String> parameterNames;

    private Map<String, String> parameters;

    private Map<String, String> customParameters = Collections.emptyMap();

    private StringBuffer value;

    public DefaultBlockParser(Listener listener, ComponentManager componentManager)
    {
        setListener(listener);
        setComponentManager(componentManager);
    }

    /**
     * Called by the component manager.
     */
    public DefaultBlockParser()
    {

    }

    protected DefaultBlockParser(Set<String> parameterNames)
    {
        if (parameterNames != null) {
            this.parameterNames = parameterNames;
            this.parameters = new HashMap<String, String>();
        }
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public Map<String, String> getCustomParameters()
    {
        return this.customParameters;
    }

    public int getParameterAsInt(String name, int defaultValue)
    {
        String value = getParameters().get(name);

        return value != null ? Integer.valueOf(value) : defaultValue;
    }

    public boolean getParameterAsBoolean(String name, boolean defaultValue)
    {
        String value = getParameters().get(name);

        return value != null ? Boolean.valueOf(value) : defaultValue;
    }

    public char getParameterAsChar(String name, char defaultValue)
    {
        String value = getParameters().get(name);

        return value != null && value.length() > 0 ? value.charAt(0) : defaultValue;
    }

    public String getParameterAsString(String name, String defaultValue)
    {
        String value = getParameters().get(name);

        return value != null ? value : defaultValue;
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (getLevel() > 0) {
            if (this.parameterNames != null && this.parameterNames.contains(qName)) {
                this.value = new StringBuffer();
            } else if (qName.equals(XDOMXMLConstants.ELEM_PARAMETERS)) {
                // Start parsing custom parameters
                setCurrentHandler(new CustomParametersParser());
            }
        }
    }

    @Override
    public void charactersInternal(char[] ch, int start, int length) throws SAXException
    {
        if (this.value != null) {
            this.value.append(ch, start, length);
        }
    }

    @Override
    protected void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        if (getLevel() > 0) {
            if (this.value != null) {
                this.parameters.put(qName, this.value.toString());
            } else if (qName.equals(XDOMXMLConstants.ELEM_PARAMETERS)) {
                // Custom parameters has been parsed
                CustomParametersParser parametersParser = (CustomParametersParser) getCurrentHandler();
                this.customParameters = parametersParser.getParameters();
            }
        }
    }

    @Override
    protected void beginBlock() throws SAXException
    {
        if (getListener() != null) {
            String name = getBlockName().toUpperCase();

            try {
                EventType onEventType = EventType.valueOf("ON_" + name);

                onEventType.fireEvent(getListener(), new Object[] {this.customParameters});
            } catch (IllegalArgumentException e) {
                // It's a container
                try {
                    EventType beginEventType = EventType.valueOf("BEGIN_" + name);

                    beginEventType.fireEvent(getListener(), new Object[] {this.customParameters});
                } catch (IllegalArgumentException e2) {
                    throw new SAXException("Unknow block [" + name + "]", e2);
                }
            }
        }
    }

    @Override
    protected void endBlock() throws SAXException
    {
        if (getListener() != null) {
            String name = getBlockName().toUpperCase();

            try {
                EventType.valueOf("ON_" + name);

                // It's a ON_<EVENT> event. It has already been sent in #beginBlock.
            } catch (IllegalArgumentException e) {
                // It's a container
                try {
                    EventType endEventType = EventType.valueOf("END_" + name);

                    endEventType.fireEvent(getListener(), new Object[] {this.customParameters});
                } catch (IllegalArgumentException e2) {
                    throw new SAXException("Unknow block [" + name + "]", e2);
                }
            }
        }
    }
}

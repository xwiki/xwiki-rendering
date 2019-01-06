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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptor;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptorManager;
import org.xwiki.rendering.listener.descriptor.ListenerElement;
import org.xwiki.rendering.xdomxml10.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml10.internal.parser.parameter.CustomParametersParser;

@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultBlockParser extends AbstractBlockParser
{
    @Inject
    private ListenerDescriptorManager descriptorManager;

    private Set<String> parameterNames;

    private Map<String, String> parameters;

    private StringBuffer value;

    private ListenerDescriptor descriptor;

    private Map<String, String> customParameters = Collections.emptyMap();

    /**
     * Called by Component Manager.
     */
    public DefaultBlockParser()
    {
    }

    protected DefaultBlockParser(Set<String> parameterNames)
    {
        if (parameterNames != null) {
            this.parameterNames = parameterNames;
            this.parameters = new HashMap<>();
        }
    }

    @Override
    public void setListener(Listener listener)
    {
        super.setListener(listener);

        this.descriptor = this.descriptorManager.getListenerDescriptor(listener.getClass());
    }

    public Map<String, String> getParameters()
    {
        return this.parameters;
    }

    public Map<String, String> getCustomParameters()
    {
        return this.customParameters;
    }

    public int getParameterAsInt(String name, int defaultValue)
    {
        String str = getParameters().get(name);

        return str != null ? Integer.valueOf(str) : defaultValue;
    }

    public boolean getParameterAsBoolean(String name, boolean defaultValue)
    {
        String str = getParameters().get(name);

        return str != null ? Boolean.valueOf(str) : defaultValue;
    }

    public char getParameterAsChar(String name, char defaultValue)
    {
        String str = getParameters().get(name);

        return str != null && str.length() > 0 ? str.charAt(0) : defaultValue;
    }

    public String getParameterAsString(String name, String defaultValue)
    {
        String str = getParameters().get(name);

        return str != null ? str : defaultValue;
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
                this.customParameters = parametersParser.getValue();
            }
        }
    }

    private void sendEvent(Method method) throws SAXException
    {
        try {
            if (method.getParameterTypes().length == 0) {
                method.invoke(getListener());
            } else {
                method.invoke(getListener(), this.customParameters);
            }
        } catch (Exception e) {
            throw new SAXException("Failed to send listener event [" + method + "]", e);
        }
    }

    @Override
    protected void beginBlock() throws SAXException
    {
        if (getListener() != null) {
            String name = getBlockName().toUpperCase();

            ListenerElement element = this.descriptor.getElements().get(name.toLowerCase());

            if (element.getBeginMethod() != null) {
                sendEvent(element.getBeginMethod());
            }
        }
    }

    @Override
    protected void endBlock() throws SAXException
    {
        if (getListener() != null) {
            String name = getBlockName().toUpperCase();

            ListenerElement element = descriptor.getElements().get(name.toLowerCase());

            if (element.getOnMethod() != null) {
                sendEvent(element.getOnMethod());
            } else {
                sendEvent(element.getEndMethod());
            }
        }
    }
}

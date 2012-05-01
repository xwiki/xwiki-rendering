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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptor;
import org.xwiki.rendering.listener.descriptor.ListenerElement;
import org.xwiki.rendering.xdomxmlcurrent.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxmlcurrent.internal.XDOMXMLCurrentUtils;
import org.xwiki.rendering.xdomxmlcurrent.internal.parameter.ParameterManager;

/**
 * Current version of the XDOM+XML stream based renderer.
 * 
 * @version $Id$
 */
public class XDOMXMLChainingStreamRenderer implements InvocationHandler
{
    private ContentHandler contentHandler;

    private ParameterManager parameterManager;

    private ListenerDescriptor descriptor;

    public XDOMXMLChainingStreamRenderer(ParameterManager parameterManager, ListenerDescriptor descriptor)
    {
        this.parameterManager = parameterManager;
        this.descriptor = descriptor;
    }

    private boolean isValidBlockElementName(String blockName)
    {
        return XDOMXMLConstants.VALID_ELEMENTNAME.matcher(blockName).matches()
            && !XDOMXMLConstants.ELEM_PARAMETER.equals(blockName);
    }

    private String getBlockName(String eventName, String prefix)
    {
        String blockName = eventName.substring(prefix.length());
        blockName = Character.toLowerCase(blockName.charAt(0)) + blockName.substring(1);

        return blockName;
    }

    private void beginEvent(String eventName, Object[] parameters)
    {
        String blockName = getBlockName(eventName, "begin");

        if (isValidBlockElementName(blockName)) {
            startElement(blockName);
        } else {
            startElement(XDOMXMLConstants.ELEM_BLOCK, new String[][] {{XDOMXMLConstants.ATT_BLOCK_NAME, blockName}});
        }

        printParameters(parameters, this.descriptor.getElements().get(blockName.toLowerCase()));
    }

    private void endEvent(String eventName)
    {
        String blockName = getBlockName(eventName, "end");

        if (isValidBlockElementName(blockName)) {
            endElement(blockName);
        } else {
            endElement(XDOMXMLConstants.ELEM_BLOCK);
        }
    }

    private void onEvent(String eventName, Object[] parameters)
    {
        String blockName = getBlockName(eventName, "on");

        String elementName;
        AttributesImpl attributes = new AttributesImpl();
        if (isValidBlockElementName(blockName)) {
            elementName = blockName;
        } else {
            elementName = XDOMXMLConstants.ELEM_BLOCK;
            attributes.addAttribute(null, null, XDOMXMLConstants.ATT_BLOCK_NAME, null, blockName);
        }

        startElement(elementName, attributes);

        ListenerElement descriptor = this.descriptor.getElements().get(blockName.toLowerCase());
        if (parameters != null && parameters.length == 1
            && XDOMXMLCurrentUtils.isSimpleType(descriptor.getParameters().get(0))) {
            String value = parameters[0].toString();
            try {
                this.contentHandler.characters(value.toCharArray(), 0, value.length());
            } catch (SAXException e) {
                throw new RuntimeException("Failed to send sax event", e);
            }
        } else {
            printParameters(parameters, descriptor);
        }

        endElement(elementName);
    }

    private boolean printParameter(Object value, int index, ListenerElement descriptor)
    {
        boolean print = true;

        Type type = descriptor.getParameters().get(index);

        if (type instanceof Class) {
            Class< ? > typeClass = (Class< ? >) type;
            try {
                if (typeClass.isPrimitive()) {
                    print = !XDOMXMLCurrentUtils.defaultValue(typeClass).equals(value);
                }
            } catch (Exception e) {
                // Should never happen
            }
        }

        return print;
    }

    private void printParameters(Object[] parameters, ListenerElement descriptor)
    {
        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; ++i) {
                Object value = parameters[i];

                if (value != null && printParameter(value, i, descriptor)) {
                    startElement(XDOMXMLConstants.ELEM_PARAMETER + i);

                    this.parameterManager.serialize(descriptor.getParameters().get(i), parameters[i],
                        this.contentHandler);

                    endElement(XDOMXMLConstants.ELEM_PARAMETER + i);
                }
            }
        }
    }

    private void startElement(String elemntName)
    {
        startElement(elemntName, (String[][]) null);
    }

    private void startElement(String elemntName, String[][] parameters)
    {
        startElement(elemntName, createAttributes(parameters));
    }

    private void startElement(String elemntName, Attributes attributes)
    {
        try {
            this.contentHandler.startElement("", elemntName, elemntName, attributes);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }

    private void endElement(String elemntName)
    {
        try {
            this.contentHandler.endElement("", elemntName, elemntName);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }

    /**
     * Convert provided table into {@link Attributes} to use in xml writer.
     */
    private Attributes createAttributes(String[][] parameters)
    {
        AttributesImpl attributes = new AttributesImpl();

        if (parameters != null && parameters.length > 0) {
            for (String[] entry : parameters) {
                attributes.addAttribute(null, null, entry[0], null, entry[1]);
            }
        }

        return attributes;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object result = null;

        if (method.getName().equals("setContentHandler")) {
            this.contentHandler = (ContentHandler) args[0];
        } else if (method.getName().equals("getContentHandler")) {
            result = this.contentHandler;
        } else if (method.getName().startsWith("begin")) {
            beginEvent(method.getName(), args);
        } else if (method.getName().startsWith("end")) {
            endEvent(method.getName());
        } else if (method.getName().startsWith("on")) {
            onEvent(method.getName(), args);
        } else {
            throw new NoSuchMethodException(method.toGenericString());
        }

        return result;
    }
}

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
package org.xwiki.rendering.xdomxml.internal.current.renderer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.rendering.listener.ListenerDescriptor;
import org.xwiki.rendering.listener.ListenerElement;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml.internal.current.parameter.ParameterManager;

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

    private String getBlockName(String eventName, String prefix)
    {
        String blockName = eventName.substring(prefix.length());
        blockName = Character.toLowerCase(blockName.charAt(0)) + blockName.substring(1);

        return blockName;
    }

    private void beginEvent(String eventName, Object[] parameters)
    {
        String blockName = getBlockName(eventName, "begin");
        
        startElement(XDOMXMLConstants.ELEM_BLOCK,
            new String[][] {{XDOMXMLConstants.ATT_BLOCK_NAME, blockName}});

        printParameters(parameters, this.descriptor.getElements().get(blockName));
    }

    private void endEvent()
    {
        endElement(XDOMXMLConstants.ELEM_BLOCK);
    }

    private void onEvent(String eventName, Object[] parameters)
    {
        String blockName = getBlockName(eventName, "on");

        if (parameters.length > 0) {
            startElement(XDOMXMLConstants.ELEM_BLOCK, new String[][] {{XDOMXMLConstants.ATT_BLOCK_NAME, blockName}});
            printParameters(parameters, this.descriptor.getElements().get(blockName));
            endEvent();
        } else {
            emptyElement(XDOMXMLConstants.ELEM_BLOCK, new String[][] {{XDOMXMLConstants.ATT_BLOCK_NAME, blockName}});
        }
    }

    private void printParameters(Object[] parameters, ListenerElement descriptor)
    {
        if (parameters.length > 0) {
            startElement(XDOMXMLConstants.ELEM_PARAMETERS, null);
            for (int i = 0; i< parameters.length; ++i) {
                Object parameter = parameters[i];
 
                Class<?> eventType = descriptor.getParameters().get(i);
                if (eventType == Map.class) {
                    parameter = new LinkedHashMap((Map)parameter);
                }

                this.parameterManager.serialize(parameter, this.contentHandler);
            }
            endElement(XDOMXMLConstants.ELEM_PARAMETERS);
        }
    }

    private void emptyElement(String elemntName, String[][] parameters)
    {
        startElement(elemntName, parameters);
        endElement(elemntName);
    }

    private void startElement(String elemntName, String[][] parameters)
    {
        try {
            this.contentHandler.startElement("", elemntName, elemntName, createAttributes(parameters));
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
            endEvent();
        } else if (method.getName().startsWith("on")) {
            onEvent(method.getName(), args);
        } else {
            throw new NoSuchMethodException(method.toGenericString());
        }

        return result;
    }
}

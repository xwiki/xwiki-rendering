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
package org.xwiki.rendering.xml.internal.parser;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.ConverterManager;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptorManager;
import org.xwiki.rendering.xml.internal.XMLConfiguration;
import org.xwiki.rendering.xml.internal.parameter.ParameterManager;

/**
 * Default implementation of {@link XMLParserFactory}.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class DefaultXMLParserFactory implements XMLParserFactory
{
    /**
     * The parameter converter.
     */
    @Inject
    private ParameterManager parameterManager;

    /**
     * The events supported by the listener.
     */
    @Inject
    private ListenerDescriptorManager descriptorManager;

    /**
     * Used to convert simple types.
     */
    @Inject
    private ConverterManager converter;

    @Override
    public XMLParser createContentHandler(Object listener, XMLConfiguration configuration)
    {
        DefaultXMLParser handler =
            new DefaultXMLParser(listener, this.descriptorManager.getListenerDescriptor(listener.getClass()),
                this.converter, this.parameterManager, configuration);

        return handler;
    }
}

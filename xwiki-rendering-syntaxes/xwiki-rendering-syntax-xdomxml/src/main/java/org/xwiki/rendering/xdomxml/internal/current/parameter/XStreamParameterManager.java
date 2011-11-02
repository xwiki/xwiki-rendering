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
package org.xwiki.rendering.xdomxml.internal.current.parameter;

import java.lang.reflect.Type;

import org.dom4j.Element;
import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.core.MapBackedDataHolder;
import com.thoughtworks.xstream.io.xml.Dom4JReader;
import com.thoughtworks.xstream.io.xml.SaxWriter;

/**
 * XStream based implementation of {@link ParameterManager}.
 * 
 * @version $Id$
 */
@Component
public class XStreamParameterManager implements ParameterManager, Initializable
{
    private XStream xstream;

    @Override
    public void initialize() throws InitializationException
    {
        this.xstream = new XStream();

        this.xstream.setMarshallingStrategy(new XDOMXMLTreeMarshallingStrategy());
        
        this.xstream.registerConverter(new XDOMXMLCollectionConverter(this.xstream.getMapper()));
        this.xstream.registerConverter(new XDOMXMLMapConverter(this.xstream.getMapper()));
    }

    @Override
    public void serialize(Type type, Object object, ContentHandler xmlContent)
    {
        SaxWriter saxWriter = new SaxWriter(false);
        saxWriter.setContentHandler(xmlContent);

        DataHolder dataHolder = new MapBackedDataHolder();

        dataHolder.put("type", type);

        this.xstream.marshal(object, saxWriter, dataHolder);
    }

    @Override
    public Object unSerialize(Type type, Element rootElement)
    {
        DataHolder dataHolder = new MapBackedDataHolder();

        dataHolder.put("type", type);

        return this.xstream.unmarshal(new Dom4JReader(rootElement), null, dataHolder);
    }
}

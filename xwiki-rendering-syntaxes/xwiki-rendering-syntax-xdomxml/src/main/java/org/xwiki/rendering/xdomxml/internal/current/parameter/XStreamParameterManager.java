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

import org.dom4j.Element;
import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JReader;
import com.thoughtworks.xstream.io.xml.SaxWriter;

@Component
public class XStreamParameterManager implements ParameterManager, Initializable
{
    private XStream xstream;

    @Override
    public void initialize() throws InitializationException
    {
        this.xstream = new XStream();

        this.xstream.alias("resource-reference", ResourceReference.class);
        this.xstream.alias("meta-data", MetaData.class);
    }

    @Override
    public void serialize(Object object, ContentHandler xmlContent)
    {
        SaxWriter saxWriter = new SaxWriter(false);
        saxWriter.setContentHandler(xmlContent);

        this.xstream.marshal(object, saxWriter);
    }

    @Override
    public Object unSerialize(Element rootElement)
    {
        return this.xstream.unmarshal(new Dom4JReader(rootElement));
    }
}

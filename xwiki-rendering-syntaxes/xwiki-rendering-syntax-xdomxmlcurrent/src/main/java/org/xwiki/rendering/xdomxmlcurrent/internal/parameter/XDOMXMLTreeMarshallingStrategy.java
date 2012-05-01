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
package org.xwiki.rendering.xdomxmlcurrent.internal.parameter;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.TreeMarshallingStrategy;
import com.thoughtworks.xstream.core.TreeUnmarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class XDOMXMLTreeMarshallingStrategy extends TreeMarshallingStrategy
{
    @Override
    protected XDOMXMLTreeMarshaller createMarshallingContext(HierarchicalStreamWriter writer,
        ConverterLookup converterLookup, Mapper mapper)
    {
        return new XDOMXMLTreeMarshaller(writer, converterLookup, mapper);
    }

    @Override
    protected TreeUnmarshaller createUnmarshallingContext(Object root, HierarchicalStreamReader reader,
        ConverterLookup converterLookup, Mapper mapper)
    {
        return new XDOMXMLTreeUnmarshaller(root, reader, converterLookup, mapper);
    }
}

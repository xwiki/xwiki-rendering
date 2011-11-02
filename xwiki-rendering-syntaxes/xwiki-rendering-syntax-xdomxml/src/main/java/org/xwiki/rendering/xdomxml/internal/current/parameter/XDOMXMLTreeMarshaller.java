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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class XDOMXMLTreeMarshaller extends TreeMarshaller
{
    public XDOMXMLTreeMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper)
    {
        super(writer, converterLookup, mapper);
    }

    @Override
    public void start(Object item, DataHolder dataHolder)
    {
        Type type = (Type) dataHolder.get("type");
        if (item != null) {
            Converter converter = getConverter(type);

            if (converter == null) {
                convertAnother(item);
            } else {
                convert(item, converter);
            }
        }
    }

    private Converter getConverter(Type type)
    {
        if (type != null) {
            Class< ? > clazz = null;
            if (type instanceof ParameterizedType) {
                clazz = (Class< ? >) ((ParameterizedType) type).getRawType();
            } else if (type instanceof Class) {
                clazz = (Class< ? >) type;
            } else {
                throw new ConversionException("Can't find any converter for the type [" + type + "]");
            }

            Converter converter = this.converterLookup.lookupConverterForType(clazz);

            if (converter.getClass() != ReflectionConverter.class) {
                return converter;
            }
        }

        return null;
    }
}

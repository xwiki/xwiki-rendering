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
package org.xwiki.rendering.internal.listener;

import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.listener.MetaData;

/**
 * Converter that converts a value into an {@link MetaData} object.
 *
 * @version $Id$
 * @since 7.0M2
 */
// TODO: add real syntax support (only convert from empty String to empty Map right now)
@Component
@Singleton
public class MetaDataConverter extends AbstractConverter<MetaData>
{
    private static final String UNSUPPORTED_EXCEPTION = "Conversion from [" + MetaData.class.getName() + "] to ["
        + String.class.getName() + "] is not supported";

    @Override
    protected MetaData convertToType(Type type, Object value)
    {
        if (value == null) {
            return null;
        }

        MetaData parameters;

        if (value instanceof MetaData) {
            parameters = (MetaData) value;
        } else if (value instanceof Map) {
            parameters = new MetaData((Map) value);
        } else {
            String parametersString = value.toString().trim();

            if (StringUtils.isEmpty(parametersString)) {
                parameters = MetaData.EMPTY;
            } else {
                throw new ConversionException(UNSUPPORTED_EXCEPTION);
            }
        }

        return parameters;
    }

    @Override
    protected String convertToString(MetaData value)
    {
        throw new ConversionException(UNSUPPORTED_EXCEPTION);
    }
}

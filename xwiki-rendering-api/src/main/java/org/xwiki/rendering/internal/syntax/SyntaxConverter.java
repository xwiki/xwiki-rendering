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
package org.xwiki.rendering.internal.syntax;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Convert a Syntax from a String to a Syntax object and the other way around.
 *
 * @version $Id$
 * @since 4.2M3
 */
@Component
@Singleton
public class SyntaxConverter extends AbstractConverter<Syntax>
{
    @Override
    protected Syntax convertToType(Type targetType, Object value)
    {
        try {
            return value == null || value.toString().isEmpty() ? null
                : Syntax.valueOf(value.toString());
        } catch (ParseException e) {
            // The specified syntax is not recognized, return an error
            throw new ConversionException(String.format("Unknown syntax [%s]", value.toString()), e);
        }
    }

    @Override
    protected String convertToString(Syntax value)
    {
        return value == null ? null : value.toIdString();
    }
}

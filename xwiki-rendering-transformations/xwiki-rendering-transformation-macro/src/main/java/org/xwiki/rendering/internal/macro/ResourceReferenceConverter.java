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
package org.xwiki.rendering.internal.macro;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.parser.ResourceReferenceParser;

/**
 * XWiki Properties Bean Converter to convert Strings into
 * {@link org.xwiki.rendering.listener.reference.ResourceReference}.
 *
 * @version $Id$
 * @since 2.6M1
 * @see org.xwiki.properties.converter.Converter
 */
@Component
@Named("org.xwiki.rendering.listener.reference.ResourceReference")
@Singleton
public class ResourceReferenceConverter extends AbstractConverter
{
    /**
     * Used to convert Resource References from String to ResourceReference object.
     */
    @Inject
    private ResourceReferenceParser referenceParser;

    @Override
    protected <T> T convertToType(Class<T> type, Object value)
    {
        T reference = null;
        if (value != null) {
            reference = type.cast(this.referenceParser.parse(value.toString()));
        }

        return reference;
    }

    @Override
    protected String convertToString(Object value)
    {
        throw new ConversionException("not implemented yet");
    }
}

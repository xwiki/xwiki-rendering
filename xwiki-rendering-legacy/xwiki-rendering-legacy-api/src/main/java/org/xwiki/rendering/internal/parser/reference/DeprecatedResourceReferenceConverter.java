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
package org.xwiki.rendering.internal.parser.reference;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.Converter;
import org.xwiki.rendering.listener.reference.ResourceReference;

/**
 * XWiki Properties Bean Converter to convert Strings into
 * {@link org.xwiki.rendering.listener.reference.ResourceReference}.
 * 
 * @version $Id$
 * @since 2.6M1
 * @see org.xwiki.properties.converter.Converter
 * @deprecated since 5.2M1 use {@link ResourceReferenceConverter} instead
 */
@Component
@Named("org.xwiki.rendering.listener.reference.ResourceReference")
@Singleton
@Deprecated
public class DeprecatedResourceReferenceConverter implements Converter
{
    /**
     * The actual converter.
     */
    @Inject
    private Converter<ResourceReference> converter;

    @Override
    public Object convert(Type targetType, Object sourceValue)
    {
        return this.converter.convert(targetType, sourceValue);
    }
}

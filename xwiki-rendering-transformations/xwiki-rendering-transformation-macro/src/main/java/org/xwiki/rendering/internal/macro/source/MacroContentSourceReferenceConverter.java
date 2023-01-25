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
package org.xwiki.rendering.internal.macro.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;

/**
 * Convert various types (especially String) to {@link MacroContentSourceReference}.
 * 
 * @version $Id$
 * @since 15.0RC1
 * @since 14.10.2
 */
@Component
@Singleton
public class MacroContentSourceReferenceConverter extends AbstractConverter<MacroContentSourceReference>
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Override
    protected MacroContentSourceReference convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        try {
            return convertToType(value);
        } catch (IOException e) {
            throw new ConversionException("Failed to convert to MacroWikiSourceReference", e);
        }
    }

    private MacroContentSourceReference convertToType(Object value) throws IOException
    {
        MacroContentSourceReference reference;

        if (value instanceof String) {
            reference = fromString((String) value);
        } else if (value instanceof InputStream) {
            reference = new MacroContentSourceReference(MacroContentSourceReference.TYPE_STRING,
                IOUtils.toString((InputStream) value, StandardCharsets.UTF_8));
        } else if (value instanceof byte[]) {
            reference = new MacroContentSourceReference(MacroContentSourceReference.TYPE_STRING,
                new String((byte[]) value, StandardCharsets.UTF_8));
        } else if (value instanceof Reader) {
            reference = new MacroContentSourceReference(MacroContentSourceReference.TYPE_STRING,
                IOUtils.toString((Reader) value));
        } else if (value instanceof URL) {
            reference =
                new MacroContentSourceReference(MacroContentSourceReference.TYPE_URL, ((URL) value).toExternalForm());
        } else if (value instanceof File) {
            reference = new MacroContentSourceReference(MacroContentSourceReference.TYPE_FILE,
                ((File) value).getAbsolutePath());
        } else {
            reference = fromUnknownType(value);
        }

        return reference;
    }

    private MacroContentSourceReference fromUnknownType(Object value)
    {
        ParameterizedType componentRole = TypeUtils.parameterize(
            org.xwiki.rendering.macro.source.MacroContentSourceReferenceConverter.class, value.getClass());

        ComponentManager componentManager = this.contextComponentManagerProvider.get();

        if (componentManager.hasComponent(componentRole)) {
            try {
                org.xwiki.rendering.macro.source.MacroContentSourceReferenceConverter converter =
                    componentManager.getInstance(componentRole);

                return converter.convert(value);
            } catch (ComponentLookupException e) {
                throw new ConversionException(
                    "Failed to get the code macro source reference converter component for type [" + value.getClass()
                        + "]",
                    e);
            }
        }

        // Fallback on the String logic
        return fromString(value.toString());
    }

    private MacroContentSourceReference fromString(String source)
    {
        int index = source.indexOf(':');

        return new MacroContentSourceReference(
            index <= 0 ? MacroContentSourceReference.TYPE_STRING : source.substring(0, index),
            index < source.length() ? source.substring(index + 1) : "");
    }
}

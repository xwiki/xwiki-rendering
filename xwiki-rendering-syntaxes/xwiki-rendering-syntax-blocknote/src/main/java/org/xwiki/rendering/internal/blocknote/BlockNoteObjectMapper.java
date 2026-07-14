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
package org.xwiki.rendering.internal.blocknote;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.xwiki.rendering.listener.reference.ResourceType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Provides the Jackson {@link ObjectMapper} used to (de)serialize the BlockNote JSON, on both the rendering and the
 * parsing side.
 * <p>
 * It configures a custom mapping for {@link ResourceType} so that it is (de)serialized as a plain string (its scheme)
 * rather than as a nested {@code {"scheme": ...}} object. This way the serialized resource reference matches the shape
 * expected by the client-side editor, where the reference type is a plain string.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public final class BlockNoteObjectMapper
{
    private BlockNoteObjectMapper()
    {
        // Utility class with only static methods.
    }

    /**
     * @return a new Jackson object mapper configured for the BlockNote JSON format
     */
    public static ObjectMapper create()
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ResourceType.class, new ResourceTypeSerializer());
        module.addDeserializer(ResourceType.class, new ResourceTypeDeserializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private static final class ResourceTypeSerializer extends JsonSerializer<@NonNull ResourceType>
    {
        @Override
        public void serialize(@NonNull ResourceType value, JsonGenerator generator, SerializerProvider serializers)
            throws IOException
        {
            generator.writeString(value.getScheme());
        }
    }

    private static final class ResourceTypeDeserializer extends JsonDeserializer<@NonNull ResourceType>
    {
        @Override
        public @NonNull ResourceType deserialize(JsonParser parser, DeserializationContext context) throws IOException
        {
            return new ResourceType(parser.getValueAsString());
        }
    }
}

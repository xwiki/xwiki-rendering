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
package org.xwiki.rendering.internal.parser.uniast;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Used to obtain a {@link ResourceReference} from a UniAst {@code LinkTarget} node.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
@Component(roles = UniAstResourceReferenceParser.class)
@Singleton
public class UniAstResourceReferenceParser
{
    @Inject
    private ResourceReferenceParser resourceReferenceParser;

    /**
     * Extract a resource reference from a UniAst LinkTarget node.
     *
     * @param linkTarget the UniAst LinkTarget node to extract the resource reference from
     * @return the parsed resource reference
     * @throws ParseException if the LinkTarget node is malformed or if the reference can't be parsed
     */
    public ResourceReference parse(JsonNode linkTarget) throws ParseException
    {
        JsonNode type = linkTarget.path(UniAstStreamParser.TYPE);
        if (!type.isTextual()) {
            throw new ParseException("The 'type' property of the LinkTarget is missing or its value is unexpected.");
        }
        String reference;
        ResourceType defaultType = ResourceType.URL;
        switch (type.asText()) {
            case "internal":
                reference = linkTarget.path("rawReference").asText("");
                defaultType = ResourceType.DOCUMENT;
                break;
            case "external":
                reference = linkTarget.path("url").asText("");
                break;
            default:
                throw new ParseException("Unsupported link target type: " + type.asText());
        }
        ResourceReference resourceReference = this.resourceReferenceParser.parse(reference);
        if (resourceReference.getType() == ResourceType.UNKNOWN) {
            resourceReference.setType(defaultType);
            resourceReference.setTyped(false);
        }
        return resourceReference;
    }
}

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

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;

/**
 * Handles the case where the resource type wasn't specified. In this case it tries to guess the type by first looking
 * for a URL and then considering it's a reference to an attachment.
 *
 * @version $Id$
 * @since 5.1M1
 */
public abstract class AbstractImageReferenceParser implements ResourceReferenceParser
{
    /**
     * Used to parse untyped resource reference and guess their types.
     */
    @Inject
    @Named("image/untyped")
    private ResourceReferenceParser untypedImageReferenceParser;

    /**
     * @return the default resource reference parser to use to parse the passed reference (if this parser doesn't
     *         understand the passed reference then we default to using a parser that will try to guess the reference
     *         type and that will use some defaults)
     */
    protected abstract ResourceReferenceParser getDefaultResourceReferenceParser();

    @Override
    public ResourceReference parse(String rawReference)
    {
        ResourceReference reference = getDefaultResourceReferenceParser().parse(rawReference);
        if (reference.getType().equals(ResourceType.UNKNOWN)) {
            reference = this.untypedImageReferenceParser.parse(rawReference);
        }
        return reference;
    }
}

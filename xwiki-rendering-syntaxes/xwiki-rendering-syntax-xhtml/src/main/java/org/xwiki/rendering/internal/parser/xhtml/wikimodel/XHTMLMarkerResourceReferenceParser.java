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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;

/**
 * Parse a resource reference specified using the syntax used by the XHTML Annotated Renderer to represent resources as
 * XHTML comments, using the syntax {@code (isTyped)|-|(type)|-|(reference)|-|(parameters: key="value")}.
 *
 * @version $Id$
 * @since 2.5RC1
 */
@Component
@Named("xhtmlmarker")
@Singleton
public class XHTMLMarkerResourceReferenceParser implements ResourceReferenceParser
{
    /**
     * Character to separate data in XHTML comments.
     */
    private static final String COMMENT_SEPARATOR = "|-|";

    @Inject
    private Logger logger;

    @Override
    public ResourceReference parse(String rawReference)
    {
        String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(rawReference, COMMENT_SEPARATOR);
        boolean isTyped;
        ResourceType type;
        String reference;
        if (tokens.length < 3) {
            this.logger.warn("Comment resource [{}] is missing resource type, falling back to document.", rawReference);
            isTyped = false;
            type = ResourceType.DOCUMENT;
            reference = rawReference;
        } else {
            isTyped = "true".equalsIgnoreCase(tokens[0]);
            type = new ResourceType(tokens[1]);
            reference = tokens[2];
        }

        ResourceReference resourceReference = new ResourceReference(reference, type);
        resourceReference.setTyped(isTyped);

        if (tokens.length == 4) {
            for (WikiParameter parameter : WikiParameters.newWikiParameters(tokens[3])) {
                resourceReference.setParameter(parameter.getKey(), parameter.getValue());
            }
        }

        return resourceReference;
    }
}

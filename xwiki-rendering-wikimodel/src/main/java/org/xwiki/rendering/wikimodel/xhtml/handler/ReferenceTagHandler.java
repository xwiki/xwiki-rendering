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
package org.xwiki.rendering.wikimodel.xhtml.handler;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler.TagStack.TagContext;

/**
 * Handles references.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class ReferenceTagHandler extends TagHandler
{
    public ReferenceTagHandler()
    {
        super(false, false, true);
    }

    @Override
    protected void begin(TagContext context)
    {
        setAccumulateContent(true);
    }

    @Override
    protected void end(TagContext context)
    {
        WikiParameters parameters = context.getParams();

        // TODO: it should be replaced by a normal parameters
        WikiParameter ref = parameters.getParameter("href");

        if (ref != null) {
            // Check if there's a class attribute with a
            // "wikimodel-freestanding" value.
            // If so it means we have a free standing link.
            if (isFreeStandingReference(context)) {
                context.getScannerContext().onReference(ref.getValue());
            } else {
                String content = context.getContent();

                WikiReference reference = new WikiReference(
                    ref.getValue(),
                    content,
                    removeMeaningfulParameters(parameters));
                context.getScannerContext().onReference(reference);
            }
        }
    }

    protected boolean isFreeStandingReference(TagContext context)
    {
        WikiParameters parameters = context.getParams();

        // Check if there's a class attribute with a "wikimodel-freestanding"
        // value.
        // If so it means we have a free standing link.
        WikiParameter classParam = context.getParams().getParameter("class");

        boolean isFreeStanding = ((classParam != null) && classParam.getValue().equalsIgnoreCase(
            "wikimodel-freestanding"));

        if (isFreeStanding) {
            parameters = removeMeaningfulParameters(parameters);

            return parameters.getSize() == 0;
        } else {
            return false;
        }
    }

    protected WikiParameters removeFreestanding(WikiParameters parameters)
    {
        WikiParameter classParam = parameters.getParameter("class");
        boolean isFreeStanding = ((classParam != null) && classParam.getValue().equalsIgnoreCase(
            "wikimodel-freestanding"));
        if (isFreeStanding) {
            parameters = parameters.remove("class");
        }

        return parameters;
    }

    protected WikiParameters removeMeaningfulParameters(WikiParameters parameters)
    {
        return removeFreestanding(parameters).remove("href");
    }
}

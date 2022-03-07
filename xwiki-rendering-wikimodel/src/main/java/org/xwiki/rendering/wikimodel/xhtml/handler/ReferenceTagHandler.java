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

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;
import org.xwiki.stability.Unstable;

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
        super(true);
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
        boolean isFreeStanding = containsFreeStandingClass(parameters);

        if (isFreeStanding) {
            return removeMeaningfulParameters(parameters).getSize() == 0;
        } else {
            return false;
        }
    }

    /**
     * Checks if the parameters contain the free standing class.
     *
     * @param parameters the parameters to check
     * @return if the class parameter contains the class "wikimodel-freestanding"
     * @since 14.2RC1
     */
    @Unstable
    protected boolean containsFreeStandingClass(WikiParameters parameters)
    {
        return containsClass(parameters, "wikimodel-freestanding");
    }

    /**
     * Checks if the parameters contain a class name.
     *
     * @param parameters the parameters to test
     * @param className the class name to look for
     * @return true if the parameters contain the given class name, false otherwise
     * @since 14.2RC1
     */
    @Unstable
    protected boolean containsClass(WikiParameters parameters, String className)
    {
        WikiParameter classParam = parameters.getParameter("class");

        return (classParam != null) && classParam.getValue() != null && Arrays.stream(classParam.getValue().split(" "))
            .anyMatch(className::equalsIgnoreCase);
    }

    /**
     * Removes a class name from wiki parameters.
     *
     * @param parameters the parameters to modify
     * @param className the class name to remove
     * @return the parameters with the given class name removed
     * @since 14.2RC1
     */
    @Unstable
    protected WikiParameters removeClass(WikiParameters parameters, String className)
    {
        WikiParameter classParam = parameters.getParameter("class");
        WikiParameters result = parameters;

        if (classParam != null) {
            String classes = classParam.getValue();

            if (StringUtils.isNotBlank(classes)) {
                classes = Arrays.stream(classes.split(" "))
                    .filter(Predicate.not(value -> StringUtils.equalsIgnoreCase(className, value)))
                    .collect(Collectors.joining(" "));
            }

            if (StringUtils.isBlank(classes)) {
                result = parameters.remove("class");
            } else {
                result = parameters.setParameter("class", classes);
            }
        }

        return result;
    }

    protected WikiParameters removeFreestanding(WikiParameters parameters)
    {
        return removeClass(parameters, "wikimodel-freestanding");
    }

    protected WikiParameters removeMeaningfulParameters(WikiParameters parameters)
    {
        return removeFreestanding(parameters).remove("href");
    }
}

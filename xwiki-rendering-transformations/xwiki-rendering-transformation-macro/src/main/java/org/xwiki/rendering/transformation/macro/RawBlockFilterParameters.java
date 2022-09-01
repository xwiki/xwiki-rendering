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
package org.xwiki.rendering.transformation.macro;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

/**
 * Parameters for the {@link RawBlockFilter}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
@Unstable
public class RawBlockFilterParameters
{
    /**
     * If the filtering shall use a safe, restricted mode.
     */
    private boolean restricted;

    /**
     * If cleaning of the input should be performed.
     */
    private boolean clean;

    /**
     * The macro transformation context.
     */
    private final MacroTransformationContext macroTransformationContext;

    /**
     * Default constructor.
     *
     * @param macroTransformationContext the macro transformation context that should be used.
     */
    public RawBlockFilterParameters(MacroTransformationContext macroTransformationContext)
    {
        this.macroTransformationContext = macroTransformationContext;
    }

    /**
     * @return if the filtering shall use a safe, restricted mode
     */
    public boolean isRestricted()
    {
        return this.restricted;
    }

    /**
     * @param restricted if the filtering shall use a safe, restricted mode
     */
    public void setRestricted(boolean restricted)
    {
        this.restricted = restricted;
    }

    /**
     * @return if cleaning of the input should be performed
     */
    public boolean isClean()
    {
        return this.clean;
    }

    /**
     * @param clean if cleaning of the input should be performed
     */
    public void setClean(boolean clean)
    {
        this.clean = clean;
    }

    /**
     * @return the macro transformation context in which the filter is applied
     */
    public MacroTransformationContext getMacroTransformationContext()
    {
        return this.macroTransformationContext;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RawBlockFilterParameters that = (RawBlockFilterParameters) o;

        return new EqualsBuilder().append(isRestricted(), that.isRestricted())
            .append(isClean(), that.isClean())
            .append(getMacroTransformationContext(), that.getMacroTransformationContext())
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37).append(isRestricted()).append(isClean())
            .append(getMacroTransformationContext())
            .toHashCode();
    }
}

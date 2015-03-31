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
package org.xwiki.rendering.block;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.listener.Listener;

/**
 * A Verbatim block.
 *
 * @version $Id$
 * @since 1.8M2
 */
public class VerbatimBlock extends AbstractBlock
{
    /**
     * The string to protect from rendering.
     */
    private String protectedString;

    /**
     * If true the macro is located in a inline content (like paragraph, etc.).
     */
    private boolean inline;

    /**
     * @param protectedString the string to protect from rendering.
     * @param isInline if true the macro is located in a inline content (like paragraph, etc.).
     */
    public VerbatimBlock(String protectedString, boolean isInline)
    {
        this.protectedString = protectedString;
        this.inline = isInline;
    }

    /**
     * @param protectedString the string to protect from rendering.
     * @param parameters the custom parameters
     * @param isInline if true the macro is located in a inline content (like paragraph, etc.).
     */
    public VerbatimBlock(String protectedString, Map<String, String> parameters, boolean isInline)
    {
        super(parameters);

        this.protectedString = protectedString;
        this.inline = isInline;
    }

    /**
     * @return the string to protect from rendering
     */
    public String getProtectedString()
    {
        return this.protectedString;
    }

    /**
     * @return if true the macro is located in a inline content (like paragraph, etc.).
     */
    public boolean isInline()
    {
        return this.inline;
    }

    @Override
    public void traverse(Listener listener)
    {
        listener.onVerbatim(getProtectedString(), isInline(), getParameters());
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.8RC2
     */
    @Override
    public String toString()
    {
        return getProtectedString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof VerbatimBlock && super.equals(obj)) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(isInline(), ((VerbatimBlock) obj).isInline());
            builder.append(getProtectedString(), ((VerbatimBlock) obj).getProtectedString());

            return builder.isEquals();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(isInline());
        builder.append(getProtectedString());

        return builder.toHashCode();
    }
}

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

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.stability.Unstable;

/**
 * Represents an image.
 *
 * @version $Id$
 * @since 1.7M2
 */
public class ImageBlock extends AbstractBlock
{
    /**
     * A reference to the image target. See {@link org.xwiki.rendering.listener.reference.ResourceReference} for more
     * details.
     */
    private ResourceReference reference;

    /**
     * If true then the image is defined as a free standing URI directly in the text.
     */
    private boolean freestanding;

    /**
     * The (automatically generated) id. Optional.
     *
     * @since 14.2RC1
     */
    private String id;

    /**
     * @param reference the image reference
     * @param freestanding indicate if the image syntax is simple a full descriptive syntax (detail depending of the
     *            syntax)
     * @since 2.5RC1
     */
    public ImageBlock(ResourceReference reference, boolean freestanding)
    {
        this(reference, freestanding, null, Collections.emptyMap());
    }

    /**
     * @param reference the image reference
     * @param freestanding indicate if the image syntax is simple a full descriptive syntax (detail depending of the
     *            syntax)
     * @param parameters the custom parameters
     * @since 2.5RC1
     */
    public ImageBlock(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this(reference, freestanding, null, parameters);
    }

    /**
     * @param reference the image reference
     * @param freestanding indicate if the image syntax is simple a full descriptive syntax (detail depending of the
     *            syntax)
     * @param id the (automatically generated) id of the image
     * @param parameters the custom parameters
     * @since 14.2RC1
     */
    @Unstable
    public ImageBlock(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        super(parameters);

        this.reference = reference;
        this.freestanding = freestanding;
        this.id = id;
    }

    /**
     * @return the reference to the image
     * @see org.xwiki.rendering.listener.reference.ResourceReference
     * @since 2.5RC1
     */
    public ResourceReference getReference()
    {
        return this.reference;
    }

    /**
     * @return true if the image is defined as a free standing URI directly in the text, false otherwise
     */
    public boolean isFreeStandingURI()
    {
        return this.freestanding;
    }

    /**
     * @param id the id of the image to set
     * @since 14.2RC1
     */
    @Unstable
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the id of the image
     * @since 14.2RC1
     */
    @Unstable
    public String getId()
    {
        return this.id;
    }

    @Override
    public void traverse(Listener listener)
    {
        String idParameter = getId();
        if (idParameter == null) {
            listener.onImage(getReference(), isFreeStandingURI(), getParameters());
        } else {
            listener.onImage(getReference(), isFreeStandingURI(), idParameter, getParameters());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.8RC2
     */
    @Override
    public ImageBlock clone(BlockFilter blockFilter)
    {
        ImageBlock clone = (ImageBlock) super.clone(blockFilter);
        clone.reference = getReference().clone();
        clone.freestanding = isFreeStandingURI();
        clone.id = getId();
        return clone;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ImageBlock that = (ImageBlock) obj;

        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(getReference(), that.getReference())
            .append(isFreeStandingURI(), that.isFreeStandingURI())
            .append(getId(), that.getId())
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(getReference());
        builder.append(isFreeStandingURI());
        builder.append(getId());

        return builder.toHashCode();
    }
}

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
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Represents any kind of MetaData in the XDOM (eg saving original blocks so that the XWiki Syntax Renderer can restore
 * them after a transformation has been executed, source reference, etc).
 *
 * @version $Id$
 * @since 3.0M2
 */
public class MetaDataBlock extends AbstractBlock
{
    /**
     * Contains all MetaData for this Block and its children.
     */
    private MetaData metaData;

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param metaData the metadata to set
     * @see AbstractBlock#AbstractBlock(List)
     */
    public MetaDataBlock(List<? extends Block> childBlocks, MetaData metaData)
    {
        super(childBlocks);
        this.metaData = metaData;
    }

    /**
     * Helper constructor.
     *
     * @param childBlocks the list of children blocks of the block to construct
     * @param key the metadata key to set
     * @param value the metadata value to set
     * @see AbstractBlock#AbstractBlock(List)
     */
    public MetaDataBlock(List<? extends Block> childBlocks, String key, Object value)
    {
        this(childBlocks, new MetaData(Collections.singletonMap(key, value)));
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @see AbstractBlock#AbstractBlock(List)
     */
    public MetaDataBlock(List<? extends Block> childBlocks)
    {
        this(childBlocks, new MetaData());
    }

    /**
     * @return the metadata for this block, see {@link MetaData}
     */
    public MetaData getMetaData()
    {
        return this.metaData;
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginMetaData(getMetaData());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endMetaData(getMetaData());
    }

    @Override
    public MetaDataBlock clone()
    {
        MetaDataBlock cloned = (MetaDataBlock) super.clone();

        cloned.metaData = new MetaData(this.metaData.getMetaData());

        return cloned;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof MetaDataBlock && super.equals(obj)) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getMetaData(), ((MetaDataBlock) obj).getMetaData());

            return builder.isEquals();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(getMetaData());

        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        XWikiToStringBuilder builder = new XWikiToStringBuilder(this);

        builder.append("metadata", getMetaData());
        builder.append("children", getChildren());

        return builder.build();
    }
}

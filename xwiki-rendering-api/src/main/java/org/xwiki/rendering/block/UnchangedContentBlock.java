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

import java.util.List;

import org.xwiki.rendering.listener.MetaData;
import org.xwiki.stability.Unstable;

/**
 * This block represents a part of a macro content which is not transformed during the macro execution.
 *
 * @version $Id$
 * @since 10.9RC1
 */
@Unstable
public class UnchangedContentBlock extends MetaDataBlock
{
    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @see AbstractBlock#AbstractBlock(List)
     */
    public UnchangedContentBlock(List<? extends Block> childBlocks)
    {
        super(childBlocks);
        this.getMetaData().addMetaData(MetaData.UNCHANGED_CONTENT, true);
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param metaData the metadata to set
     * @see AbstractBlock#AbstractBlock(List)
     */
    public UnchangedContentBlock(List<? extends Block> childBlocks, MetaData metaData)
    {
        super(childBlocks, metaData);
        this.getMetaData().addMetaData(MetaData.UNCHANGED_CONTENT, true);
    }

    /**
     * Helper constructor.
     *
     * @param childBlocks the list of children blocks of the block to construct
     * @param key the metadata key to set
     * @param value the metadata value to set
     * @see AbstractBlock#AbstractBlock(List)
     */
    public UnchangedContentBlock(List<? extends Block> childBlocks, String key, Object value)
    {
        super(childBlocks, key, value);
        this.getMetaData().addMetaData(MetaData.UNCHANGED_CONTENT, true);
    }
}

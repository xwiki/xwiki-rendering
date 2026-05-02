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

import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.stability.Unstable;

/**
 * Contains the full tree of {@link Block} that represent a XWiki Document's content.
 *
 * @version $Id$
 * @since 1.5M2
 */
public class XDOM extends MetaDataBlock
{
    /**
     * Constructs an empty XDOM. Useful for example when calling a macro that doesn't use the XDOM parameter passed to
     * it.
     */
    public static final XDOM EMPTY = new XDOM(Collections.<Block>emptyList());

    /**
     * Stateful id generator for this document. We store it in the XDOM because it is the only object which remains the
     * same between parsing, transformation and rendering, and we need to generate ids during parsing and during
     * transformation.
     */
    private transient IdGenerator idGenerator;

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @see AbstractBlock#AbstractBlock(List)
     */
    public XDOM(List<? extends Block> childBlocks)
    {
        this(childBlocks, new IdGenerator(), new MetaData());
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param metaData the meta data to add for this block
     * @see AbstractBlock#AbstractBlock(List)
     */
    public XDOM(List<? extends Block> childBlocks, MetaData metaData)
    {
        this(childBlocks, new IdGenerator(), metaData);
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param idGenerator a stateful id generator for this document
     */
    public XDOM(List<? extends Block> childBlocks, IdGenerator idGenerator)
    {
        this(childBlocks, idGenerator, new MetaData());
    }

    /**
     * @param childBlocks the list of children blocks of the block to construct
     * @param metaData the meta data to add for this block
     * @param idGenerator a stateful id generator for this document
     * @see AbstractBlock#AbstractBlock(List)
     */
    public XDOM(List<? extends Block> childBlocks, IdGenerator idGenerator, MetaData metaData)
    {
        super(childBlocks, metaData);
        this.idGenerator = idGenerator;
    }

    /**
     * @return a stateful id generator for the whole document.
     */
    public IdGenerator getIdGenerator()
    {
        return this.idGenerator;
    }

    /**
     * @param idGenerator a stateful id generator for the whole document.
     * @since 2.1M1
     */
    public void setIdGenerator(IdGenerator idGenerator)
    {
        setIdGenerator(idGenerator, false);
    }

    /**
     * Sets a new id generator for this document and optionally adapts the existing ids to make them unique in the scope
     * of the new id generator. Adapting the existing ids is needed if you plan to insert this document in a larger one,
     * in which case you will have to reuse the id generator of the larger document for this document. On the other
     * hand, if this document is a clone of another document, and you plan to use it alone then you don't need to adapt
     * the existing ids. In this case, even if the id generator is different, it was created as a copy of the original
     * id generator, so the existing ids are already unique.
     *
     * @param idGenerator a stateful id generator for the whole document
     * @param adaptExistingIds whether to adapt the existing ids to make them unique in the scope of the new id
     *            generator; pass true if the new id generator is from a another document where you plan to insert this
     *            document; pass false if this document is a clone and the new id generator is a copy of the original id
     *            generator
     * @since 17.10.6
     * @since 18.3.0RC1
     */
    @Unstable
    public void setIdGenerator(IdGenerator idGenerator, boolean adaptExistingIds)
    {
        boolean changed = this.idGenerator != idGenerator;
        this.idGenerator = idGenerator;
        if (this.idGenerator != null && changed && adaptExistingIds) {
            // Make sure the existing ids are unique in the scope of the new id generator.
            makeIdsUnique();
        }
    }

    /**
     * Make sure heading and image blocks have unique ids in the scope of the provided id generator. We target only
     * heading and image blocks because these are currently the only blocks that can have generated ids. The ids of
     * macro blocks are not generated.
     */
    private void makeIdsUnique()
    {
        // Traverse the XDOM and adapt all image and heading blocks.
        this.getBlocks(block -> {
            // Would be nice to have an interface that marks blocks with generated ids, but for now we just check the
            // known block types.
            if (block instanceof ImageBlock imageBlock) {
                imageBlock.setId(this.idGenerator.adaptId(imageBlock.getId()));
            } else if (block instanceof HeaderBlock headerBlock) {
                headerBlock.setId(this.idGenerator.adaptId(headerBlock.getId()));
            }
            return false;
        }, Block.Axes.DESCENDANT);
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginDocument(getMetaData());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endDocument(getMetaData());
    }

    @Override
    public XDOM clone()
    {
        XDOM clone = (XDOM) super.clone();

        // The cloned XDOM should not increment the current id generator
        if (this.idGenerator != null) {
            clone.idGenerator = new IdGenerator(this.idGenerator);
        }

        return clone;
    }
}

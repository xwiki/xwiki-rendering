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
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.stability.Unstable;

/**
 * @version $Id$
 * @since 1.5M2
 */
public class HeaderBlock extends AbstractBlock
{
    /**
     * The level of the header.
     */
    private HeaderLevel level;

    /**
     * The id of the header.
     */
    private String id;

    /**
     * @param childBlocks the children of the header.
     * @param level the level of the header
     */
    public HeaderBlock(List<Block> childBlocks, HeaderLevel level)
    {
        super(childBlocks);

        this.level = level;
    }

    /**
     * @param childBlocks the children of the header.
     * @param level the level of the header
     * @param parameters the parameters of the header
     */
    public HeaderBlock(List<Block> childBlocks, HeaderLevel level, Map<String, String> parameters)
    {
        super(childBlocks, parameters);

        this.level = level;
    }

    /**
     * @param childBlocks the children of the header.
     * @param level the level of the header
     * @param id the id of the header.
     */
    public HeaderBlock(List<Block> childBlocks, HeaderLevel level, String id)
    {
        this(childBlocks, level);

        this.id = id;
    }

    /**
     * @param childBlocks the children of the header.
     * @param level the level of the header
     * @param parameters the parameters of the header
     * @param id the id of the header.
     */
    public HeaderBlock(List<Block> childBlocks, HeaderLevel level, Map<String, String> parameters, String id)
    {
        this(childBlocks, level, parameters);

        this.id = id;
    }

    /**
     * @return the level of the header
     */
    public HeaderLevel getLevel()
    {
        return this.level;
    }

    /**
     * @return the id of the header.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @param id the id of the header to set
     * @since 14.2RC1
     */
    @Unstable
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the {@link SectionBlock} corresponding to this header
     */
    public SectionBlock getSection()
    {
        return (SectionBlock) getParent();
    }

    @Override
    public void before(Listener listener)
    {
        listener.beginHeader(getLevel(), getId(), getParameters());
    }

    @Override
    public void after(Listener listener)
    {
        listener.endHeader(getLevel(), getId(), getParameters());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof HeaderBlock && super.equals(obj)) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getLevel(), ((HeaderBlock) obj).getLevel());
            builder.append(getId(), ((HeaderBlock) obj).getId());

            return builder.isEquals();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(getLevel());
        builder.append(getId());

        return builder.toHashCode();
    }
}

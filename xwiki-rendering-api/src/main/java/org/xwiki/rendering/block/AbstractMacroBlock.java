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

/**
 * Common class to MacroBlock and MacroMakerBlock.
 *
 * @version $Id$
 * @since 10.0
 */
public abstract class AbstractMacroBlock extends AbstractBlock
{
    /**
     * @see #getId
     */
    private String id;

    /**
     * The macro content for macro that have content. Otherwise it's null.
     */
    private String content;

    /**
     * The macro is located in a inline content (like paragraph, etc.).
     */
    private boolean inline;

    /**
     * @param childrenBlocks the list of children blocks of the block to construct
     * @param parameters the parameters to set
     * @param id the name of the macro
     * @param content the content of the macro
     * @param inline indicate if the macro is located in a inline content
     */
    public AbstractMacroBlock(List<? extends Block> childrenBlocks, Map<String, String> parameters, String id,
        String content, boolean inline)
    {
        super(childrenBlocks, parameters);

        this.id = id;
        this.content = content;
        this.inline = inline;
    }

    /**
     * @return the macro content.
     */
    public String getContent()
    {
        return content;
    }

    /**
     * @return the macro identifier.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return if true the macro is located in a inline content (like paragraph, etc.).
     */
    public boolean isInline()
    {
        return inline;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (!super.equals(obj)) {
            return false;
        }

        return equals((AbstractMacroBlock) obj);
    }

    private boolean equals(AbstractMacroBlock obj)
    {
        EqualsBuilder builder = new EqualsBuilder();

        builder.append(getContent(), obj.getContent());
        builder.append(getId(), obj.getId());
        builder.append(isInline(), obj.isInline());

        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(getContent());
        builder.append(getId());
        builder.append(isInline());

        return builder.toHashCode();
    }
}

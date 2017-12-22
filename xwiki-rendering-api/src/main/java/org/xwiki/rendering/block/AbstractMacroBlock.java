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
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Common class to MacroBlock and MacroMakerBlock.
 *
 * @version $Id$
 * @since 10.0RC1
 */
public abstract class AbstractMacroBlock extends AbstractBlock
{

    /**
     * @see AbstractMacroBlock#AbstractMacroBlock() 
     */
    public AbstractMacroBlock()
    {
        // Nothing to do
    }

    /**
     * @see AbstractMacroBlock#AbstractMacroBlock(Map)
     *
     * @param parameters the parameters to set
     */    
    public AbstractMacroBlock(Map<String, String> parameters)
    {
        super(parameters);
    }


    /**
     * @see AbstractMacroBlock#AbstractMacroBlock(Block)
     *
     * @param childBlock the child block of this block
     */
    public AbstractMacroBlock(Block childBlock)
    {
        super(childBlock);
    }

    /**
     * @see AbstractMacroBlock#AbstractMacroBlock(List)
     *
     * @param childrenBlocks the list of children blocks of the block to construct
     */
    public AbstractMacroBlock(List<? extends Block> childrenBlocks)
    {
        super(childrenBlocks);
    }

    /**
     * @see AbstractMacroBlock#AbstractMacroBlock(Block, Map)
     *
     * @param childBlock the child block of this block
     * @param parameters the parameters to set
     */
    public AbstractMacroBlock(Block childBlock, Map<String, String> parameters)
    {
        super(childBlock, parameters);
    }

    /**
     * @see AbstractMacroBlock#AbstractMacroBlock(List, Map)
     *
     * @param childrenBlocks the list of children blocks of the block to construct
     * @param parameters the parameters to set
     */
    public AbstractMacroBlock(List<? extends Block> childrenBlocks, Map<String, String> parameters)
    {
        super(childrenBlocks, parameters);
    }

    /**
     * @return the macro content.
     */
    public abstract String getContent();

    /**
     * @return the macro identifier.
     */
    public abstract String getId();

    /**
     * @return if true the macro is located in a inline content (like paragraph, etc.).
     */
    public abstract boolean isInline();

    /**
     * Helper hashCode method for any AbstractMacroBlock.
     *
     * @return hashCode.
     */
    protected int abstractBlockHashCode()
    {   
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(getContent());
        builder.append(getId());
        builder.append(isInline());

        return builder.toHashCode();
    }

    /**
     * Helper equals method for any AbstractMacroBlock.
     *
     * @param obj AbstractMacroBlock.
     * @return true  
     */
    protected boolean abstractBlockEquals(AbstractMacroBlock obj)
    {
        if (!super.equals(obj)) {
            return false;
        }

        EqualsBuilder builder = new EqualsBuilder();

        builder.append(getContent(), obj.getContent());
        builder.append(getId(), obj.getId());
        builder.append(isInline(), obj.isInline());

        return builder.isEquals();
    }
}

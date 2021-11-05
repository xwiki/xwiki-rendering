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
package org.xwiki.rendering.internal.macro.message;

import java.util.Collections;
import java.util.List;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.box.AbstractBoxMacro;
import org.xwiki.rendering.macro.box.BoxMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Common implementation for message macros (e.g. info, error, warning, success, etc).
 *
 * @version $Id$
 * @since 2.0M3
 */
public abstract class AbstractMessageMacro extends AbstractBoxMacro<BoxMacroParameters>
{
    /**
     * Create and initialize the descriptor of the macro.
     *
     * @param macroName the macro name (eg "Error", "Info", etc)
     * @param macroDescription the macro description
     */
    public AbstractMessageMacro(String macroName, String macroDescription)
    {
        super(macroName, macroDescription,
            new DefaultContentDescriptor("Content of the message", true, Block.LIST_BLOCK_TYPE),
            BoxMacroParameters.class);
    }

    @Override
    protected List<Block> parseContent(BoxMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        List<Block> macroContent = getMacroContentParser().parse(content, context, false, context.isInline())
            .getChildren();
        return Collections.singletonList(new MetaDataBlock(macroContent, this.getNonGeneratedContentMetaData()));
    }

    @Override
    protected String getClassProperty()
    {
        return super.getClassProperty() + ' ' + this.getDescriptor().getId().getId() + "message";
    }
}

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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.box.BoxMacroParameters;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Common implementation for message macros (e.g. info, error, warning, success, etc).
 *
 * @version $Id$
 * @since 2.0M3
 */
public abstract class AbstractMessageMacro extends AbstractMacro<Object>
{
    /**
     * Predefined error message.
     */
    public static final String CONTENT_MISSING_ERROR = "The required content is missing.";

    /**
     * Injected by the component manager.
     */
    @Inject
    @Named("box")
    private Macro<BoxMacroParameters> boxMacro;

    /**
     * Create and initialize the descriptor of the macro.
     *
     * @param macroName the macro name (eg "Error", "Info", etc)
     * @param macroDescription the macro description
     */
    public AbstractMessageMacro(String macroName, String macroDescription)
    {
        super(macroName, macroDescription,
            new DefaultContentDescriptor("Content of the message", true, Block.LIST_BLOCK_TYPE));
    }

    @Override
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        if (StringUtils.isEmpty(content)) {
            throw new MacroExecutionException(CONTENT_MISSING_ERROR);
        }

        BoxMacroParameters boxParameters = new BoxMacroParameters();

        boxParameters.setCssClass(context.getCurrentMacroBlock().getId() + "message");

        // TODO: we should not rely directly on the concrete macro, but only on the AbstractBoxMacro
        return this.boxMacro.execute(boxParameters, content, context);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }
}

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
package org.xwiki.rendering.macro.box;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Used in some {@code *.test} files.
 *
 * @version $Id$
 */
@Component
@Named("mandatorybox")
@Singleton
public class MandatoryContentBoxMacro extends AbstractBoxMacro<BoxMacroParameters>
{
    public MandatoryContentBoxMacro()
    {
        super("Test Box Macro", "Description",
            new DefaultContentDescriptor("", true, Block.LIST_BLOCK_TYPE),
            BoxMacroParameters.class);
    }

    @Override
    protected List<Block> parseContent(BoxMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        return Collections.singletonList(new MetaDataBlock(
            Collections.<Block>singletonList(new VerbatimBlock(content, context.isInline())),
            this.getNonGeneratedContentMetaData()
        ));
    }

    @Override
    public List<Block> execute(BoxMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        try {
            return super.execute(parameters, content, context);
        } catch (MacroExecutionException e) {
            if (e.getMessage().equals(CONTENT_MISSING_ERROR)) {
                return Arrays.asList(new VerbatimBlock(CONTENT_MISSING_ERROR, false));
            }
            throw e;
        }
    }
}

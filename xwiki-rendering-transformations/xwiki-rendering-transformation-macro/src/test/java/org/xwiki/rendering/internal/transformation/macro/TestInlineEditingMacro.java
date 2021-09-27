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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import static org.xwiki.rendering.block.Block.LIST_BLOCK_TYPE;

@Component
@Named("testinlineeditingmacro")
@Singleton
public class TestInlineEditingMacro extends AbstractNoParameterMacro
{
    public TestInlineEditingMacro()
    {
        super("Macro Inline Editing", "", new DefaultContentDescriptor("content", true, LIST_BLOCK_TYPE));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        Block contentBlock;

        WordBlock wordBlock = new WordBlock(content);
        if (context.isInline()) {
            contentBlock = wordBlock;
        } else {
            contentBlock = new ParagraphBlock(Collections.singletonList(wordBlock));
        }

        return Collections.singletonList(new MetaDataBlock(Collections.singletonList(contentBlock),
            this.getNonGeneratedContentMetaData()));
    }
}

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
package org.xwiki.rendering.macro.footnote;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.AbstractNoParameterMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Executed content macro used in some {@code *.test} files.
 * <p>
 * This macro executes its content and thus allows putFootnotes macros to be executed early.
 *
 * @version $Id$
 */
@Component
@Named("executedcontent")
@Singleton
public class ExecutedContentMacro extends AbstractNoParameterMacro
{
    /**
     * The parser used to parse the content.
     */
    @Inject
    private MacroContentParser contentParser;

    /**
     * Default constructor.
     */
    public ExecutedContentMacro()
    {
        super("Executed Content Macro", "Executes its content.",
            new DefaultContentDescriptor("Content", true, Block.LIST_BLOCK_TYPE));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        return this.contentParser.parse(content, context, true, context.isInline()).getChildren();
    }
}

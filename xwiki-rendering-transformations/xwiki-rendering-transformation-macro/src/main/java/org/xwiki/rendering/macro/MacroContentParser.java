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
package org.xwiki.rendering.macro;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Parses content of a macro field (parameter, macro content) in a given syntax.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Role
public interface MacroContentParser
{
    /**
     * Parses content of a macro field (parameter, macro content) in a given syntax and optionally remove the top level
     * paragraph.
     *
     * @param content the content to parse
     * @param macroContext the executing Macro context (from which to get the current syntax, etc)
     * @param transform if true then executes transformations
     * @param inline whether the content should be parsed as inline content (not top level paragraph, etc)
     * @return the result as a {@link org.xwiki.rendering.block.Block}s
     * @throws MacroExecutionException in case of a parsing error
     */
    XDOM parse(String content, MacroTransformationContext macroContext, boolean transform, boolean inline)
        throws MacroExecutionException;

    /**
     * Parses content of a macro field (parameter, macro content) in a given syntax and optionally remove the top level
     * paragraph.
     *
     * @param content the content to parse
     * @param macroContext the executing Macro context (from which to get the current syntax, etc)
     * @param transform if true then executes transformations
     * @param metadata the metadata to add to the XDOM before executing transformations
     * @param inline whether the content should be parsed as inline content (not top level paragraph, etc)
     * @return the result as a {@link org.xwiki.rendering.block.Block}s
     * @throws MacroExecutionException in case of a parsing error
     * @since 6.4
     */
    XDOM parse(String content, MacroTransformationContext macroContext, boolean transform, MetaData metadata,
        boolean inline) throws MacroExecutionException;

    /**
     * Find the current syntax to use for macro supporting wiki content/parameters/whatever.
     *
     * @param context the macro execution context containing the default syntax and the current macro block
     * @return the current syntax
     */
    Syntax getCurrentSyntax(MacroTransformationContext context);
}

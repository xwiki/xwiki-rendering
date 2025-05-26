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

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.stability.Unstable;

/**
 * Represents a Macro, ie a mechanism to generate Rendering {@link Block}s, that we use as a way to either generate
 * dynamic content or simply as a way to reuse Blocks in content.
 *
 * @param <P> the type of the macro parameters bean
 * @version $Id$
 * @since 1.5M2
 */
// Note: We cannot replace @ComponentRole with @Role ATM since @Role supports generics and we have Macro<P>. Changing
// it will thus break all code looking up components implementing this role.
@ComponentRole
public interface Macro<P> extends Comparable<Macro<?>>
{
    /**
     * The priority of execution relative to the other Macros. The lowest values have the highest priorities and execute
     * first. For example a Macro with a priority of 100 will execute before one with a priority of 500.
     *
     * @return the execution priority
     */
    int getPriority();

    /**
     * @return the macro descriptor
     */
    MacroDescriptor getDescriptor();

    /**
     * @return true if the macro can be inserted in some existing content such as a paragraph, a list item etc. For
     *         example if I have <code>== hello {{velocity}}world{{/velocity}}</code> then the Velocity macro must
     *         support the inline mode and not generate a paragraph.
     */
    boolean supportsInlineMode();

    /**
     * Executes the macro.
     *
     * @param parameters the macro parameters in the form of a bean defined by the {@link Macro} implementation
     * @param content the content of the macro
     * @param context the context of the macros transformation process
     * @return the result of the macro execution as a list of Block elements
     * @throws MacroExecutionException error when executing the macro
     */
    List<Block> execute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException;

    /**
     * Prepare a {@link MacroBlock} meant to be cached to be executed several times. The goal is to pre-execute
     * everything that is independent of any context and store it in an annotation of the passed {@link MacroBlock}.
     * <p>
     * The result of the pre-execution is generally stored in the {@link MacroBlock} as attribute. Since the prepared
     * block might end up exposed in an unsafe environment the value should be either clonable or immutable.
     * 
     * @param macroBlock the macro block to prepare
     * @since 15.9RC1
     */
    default void prepare(MacroBlock macroBlock) throws MacroPreparationException
    {
        // Do nothing by default
    }

    /**
     * @param parameters the parameters with which the macro would be executed
     * @param content the content with which the macro would be executed
     * @return {@code true}, if executing the macro with the parameters definitely won't modify the XDOM,
     * {@code false}, otherwise.
     * A macro needs to return {@code false} if it executes macro transformations with macros it doesn't know
     * (e.g., parsed from its content or another page) with the XDOM or the macro block accessible for these macros.
     * @since 17.3.0RC1
     * @since 16.10.9
     */
    @Unstable
    default boolean isExecutionIsolated(P parameters, String content)
    {
        return false;
    }
}

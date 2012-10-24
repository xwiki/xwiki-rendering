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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.properties.BeanManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.MacroNotFoundException;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Look for all {@link org.xwiki.rendering.block.MacroBlock} blocks in the passed {@link Block} and iteratively execute
 * each Macro in the correct order. Macros can:
 * <ul>
 * <li>provide a hint specifying when they should run (priority)</li>
 * <li>generate other Macros</li>
 * </ul>
 * 
 * @version $Id$
 * @since 1.5M2
 */
@Component
@Named("macro")
@Singleton
public class MacroTransformation extends AbstractTransformation
{
    /**
     * Number of macro executions allowed when rendering the current content before considering that we are in a loop.
     * Such a loop can happen if a macro generates itself for example.
     */
    private int maxMacroExecutions = 1000;

    /**
     * Handles macro registration and macro lookups. Injected by the Component Manager.
     */
    @Inject
    private MacroManager macroManager;

    /**
     * Used to populate automatically macros parameters classes with parameters specified in the Macro Block.
     */
    @Inject
    private BeanManager beanManager;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to generate Macro error blocks when a Macro fails to execute.
     */
    private MacroErrorManager macroErrorManager;

    private class MacroHolder implements Comparable<MacroHolder>
    {
        Macro< ? > macro;

        MacroBlock macroBlock;

        public MacroHolder(Macro< ? > macro, MacroBlock macroBlock)
        {
            this.macro = macro;
            this.macroBlock = macroBlock;
        }

        @Override
        public int compareTo(MacroHolder holder)
        {
            return this.macro.compareTo(holder.macro);
        }
    }

    @Override
    public int getPriority()
    {
        // Make it one of the transformations that's executed first so that other transformations run on the executed
        // macros.
        return 100;
    }

    @Override
    public void transform(Block rootBlock, TransformationContext context) throws TransformationException
    {
        // Create a macro execution context with all the information required for macros.
        MacroTransformationContext macroContext = new MacroTransformationContext(context);
        macroContext.setTransformation(this);

        // Counter to prevent infinite recursion if a macro generates the same macro for example.
        int executions = 0;
        List<MacroBlock> macroBlocks =
            rootBlock.getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT);
        while (!macroBlocks.isEmpty() && executions < this.maxMacroExecutions) {
            transformOnce(rootBlock, macroContext, context.getSyntax());

            // TODO: Make this less inefficient by caching the blocks list.
            macroBlocks = rootBlock.getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT);
            executions++;
        }
    }

    private void transformOnce(Block rootBlock, MacroTransformationContext context, Syntax syntax)
    {
        // 1) Get highest priority macro to execute
        MacroHolder macroHolder = getHighestPriorityMacro(rootBlock, syntax);
        if (macroHolder == null) {
            return;
        }

        List<Block> newBlocks;
        try {
            // 2) Verify if we're in macro inline mode and if the macro supports it. If not, send an error.
            if (macroHolder.macroBlock.isInline()) {
                context.setInline(true);
                if (!macroHolder.macro.supportsInlineMode()) {
                    // The macro doesn't support inline mode, raise a warning but continue.
                    // The macro will not be executed and we generate an error message instead of the macro
                    // execution result.
                    this.macroErrorManager.generateError(macroHolder.macroBlock, "Not an inline macro",
                            "This macro can only be used by itself on a new line");
                    this.logger.debug("The [{}] macro doesn't support inline mode.", macroHolder.macroBlock.getId());
                    return;
                }
            } else {
                context.setInline(false);
            }

            // 3) Execute the highest priority macro
            context.setCurrentMacroBlock(macroHolder.macroBlock);

            // Populate and validate macro parameters.
            Object macroParameters = macroHolder.macro.getDescriptor().getParametersBeanClass().newInstance();
            try {
                this.beanManager.populate(macroParameters, macroHolder.macroBlock.getParameters());
            } catch (Throwable e) {
                // One macro parameter was invalid.
                // The macro will not be executed and we generate an error message instead of the macro
                // execution result.
                this.macroErrorManager.generateError(macroHolder.macroBlock, String.format(
                    "Invalid macro parameters used for the \"%s\" macro", macroHolder.macroBlock.getId()), e);
                this.logger.debug("Invalid macro parameter for the [{}] macro. Internal error: [{}]",
                    macroHolder.macroBlock.getId(), e.getMessage());

                return;
            }

            newBlocks = ((Macro<Object>) macroHolder.macro).execute(
                macroParameters, macroHolder.macroBlock.getContent(), context);
        } catch (Throwable e) {
            // The Macro failed to execute.
            // The macro will not be executed and we generate an error message instead of the macro
            // execution result.
            // Note: We catch any Exception because we want to never break the whole rendering.
            this.macroErrorManager.generateError(macroHolder.macroBlock,
                    String.format("Failed to execute the [%s] macro", macroHolder.macroBlock.getId()), e);
            this.logger.debug("Failed to execute the [{}] macro. Internal error [{}]", macroHolder.macroBlock.getId(),
                e.getMessage());
            return;
        }

        // We wrap the blocks generated by the macro execution with MacroMarker blocks so that listeners/renderers
        // who wish to know the group of blocks that makes up the executed macro can. For example this is useful for
        // the XWiki Syntax renderer so that it can reconstruct the macros from the transformed XDOM.
        Block resultBlock = wrapInMacroMarker(macroHolder.macroBlock, newBlocks);

        // 4) Replace the MacroBlock by the Blocks generated by the execution of the Macro
        macroHolder.macroBlock.getParent().replaceChild(resultBlock, macroHolder.macroBlock);
    }

    /**
     * @return the macro with the highest priority for the passed syntax or null if no macro is found
     */
    private MacroHolder getHighestPriorityMacro(Block rootBlock, Syntax syntax)
    {
        List<MacroHolder> macroHolders = new ArrayList<MacroHolder>();

        // 1) Sort the macros by priority to find the highest priority macro to execute
        List<MacroBlock> macroBlocks =
            rootBlock.getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT);
        for (MacroBlock macroBlock : macroBlocks) {
            try {
                Macro< ? > macro = this.macroManager.getMacro(new MacroId(macroBlock.getId(), syntax));
                macroHolders.add(new MacroHolder(macro, macroBlock));
            } catch (MacroNotFoundException e) {
                // Macro cannot be found. Generate an error message instead of the macro execution result.
                // TODO: make it internationalized
                this.macroErrorManager.generateError(macroBlock,
                        String.format("Unknown macro: %s", macroBlock.getId()),
                        String.format(
                                "The \"%s\" macro is not in the list of registered macros. Verify the spelling or "
                                        + "contact your administrator.", macroBlock.getId()));
                this.logger.debug("Failed to locate the [{}] macro. Ignoring it.", macroBlock.getId());
            } catch (MacroLookupException e) {
                // TODO: make it internationalized
                this.macroErrorManager.generateError(macroBlock,
                        String.format("Invalid macro: %s", macroBlock.getId()), e);
                this.logger.debug("Failed to instantiate the [{}] macro. Ignoring it.", macroBlock.getId());
            }
        }

        // Sort the Macros by priority
        Collections.sort(macroHolders);

        return macroHolders.size() > 0 ? macroHolders.get(0) : null;
    }

    /**
     * Wrap the output of a macro block with a {@link MacroMarkerBlock}.
     *
     * @param macroBlockToWrap the block that should be replaced
     * @param newBlocks list of blocks to wrap
     * @return the wrapper
     */
    private Block wrapInMacroMarker(MacroBlock macroBlockToWrap, List<Block> newBlocks)
    {
        return new MacroMarkerBlock(macroBlockToWrap.getId(), macroBlockToWrap.getParameters(), macroBlockToWrap
            .getContent(), newBlocks, macroBlockToWrap.isInline());
    }
}

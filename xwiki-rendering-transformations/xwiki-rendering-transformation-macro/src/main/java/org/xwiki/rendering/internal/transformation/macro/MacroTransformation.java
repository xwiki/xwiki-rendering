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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.properties.BeanManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.MacroNotFoundException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.util.ErrorBlockGenerator;

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
public class MacroTransformation extends AbstractTransformation implements Initializable
{
    private static final String TM_UNKNOWNMACRO = "rendering.macro.error.unknown";

    private static final String TM_FAILEDMACRO = "rendering.macro.error.failed";

    private static final String TM_INVALIDMACRO = "rendering.macro.error.invalid";

    private static final String TM_STANDALONEMACRO = "rendering.macro.error.standalone";

    private static final String TM_INVALIDMACROPARAMETER = "rendering.macro.error.invalidParameter";

    private static class MacroLookupExceptionElement
    {
        private MacroBlock macroBlock;

        private MacroLookupException exception;

        public MacroLookupExceptionElement(MacroBlock macroBlock, MacroLookupException exception)
        {
            this.macroBlock = macroBlock;
            this.exception = exception;
        }

        public MacroBlock getMacroBlock()
        {
            return macroBlock;
        }

        public MacroLookupException getException()
        {
            return exception;
        }
    }

    private class PriorityMacroBlockMatcher implements BlockMatcher
    {
        private final Syntax syntax;

        private MacroBlock block;

        private Macro<?> blockMacro;

        private List<MacroLookupExceptionElement> errors;

        // Cache known macros since getting them again and again from the ComponentManager might be expensive
        private final Map<String, Macro<?>> knownMacros = new HashMap<>();

        PriorityMacroBlockMatcher(Syntax syntax)
        {
            this.syntax = syntax;
        }

        public MacroBlock getBlock()
        {
            return block;
        }

        public Macro<?> getBlockMacro()
        {
            return blockMacro;
        }

        public List<MacroLookupExceptionElement> getErrors()
        {
            return errors;
        }

        @Override
        public boolean match(Block block)
        {
            if (block instanceof MacroBlock) {
                MacroBlock macroBlock = (MacroBlock) block;

                try {
                    // Try to find a known macros
                    Macro<?> macro = this.knownMacros.get(macroBlock.getId());

                    // If not found use the macro manager
                    if (macro == null) {
                        macro =
                            MacroTransformation.this.macroManager
                                .getMacro(new MacroId(macroBlock.getId(), this.syntax));

                        // Cache the found macro for later
                        this.knownMacros.put(macroBlock.getId(), macro);
                    }

                    // Find higher priority macro
                    if (this.block == null || this.blockMacro.compareTo(macro) > 0) {
                        this.block = macroBlock;
                        this.blockMacro = macro;
                    }
                } catch (MacroLookupException e) {
                    if (this.errors == null) {
                        this.errors = new LinkedList<MacroLookupExceptionElement>();
                    }

                    this.errors.add(new MacroLookupExceptionElement(macroBlock, e));
                }
            }

            return false;
        }
    }

    /**
     * Number of times a macro can generate another macro before considering that we are in a loop. Such a loop can
     * happen if a macro generates itself for example.
     */
    private int maxRecursions = 1000;

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
     * Used to updated the rendering context.
     */
    @Inject
    private RenderingContext renderingContext;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    @Inject
    private ErrorBlockGenerator errorBlockGenerator;

    /**
     * Used to generate Macro error blocks when a Macro fails to execute.
     */
    private MacroErrorManager macroErrorManager;

    @Override
    public void initialize() throws InitializationException
    {
        this.macroErrorManager = new MacroErrorManager(this.errorBlockGenerator);
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
        for (int recursions = 0; recursions < this.maxRecursions;) {
            // 1) Get highest priority macro
            PriorityMacroBlockMatcher priorityMacroBlockMatcher = new PriorityMacroBlockMatcher(context.getSyntax());
            rootBlock.getFirstBlock(priorityMacroBlockMatcher, Block.Axes.DESCENDANT);

            // 2) Apply macros lookup errors
            if (priorityMacroBlockMatcher.getErrors() != null) {
                for (MacroLookupExceptionElement error : priorityMacroBlockMatcher.getErrors()) {
                    if (error.getException() instanceof MacroNotFoundException) {
                        // Macro cannot be found. Generate an error message instead of the macro execution result.
                        this.macroErrorManager.generateError(error.getMacroBlock(), TM_UNKNOWNMACRO,
                            "Unknown macro: {}.",
                            "The [{}] macro is not in the list of registered macros. Verify the spelling or "
                                + "contact your administrator.",
                            error.getMacroBlock().getId());
                    } else {
                        this.macroErrorManager.generateError(error.getMacroBlock(), TM_INVALIDMACRO,
                            "Invalid macro: {}.", null, error.getMacroBlock().getId(), error.getException());
                    }
                }
            }

            MacroBlock macroBlock = priorityMacroBlockMatcher.getBlock();

            if (macroBlock == null) {
                // Nothing left to do
                return;
            }

            Macro<?> macro = priorityMacroBlockMatcher.getBlockMacro();

            boolean incrementRecursions = macroBlock.getParent() instanceof MacroMarkerBlock;

            List<Block> newBlocks;
            try {
                // 3) Verify if we're in macro inline mode and if the macro supports it. If not, send an error.
                if (macroBlock.isInline()) {
                    macroContext.setInline(true);
                    if (!macro.supportsInlineMode()) {
                        // The macro doesn't support inline mode, raise a warning but continue.
                        // The macro will not be executed and we generate an error message instead of the macro
                        // execution result.
                        this.macroErrorManager.generateError(macroBlock, TM_STANDALONEMACRO,
                            "The [{}] macro is a standalone macro and it cannot be used inline",
                            "This macro generates standalone content. As a consequence you need to make sure to use a "
                                + "syntax that separates your macro from the content before and after it so that it's on a "
                                + "line by itself. For example in XWiki Syntax 2.0+ this means having 2 newline characters "
                                + "(a.k.a line breaks) separating your macro from the content before and after it.",
                            macroBlock.getId());

                        continue;
                    }
                } else {
                    macroContext.setInline(false);
                }

                // 4) Execute the highest priority macro
                macroContext.setCurrentMacroBlock(macroBlock);
                ((MutableRenderingContext) this.renderingContext).setCurrentBlock(macroBlock);

                // Populate and validate macro parameters.
                Object macroParameters =
                    macro.getDescriptor().getParametersBeanClass().getDeclaredConstructor().newInstance();
                try {
                    this.beanManager.populate(macroParameters, macroBlock.getParameters());
                } catch (Throwable e) {
                    // One macro parameter was invalid.
                    // The macro will not be executed and we generate an error message instead of the macro
                    // execution result.
                    this.macroErrorManager.generateError(macroBlock, TM_INVALIDMACROPARAMETER,
                        "Invalid macro parameters used for the [{}] macro.", null, macroBlock.getId(), e);

                    continue;
                }

                newBlocks = ((Macro) macro).execute(macroParameters, macroBlock.getContent(), macroContext);
            } catch (Throwable e) {
                // The Macro failed to execute.
                // The macro will not be executed and we generate an error message instead of the macro
                // execution result.
                // Note: We catch any Exception because we want to never break the whole rendering.
                this.macroErrorManager.generateError(macroBlock, TM_FAILEDMACRO, "Failed to execute the [{}] macro.",
                    null, macroBlock.getId(), e);

                continue;
            } finally {
                ((MutableRenderingContext) this.renderingContext).setCurrentBlock(null);
            }

            // We wrap the blocks generated by the macro execution with MacroMarker blocks so that listeners/renderers
            // who wish to know the group of blocks that makes up the executed macro can. For example this is useful for
            // the XWiki Syntax renderer so that it can reconstruct the macros from the transformed XDOM.
            Block resultBlock = wrapInMacroMarker(macroBlock, newBlocks);

            // 5) Replace the MacroBlock by the Blocks generated by the execution of the Macro
            macroBlock.getParent().replaceChild(resultBlock, macroBlock);

            if (incrementRecursions) {
                ++recursions;
            }
        }
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
        return new MacroMarkerBlock(macroBlockToWrap.getId(), macroBlockToWrap.getParameters(),
            macroBlockToWrap.getContent(), newBlocks, macroBlockToWrap.isInline());
    }

    /**
     * @param maxRecursions the max numnber of recursion allowed before we stop transformations
     */
    public void setMaxRecursions(int maxRecursions)
    {
        this.maxRecursions = maxRecursions;
    }
}

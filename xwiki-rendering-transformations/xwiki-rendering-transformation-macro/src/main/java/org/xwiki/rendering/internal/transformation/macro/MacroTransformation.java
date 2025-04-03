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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.properties.BeanManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.internal.transformation.MutableRenderingContext;
import org.xwiki.rendering.listener.MetaData;
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
import org.xwiki.text.XWikiToStringBuilder;

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

    private record MacroItem(MacroBlock block, Macro<?> macro, int[] index) implements Comparable<MacroItem>
    {
        @Override
        public int compareTo(MacroItem macroItem)
        {
            // Compare first by macro priority, then by index.
            int macroComparison = this.macro.compareTo(macroItem.macro);
            if (macroComparison == 0) {
                return Arrays.compare(this.index, macroItem.index);
            } else {
                return macroComparison;
            }
        }

        @Override
        public boolean equals(Object object)
        {
            if (this == object) {
                return true;
            }

            if (!(object instanceof MacroItem macroItem)) {
                return false;
            }

            return new EqualsBuilder().append(index(), macroItem.index())
                .append(macro(), macroItem.macro())
                .append(block(), macroItem.block())
                .isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder(17, 37).append(block()).append(macro()).append(index()).toHashCode();
        }

        @Override
        public String toString()
        {
            return new XWikiToStringBuilder(this)
                .append("block", block())
                .append("macro", macro())
                .append("index", index())
                .toString();
        }
    }

    private class PriorityMacroBlockMatcher implements BlockMatcher
    {
        private class ChildrenMatcher implements BlockMatcher
        {
            private final int[] prefix;

            ChildrenMatcher(MacroItem parentMacro)
            {
                this.prefix = parentMacro.index();
            }

            @Override
            public boolean match(Block block)
            {
                // Add a prefix to all new blocks to ensure that they're directly after the current macro.
                PriorityMacroBlockMatcher.this.matchBlock(block, this.prefix);

                return false;
            }
        }

        private final Syntax syntax;

        private boolean needsScan = true;

        private int currentIndex;

        private PriorityQueue<MacroItem> priorityQueue;

        // The list of blocks to add to the priority queue, collected in a simple list to add them all at once in one
        // efficient heap construction.
        private final List<MacroItem> nextBlocks;

        private List<MacroLookupExceptionElement> errors;

        // Cache known macros since getting them again and again from the ComponentManager might be expensive
        private final Map<String, Macro<?>> knownMacros = new HashMap<>();

        PriorityMacroBlockMatcher(Syntax syntax)
        {
            this.syntax = syntax;
            this.nextBlocks = new ArrayList<>();
        }

        public MacroItem getNextBlock()
        {
            if (this.priorityQueue == null) {
                if (this.nextBlocks.isEmpty()) {
                    return null;
                }
                // Construct the priority queue here where we actually need it.
                this.priorityQueue = new PriorityQueue<>(this.nextBlocks);
                this.nextBlocks.clear();
            }
            MacroItem item = this.priorityQueue.poll();
            // When the priority queue becomes empty, replace it by null so we don't need to check both for empty and
            // null.
            if (this.priorityQueue.isEmpty()) {
                this.priorityQueue = null;
            }
            return item;
        }

        /**
         * @return if a full scan of the whole XDOM is needed (due to reset having been called)
         */
        public boolean isFullScanNeeded()
        {
            return this.needsScan;
        }

        public List<MacroLookupExceptionElement> getErrors()
        {
            return this.errors;
        }

        public BlockMatcher getChildrenMatcher(MacroItem parentMacro)
        {
            return new ChildrenMatcher(parentMacro);
        }

        public void reset()
        {
            this.needsScan = true;
            this.currentIndex = 0;
            this.priorityQueue = null;
            this.nextBlocks.clear();
            this.errors = null;
        }

        @Override
        public boolean match(Block block)
        {
            matchBlock(block, new int[0]);

            return false;
        }

        private void matchBlock(Block block, int[] prefix)
        {
            // Record that a scan has happened. The caller is responsible for making sure that this method is only
            // called as part of a full scan when one is required.
            this.needsScan = false;

            if (block instanceof MacroBlock macroBlock) {

                try {
                    // Try to find a known macros
                    Macro<?> macro = this.knownMacros.get(macroBlock.getId());

                    // If not found use the macro manager
                    if (macro == null) {
                        macro = MacroTransformation.this.macroManager
                            .getMacro(new MacroId(macroBlock.getId(), this.syntax));

                        // Cache the found macro for later
                        this.knownMacros.put(macroBlock.getId(), macro);
                    }

                    // Combine prefix and the currentIndex
                    int[] macroIndex = new int[prefix.length + 1];
                    System.arraycopy(prefix, 0, macroIndex, 0, prefix.length);
                    macroIndex[prefix.length] = this.currentIndex++;

                    MacroItem item = new MacroItem(macroBlock, macro, macroIndex);
                    // If we have a priority queue, add it directly to it, otherwise first collect everything for a
                    // more efficient priority queue construction.
                    if (this.priorityQueue == null) {
                        this.nextBlocks.add(item);
                    } else {
                        this.priorityQueue.add(item);
                    }
                } catch (MacroLookupException e) {
                    if (this.errors == null) {
                        this.errors = new ArrayList<>();
                    }

                    this.errors.add(new MacroLookupExceptionElement(macroBlock, e));
                }
            }
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

    @Inject
    private IsolatedExecutionConfiguration isolatedExecutionConfiguration;

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

        PriorityMacroBlockMatcher priorityMacroBlockMatcher = new PriorityMacroBlockMatcher(context.getSyntax());

        // Counter to prevent infinite recursion if a macro generates the same macro for example.
        for (int recursions = 0; recursions < this.maxRecursions;) {
            // 1) Get highest priority macros.
            if (priorityMacroBlockMatcher.isFullScanNeeded()) {
                priorityMacroBlockMatcher.reset();
                rootBlock.getFirstBlock(priorityMacroBlockMatcher, Block.Axes.DESCENDANT);

                // 2) Apply macros lookup errors
                processErrors(priorityMacroBlockMatcher);
            }

            MacroItem macroItem = priorityMacroBlockMatcher.getNextBlock();

            if (macroItem == null) {
                // Nothing left to do
                return;
            }

            MacroBlock macroBlock = macroItem.block();
            Macro<?> macro = macroItem.macro();

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

                // Rescan if either the indexes of the macros get too long or the macro's execution isn't isolated.
                // The value "64" was chosen because 64 ints should hardly cause any impact, but having a hierarchy of
                // 64 nested macros seems already pretty unlikely.
                // In the worst case, a very deep tree of macros, this could cause a re-scan every 63 macro executions.
                if (macroItem.index().length >= 64
                    || !this.isolatedExecutionConfiguration.isExecutionIsolated(macroBlock.getId(),
                    ((Macro<Object>) macro).isExecutionIsolated(macroParameters, macroBlock.getContent())))
                {
                    priorityMacroBlockMatcher.reset();
                }
                newBlocks = ((Macro) macro).execute(macroParameters, macroBlock.getContent(), macroContext);
            } catch (Throwable e) {
                // The Macro failed to execute.
                // The macro will not be executed and we generate an error message instead of the macro
                // execution result.
                // Note: We catch any Exception because we want to never break the whole rendering.
                if (macroBlock.getParent() == null) {
                    this.logger.warn("The macro [{}] failed to execute and removed itself from the document so no "
                            + "error can be displayed. The root cause of the error is: [{}]", macroBlock.getId(),
                        ExceptionUtils.getRootCauseMessage(e));
                } else {
                    this.macroErrorManager.generateError(macroBlock, TM_FAILEDMACRO,
                        "Failed to execute the [{}] macro.", null, macroBlock.getId(), e);
                }

                continue;
            } finally {
                ((MutableRenderingContext) this.renderingContext).setCurrentBlock(null);
            }

            // Only wrap and set the blocks when there is a parent. Otherwise, the macro has removed the macro block
            // from the XDOM and is itself responsible for handling the update of the XDOM.
            if (macroBlock.getParent() != null) {
                // We wrap the blocks generated by the macro execution with MacroMarker blocks so that
                // listeners/renderers who wish to know the group of blocks that makes up the executed macro can.
                // For example this is useful for the XWiki Syntax renderer so that it can reconstruct the macros from
                // the transformed XDOM.
                Block resultBlock = wrapInMacroMarker(macroBlock, newBlocks);

                if (!priorityMacroBlockMatcher.isFullScanNeeded()) {
                    // Find descendant blocks if no full scan is needed. Those descendant blocks will be inserted
                    // into the existing priority queue with indexes that are in the same position as the current macro.
                    BlockMatcher childrenMatcher = priorityMacroBlockMatcher.getChildrenMatcher(macroItem);
                    resultBlock.getFirstBlock(childrenMatcher, Block.Axes.DESCENDANT);
                    processErrors(priorityMacroBlockMatcher);
                }

                // 5) Replace the MacroBlock by the Blocks generated by the execution of the Macro
                macroBlock.getParent().replaceChild(resultBlock, macroBlock);
            }

            if (incrementRecursions) {
                ++recursions;
            }
        }
    }

    private void processErrors(PriorityMacroBlockMatcher priorityMacroBlockMatcher)
    {
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

            // Clear the errors so that we don't apply them again.
            priorityMacroBlockMatcher.getErrors().clear();
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

    @Override
    public void prepare(Block block)
    {
        // Find the current syntax
        Syntax syntax = block.getSyntaxMetadata().orElse(null);

        // Prepare the block
        // FIXME: remove the try/catch while fixing http://jira.xwiki.org/browse/XRENDERING-725.
        try {
            prepare(block, syntax);
        } catch (StackOverflowError e) {
            this.logger.error("Failed to prepare the block", e);
        }
    }

    private void prepare(Block block, Syntax parentSyntax)
    {
        Syntax currentSyntax = parentSyntax;

        // Check if the syntax changes
        if (block instanceof MetaDataBlock) {
            Syntax blockSyntax = (Syntax) ((MetaDataBlock) block).getMetaData().getMetaData(MetaData.SYNTAX);
            if (blockSyntax != null) {
                currentSyntax = blockSyntax;
            }
        }

        // Prepare the block
        if (block instanceof MacroBlock) {
            MacroBlock macroBlock = (MacroBlock) block;

            // Find the macro
            Macro<?> macro = null;
            try {
                macro = this.macroManager.getMacro(new MacroId(macroBlock.getId(), currentSyntax));
            } catch (Exception e) {
                this.logger.debug(
                    "Failed to get the macro with identifier [{}] for syntax [{}] (this macro block won't be prepared): {}",
                    macroBlock.getId(), currentSyntax, ExceptionUtils.getRootCauseMessage(e));
            }

            // Prepare the macro block
            if (macro != null) {
                try {
                    macro.prepare(macroBlock);
                } catch (Exception e) {
                    this.logger.error("Failed to prepare the macro block", e);
                }
            }
        }

        // Prepare the children
        for (Block child : block.getChildren()) {
            prepare(child, currentSyntax);
        }
    }
}

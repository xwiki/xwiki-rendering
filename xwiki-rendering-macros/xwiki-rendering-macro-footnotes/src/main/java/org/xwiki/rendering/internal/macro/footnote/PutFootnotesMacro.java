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
package org.xwiki.rendering.internal.macro.footnote;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.match.MacroMarkerBlockMatcher;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.footnote.FootnoteMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.IdGenerator;

/**
 * List footnotes at the location where the macro is used. Note that only a single such macro is supported (and the
 * last one used is honored, the others are dropped).
 *
 * @version $Id$
 * @since 2.0M2
 */
@Component
@Named(PutFootnotesMacro.MACRO_NAME)
@Singleton
public class PutFootnotesMacro extends AbstractMacro<FootnoteMacroParameters>
{
    /** The name of this macro. */
    public static final String MACRO_NAME = "putFootnotes";

    /** The description of the macro. */
    private static final String DESCRIPTION = "Displays the footnotes defined so far."
        + " If missing, all footnotes are displayed by default at the end of the page.";

    /** ID attribute name. */
    private static final String ID_ATTRIBUTE_NAME = "id";

    /** CSS Class attribute name. */
    private static final String CLASS_ATTRIBUTE_NAME = "class";

    /** Prefix for the ID of the reference link to the footnote. */
    private static final String FOOTNOTE_ID_PREFIX = "x_footnote_";

    /** Prefix for the ID of the footnote. */
    private static final String FOOTNOTE_REFERENCE_ID_PREFIX = "x_footnote_ref_";

    /** Class name for the reference to the footnote. */
    private static final String FOOTNOTE_REF_CLASS_NAME = "footnoteRef";

    /**
     * Internal data structure for representing a footnote.
     */
    private static class Footnote
    {
        /**
         * The HTML id of the footnote.
         */
        private String id;

        /**
         * The HTML id of the reference to the footnote (for jumping back).
         */
        private String referenceId;

        /**
         * The macro marker block of the footnote macro.
         */
        private final MacroMarkerBlock macroMarkerBlock;

        /**
         * The content of the footnote.
         */
        private CompositeBlock content;

        /**
         * Create a new footnote for the given footnote macro marker block.
         * <p>
         * If the only child is a format block with a reference id, the ids are extracted from the format block and
         * its link child, otherwise the content of the macro marker is set as content and ids should be set later.
         *
         * @param macroMarkerBlock the footnote macro marker block
         */
        Footnote(MacroMarkerBlock macroMarkerBlock)
        {
            this.macroMarkerBlock = macroMarkerBlock;

            if (macroMarkerBlock.getChildren().size() == 1
                && macroMarkerBlock.getChildren().get(0) instanceof FormatBlock
                && StringUtils.startsWith(macroMarkerBlock.getChildren().get(0).getParameter(ID_ATTRIBUTE_NAME),
                FOOTNOTE_REFERENCE_ID_PREFIX))
            {
                Block formatBlock = macroMarkerBlock.getChildren().get(0);
                if (!formatBlock.getChildren().isEmpty() && formatBlock.getChildren().get(0) instanceof LinkBlock) {
                    LinkBlock linkBlock = (LinkBlock) formatBlock.getChildren().get(0);
                    ResourceReference reference = linkBlock.getReference();
                    if (reference instanceof DocumentResourceReference) {
                        this.id = ((DocumentResourceReference) reference).getAnchor();
                    }
                }
                this.referenceId = formatBlock.getParameter(ID_ATTRIBUTE_NAME);
            } else {
                this.content = new CompositeBlock(macroMarkerBlock.getChildren());
            }
        }
    }

    @Inject
    private Logger logger;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public PutFootnotesMacro()
    {
        super("Put Footnote", DESCRIPTION, FootnoteMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_CONTENT));

        // Must be executed after footnote macro because it's injecting links in it
        setPriority(FootnoteMacro.PRIORITY + 1);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(FootnoteMacroParameters parameters, String content, MacroTransformationContext context)
    {
        List<Block> result = Collections.emptyList();

        // If this macro is inside a footnote list or a footnote, don't do anything as this creates a huge mess - it
        // would remove the generated footnote list from the XDOM in the process of creating it.
        Block footnoteAncestor =
            context.getCurrentMacroBlock()
                .getFirstBlock(new MacroMarkerBlockMatcher(PutFootnotesMacro.MACRO_NAME, FootnoteMacro.MACRO_NAME),
                    Block.Axes.ANCESTOR);

        if (footnoteAncestor != null) {
            return result;
        }

        Block root = context.getXDOM();
        List<MacroMarkerBlock> macroMarkerBlocks =
            root.getBlocks(new MacroMarkerBlockMatcher(PutFootnotesMacro.MACRO_NAME, FootnoteMacro.MACRO_NAME),
                Block.Axes.DESCENDANT);

        Map<String, Footnote> footnotes = new LinkedHashMap<>();

        // Give footnotes without id temporary, numeric ids, we don't need to retrieve them by id.
        AtomicInteger temporaryId = new AtomicInteger(0);
        // Get the list of footnotes in the document
        macroMarkerBlocks.stream()
            .filter(macro -> FootnoteMacro.MACRO_NAME.equals(macro.getId()))
            .map(PutFootnotesMacro::repairStandaloneFootnote)
            .map(Footnote::new)
            .forEach(footnote -> footnotes.put(
                Objects.requireNonNullElseGet(footnote.id, () -> String.valueOf(temporaryId.getAndIncrement())),
                footnote));

        // Collect the footnote content from the existing footnote lists and remove them.
        macroMarkerBlocks.stream()
            .filter(macro -> PutFootnotesMacro.MACRO_NAME.equals(macro.getId()))
            .forEach(macro -> collectFootnoteContents(macro, footnotes));

        IdGenerator idGenerator = null;
        if (context.getXDOM() != null) {
            idGenerator = context.getXDOM().getIdGenerator();
        }

        if (!footnotes.isEmpty()) {
            NumberedListBlock container = new NumberedListBlock(Collections.emptyList());
            container.setParameter(CLASS_ATTRIBUTE_NAME, "footnotes");
            result = Collections.singletonList(container);

            int counter = 1;
            for (Footnote footnote : footnotes.values()) {
                if (footnote.content != null) {
                    Block footnoteResult = processFootnote(footnote, counter, idGenerator);
                    container.addChild(footnoteResult);
                    counter++;
                } else {
                    this.logger.warn("No content for footnote [{}] found, ignoring it.",
                        footnote.macroMarkerBlock);
                }
            }
        }

        return result;
    }

    /**
     * Repair a standalone footnote macro marker block by wrapping it in a paragraph if it is not already inline.
     *
     * @param macroMarkerBlock the macro marker block to repair
     * @return the repaired macro marker block
     */
    private static MacroMarkerBlock repairStandaloneFootnote(MacroMarkerBlock macroMarkerBlock)
    {
        if (macroMarkerBlock.isInline()) {
            return macroMarkerBlock;
        } else {
            // Wrap the macro marker block in a paragraph and make it inline.
            MacroMarkerBlock result = new MacroMarkerBlock(macroMarkerBlock.getId(), macroMarkerBlock.getParameters(),
                macroMarkerBlock.getContent(), macroMarkerBlock.getChildren(), true);
            macroMarkerBlock.getParent().replaceChild(new ParagraphBlock(List.of(result)), macroMarkerBlock);
            return result;
        }
    }

    /**
     * Collect and remove footnote contents from the given putFootnotes macro marker block.
     *
     * @param macro the putFootnotes macro marker block from which footnote contents shall be collected
     * @param footnotes the footnotes where the content shall be collected
     */
    private void collectFootnoteContents(MacroMarkerBlock macro, Map<String, Footnote> footnotes)
    {
        if (macro.getChildren().size() == 1 && macro.getChildren().get(0) instanceof NumberedListBlock) {
            for (Block listItemBlock : macro.getChildren().get(0).getChildren()) {
                if (listItemBlock.getChildren().size() == 3
                    && listItemBlock.getChildren().get(2) instanceof CompositeBlock
                    && StringUtils.startsWith(listItemBlock.getChildren().get(0).getParameter(ID_ATTRIBUTE_NAME),
                    FOOTNOTE_ID_PREFIX))
                {
                    CompositeBlock footnoteContent = (CompositeBlock) listItemBlock.getChildren().get(2);
                    String id = listItemBlock.getChildren().get(0).getParameter(ID_ATTRIBUTE_NAME);
                    if (footnotes.containsKey(id)) {
                        footnotes.get(id).content = footnoteContent;
                    } else {
                        this.logger.warn("Could not find footnote marker for footnote [{}], ignoring it.",
                            footnoteContent);
                    }
                }
            }
        }

        macro.getParent().removeBlock(macro);
    }

    /**
     * Processes a footnote macro, by generating a footnote element to insert in the footnote list and a reference
     * to it, which is placed instead of the macro call.
     *
     * @param footnote the footnote
     * @param counter the current footnote counter
     * @return the footnote element which should be inserted in the footnote list
     */
    private ListItemBlock processFootnote(Footnote footnote, int counter, IdGenerator idGenerator)
    {
        if (footnote.referenceId == null) {
            footnote.referenceId = generateId(counter, FOOTNOTE_REFERENCE_ID_PREFIX, idGenerator);
        }

        if (footnote.id == null) {
            footnote.id = generateId(counter, FOOTNOTE_ID_PREFIX, idGenerator);
        }

        // Construct the footnote and reference blocks
        Block referenceBlock = createFootnoteReferenceBlock(counter, footnote.id, footnote.referenceId);
        ListItemBlock footnoteBlock = createFootnoteBlock(footnote.content, footnote.id, footnote.referenceId);
        // Insert the footnote and the reference in the document.
        addFootnoteRef(footnote.macroMarkerBlock, referenceBlock);
        return footnoteBlock;
    }

    private String generateId(int counter, String idPrefix, IdGenerator idGenerator)
    {
        String footnoteId = idPrefix + counter;
        if (idGenerator != null) {
            footnoteId = idGenerator.generateUniqueId(footnoteId.substring(0, 1), footnoteId.substring(1));
        }

        return footnoteId;
    }

    /**
     * Add a footnote to the list of document footnotes. If such a list doesn't exist yet, create it and append it to
     * the end of the document.
     *
     * @param footnoteMacro the {{footnote}} macro being processed
     * @param footnoteRef the generated block corresponding to the footnote to be inserted
     */
    private void addFootnoteRef(MacroMarkerBlock footnoteMacro, Block footnoteRef)
    {
        footnoteMacro.getChildren().clear();
        footnoteMacro.addChild(footnoteRef);
    }

    /**
     * Generate the footnote reference (link) that should be inserted at the location of the macro, and should point to
     * the actual footnote at the end of the document.
     *
     * @param counter the current footnote counter
     * @param footnoteId the id of the footnote
     * @param referenceId the id of the reference
     * @return the generated reference element, displayed as {@code (superscript(link(footnote index)))}
     */
    private Block createFootnoteReferenceBlock(int counter, String footnoteId, String referenceId)
    {
        Block result = new WordBlock(String.valueOf(counter));
        DocumentResourceReference reference = new DocumentResourceReference(null);
        reference.setAnchor(footnoteId);
        result = new LinkBlock(Collections.singletonList(result), reference, false);
        result = new FormatBlock(Collections.singletonList(result), Format.SUPERSCRIPT);
        result.setParameter(ID_ATTRIBUTE_NAME, referenceId);
        result.setParameter(CLASS_ATTRIBUTE_NAME, FOOTNOTE_REF_CLASS_NAME);
        return result;
    }

    /**
     * Generate the footnote block, a numbered list item containing a backlink to the footnote's reference, and the
     * actual footnote content.
     *
     * @param content the block with the actual footnote content
     * @param footnoteId the id of the footnote
     * @param referenceId the id of the reference
     * @return the generated footnote block
     */
    private ListItemBlock createFootnoteBlock(CompositeBlock content, String footnoteId, String referenceId)
    {
        Block result = new WordBlock("^");
        DocumentResourceReference reference = new DocumentResourceReference(null);
        reference.setAnchor(referenceId);
        result = new LinkBlock(Collections.singletonList(result), reference, false);
        result.setParameter(ID_ATTRIBUTE_NAME, footnoteId);
        result.setParameter(CLASS_ATTRIBUTE_NAME, "footnoteBackRef");
        result = new ListItemBlock(Collections.singletonList(result));
        result.addChild(new SpaceBlock());
        result.addChild(content);
        return (ListItemBlock) result;
    }
}

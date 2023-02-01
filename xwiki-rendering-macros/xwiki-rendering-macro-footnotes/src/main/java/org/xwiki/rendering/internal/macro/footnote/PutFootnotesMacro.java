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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.match.MacroMarkerBlockMatcher;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.footnote.FootnoteMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.IdGenerator;

/**
 * List footnotes at the end of the page.
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

    /** Name of the attribute in the footnote macro marker block that stores the footnote id. */
    private static final String FOOTNOTE_ID_ATTRIBUTE = "footnoteId";

    /** Name of the attribute in the footnote macro marker block that stores the footnote reference's id. */
    private static final String FOOTNOTE_REFERENCE_ID_ATTRIBUTE = "footnoteReferenceId";

    /** Name of the attribute in the footnote macro marker block that stores the original content of the footnote. */
    private static final String FOOTNOTE_CONTENT_ATTRIBUTE = "footnoteContent";

    /**
     * Used to parse the content of the macro.
     */
    @Inject
    private MacroContentParser contentParser;

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

        // Get the list of footnotes in the document
        Block root = context.getXDOM();
        List<MacroMarkerBlock> macroMarkerBlocks =
            root.getBlocks(new MacroMarkerBlockMatcher(PutFootnotesMacro.MACRO_NAME, FootnoteMacro.MACRO_NAME),
                Block.Axes.DESCENDANT);

        // Clear the existing footnote lists.
        macroMarkerBlocks.stream()
            .filter(macro -> PutFootnotesMacro.MACRO_NAME.equals(macro.getId()))
            .forEach(macro -> macro.setChildren(Collections.emptyList()));

        IdGenerator idGenerator = null;

        if (context.getXDOM() != null) {
            idGenerator = context.getXDOM().getIdGenerator();
        }

        // Collect the footnote macros.
        List<MacroMarkerBlock> footnotes = macroMarkerBlocks.stream()
            .filter(macro -> FootnoteMacro.MACRO_NAME.equals(macro.getId()))
            .collect(Collectors.toList());

        if (!footnotes.isEmpty()) {
            NumberedListBlock container = new NumberedListBlock(Collections.<Block>emptyList());
            container.setParameter(CLASS_ATTRIBUTE_NAME, "footnotes");
            result = Collections.singletonList(container);

            int counter = 1;
            for (MacroMarkerBlock footnote : footnotes) {
                Block footnoteResult = processFootnote(footnote, counter, idGenerator);
                container.addChild(footnoteResult);
                counter++;
            }
        }

        return result;
    }

    /**
     * Processes a {{footnote}} macro, by generating a footnote element to insert in the footnote list and a reference
     * to it, which is placed instead of the macro call.
     *
     * @param footnoteMacro the {{footnote}} macro element
     * @param counter the current footnote counter
     * @return the footnote element which should be inserted in the footnote list
     */
    private ListItemBlock processFootnote(MacroMarkerBlock footnoteMacro, int counter, IdGenerator idGenerator)
    {
        @SuppressWarnings("unchecked")
        List<Block> content = (List<Block>) footnoteMacro.getAttribute(FOOTNOTE_CONTENT_ATTRIBUTE);
        if (content == null) {
            // Copy the list of children as the code will later clear the original children list.
            content = List.copyOf(footnoteMacro.getChildren());
            footnoteMacro.setAttribute(FOOTNOTE_CONTENT_ATTRIBUTE, content);
        }

        String referenceId = generateId(footnoteMacro, counter, idGenerator, FOOTNOTE_REFERENCE_ID_ATTRIBUTE,
            FOOTNOTE_REFERENCE_ID_PREFIX);
        String footnoteId = generateId(footnoteMacro, counter, idGenerator, FOOTNOTE_ID_ATTRIBUTE, FOOTNOTE_ID_PREFIX);

        // Construct the footnote and reference blocks
        Block referenceBlock = createFootnoteReferenceBlock(counter, footnoteId, referenceId);
        ListItemBlock footnoteBlock = createFootnoteBlock(content, footnoteId, referenceId);
        // Insert the footnote and the reference in the document.
        addFootnoteRef(footnoteMacro, referenceBlock);
        return footnoteBlock;
    }

    private String generateId(MacroMarkerBlock footnoteMacro, int counter, IdGenerator idGenerator,
        String attributeName, String idPrefix)
    {
        String footnoteId = (String) footnoteMacro.getAttribute(attributeName);
        if (footnoteId == null) {
            footnoteId = idPrefix + counter;
            if (idGenerator != null) {
                footnoteId = idGenerator.generateUniqueId(footnoteId.substring(0, 1), footnoteId.substring(1));
            }

            footnoteMacro.setAttribute(attributeName, footnoteId);
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
     * actual footnote text, parsed into XDOM.
     *
     * @param content the string representation of the actual footnote text; the content of the macro
     * @param footnoteId the id of the footnote
     * @param referenceId the id of the reference
     * @return the generated footnote block
     */
    private ListItemBlock createFootnoteBlock(List<Block> content, String footnoteId, String referenceId)
    {
        Block result = new WordBlock("^");
        DocumentResourceReference reference = new DocumentResourceReference(null);
        reference.setAnchor(referenceId);
        result = new LinkBlock(Collections.singletonList(result), reference, false);
        result.setParameter(ID_ATTRIBUTE_NAME, footnoteId);
        result.setParameter(CLASS_ATTRIBUTE_NAME, "footnoteBackRef");
        result = new ListItemBlock(Collections.singletonList(result));
        result.addChild(new SpaceBlock());
        result.addChildren(content);
        return (ListItemBlock) result;
    }
}

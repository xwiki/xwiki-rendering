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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.block.match.MacroMarkerBlockMatcher;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.footnote.FootnoteMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Generate footnotes, listed at the end of the page. A reference to the generated footnote is inserted at the location
 * where the macro is called.
 *
 * @version $Id$
 * @since 2.0M2
 */
@Component
@Named(FootnoteMacro.MACRO_NAME)
@Singleton
public class FootnoteMacro extends AbstractMacro<FootnoteMacroParameters>
{
    /**
     * The name of this macro.
     */
    public static final String MACRO_NAME = "footnote";

    /**
     * The priority of the macro.
     */
    public static final int PRIORITY = 2000;

    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Generates a footnote to display at the end of the page.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "the text to place in the footnote";

    /**
     * Matches MacroBlocks having a macro id of {@link PutFootnotesMacro#MACRO_NAME}.
     */
    private static final BlockMatcher PUTFOOTNOTE_MATCHER = new MacroBlockMatcher(PutFootnotesMacro.MACRO_NAME);

    /**
     * Matches MacroMarkerBlocks having a macro id of {@link PutFootnotesMacro#MACRO_NAME}.
     */
    private static final BlockMatcher PUTFOOTNOTE_MARKER_MATCHER =
        new MacroMarkerBlockMatcher(PutFootnotesMacro.MACRO_NAME);

    /**
     * Create and initialize the descriptor of the macro.
     */
    public FootnoteMacro()
    {
        super("Footnote", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION),
            FootnoteMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_CONTENT));

        // The putfootnote macro might already exist in some other macro but to find it the footnote macro need to be
        // executed later
        setPriority(PRIORITY);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    @Override
    public List<Block> execute(FootnoteMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        Block root = context.getXDOM();

        // Only add a putfootnote macro at the end of the document if there's not already one (either already executed
        // or not).
        if (root.getFirstBlock(PUTFOOTNOTE_MATCHER, Block.Axes.DESCENDANT) == null) {
            // Make sure putfootnote is not itself the content of a putfootnote (to avoid an infinite loop)
            if (context.getCurrentMacroBlock().getFirstBlock(PUTFOOTNOTE_MARKER_MATCHER, Block.Axes.ANCESTOR) == null) {
                Block putFootnotesMacro = new MacroBlock(PutFootnotesMacro.MACRO_NAME, Collections.emptyMap(), false);
                root.addChild(putFootnotesMacro);
            }
        }
        return Collections.emptyList();
    }
}

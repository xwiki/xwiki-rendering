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
package org.xwiki.rendering.internal.renderer.xwiki21;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the reference parameters that are specific to XWiki Syntax 2.1 (the query string and the anchor of a
 * document reference) survive a render-parse-render round-trip when they carry the closing marker of the macro they are
 * serialized in. See {@code XWikiSyntaxMacroContentRoundTripTest} of the XWiki Syntax 2.0 module for the carriers that
 * both syntaxes share.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class XWikiSyntaxMacroContentRoundTripTest
{
    private static final String MACRO_NAME = "info";

    private static final String MACRO_CLOSE = "{{/info}}";

    @Inject
    @Named("xwiki/2.1")
    private Parser parser;

    @Inject
    @Named("xwiki/2.1")
    private BlockRenderer renderer;

    private String render(Block block)
    {
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.renderer.render(block, printer);

        return printer.toString();
    }

    /**
     * Renders the passed blocks as they would be stored as the content of the macro, puts that content back inside the
     * macro, parses again and asserts that the macro still holds the same content instead of having been closed early
     * by the injected content.
     */
    private void assertMacroContentRoundTrips(Block... innerBlocks) throws Exception
    {
        String content = render(new XDOM(List.of(innerBlocks)));

        XDOM roundTripped = this.parser
            .parse(new StringReader("{{" + MACRO_NAME + "}}\n" + content + "\n{{/" + MACRO_NAME + "}}"));

        List<MacroBlock> macros = roundTripped.getBlocks(new MacroBlockMatcher(MACRO_NAME), Block.Axes.DESCENDANT);

        assertEquals(1, macros.size(),
            "The [" + MACRO_NAME + "] macro broke apart during the round-trip. Rendered content was [" + content + "]");
        assertEquals(content, render(this.parser.parse(new StringReader(macros.get(0).getContent()))),
            "The macro content changed meaning during the round-trip. Rendered content was [" + content + "]");
    }

    private static ParagraphBlock paragraphWithLink(ResourceReference reference, Map<String, String> parameters)
    {
        return new ParagraphBlock(
            List.of(new LinkBlock(List.of(new WordBlock("label")), reference, false, parameters)));
    }

    @Test
    void queryStringContainingMacroClose() throws Exception
    {
        DocumentResourceReference reference = new DocumentResourceReference("Space.Page");
        reference.setQueryString("parameter=" + MACRO_CLOSE);

        assertMacroContentRoundTrips(paragraphWithLink(reference, Map.of()));
    }

    @Test
    void anchorContainingMacroClose() throws Exception
    {
        DocumentResourceReference reference = new DocumentResourceReference("Space.Page");
        reference.setAnchor(MACRO_CLOSE);

        assertMacroContentRoundTrips(paragraphWithLink(reference, Map.of()));
    }

    @Test
    void linkParameterContainingMacroClose() throws Exception
    {
        assertMacroContentRoundTrips(
            paragraphWithLink(new DocumentResourceReference("Space.Page"), Map.of("class", MACRO_CLOSE)));
    }
}

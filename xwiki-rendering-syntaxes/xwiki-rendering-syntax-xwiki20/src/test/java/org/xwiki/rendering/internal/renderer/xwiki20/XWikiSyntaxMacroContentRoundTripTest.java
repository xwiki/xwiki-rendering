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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.MacroBlockMatcher;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that content produced by the XWiki Syntax 2.0 renderer for the body of a macro survives a
 * parse-render-parse round-trip: leaf values (macro/group parameters, resource references) which happen to contain
 * the {@code {{/macro}}} sequence are escaped so that they cannot prematurely close the enclosing macro. This
 * round-trip is exercised by the WYSIWYG editor (annotated XHTML) and by link refactoring, both of which re-parse and
 * re-render macro content.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class XWikiSyntaxMacroContentRoundTripTest
{
    private Parser parser;

    private BlockRenderer renderer;

    @BeforeEach
    void before(ComponentManager componentManager) throws Exception
    {
        this.parser = componentManager.getInstance(Parser.class, Syntax.XWIKI_2_0.toIdString());
        this.renderer = componentManager.getInstance(BlockRenderer.class, Syntax.XWIKI_2_0.toIdString());
    }

    private String render(Block block)
    {
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.renderer.render(block, printer);

        return printer.toString();
    }

    /**
     * Renders {@code innerSyntax} as it would be stored as the content of a {@code macroName} macro (this is what the
     * WYSIWYG editor and link refactoring do), puts it back inside the macro, parses again, and asserts that the macro
     * still holds the same content instead of having been closed early by the injected content.
     */
    private void assertMacroContentRoundTrips(String macroName, String innerSyntax) throws Exception
    {
        assertRenderedContentRoundTrips(macroName, this.parser.parse(new StringReader(innerSyntax)));
    }

    /**
     * Same as {@link #assertMacroContentRoundTrips(String, String)} for content that cannot be expressed in XWiki
     * Syntax in the first place, like a free-standing reference that needs escaping.
     */
    private void assertMacroContentRoundTrips(String macroName, Block... innerBlocks) throws Exception
    {
        assertRenderedContentRoundTrips(macroName, new XDOM(List.of(innerBlocks)));
    }

    private void assertRenderedContentRoundTrips(String macroName, XDOM innerXDOM) throws Exception
    {
        // What the renderer produces for the inner blocks: the string stored as the macro content.
        String content = render(innerXDOM);

        // Put that content back inside the macro and parse it again, as happens on the next view/save.
        XDOM roundTripped =
            this.parser.parse(new StringReader("{{" + macroName + "}}\n" + content + "\n{{/" + macroName + "}}"));

        List<MacroBlock> macros = roundTripped.getBlocks(new MacroBlockMatcher(macroName), Block.Axes.DESCENDANT);

        assertEquals(1, macros.size(),
            "The [" + macroName + "] macro broke apart during the round-trip. Rendered content was [" + content + "]");
        assertEquals(macroName, macros.get(0).getId());
        // The macro content must still mean the same thing (compare via a normalising render).
        assertEquals(content, render(this.parser.parse(new StringReader(macros.get(0).getContent()))),
            "The macro content changed meaning during the round-trip. Rendered content was [" + content + "]");
    }

    @Test
    void groupParameterContainingMacroClose() throws Exception
    {
        assertMacroContentRoundTrips("info", "(% class=\"{{/info}}\" %)Info content");
    }

    @Test
    void linkReferenceContainingMacroClose() throws Exception
    {
        assertMacroContentRoundTrips("info", "[[label>>{{/info}}]]");
    }

    @Test
    void textContainingMacroCloseInCurlyBracketRun() throws Exception
    {
        // The text is "{{{{{/info}}": a run of curly brackets long enough that escaping only the pairs would leave a
        // "{{" behind.
        assertMacroContentRoundTrips("info", "~{~{~{~{~{/info}}");
    }

    @Test
    void linkParameterContainingMacroClose() throws Exception
    {
        assertMacroContentRoundTrips("info", "[[label>>Space.Page||class=\"{{/info}}\"]]");
    }

    @Test
    void freestandingReferenceContainingMacroClose() throws Exception
    {
        // A free-standing reference cannot be escaped, so the renderer has to fall back to the full link syntax.
        assertMacroContentRoundTrips("info", new ParagraphBlock(List.of(new LinkBlock(List.of(),
            new ResourceReference("http://example.com/{{/info}}", ResourceType.URL), true))));
    }

    @Test
    void freestandingImageReferenceContainingMacroClose() throws Exception
    {
        // The image token of the parser accepts a "{{", so the reference would survive it outside of a macro, but it
        // would still close the macro, hence the fall back to the full image syntax.
        ResourceReference reference = new ResourceReference("http://example.com/{{/info}}.png", ResourceType.URL);
        reference.setTyped(false);

        assertMacroContentRoundTrips("info", new ParagraphBlock(List.of(new ImageBlock(reference, true))));
    }

    @Test
    void idContainingMacroCloseAndQuote() throws Exception
    {
        assertMacroContentRoundTrips("info", new ParagraphBlock(List.of(new IdBlock("anchor\"{{/info}}"))));
    }

    @Test
    void linkLabelContainingMacroKeepsTheMacro() throws Exception
    {
        String innerSyntax = "[[{{id name=\"anchor\"/}}>>Space.Page]]";

        assertMacroContentRoundTrips("info", innerSyntax);

        // The label is passed to the resource renderer as already rendered XWiki Syntax, so a "{{" in it is a real
        // macro and must not be escaped.
        String content = render(this.parser.parse(new StringReader(innerSyntax)));
        assertTrue(content.contains("{{id"), "The macro of the link label was escaped: [" + content + "]");
    }
}

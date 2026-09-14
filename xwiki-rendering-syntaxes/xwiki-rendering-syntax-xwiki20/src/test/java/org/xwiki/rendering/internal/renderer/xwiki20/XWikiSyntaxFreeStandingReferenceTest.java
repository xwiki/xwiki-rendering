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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a free-standing reference containing a "{{" survives a render-parse round-trip. A free-standing
 * reference is printed as-is, without any escaping, so the renderer has to fall back to the full {@code [[...]]}
 * syntax whenever printing the reference free-standing could produce a "{{" in the output, which could close the
 * macro the reference is serialized in.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class XWikiSyntaxFreeStandingReferenceTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    private String render(Block block) throws Exception
    {
        BlockRenderer renderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.XWIKI_2_0.toIdString());
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        renderer.render(block, printer);

        return printer.toString().trim();
    }

    private XDOM parse(String content) throws Exception
    {
        Parser parser = this.componentManager.getInstance(Parser.class, Syntax.XWIKI_2_0.toIdString());

        return parser.parse(new StringReader(content));
    }

    private static ResourceReference untypedURL(String reference)
    {
        ResourceReference result = new ResourceReference(reference, ResourceType.URL);
        result.setTyped(false);

        return result;
    }

    private String assertReferenceRoundTrips(String reference) throws Exception
    {
        String content = render(new XDOM(
            List.of(new ParagraphBlock(List.of(new LinkBlock(List.of(), untypedURL(reference), true))))));

        List<LinkBlock> links =
            parse(content).getBlocks(new ClassBlockMatcher(LinkBlock.class), Block.Axes.DESCENDANT);

        assertEquals(1, links.size(), "The reference didn't round-trip to a single link. Rendered content was ["
            + content + "]");
        assertEquals(reference, links.get(0).getReference().getReference(),
            "The reference changed during the round-trip. Rendered content was [" + content + "]");

        return content;
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // A "{{" closes the macro the reference might be serialized in.
        "http://example.com/{{/info}}",
        "{{macro}}http://example.com/",
        // A run of "{" longer than two must be escaped completely, no "{{" may be left behind.
        "http://example.com/{{{{{x",
        // A leading "{" forms a "{{" with a "{" that has been printed just before the reference.
        "{x://example.com/",
        // Characters that the full syntax escapes must not be lost when the full syntax is forced by the "{{".
        "http://example.com/a~b{{x}}",
        "http://example.com/a]]b{{x}}",
        "http://example.com/a>>b{{x}}",
        "http://example.com/a||b{{x}}"
    })
    void referenceNeedingTheFullSyntax(String reference) throws Exception
    {
        String content = assertReferenceRoundTrips(reference);

        assertTrue(content.startsWith("[[") && content.endsWith("]]"),
            "The reference wasn't printed with the full syntax: [" + content + "]");
    }

    @Test
    void referenceStayingFreeStanding() throws Exception
    {
        // A "~" is not unescaped in a free-standing reference, so it must stay unescaped and must not force the full
        // syntax.
        String reference = "http://example.com/a~b";

        assertEquals(reference, assertReferenceRoundTrips(reference));
    }

    @Test
    void referenceWithASingleCurlyBracketStaysFreeStanding() throws Exception
    {
        // A single "{" cannot close a macro, so it doesn't force the full syntax. That the URI token stops at it and
        // thus truncates the reference when it is parsed again is a limitation which it shares with every other
        // character the URI token doesn't accept (a "}" or a "," for instance).
        String reference = "http://example.com/a{b";

        String content = render(new XDOM(
            List.of(new ParagraphBlock(List.of(new LinkBlock(List.of(), untypedURL(reference), true))))));

        assertEquals(reference, content);
    }
}

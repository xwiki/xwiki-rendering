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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ComponentTest
@AllComponents
class XWikiSyntaxBlockRendererTest
{
    @Inject
    @Named("xwiki/2.1")
    private BlockRenderer renderer;

    private String render(Block block)
    {
        DefaultWikiPrinter wikiPrinter = new DefaultWikiPrinter();
        this.renderer.render(block, wikiPrinter);

        return wikiPrinter.toString();
    }

    // Tests

    @Test
    void inline()
    {
        assertEquals("word", render(new WordBlock("word")));
    }

    /**
     * Proves that a link block with a query string parameter, located inside a Group Block, correctly renders the
     * query string parameter.
     */
    @Test
    void renderLinkWithQueryStringParameterInsideGroupBlock()
    {
        DocumentResourceReference reference = new DocumentResourceReference("reference");
        reference.setQueryString("a=b");
        Block block = new GroupBlock(List.of(new LinkBlock(
            List.of(new WordBlock("label")), reference, false)));

        String expected = """
            (((
            [[label>>doc:reference||queryString="a=b"]]
            )))""";

        assertEquals(expected, render(block));
    }
}

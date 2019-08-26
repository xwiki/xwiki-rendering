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
package org.xwiki.rendering.test;

import java.util.List;

import org.junit.Assert;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Class to be imported in unit tests as a static import and which contains helper methods to assert Rendering Blocks.
 *
 * @version $Id$
 * @since 2.4M2
 * @deprecated starting with 11.6RC1 use {@link org.xwiki.rendering.test.integration.junit5.BlockAssert} instead
 */
// TODO: Remove class once all tests have moved to using the JUnit5 version
@Deprecated
public class BlockAssert
{
    /**
     * Protect constructor since it is a static only class.
     */
    protected BlockAssert()
    {
        // Nothing to do
    }

    /**
     * @param blocks the Blocks to assert
     * @param factory the Renderer Factory to use to serialize the passed Block
     * @return The serialized block.
     * @since 4.2M1
     */
    private static String render(List<Block> blocks, PrintRendererFactory factory)
    {
        // Assert the result by parsing it through the EventsRenderer to generate easily
        // assertable events.
        XDOM dom = new XDOM(blocks);
        WikiPrinter printer = new DefaultWikiPrinter();

        PrintRenderer eventRenderer = factory.createRenderer(printer);

        dom.traverse(eventRenderer);

        return printer.toString();
    }

    /**
     * @param expected the expected value of passed Blocks when rendered using the passed Renderer Factory
     * @param blocks the Blocks to assert
     * @param factory the Renderer Factory to use to serialize the passed Block and to compare them with the passed
     *            String
     */
    public static void assertBlocks(String expected, List<Block> blocks, PrintRendererFactory factory)
    {
        Assert.assertEquals(expected, render(blocks, factory));
    }

    /**
     * @param expectedPrefix the expected prefix of the passed Blocks when rendered using the passed Renderer Factory
     * @param blocks the Blocks to assert
     * @param factory the Renderer Factory to use to serialize the passed Block and to compare them with the passed
     *            String
     * @since 4.2M1
     */
    public static void assertBlocksStartsWith(String expectedPrefix, List<Block> blocks, PrintRendererFactory factory)
    {
        Assert.assertTrue(render(blocks, factory).startsWith(expectedPrefix));
    }
}

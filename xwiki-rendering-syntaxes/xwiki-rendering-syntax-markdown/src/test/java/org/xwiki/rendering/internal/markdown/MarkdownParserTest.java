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
package org.xwiki.rendering.internal.markdown;

import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.parser.markdown.MarkdownParser}.
 *
 * @version $Id$
 * @since 3.4M1
 */
public class MarkdownParserTest extends AbstractComponentTestCase
{
    @Test
    public void parseMarkdownExample() throws Exception
    {
        Parser parser = getComponentManager().getInstance(Parser.class, "markdown/1.0");
        XDOM xdom = parser.parse(new InputStreamReader(getClass().getResourceAsStream("/markdown/example.md")));

        BlockRenderer renderer = getComponentManager().getInstance(BlockRenderer.class, "event/1.0");
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        renderer.render(xdom, printer);

        // Read expected content and remove license header for comparison.
        String expected = IOUtils.toString(getClass().getResourceAsStream("/markdown/expected.txt"));
        Pattern pattern = Pattern.compile("====.*====\n\n", Pattern.DOTALL);
        expected = pattern.matcher(expected).replaceFirst("");
        
        Assert.assertEquals(expected, printer.toString());
    }
}

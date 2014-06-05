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
package org.xwiki.rendering.internal.transformation.autotoc;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.AllComponents;
import org.junit.Assert;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.transformation.autotoc.AutoTOCTransformation}.
 *
 * @version $Id$
 * @since 6.1M2
 */
@AllComponents
public class AutoTOCTransformationTest
{
    @Rule
    public ComponentManagerRule componentManager = new ComponentManagerRule();

    private Transformation autoTOCTransformation;

    @Before
    public void setUp() throws Exception
    {
        this.autoTOCTransformation = this.componentManager.getInstance(Transformation.class, "autotoc");
    }

    @Test
    public void testTransformation() throws Exception
    {
        String content = "Some content without toc\n= heading 1=\nother content";

        Parser parser = this.componentManager.getInstance(Parser.class, "xwiki/2.1");
        XDOM xdom = parser.parse(new StringReader(content));
        this.autoTOCTransformation.transform(xdom, new TransformationContext());
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer xwiki21BlockRenderer = this.componentManager.getInstance(BlockRenderer.class, "xwiki/2.1");
        xwiki21BlockRenderer.render(xdom, printer);
        Assert.assertEquals("{{toc/}}\n\nSome content without toc\n\n= heading 1 =\n\nother content",
            printer.toString());
    }
}

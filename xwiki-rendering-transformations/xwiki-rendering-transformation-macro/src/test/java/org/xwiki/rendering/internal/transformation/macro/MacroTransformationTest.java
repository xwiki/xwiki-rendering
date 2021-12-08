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
package org.xwiki.rendering.internal.transformation.macro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.AllComponents;

/**
 * Unit tests for {@link MacroTransformation}.
 *
 * @version $Id$
 */
@AllComponents
public class MacroTransformationTest
{
    @Rule
    public final ComponentManagerRule componentManager = new ComponentManagerRule();

    private MacroTransformation transformation;

    @Before
    public void setUp() throws Exception
    {
        this.transformation = this.componentManager.getInstance(Transformation.class, "macro");
    }

    /**
     * Test that a simple macro is correctly evaluated.
     */
    @Test
    public void transformSimpleMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testsimplemacro",
            Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Test that a macro can generate another macro.
     */
    @Test
    public void transformNestedMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testnestedmacro] []\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endMacroMarkerStandalone [testnestedmacro] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testnestedmacro",
            Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Test that we have a safeguard against infinite recursive macros.
     */
    @Test
    public void transformMacroWithInfiniteRecursion() throws Exception
    {
        String expected = "beginDocument\n"
            + StringUtils.repeat("beginMacroMarkerStandalone [testrecursivemacro] []\n", 5)
            + "onMacroStandalone [testrecursivemacro] []\n"
            + StringUtils.repeat("endMacroMarkerStandalone [testrecursivemacro] []\n", 5)
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testrecursivemacro",
            Collections.<String, String>emptyMap(), false)));

        // In order to have a fast test, set the max macro execution to 5 macros.
        this.transformation.setMaxRecursions(4);

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        Assert.assertEquals(expected, printer.toString());
    }

    @Test
    public void transformWhenLotsOfMacrosButNoInfiniteRecursion() throws Exception
    {
        String expected = "beginDocument\n";
        for (int i = 0; i < 10; i++) {
            expected += "beginMacroMarkerStandalone [testsimplemacro] []\n"
                + "beginParagraph\n"
                + "onWord [simplemacro" + i + "]\n"
                + "endParagraph\n"
                + "endMacroMarkerStandalone [testsimplemacro] []\n";
        }
        expected += "endDocument";

        List<Block> macroBlocks = new ArrayList<Block>();
        for (int i = 0; i < 10; i++) {
            macroBlocks.add(new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false));
        }
        XDOM dom = new XDOM(macroBlocks);

        // Make sure that the max macro execution is less than the # of macros we have in the content to prove that
        // we only stop on real recursion.
        this.transformation.setMaxRecursions(5);

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Test that macro priorities are working.
     */
    @Test
    public void transformMacrosWithPriorities() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro1]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginMacroMarkerStandalone [testprioritymacro] []\n"
            + "beginParagraph\n"
            + "onWord [word]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testprioritymacro] []\n"
            + "endDocument";

        // "testprioritymacro" has a highest priority than "testsimplemacro" and will be executed first.
        // This is verified as follows:
        // - "testprioritymacro" generates a WordBlock
        // - "testsimplemacro" outputs "simplemacro" followed by the number of WordBlocks that exist in the document
        // Thus if "testsimplemacro" is executed before "testprioritymacro" it would print "simplemacro0"
        XDOM dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false),
            new MacroBlock("testprioritymacro", Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Test that macro with same priorities execute in the order in which they are defined.
     */
    @Test
    public void macroWithSamePriorityExecuteOnPageOrder() throws Exception
    {
        // Both macros have the same priorities and thus "testsimplemacro" should be executed first and generate
        // "simplemacro0".
        XDOM dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false),
            new MacroBlock("testcontentmacro", Collections.<String, String>emptyMap(), "content", false)));

        TransformationContext context = new TransformationContext(dom, Syntax.XWIKI_2_0);
        this.transformation.transform(dom, context);

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);

        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "onWord [content]\n"
            + "endMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "endDocument";
        Assert.assertEquals(expected, printer.toString());

        // We must also test the other order ("testcontentmacro" before "testsimplemacro") to ensure for example that
        // there's no lexical order on Macro class names for example.
        dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testcontentmacro", Collections.<String, String>emptyMap(), "content", false),
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false)));

        context.setXDOM(dom);
        this.transformation.transform(dom, context);

        printer = new DefaultWikiPrinter();
        eventBlockRenderer.render(dom, printer);

        expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "onWord [content]\n"
            + "endMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro1]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endDocument";
        Assert.assertEquals(expected, printer.toString());
    }

    /**
     * Test that a not existing macro generate an error in the XDOM.
     */
    @Test
    public void transformNotExistingMacro() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [notexisting] []\n"
            + "beginGroup [[class]=[xwikirenderingerror]]\n"
            + "onWord [Unknown macro: notexisting. Click on this message for details.]\n"
            + "endGroup [[class]=[xwikirenderingerror]]\n"
            + "beginGroup [[class]=[xwikirenderingerrordescription hidden]]\n"
            + "onVerbatim [The [notexisting] macro is not in the list of registered macros. "
                + "Verify the spelling or contact your administrator.] [false]\n"
            + "endGroup [[class]=[xwikirenderingerrordescription hidden]]\n"
            + "endMacroMarkerStandalone [notexisting] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("notexisting",
            Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new TransformationContext(dom, Syntax.XWIKI_2_0));

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer =
            this.componentManager.getInstance(BlockRenderer.class, Syntax.EVENT_1_0.toIdString());
        eventBlockRenderer.render(dom, printer);
        Assert.assertEquals(expected, printer.toString());
    }
}

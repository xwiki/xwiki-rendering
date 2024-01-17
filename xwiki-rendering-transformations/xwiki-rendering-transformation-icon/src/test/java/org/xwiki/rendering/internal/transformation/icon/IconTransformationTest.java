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
package org.xwiki.rendering.internal.transformation.icon;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.transformation.icon.IconTransformation}.
 *
 * @version $Id$
 * @since 2.6RC1
 */
@ComponentTest
@AllComponents
class IconTransformationTest
{
    @InjectMockComponents
    private IconTransformation transformation;

    @InjectComponentManager
    private ComponentManager componentManager;

    @Test
    void transform() throws Exception
    {
        String expected = "beginDocument [[syntax]=[XWiki 2.1]]\n"
            + "beginParagraph\n"
            + "onWord [Some]\n"
            + "onSpace\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [emoticon_smile]] [true]\n"
            + "onSpace\n"
            + "onWord [smileys]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [emoticon_unhappy]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [emoticon_tongue]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [emoticon_grin]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [emoticon_wink]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [thumb_up]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [thumb_down]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [information]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [accept]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [cancel]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [error]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [add]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [delete]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [help]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [lightbulb]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [lightbulb_off]] [true]\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [star]] [true]\n"
            + "endParagraph\n"
            + "endDocument [[syntax]=[XWiki 2.1]]";

        Parser parser = this.componentManager.getInstance(Parser.class, "xwiki/2.1");
        XDOM xdom = parser.parse(new StringReader("Some :) smileys:(:P:D;)(y)(n)(i)(/)(x)(!)(+)(-)(?)(on)(off)(*)"));
        this.transformation.transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer = this.componentManager.getInstance(BlockRenderer.class, "event/1.0");
        eventBlockRenderer.render(xdom, printer);
        assertEquals(expected, printer.toString());
    }

    @Test
    void transformIgnoresProtectedContent() throws Exception
    {
        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [code] []\n"
            + "onSpecialSymbol [:]\n"
            + "onSpecialSymbol [)]\n"
            + "endMacroMarkerStandalone [code] []\n"
            + "endDocument";

        XDOM xdom = new XDOM(Arrays.asList((Block) new MacroMarkerBlock("code", Collections.emptyMap(),
            Arrays.asList(new SpecialSymbolBlock(':'), new SpecialSymbolBlock(')')), false)));
        this.transformation.transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer = this.componentManager.getInstance(BlockRenderer.class, "event/1.0");
        eventBlockRenderer.render(xdom, printer);
        assertEquals(expected, printer.toString());
    }

    /**
     * Fixes XWIKI-5729.
     */
    @Test
    void transformWhenIncompleteMatchExistsFollowedByMatch() throws Exception
    {
        String expected = "beginDocument [[syntax]=[XWiki 2.1]]\n"
            + "beginParagraph\n"
            + "onSpecialSymbol [(]\n"
            + "onSpace\n"
            + "onImage [Typed = [true] Type = [icon] Reference = [information]] [true]\n"
            + "endParagraph\n"
            + "endDocument [[syntax]=[XWiki 2.1]]";

        Parser parser = this.componentManager.getInstance(Parser.class, "xwiki/2.1");
        XDOM xdom = parser.parse(new StringReader("( (i)"));
        this.transformation.transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer eventBlockRenderer = this.componentManager.getInstance(BlockRenderer.class, "event/1.0");
        eventBlockRenderer.render(xdom, printer);
        assertEquals(expected, printer.toString());
    }
}

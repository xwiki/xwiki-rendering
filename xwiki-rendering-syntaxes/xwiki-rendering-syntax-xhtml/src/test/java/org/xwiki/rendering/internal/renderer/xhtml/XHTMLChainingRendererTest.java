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
package org.xwiki.rendering.internal.renderer.xhtml;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link XHTMLChainingRenderer} for methods that cannot be easily tested using the Rendering Test
 * framework.
 *
 * @version $Id$
 */
class XHTMLChainingRendererTest
{
    /**
     * Those events are hard to test since there's no easy syntax to input them (requires XWiki 2.0+ Syntax with a
     * Macro transformation applied).
     */
    @Test
    void outputFigureCaptionEvents()
    {
        XHTMLChainingRenderer renderer = new XHTMLChainingRenderer(null, null, null, new ListenerChain());
        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        renderer.setPrinter(wikiPrinter);
        renderer.beginFigureCaption(Collections.emptyMap());
        renderer.onWord("caption");
        renderer.endFigureCaption(Collections.emptyMap());

        assertEquals("<div class=\"figcaption\">caption</div>", wikiPrinter.toString());
    }

    @Test
    void onRawText()
    {
        XHTMLChainingRenderer renderer = new XHTMLChainingRenderer(null, null, null, new ListenerChain());
        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        renderer.setPrinter(wikiPrinter);
        renderer.onRawText("xhtml/1.0", Syntax.XHTML_1_0);
        renderer.onRawText("html/4.01", Syntax.HTML_4_01);
        renderer.onRawText("html/5.0", Syntax.HTML_5_0);
        renderer.onRawText("annotatedxhtml/1.0", Syntax.ANNOTATED_XHTML_1_0);
        renderer.onRawText("annotatedhtml/5.0", Syntax.ANNOTATED_HTML_5_0);
        renderer.onRawText("plain/1.0", Syntax.PLAIN_1_0);

        assertEquals("xhtml/1.0html/4.01html/5.0annotatedxhtml/1.0annotatedhtml/5.0", wikiPrinter.toString());
    }
}
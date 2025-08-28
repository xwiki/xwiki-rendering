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
package org.xwiki.rendering.internal.html5;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.internal.renderer.html5.HTML5ChainingRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link org.xwiki.rendering.internal.renderer.html5.HTML5ChainingRenderer}.
 *  
 * @version $Id$
 * @since 7.1M1
 */
class HTML5ChainingRendererTest
{
    private HTML5ChainingRenderer chainingRenderer;

    private DefaultWikiPrinter printer;

    @BeforeEach
    void setUp()
    {
        XHTMLLinkRenderer linkRenderer = mock(XHTMLLinkRenderer.class);
        XHTMLImageRenderer imageRenderer = mock(XHTMLImageRenderer.class);
        ListenerChain listenerChain = new ListenerChain();
        this.chainingRenderer = new HTML5ChainingRenderer(linkRenderer, imageRenderer, null, listenerChain);
        this.printer = new DefaultWikiPrinter();
        this.chainingRenderer.setPrinter(this.printer);
    }
    
    @Test
    void withoutMonospace()
    {   
        Map<String, String> parameters = new HashMap<>();
        this.chainingRenderer.beginFormat(Format.BOLD, parameters);
        this.chainingRenderer.onWord("hello");
        this.chainingRenderer.endFormat(Format.BOLD, parameters);
        assertEquals("<strong>hello</strong>", this.printer.toString());
    }

    @Test
    void withMonospace()
    {
        Map<String, String> parameters = new HashMap<>();
        this.chainingRenderer.beginFormat(Format.MONOSPACE, parameters);
        this.chainingRenderer.onWord("hello");
        this.chainingRenderer.endFormat(Format.MONOSPACE, parameters);
        assertEquals("<span class=\"monospace\">hello</span>", this.printer.toString());
    }

    @Test
    void withMonospaceAndParameters()
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("class", "myClass");
        this.chainingRenderer.beginFormat(Format.MONOSPACE, parameters);
        this.chainingRenderer.onWord("hello");
        this.chainingRenderer.endFormat(Format.MONOSPACE, parameters);
        assertEquals("<span class=\"monospace myClass\">hello</span>", this.printer.toString());
    }
}

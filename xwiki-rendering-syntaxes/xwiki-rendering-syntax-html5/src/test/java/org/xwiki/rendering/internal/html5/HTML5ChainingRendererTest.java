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

import org.junit.Before;
import org.junit.Test;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.internal.renderer.html5.HTML5ChainingRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link org.xwiki.rendering.internal.renderer.html5.HTML5ChainingRenderer}.
 *  
 * @version $Id$
 * @since 7.1M1
 */
public class HTML5ChainingRendererTest
{
    private XHTMLLinkRenderer linkRenderer;
    
    private XHTMLImageRenderer imageRenderer;
    
    private ListenerChain listenerChain;
    
    private HTML5ChainingRenderer chainingRenderer;
    
    private DefaultWikiPrinter printer;

    @Before
    public void setUp() throws Exception
    {
        linkRenderer = mock(XHTMLLinkRenderer.class);
        imageRenderer = mock(XHTMLImageRenderer.class);
        listenerChain = new ListenerChain();
        chainingRenderer = new HTML5ChainingRenderer(linkRenderer, imageRenderer, listenerChain);
        printer = new DefaultWikiPrinter();
        chainingRenderer.setPrinter(printer);
    }
    
    @Test
    public void testWithoutMonospace() throws Exception
    {   
        Map<String, String> parameters = new HashMap<>();
        chainingRenderer.beginFormat(Format.BOLD, parameters);
        chainingRenderer.onWord("hello");
        chainingRenderer.endFormat(Format.BOLD, parameters);
        assertEquals("<strong>hello</strong>", printer.toString());
    }

    @Test
    public void testWithMonospace() throws Exception
    {
        Map<String, String> parameters = new HashMap<>();
        chainingRenderer.beginFormat(Format.MONOSPACE, parameters);
        chainingRenderer.onWord("hello");
        chainingRenderer.endFormat(Format.MONOSPACE, parameters);
        assertEquals("<span class=\"monospace\">hello</span>", printer.toString());
    }

    @Test
    public void testWithMonospaceAndParameters() throws Exception
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("class", "myClass");
        chainingRenderer.beginFormat(Format.MONOSPACE, parameters);
        chainingRenderer.onWord("hello");
        chainingRenderer.endFormat(Format.MONOSPACE, parameters);
        assertEquals("<span class=\"monospace myClass\">hello</span>", printer.toString());
    }
    
}

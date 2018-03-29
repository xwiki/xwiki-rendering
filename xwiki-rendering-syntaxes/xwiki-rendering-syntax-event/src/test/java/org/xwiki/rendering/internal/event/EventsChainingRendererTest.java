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
package org.xwiki.rendering.internal.event;

import java.util.Collections;

import org.junit.Test;
import org.xwiki.rendering.internal.renderer.event.EventsChainingRenderer;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link EventsChainingRenderer} for methods that cannot be easily tested using the Rendering Test
 * framework.
 *
 * @version $Id$
 * @since 10.3RC1
 */
public class EventsChainingRendererTest
{
    /**
     * Those events are hard to test since there's no easy syntax to input them (requires XWiki 2.0+ Syntax with a
     * Macro transformation applied).
     */
    @Test
    public void outputFigureEvents() throws Exception
    {
        EventsChainingRenderer renderer = new EventsChainingRenderer(new ListenerChain());
        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        renderer.setPrinter(wikiPrinter);
        renderer.beginFigure(Collections.emptyMap());
        renderer.beginFigureCaption(Collections.emptyMap());
        renderer.endFigureCaption(Collections.emptyMap());
        renderer.endFigure(Collections.emptyMap());

        String expected = "beginFigure\n"
            + "beginFigureCaption\n"
            + "endFigureCaption\n"
            + "endFigure\n";

        assertEquals(expected, wikiPrinter.toString());
    }
}

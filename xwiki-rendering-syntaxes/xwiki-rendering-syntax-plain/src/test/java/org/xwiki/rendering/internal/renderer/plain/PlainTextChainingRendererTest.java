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
package org.xwiki.rendering.internal.renderer.plain;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link PlainTextChainingRenderer} that cannot easily be performed using the Rendering Test framework.
 *
 * @version $Id$
 * @since 2.1M1
 */
class PlainTextChainingRendererTest
{
    private PlainTextRenderer renderer;

    @BeforeEach
    public void setUp() throws Exception
    {
        // Force the link label generator to be null
        this.renderer = new PlainTextRenderer();
        this.renderer.initialize();
    }

    @Test
    void beginLinkWhenLinkLabelGeneratorIsNull()
    {
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.renderer.setPrinter(printer);

        DocumentResourceReference reference = new DocumentResourceReference("reference");
        reference.setAnchor("anchor");
        reference.setQueryString("param=value");

        this.renderer.beginLink(reference, false, Collections.emptyMap());
        this.renderer.endLink(reference, false, Collections.emptyMap());

        assertEquals("reference", printer.toString());
    }

    @Test
    void beginLinkWhenExternalLink() throws Exception
    {
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.renderer.setPrinter(printer);

        ResourceReference reference = new ResourceReference("http://some/url", ResourceType.URL);
        this.renderer.beginLink(reference, false, Collections.emptyMap());
        this.renderer.endLink(reference, false, Collections.emptyMap());

        assertEquals("http://some/url", printer.toString());
    }

    @Test
    void rawBlock()
    {
        DefaultWikiPrinter printer = new DefaultWikiPrinter();
        this.renderer.setPrinter(printer);

        this.renderer.onRawText("raw content", Syntax.PLAIN_1_0);

        assertEquals("raw content", printer.toString());
    }
}

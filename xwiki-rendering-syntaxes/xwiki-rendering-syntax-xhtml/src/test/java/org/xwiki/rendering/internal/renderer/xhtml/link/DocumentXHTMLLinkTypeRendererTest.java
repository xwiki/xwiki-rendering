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
package org.xwiki.rendering.internal.renderer.xhtml.link;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.rendering.renderer.reference.link.WantedLinkTitleGenerator;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentXHTMLLinkTypeRenderer}.
 *
 * @version $Id$
 */
@ComponentTest
class DocumentXHTMLLinkTypeRendererTest
{
    @InjectMockComponents
    private DocumentXHTMLLinkTypeRenderer renderer;

    @MockComponent
    private WikiModel wikiModel;

    @MockComponent
    private LinkLabelGenerator linkLabelGenerator;

    @MockComponent
    private WantedLinkTitleGenerator defaultTitleGenerator;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private DefaultWikiPrinter printer;

    @BeforeEach
    void setUp()
    {
        this.printer = new DefaultWikiPrinter();
        this.renderer.setXHTMLWikiPrinter(new XHTMLWikiPrinter(this.printer));
    }

    @Test
    void beginLinkOnExistingDocumentDoesNotAddATitle()
    {
        ResourceReference reference = new DocumentResourceReference("Space.ExistingPage");
        when(this.wikiModel.isDocumentAvailable(reference)).thenReturn(true);
        when(this.wikiModel.getDocumentViewURL(reference)).thenReturn("/view/Space/ExistingPage");

        this.renderer.beginLink(reference, false, Collections.emptyMap());

        assertEquals("<span class=\"wikilink\"><a href=\"/view/Space/ExistingPage\">", this.printer.toString());
        verifyNoInteractions(this.defaultTitleGenerator);
    }

    @Test
    void beginLinkOnWantedLinkFallsBackOnTheDefaultTitleGenerator()
    {
        ResourceReference reference = new DocumentResourceReference("Space.WantedPage");
        when(this.wikiModel.isDocumentAvailable(reference)).thenReturn(false);
        when(this.wikiModel.getDocumentEditURL(reference)).thenReturn("/edit/Space/WantedPage");
        when(this.defaultTitleGenerator.generateWantedLinkTitle(reference)).thenReturn("Create resource: WantedPage");

        this.renderer.beginLink(reference, false, Collections.emptyMap());

        assertEquals("<span class=\"wikicreatelink\" title=\"Create resource: WantedPage\">"
            + "<a href=\"/edit/Space/WantedPage\">", this.printer.toString());
    }

    @Test
    void beginLinkOnWantedLinkUsesTheGeneratorMatchingTheReferenceScheme() throws Exception
    {
        WantedLinkTitleGenerator documentTitleGenerator =
            this.componentManager.registerMockComponent(WantedLinkTitleGenerator.class, "doc");

        ResourceReference reference = new DocumentResourceReference("Space.WantedPage");
        when(this.wikiModel.isDocumentAvailable(reference)).thenReturn(false);
        when(this.wikiModel.getDocumentEditURL(reference)).thenReturn("/edit/Space/WantedPage");
        when(documentTitleGenerator.generateWantedLinkTitle(reference)).thenReturn("Créer la page WantedPage");

        this.renderer.beginLink(reference, false, Collections.emptyMap());

        assertEquals("<span class=\"wikicreatelink\" title=\"Créer la page WantedPage\">"
            + "<a href=\"/edit/Space/WantedPage\">", this.printer.toString());
        verifyNoInteractions(this.defaultTitleGenerator);
    }
}

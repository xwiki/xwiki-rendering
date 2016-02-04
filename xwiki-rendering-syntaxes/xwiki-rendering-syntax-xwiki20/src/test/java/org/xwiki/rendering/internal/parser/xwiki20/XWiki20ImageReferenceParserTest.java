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
package org.xwiki.rendering.internal.parser.xwiki20;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.rendering.internal.parser.reference.type.AttachmentResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.URLResourceReferenceTypeParser;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XWiki20ImageReferenceParser}.
 *
 * @version $Id$
 * @since 2.5RC1
 */
//@formatter:off
@ComponentList({
    XWiki20ImageReferenceParser.class,
    URLResourceReferenceTypeParser.class,
    AttachmentResourceReferenceTypeParser.class
})
//@formatter:on
public class XWiki20ImageReferenceParserTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private ResourceReferenceParser parser;

    @BeforeComponent
    public void setUpComponents() throws Exception
    {
        // Create a Mock WikiModel implementation so that the image parser works in wiki mode
        this.componentManager.registerMockComponent(WikiModel.class);

        Provider<ComponentManager> contextComponentManagerProvider = this.componentManager.registerMockComponent(
            new DefaultParameterizedType(null, Provider.class, ComponentManager.class), "context");
        when(contextComponentManagerProvider.get()).thenReturn(this.componentManager);
    }

    @Before
    public void setUp() throws Exception
    {
        this.parser = this.componentManager.getInstance(ResourceReferenceParser.class, "xwiki/2.0/image");
    }

    @Test
    public void testParseImagesCommon() throws Exception
    {
        // Verify that non-typed image referencing an attachment works.
        ResourceReference reference = this.parser.parse("wiki:space.page@filename");
        assertEquals(ResourceType.ATTACHMENT, reference.getType());
        assertEquals("wiki:space.page@filename", reference.getReference());
        assertEquals("Typed = [false] Type = [attach] Reference = [wiki:space.page@filename]",
            reference.toString());
        assertFalse(reference.isTyped());

        // Verify that non-typed image referencing a URL works.
        reference = this.parser.parse("http://server/path/to/image");
        assertEquals(ResourceType.URL, reference.getType());
        assertEquals("http://server/path/to/image", reference.getReference());
        assertEquals("Typed = [false] Type = [url] Reference = [http://server/path/to/image]",
            reference.toString());
        assertFalse(reference.isTyped());

    }

    @Test
    public void testParseImages() throws Exception
    {
        // Verify that "attach:" prefix isn't taken into account in XWiki Syntax 2.0.
        ResourceReference reference = this.parser.parse("attach:wiki:space.page@filename");
        assertEquals(ResourceType.ATTACHMENT, reference.getType());
        assertEquals("attach:wiki:space.page@filename", reference.getReference());
        assertFalse(reference.isTyped());
        assertEquals("Typed = [false] Type = [attach] Reference = [attach:wiki:space.page@filename]",
            reference.toString());
    }
}

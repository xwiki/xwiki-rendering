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
package org.xwiki.rendering.internal.parser.reference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultImageReferenceParser}.
 *
 * @version $Id$
 * @since 2.6M1
 */
@ComponentTest
@AllComponents
public class DefaultImageReferenceParserTest
{
    private ResourceReferenceParser parser;

    @InjectComponentManager
    private MockitoComponentManager componentManager;
    
    @BeforeEach
    protected void registerComponents() throws Exception
    {
        // Create a Mock WikiModel implementation so that the link parser works in wiki mode
        this.componentManager.registerMockComponent(WikiModel.class);

        this.parser = this.componentManager.getInstance(ResourceReferenceParser.class, "image");
    }

    @Test
    public void parseImagesCommon()
    {
        // Verify that non-typed image referencing an attachment works.
        ResourceReference reference = this.parser.parse("wiki:space.page@filename");
        assertEquals(ResourceType.ATTACHMENT, reference.getType());
        assertEquals("wiki:space.page@filename", reference.getReference());
        assertEquals("Typed = [false] Type = [attach] Reference = [wiki:space.page@filename]",
            reference.toString());
        assertFalse(reference.isTyped());

        // Verify that non-typed image referencing a URL works.
        reference = this.parser.parse("scheme://server/path/to/image");
        assertEquals(ResourceType.URL, reference.getType());
        assertEquals("scheme://server/path/to/image", reference.getReference());
        assertEquals("Typed = [false] Type = [url] Reference = [scheme://server/path/to/image]",
            reference.toString());
        assertFalse(reference.isTyped());
    }

    @Test
    public void parseImages()
    {
        ResourceReference reference = this.parser.parse("attach:wiki:space.page@filename");
        assertEquals(ResourceType.ATTACHMENT, reference.getType());
        assertEquals("wiki:space.page@filename", reference.getReference());
        assertTrue(reference.isTyped());
        assertEquals("Typed = [true] Type = [attach] Reference = [wiki:space.page@filename]",
            reference.toString());

        // Verify path: support
        reference = this.parser.parse("path:/some/image");
        assertEquals(ResourceType.PATH, reference.getType());
        assertEquals("/some/image", reference.getReference());
        assertTrue(reference.isTyped());
        assertEquals("Typed = [true] Type = [path] Reference = [/some/image]", reference.toString());

        // Verify icon: support
        reference = this.parser.parse("icon:name");
        assertEquals(ResourceType.ICON, reference.getType());
        assertEquals("name", reference.getReference());
        assertTrue(reference.isTyped());
        assertEquals("Typed = [true] Type = [icon] Reference = [name]", reference.toString());
    }
}

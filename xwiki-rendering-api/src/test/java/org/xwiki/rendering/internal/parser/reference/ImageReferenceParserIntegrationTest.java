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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

/**
 * Integration tests for {@link AbstractImageReferenceParser} and all classes it uses.
 *
 * @version $Id$
 * @since 5.1M1
 */
@AllComponents
public class ImageReferenceParserIntegrationTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private ResourceReferenceParser imageParser;

    private TestableAbstractDefaultResourceReferenceParser defaultParser;

    @Before
    public void setUp() throws Exception
    {
        this.defaultParser = this.componentManager.getInstance(ResourceReferenceParser.class, "default/test");
        this.defaultParser.setSupportedTypes(Arrays.asList(
            "attach", "path", "icon"));
        TestableAbstractImageReferenceParser imageParser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "image/test");
        imageParser.setResourceReferenceParser(this.defaultParser);
        this.imageParser = imageParser;

        // Create a Mock WikiModel implementation so that the link parser works in wiki mode
        this.componentManager.registerMockComponent(WikiModel.class);
    }

    @Test
    public void testParseImagesCommon() throws Exception
    {
        // Verify that non-typed image referencing an attachment works.
        ResourceReference reference = this.imageParser.parse("wiki:space.page@filename");
        Assert.assertEquals(ResourceType.ATTACHMENT, reference.getType());
        Assert.assertEquals("wiki:space.page@filename", reference.getReference());
        Assert.assertEquals("Typed = [false] Type = [attach] Reference = [wiki:space.page@filename]",
            reference.toString());
        Assert.assertFalse(reference.isTyped());

        // Verify that non-typed image referencing a URL works.
        reference = this.imageParser.parse("scheme://server/path/to/image");
        Assert.assertEquals(ResourceType.URL, reference.getType());
        Assert.assertEquals("scheme://server/path/to/image", reference.getReference());
        Assert.assertEquals("Typed = [false] Type = [url] Reference = [scheme://server/path/to/image]",
            reference.toString());
        Assert.assertFalse(reference.isTyped());
    }

    @Test
    public void testParseImages() throws Exception
    {
        ResourceReference reference = this.imageParser.parse("attach:wiki:space.page@filename");
        Assert.assertEquals(ResourceType.ATTACHMENT, reference.getType());
        Assert.assertEquals("wiki:space.page@filename", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("Typed = [true] Type = [attach] Reference = [wiki:space.page@filename]",
            reference.toString());

        // Verify path: support
        reference = this.imageParser.parse("path:/some/image");
        Assert.assertEquals(ResourceType.PATH, reference.getType());
        Assert.assertEquals("/some/image", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("Typed = [true] Type = [path] Reference = [/some/image]", reference.toString());

        // Verify icon: support
        reference = this.imageParser.parse("icon:name");
        Assert.assertEquals(ResourceType.ICON, reference.getType());
        Assert.assertEquals("name", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("Typed = [true] Type = [icon] Reference = [name]", reference.toString());
    }
}

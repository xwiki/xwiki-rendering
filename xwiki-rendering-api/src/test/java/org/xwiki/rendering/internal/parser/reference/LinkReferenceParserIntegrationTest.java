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
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.InterWikiResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

/**
 * Integration tests for {@link AbstractLinkReferenceParser} and all classes it uses.
 *
 * @version $Id$
 * @since 5.1M1
 */
@AllComponents
public class LinkReferenceParserIntegrationTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private ResourceReferenceParser linkParser;

    private TestableAbstractDefaultResourceReferenceParser defaultParser;

    @Before
    public void setUp() throws Exception
    {
        this.defaultParser = this.componentManager.getInstance(ResourceReferenceParser.class, "default/test");
        this.defaultParser.setSupportedTypes(Arrays.asList(
            "url", "unc", "path", "mailto", "interwiki", "doc", "attach"));
        TestableAbstractLinkReferenceParser linkParser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "link/test");
        linkParser.setResourceReferenceParser(this.defaultParser);
        this.linkParser = linkParser;
    }

    @Test
    public void testParseWhenInWikiMode() throws Exception
    {
        // Create a Mock WikiModel implementation so that the link parser works in wiki mode
        this.componentManager.registerMockComponent(WikiModel.class);

        ResourceReference reference = this.linkParser.parse("");
        Assert.assertEquals("", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [doc] Reference = []", reference.toString());

        reference = this.linkParser.parse("Hello World");
        Assert.assertEquals("Hello World", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [doc] Reference = [Hello World]", reference.toString());

        reference = this.linkParser.parse("http://xwiki.org");
        Assert.assertEquals("http://xwiki.org", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.URL, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [url] Reference = [http://xwiki.org]", reference.toString());

        // Verify mailto: URI is recognized
        reference = this.linkParser.parse("mailto:john@smith.com?subject=test");
        Assert.assertEquals("john@smith.com?subject=test", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals(ResourceType.MAILTO, reference.getType());
        Assert.assertEquals("Typed = [true] Type = [mailto] Reference = [john@smith.com?subject=test]",
            reference.toString());

        // Verify attach: URI is recognized
        reference = this.linkParser.parse("attach:some:content");
        Assert.assertEquals("some:content", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals(ResourceType.ATTACHMENT, reference.getType());
        Assert.assertEquals("Typed = [true] Type = [attach] Reference = [some:content]", reference.toString());

        // Verify that unknown URIs are ignored
        // Note: In this example we point to a document and we consider that myxwiki is the wiki name and
        // http://xwiki.org is the page name
        reference = this.linkParser.parse("mywiki:http://xwiki.org");
        Assert.assertEquals("mywiki:http://xwiki.org", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [doc] Reference = [mywiki:http://xwiki.org]", reference.toString());

        // Verify doc links work
        reference = this.linkParser.parse("doc:wiki:space.page");
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("wiki:space.page", reference.getReference());
        Assert.assertEquals("Typed = [true] Type = [doc] Reference = [wiki:space.page]", reference.toString());
        Assert.assertTrue(reference.isTyped());

        // Verify InterWiki links work
        reference = this.linkParser.parse("interwiki:alias:content");
        Assert.assertEquals(ResourceType.INTERWIKI, reference.getType());
        Assert.assertEquals("content", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("alias", ((InterWikiResourceReference) reference).getInterWikiAlias());
        Assert.assertEquals("Typed = [true] Type = [interwiki] Reference = [content] "
            + "Parameters = [[interWikiAlias] = [alias]]", reference.toString());

        // Verify that an invalid InterWiki link is considered as Document link
        reference = this.linkParser.parse("interwiki:invalid_since_doesnt_have_colon");
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("interwiki:invalid_since_doesnt_have_colon", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals("Typed = [false] Type = [doc] Reference = [interwiki:invalid_since_doesnt_have_colon]",
            reference.toString());

        // Verify typed URLs
        reference = this.linkParser.parse("url:http://xwiki.org");
        Assert.assertEquals(ResourceType.URL, reference.getType());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("http://xwiki.org", reference.getReference());
        Assert.assertEquals("Typed = [true] Type = [url] Reference = [http://xwiki.org]", reference.toString());

        // Verify query string and anchors have no meaning in link reference to documents.
        reference = this.linkParser.parse("Hello World?no=queryString#notAnAnchor");
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("Hello World?no=queryString#notAnAnchor", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertNull(((DocumentResourceReference) reference).getAnchor());
        Assert.assertNull(((DocumentResourceReference) reference).getQueryString());
        Assert.assertEquals("Typed = [false] Type = [doc] Reference = [Hello World?no=queryString#notAnAnchor]",
            reference.toString());

        // Verify that the interwiki separator from XWiki Syntax 2.0 has not meaning in link references to documents
        reference = this.linkParser.parse("page@alias");
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals("page@alias", reference.getReference());
        Assert.assertEquals("Typed = [false] Type = [doc] Reference = [page@alias]", reference.toString());

        // Verify path link types
        reference = this.linkParser.parse("path:/some/path");
        Assert.assertEquals(ResourceType.PATH, reference.getType());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("/some/path", reference.getReference());
        Assert.assertEquals("Typed = [true] Type = [path] Reference = [/some/path]", reference.toString());

        // Verify UNC link types
        reference = this.linkParser.parse("unc:\\\\myserver\\myshare\\mydoc.txt");
        Assert.assertEquals(ResourceType.UNC, reference.getType());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals("\\\\myserver\\myshare\\mydoc.txt", reference.getReference());
        Assert.assertEquals("Typed = [true] Type = [unc] Reference = [\\\\myserver\\myshare\\mydoc.txt]",
            reference.toString());

        // Verify that reference escapes are left as is by the link parser
        reference = this.linkParser.parse("pa\\.ge");
        Assert.assertEquals(ResourceType.DOCUMENT, reference.getType());
        Assert.assertEquals("pa\\.ge", reference.getReference());
    }

    @Test
    public void testParseWhenNotInWikiMode() throws Exception
    {
        // Verify that mailto: links are treated normally even when in non wiki mode
        ResourceReference reference = this.linkParser.parse("mailto:something");
        Assert.assertEquals("something", reference.getReference());
        Assert.assertTrue(reference.isTyped());
        Assert.assertEquals(ResourceType.MAILTO, reference.getType());
        Assert.assertEquals("Typed = [true] Type = [mailto] Reference = [something]", reference.toString());

        // Verify that non typed links are treated as URLs
        reference = this.linkParser.parse("something");
        Assert.assertEquals("something", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.URL, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [url] Reference = [something]", reference.toString());

        // Verify that doc: links are treated as URLs
        reference = this.linkParser.parse("doc:something");
        Assert.assertEquals("doc:something", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.URL, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [url] Reference = [doc:something]", reference.toString());

        // Verify that attach: links are treated as URLs
        reference = this.linkParser.parse("attach:something");
        Assert.assertEquals("attach:something", reference.getReference());
        Assert.assertFalse(reference.isTyped());
        Assert.assertEquals(ResourceType.URL, reference.getType());
        Assert.assertEquals("Typed = [false] Type = [url] Reference = [attach:something]", reference.toString());
    }
}

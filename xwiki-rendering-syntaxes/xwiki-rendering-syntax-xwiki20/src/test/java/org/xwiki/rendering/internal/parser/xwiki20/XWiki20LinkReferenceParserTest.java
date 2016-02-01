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
import org.xwiki.rendering.internal.parser.reference.DefaultUntypedLinkReferenceParser;
import org.xwiki.rendering.internal.parser.reference.type.AttachmentResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.DocumentResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.MailtoResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.SpaceResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.URLResourceReferenceTypeParser;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.InterWikiResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XWiki20LinkReferenceParser}.
 *
 * @version $Id$
 * @since 2.5RC1
 */
//@formatter:off
@ComponentList({
    XWiki20LinkReferenceParser.class,
    URLResourceReferenceTypeParser.class,
    MailtoResourceReferenceTypeParser.class,
    AttachmentResourceReferenceTypeParser.class,
    DefaultUntypedLinkReferenceParser.class,
    DocumentResourceReferenceTypeParser.class,
    SpaceResourceReferenceTypeParser.class
})
//@formatter:on
public class XWiki20LinkReferenceParserTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    protected ResourceReferenceParser parser;

    @BeforeComponent
    public void setUpComponents() throws Exception
    {
        // Create a Mock WikiModel implementation so that the link parser works in wiki mode
        this.componentManager.registerMockComponent(WikiModel.class);

        Provider<ComponentManager> contextComponentManagerProvider = this.componentManager.registerMockComponent(
            new DefaultParameterizedType(null, Provider.class, ComponentManager.class), "context");
        when(contextComponentManagerProvider.get()).thenReturn(this.componentManager);
    }

    @Before
    public void setUp() throws Exception
    {
        this.parser = this.componentManager.getInstance(ResourceReferenceParser.class, "xwiki/2.0/link");
    }

    @Test
    public void testParseLinksWhenInWikiModeCommon() throws Exception
    {
        ResourceReference reference = this.parser.parse("");
        assertEquals("", reference.getReference());
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.DOCUMENT, reference.getType());
        assertEquals("Typed = [false] Type = [doc] Reference = []", reference.toString());

        reference = this.parser.parse("Hello World");
        assertEquals("Hello World", reference.getReference());
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("Typed = [false] Type = [space] Reference = [Hello World]", reference.toString());

        reference = this.parser.parse("http://xwiki.org");
        assertEquals("http://xwiki.org", reference.getReference());
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.URL, reference.getType());
        assertEquals("Typed = [false] Type = [url] Reference = [http://xwiki.org]", reference.toString());

        // Verify mailto: URI is recognized
        reference = this.parser.parse("mailto:john@smith.com?subject=test");
        assertEquals("john@smith.com?subject=test", reference.getReference());
        assertTrue(reference.isTyped());
        assertEquals(ResourceType.MAILTO, reference.getType());
        assertEquals("Typed = [true] Type = [mailto] Reference = [john@smith.com?subject=test]",
            reference.toString());

        // Verify attach: URI is recognized
        reference = this.parser.parse("attach:some:content");
        assertEquals("some:content", reference.getReference());
        assertTrue(reference.isTyped());
        assertEquals(ResourceType.ATTACHMENT, reference.getType());
        assertEquals("Typed = [true] Type = [attach] Reference = [some:content]", reference.toString());

        // Verify that unknown URIs are ignored
        // Note: In this example we point to a space and we consider that myxwiki is the wiki name and
        // http://xwiki.org is the space name
        reference = this.parser.parse("mywiki:http://xwiki.org");
        assertEquals("mywiki:http://xwiki.org", reference.getReference());
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("Typed = [false] Type = [space] Reference = [mywiki:http://xwiki.org]", reference.toString());
    }

    @Test
    public void testParseLinksWhenInWikiMode() throws Exception
    {
        // Test Query Strings in links to document
        ResourceReference reference = this.parser.parse("Hello World?xredirect=../whatever");
        assertEquals("Hello World", reference.getReference());
        assertEquals("xredirect=../whatever", reference.getParameter(DocumentResourceReference.QUERY_STRING));
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("Typed = [false] Type = [space] Reference = [Hello World] "
            + "Parameters = [[queryString] = [xredirect=../whatever]]", reference.toString());

        reference = this.parser.parse("HelloWorld?xredirect=http://xwiki.org");
        assertEquals("HelloWorld", reference.getReference());
        assertEquals("xredirect=http://xwiki.org", reference.getParameter(DocumentResourceReference.QUERY_STRING));
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("Typed = [false] Type = [space] Reference = [HelloWorld] "
            + "Parameters = [[queryString] = [xredirect=http://xwiki.org]]", reference.toString());

        // Test Anchors in links to documents
        reference = this.parser.parse("#anchor");
        assertEquals("anchor", reference.getParameter(DocumentResourceReference.ANCHOR));
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.DOCUMENT, reference.getType());
        assertEquals("Typed = [false] Type = [doc] Reference = [] Parameters = [[anchor] = [anchor]]",
            reference.toString());

        reference = this.parser.parse("Hello#anchor");
        assertEquals("Hello", reference.getReference());
        assertEquals("anchor", reference.getParameter(DocumentResourceReference.ANCHOR));
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("Typed = [false] Type = [space] Reference = [Hello] Parameters = [[anchor] = [anchor]]",
            reference.toString());

        // Test InterWiki links
        reference = this.parser.parse("HelloWorld#anchor?param1=1&param2=2@wikipedia");
        assertEquals("HelloWorld#anchor?param1=1&param2=2", reference.getReference());
        assertEquals("wikipedia", ((InterWikiResourceReference) reference).getInterWikiAlias());
        assertTrue(reference.isTyped());
        assertEquals(ResourceType.INTERWIKI, reference.getType());
        assertEquals("Typed = [true] Type = [interwiki] Reference = [HelloWorld#anchor?param1=1&param2=2] "
            + "Parameters = [[interWikiAlias] = [wikipedia]]", reference.toString());

        // Verify in XWiki Syntax 2.0 the "doc" prefix is not meaningful
        reference = this.parser.parse("doc:whatever");
        assertEquals("doc:whatever", reference.getReference());
        assertFalse(reference.isTyped());
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("Typed = [false] Type = [space] Reference = [doc:whatever]", reference.toString());
    }

    @Test
    public void testParseLinksWithEscapes() throws Exception
    {
        ResourceReference reference = this.parser.parse("\\.\\#notanchor");
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("\\.#notanchor", reference.getReference());
        assertNull(reference.getParameter(DocumentResourceReference.ANCHOR));

        reference = this.parser.parse("page\\?notquerystring");
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("page?notquerystring", reference.getReference());
        assertNull(reference.getParameter(DocumentResourceReference.QUERY_STRING));

        // Verify that \ can be escaped and that escaped chars in query string, and anchors are escaped
        reference = this.parser.parse("page\\\\#anchor\\\\?querystring\\\\");
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("page\\\\", reference.getReference());
        assertEquals("anchor\\", reference.getParameter(DocumentResourceReference.ANCHOR));
        assertEquals("querystring\\", reference.getParameter(DocumentResourceReference.QUERY_STRING));

        reference = this.parser.parse("pa\\.ge\\?query\\#anchor\\@notinterwiki");
        assertEquals(ResourceType.SPACE, reference.getType());
        assertEquals("pa\\.ge?query#anchor@notinterwiki", reference.getReference());

        // Verify that \ can be escaped and that escaped chars in query string, anchors and InterWiki aliases are
        // escaped.
        reference = this.parser.parse("page\\\\#anchor\\\\?querystring\\\\@alias\\\\");
        assertEquals(ResourceType.INTERWIKI, reference.getType());
        assertEquals("page\\#anchor\\?querystring\\", reference.getReference());
        assertEquals("alias\\", ((InterWikiResourceReference) reference).getInterWikiAlias());

        reference = this.parser.parse("something\\\\@inter\\@wikilink");
        assertEquals(ResourceType.INTERWIKI, reference.getType());
        assertEquals("something\\", reference.getReference());
        assertEquals("inter@wikilink", ((InterWikiResourceReference) reference).getInterWikiAlias());
    }
}

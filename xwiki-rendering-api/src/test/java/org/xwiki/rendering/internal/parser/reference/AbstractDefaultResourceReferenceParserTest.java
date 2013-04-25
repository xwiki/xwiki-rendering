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
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.rendering.internal.parser.reference.type.DocumentResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.MailtoResourceReferenceTypeParser;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link AbstractDefaultResourceReferenceParser}.
 *
 * @version $Id$
 * @since 5.1M1
 */
@ComponentList({
    TestableAbstractDefaultResourceReferenceParser.class,
    MailtoResourceReferenceTypeParser.class,
    DocumentResourceReferenceTypeParser.class,
    ContextComponentManagerProvider.class
})
public class AbstractDefaultResourceReferenceParserTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    @Test
    public void parseWhenNotInWikiModeAndNotSupportedType() throws Exception
    {
        TestableAbstractDefaultResourceReferenceParser parser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "default/test");

        parser.setSupportedTypes(Collections.EMPTY_LIST);
        ResourceReference reference = parser.parse("mailto:something");
        assertEquals(ResourceType.UNKNOWN, reference.getType());
        assertEquals("mailto:something", reference.getReference());
        assertEquals(true, reference.isTyped());
    }

    @Test
    public void parseWhenNotInWikiModeAndSupportedType() throws Exception
    {
        TestableAbstractDefaultResourceReferenceParser parser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "default/test");

        parser.setSupportedTypes(Arrays.asList("mailto"));
        ResourceReference reference = parser.parse("mailto:something");
        assertEquals(ResourceType.MAILTO, reference.getType());
        assertEquals("something", reference.getReference());
        assertEquals(true, reference.isTyped());
    }

    @Test
    public void parseWhenEmptyReference() throws Exception
    {
        TestableAbstractDefaultResourceReferenceParser parser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "default/test");

        // When not in wiki mode
        parser.setSupportedTypes(Collections.EMPTY_LIST);
        ResourceReference reference = parser.parse("");
        assertEquals(ResourceType.UNKNOWN, reference.getType());
        assertEquals("", reference.getReference());
        assertEquals(true, reference.isTyped());

        // When in wiki mode
        this.componentManager.registerMockComponent(WikiModel.class);
        reference = parser.parse("");
        assertEquals(ResourceType.UNKNOWN, reference.getType());
        assertEquals("", reference.getReference());
        assertEquals(true, reference.isTyped());
    }

    @Test
    public void parseWhenNotInWikiModeAndResourceIsDocument() throws Exception
    {
        TestableAbstractDefaultResourceReferenceParser parser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "default/test");

        // Note: Since we have no WikiModel implementation in the execution classpath we're not in wiki mode

        parser.setSupportedTypes(Arrays.asList("doc"));
        ResourceReference reference = parser.parse("doc:something");
        assertEquals(ResourceType.URL, reference.getType());
        assertEquals("doc:something", reference.getReference());
        assertEquals(false, reference.isTyped());
    }
}

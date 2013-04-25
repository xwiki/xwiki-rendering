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

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.rendering.internal.parser.reference.type.DocumentResourceReferenceTypeParser;
import org.xwiki.rendering.internal.parser.reference.type.URLResourceReferenceTypeParser;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AbstractLinkReferenceParser}.
 *
 * @version $Id$
 * @since 5.1M1
 */
@ComponentList({
    TestableAbstractLinkReferenceParser.class,
    DefaultUntypedLinkReferenceParser.class,
    URLResourceReferenceTypeParser.class,
    DocumentResourceReferenceTypeParser.class,
    ContextComponentManagerProvider.class
})
public class AbstractLinkReferenceParserTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    @Test
    public void parseWhenTypeParserExists() throws Exception
    {
        TestableAbstractLinkReferenceParser parser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "link/test");

        ResourceReferenceParser defaultParser = mock(ResourceReferenceParser.class);
        ResourceReference defaultReference = new ResourceReference("ref", ResourceType.URL);
        when(defaultParser.parse("something")).thenReturn(defaultReference);
        parser.setResourceReferenceParser(defaultParser);

        ResourceReference reference = parser.parse("something");
        assertSame(defaultReference, reference);
    }

    @Test
    public void parseWhenTypeParserDoesntExists() throws Exception
    {
        TestableAbstractLinkReferenceParser parser =
            this.componentManager.getInstance(ResourceReferenceParser.class, "link/test");

        ResourceReferenceParser defaultParser = mock(ResourceReferenceParser.class);
        ResourceReference defaultReference = new ResourceReference("ref", ResourceType.UNKNOWN);
        when(defaultParser.parse("something")).thenReturn(defaultReference);
        parser.setResourceReferenceParser(defaultParser);

        ResourceReference reference = parser.parse("something");
        ResourceReference expectedReference = new ResourceReference("something", ResourceType.URL);
        expectedReference.setTyped(false);
        assertEquals(expectedReference, reference);
    }
}

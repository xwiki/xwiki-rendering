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
package org.xwiki.rendering.internal.parser.reference.type;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceTypeParser;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link URLResourceReferenceTypeParser}.
 *
 * @version $Id$
 * @since 5.1M1
 */
public class URLResourceReferenceTypeParserTest
{
    @Rule
    public MockitoComponentMockingRule<ResourceReferenceTypeParser> mocker =
        new MockitoComponentMockingRule<ResourceReferenceTypeParser>(URLResourceReferenceTypeParser.class);

    @Test
    public void parse() throws Exception
    {
        ResourceReference reference = this.mocker.getComponentUnderTest().parse("//not a url");
        assertNull(reference);

        reference = this.mocker.getComponentUnderTest().parse("http://xwiki.org");
        assertEquals(ResourceType.URL, reference.getType());
        assertEquals("http://xwiki.org", reference.getReference());
        assertEquals(true, reference.isTyped());
    }
}

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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.properties.converter.Converter;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ResourceReferenceConverter}.
 *
 * @version $Id$
 */
public class ResourceReferenceConverterTest
{
    @Rule
    public MockitoComponentMockingRule<Converter<ResourceReference>> mocker =
        new MockitoComponentMockingRule<Converter<ResourceReference>>(ResourceReferenceConverter.class);

    @Test
    public void convertToResourceReference() throws Exception
    {
        ResourceReference reference = new ResourceReference("reference", ResourceType.DOCUMENT);

        final ResourceReferenceParser parser = this.mocker.getInstance(ResourceReferenceParser.class);
        when(parser.parse("reference")).thenReturn(reference);

        Assert.assertSame(reference, this.mocker.getComponentUnderTest().convert(ResourceReference.class, "reference"));
    }

    @Test
    public void convertToResourceReferenceWhenNull() throws Exception
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().convert(ResourceReference.class, null));
    }
}

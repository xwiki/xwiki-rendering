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
package org.xwiki.rendering.internal.macro.source;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Validate {@link MacroContentSourceReferenceConverter}.
 * 
 * @version $Id$
 */
@ComponentTest
@ComponentList(ContextComponentManagerProvider.class)
class MacroContentSourceReferenceConverterTest
{
    @InjectMockComponents
    private MacroContentSourceReferenceConverter converter;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    public static class MyType
    {
        @Override
        public String toString()
        {
            return "MyType";
        }
    }

    @Test
    void convertToType()
    {
        assertNull(this.converter.convert(MacroContentSourceReference.class, null));
    }

    @Test
    void convertFromString()
    {
        assertEquals(new MacroContentSourceReference("string", "string"),
            this.converter.convert(MacroContentSourceReference.class, "string"));
        assertEquals(new MacroContentSourceReference("string", ""),
            this.converter.convert(MacroContentSourceReference.class, ""));
        assertEquals(new MacroContentSourceReference("string", "reference"),
            this.converter.convert(MacroContentSourceReference.class, "string:reference"));
    }

    @Test
    void convertFromReader()
    {
        assertEquals(new MacroContentSourceReference("string", "reference"),
            this.converter.convert(MacroContentSourceReference.class, new StringReader("reference")));
    }

    @Test
    void convertFromBytes()
    {
        assertEquals(new MacroContentSourceReference("string", "reference"),
            this.converter.convert(MacroContentSourceReference.class, "reference".getBytes()));
    }

    @Test
    void convertFromInputStream()
    {
        assertEquals(new MacroContentSourceReference("string", "reference"), this.converter
            .convert(MacroContentSourceReference.class, new ByteArrayInputStream("reference".getBytes())));
    }

    @Test
    void convertFromURL() throws MalformedURLException
    {
        assertEquals(new MacroContentSourceReference("url", "http://url"),
            this.converter.convert(MacroContentSourceReference.class, new URL("http://url")));
    }

    @Test
    void convertFromFile()
    {
        assertEquals(new MacroContentSourceReference("file", "/path"),
            this.converter.convert(MacroContentSourceReference.class, new File("/path")));
    }

    @Test
    void convertFromUnknownType() throws Exception
    {
        assertEquals(new MacroContentSourceReference("string", "MyType"),
            this.converter.convert(MacroContentSourceReference.class, new MyType()));
    }

    @Test
    void convertFromCustomType() throws Exception
    {
        org.xwiki.rendering.macro.source.MacroContentSourceReferenceConverter myconverter =
            this.componentManager.registerMockComponent(TypeUtils.parameterize(
                org.xwiki.rendering.macro.source.MacroContentSourceReferenceConverter.class, MyType.class));

        MyType myvalue = new MyType();
        MacroContentSourceReference myReference = new MacroContentSourceReference("mytype", "value2");

        when(myconverter.convert(myvalue)).thenReturn(myReference);

        assertEquals(myReference, this.converter.convert(MacroContentSourceReference.class, myvalue));
    }
}

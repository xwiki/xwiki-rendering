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
package org.xwiki.rendering.macro;

import org.junit.jupiter.api.Test;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.rendering.internal.transformation.macro.TestMacroCustomContentDescriptor;
import org.xwiki.rendering.internal.transformation.macro.TestSimpleMacro;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate the behaviour of {@link AbstractMacro}.
 *
 * @version $Id$
 * @since 10.9RC1
 */
@ComponentTest
@AllComponents
public class AbstractMacroTest
{
    @InjectMockComponents
    private TestSimpleMacro macro1;

    @InjectMockComponents
    private TestMacroCustomContentDescriptor macro2;

    @Test
    public void getUnchangedMetadataDefault()
    {
        assertNull(macro1.getDescriptor().getContentDescriptor());
        MetaData unchangedContentMetaData = macro1.getUnchangedContentMetaData();

        MetaData expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.UNCHANGED_CONTENT, "java.lang.String");
        assertEquals(expectedMetadata, unchangedContentMetaData);
    }

    @Test
    public void getUnchangedMetadataCustomDescriptor()
    {
        assertNotNull(macro2.getDescriptor().getContentDescriptor());
        MetaData unchangedContentMetaData = macro2.getUnchangedContentMetaData();

        MetaData expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.UNCHANGED_CONTENT, "java.util.List< org.xwiki.rendering.block.Block >");
        assertEquals(expectedMetadata, unchangedContentMetaData);
    }
}

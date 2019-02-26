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
import org.xwiki.rendering.internal.transformation.macro.TestInlineEditingMacro;
import org.xwiki.rendering.internal.transformation.macro.TestInlineEditingMacroParameter;
import org.xwiki.rendering.internal.transformation.macro.TestSimpleMacro;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate the behaviour of {@link AbstractMacro}.
 *
 * @version $Id$
 * @since 10.10RC1
 */
@ComponentTest
@AllComponents
public class AbstractMacroTest
{
    @InjectMockComponents
    private TestSimpleMacro macro1;

    @InjectMockComponents
    private TestInlineEditingMacro macro2;

    @InjectMockComponents
    private TestInlineEditingMacroParameter macro3;

    @Test
    public void getNonGeneratedMetadataDefault()
    {
        assertNull(macro1.getDescriptor().getContentDescriptor());
        MetaData nonGeneratedContentMetaData = macro1.getNonGeneratedContentMetaData();

        MetaData expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.NON_GENERATED_CONTENT, "java.lang.String");
        assertEquals(expectedMetadata, nonGeneratedContentMetaData);
    }

    @Test
    public void getNonGeneratedMetadataCustomDescriptor()
    {
        assertNotNull(macro2.getDescriptor().getContentDescriptor());
        MetaData nonGeneratedContentMetaData = macro2.getNonGeneratedContentMetaData();

        MetaData expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.NON_GENERATED_CONTENT, "java.util.List<org.xwiki.rendering.block.Block>");
        assertEquals(expectedMetadata, nonGeneratedContentMetaData);
    }

    @Test
    public void getNonGeneratedMetadataForParameterCustomDescriptor()
    {
        MetaData nonGeneratedContentMetaData = macro3.getNonGeneratedContentMetaData("param1");

        MetaData expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.NON_GENERATED_CONTENT, "java.util.List<org.xwiki.rendering.block.Block>");
        expectedMetadata.addMetaData(MetaData.PARAMETER_NAME, "param1");
        assertEquals(expectedMetadata, nonGeneratedContentMetaData);

        nonGeneratedContentMetaData = macro3.getNonGeneratedContentMetaData("param2");

        expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.NON_GENERATED_CONTENT, "java.lang.String");
        expectedMetadata.addMetaData(MetaData.PARAMETER_NAME, "param2");
        assertEquals(expectedMetadata, nonGeneratedContentMetaData);

        nonGeneratedContentMetaData = macro3.getNonGeneratedContentMetaData("param3");

        expectedMetadata = new MetaData();
        expectedMetadata.addMetaData(MetaData.NON_GENERATED_CONTENT, "java.lang.String");
        expectedMetadata.addMetaData(MetaData.PARAMETER_NAME, "param3");
        assertEquals(expectedMetadata, nonGeneratedContentMetaData);
    }

    @Test
    public void supportsInlineMode()
    {
        assertFalse(macro1.supportsInlineMode());
        assertEquals(macro1.supportsInlineMode(), macro1.getDescriptor().supportsInlineMode());

        assertTrue(macro2.supportsInlineMode());
        assertEquals(macro2.supportsInlineMode(), macro2.getDescriptor().supportsInlineMode());
    }
}

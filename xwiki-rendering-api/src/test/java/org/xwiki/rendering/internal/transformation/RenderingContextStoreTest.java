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
package org.xwiki.rendering.internal.transformation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.RenderingContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Validate {@link RenderingContextStore}.
 * 
 * @version $Id$
 */
@ComponentTest
class RenderingContextStoreTest
{
    @InjectMockComponents
    private RenderingContextStore store;

    @MockComponent
    private RenderingContext renderingContext;

    @Test
    void save()
    {
        Map<String, Serializable> contextStore = new HashMap<>();

        this.store.save(contextStore, SetUtils.hashSet(RenderingContextStore.PROP_DEFAULTSYNTAX,
            RenderingContextStore.PROP_TARGETSYNTAX, RenderingContextStore.PROP_RESTRICTED));

        Map<String, Serializable> expectedContextStore = new HashMap<>();
        expectedContextStore.put(RenderingContextStore.PROP_RESTRICTED, false);
        assertEquals(expectedContextStore, contextStore);

        when(this.renderingContext.getDefaultSyntax()).thenReturn(Syntax.XWIKI_2_1);
        when(this.renderingContext.getTargetSyntax()).thenReturn(Syntax.PLAIN_1_0);
        when(this.renderingContext.isRestricted()).thenReturn(true);

        this.store.save(contextStore, SetUtils.hashSet(RenderingContextStore.PROP_DEFAULTSYNTAX,
            RenderingContextStore.PROP_TARGETSYNTAX, RenderingContextStore.PROP_RESTRICTED));

        expectedContextStore = new HashMap<>();
        expectedContextStore.put(RenderingContextStore.PROP_DEFAULTSYNTAX, Syntax.XWIKI_2_1.toIdString());
        expectedContextStore.put(RenderingContextStore.PROP_TARGETSYNTAX, Syntax.PLAIN_1_0.toIdString());
        expectedContextStore.put(RenderingContextStore.PROP_RESTRICTED, true);
        assertEquals(expectedContextStore, contextStore);
    }
}

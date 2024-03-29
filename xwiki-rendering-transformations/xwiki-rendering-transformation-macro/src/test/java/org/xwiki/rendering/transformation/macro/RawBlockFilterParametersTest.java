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
package org.xwiki.rendering.transformation.macro;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link RawBlockFilterParameters}.
 *
 * @version $Id$
 * @since 14.8RC1
 */
class RawBlockFilterParametersTest
{
    @Test
    void equalsHashCode()
    {
        MacroTransformationContext macroTransformationContext = mock(MacroTransformationContext.class);
        RawBlockFilterParameters parameters1 = new RawBlockFilterParameters(macroTransformationContext);
        RawBlockFilterParameters parameters2 = new RawBlockFilterParameters(macroTransformationContext);
        assertEquals(parameters1, parameters2);
        assertEquals(parameters1.hashCode(), parameters2.hashCode());

        parameters1.setClean(true);
        parameters2.setClean(true);

        assertEquals(parameters1, parameters2);
        assertEquals(parameters1.hashCode(), parameters2.hashCode());

        parameters1.setRestricted(true);

        assertNotEquals(parameters1, parameters2);
        assertNotEquals(parameters1.hashCode(), parameters2.hashCode());
    }
}

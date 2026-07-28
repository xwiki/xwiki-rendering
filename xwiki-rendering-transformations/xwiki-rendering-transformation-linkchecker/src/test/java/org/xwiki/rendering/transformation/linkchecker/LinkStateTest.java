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
package org.xwiki.rendering.transformation.linkchecker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link LinkState}.
 *
 * @version $Id$
 */
class LinkStateTest
{
    @Test
    void testEquals()
    {
        LinkState linkState1 = new LinkState(0, 0);
        LinkState linkState2 = new LinkState(0, 0, null);

        assertNotEquals(linkState1, null);
        assertEquals(linkState1, linkState1);
        assertNotEquals(linkState1, "");
        assertEquals(linkState1, linkState2);
    }

    @Test
    void testHashCode()
    {
        LinkState linkState1 = new LinkState(0, 0);
        LinkState linkState2 = new LinkState(0, 0, null);

        assertEquals(linkState1.hashCode(), linkState2.hashCode());
    }
}

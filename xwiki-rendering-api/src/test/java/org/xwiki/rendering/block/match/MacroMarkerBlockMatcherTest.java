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
package org.xwiki.rendering.block.match;

import java.util.Collections;

import org.junit.Test;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.WordBlock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MacroMarkerBlockMatcher}.
 *
 * @version $Id$
 * @since 11.4
 */
public class MacroMarkerBlockMatcherTest
{
    @Test
    public void match()
    {
        MacroMarkerBlockMatcher matcher = new MacroMarkerBlockMatcher("macro1", "macro2");
        MacroMarkerBlock macroMarkerBlock1 =
            new MacroMarkerBlock("macro1", Collections.emptyMap(), "content1", Collections.emptyList(), false);
        MacroMarkerBlock macroMarkerBlock2 =
            new MacroMarkerBlock("macro2", Collections.emptyMap(), "content2", Collections.emptyList(), false);
        MacroMarkerBlock macroMarkerBlock3 =
            new MacroMarkerBlock("macro3", Collections.emptyMap(), "content3", Collections.emptyList(), false);

        assertTrue(matcher.match(macroMarkerBlock1));
        assertTrue(matcher.match(macroMarkerBlock2));
        assertFalse(matcher.match(macroMarkerBlock3));
        assertFalse(matcher.match(new WordBlock("nomatch")));
    }
}

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

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.macro.source.MacroContentSourceReference;
import org.xwiki.rendering.macro.source.MacroContentWikiSource;
import org.xwiki.rendering.syntax.Syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Validate {@link MacroContentWikiSource}.
 * 
 * @version $Id$
 */
class MacroContentWikiSourceTest
{
    @Test
    void equals()
    {
        MacroContentSourceReference reference = new MacroContentSourceReference("type", "reference");
        MacroContentWikiSource source = new MacroContentWikiSource(reference, "content", Syntax.PLAIN_1_0);

        assertEquals(source, new MacroContentWikiSource(new MacroContentSourceReference("type", "reference"), "content",
            Syntax.PLAIN_1_0));
        assertEquals(source, source);

        assertNotEquals(source, null);
        assertNotEquals(source, new MacroContentWikiSource(new MacroContentSourceReference("type", "reference"),
            "content", Syntax.ANNOTATED_HTML_5_0));
        assertNotEquals(source, new MacroContentWikiSource(new MacroContentSourceReference("type", "reference"),
            "content2", Syntax.PLAIN_1_0));
        assertNotEquals(source, new MacroContentWikiSource(new MacroContentSourceReference("type", "reference2"),
            "content", Syntax.PLAIN_1_0));
        assertNotEquals(source, new MacroContentWikiSource(new MacroContentSourceReference("type2", "reference"),
            "content", Syntax.PLAIN_1_0));
    }

    @Test
    void hasCode()
    {
        MacroContentSourceReference reference = new MacroContentSourceReference("type", "reference");
        MacroContentWikiSource source = new MacroContentWikiSource(reference, "content", Syntax.PLAIN_1_0);

        assertEquals(source.hashCode(), new MacroContentWikiSource(new MacroContentSourceReference("type", "reference"),
            "content", Syntax.PLAIN_1_0).hashCode());
        assertEquals(source.hashCode(), source.hashCode());

        assertNotEquals(source.hashCode(),
            new MacroContentWikiSource(new MacroContentSourceReference("type", "reference"), "content",
                Syntax.ANNOTATED_HTML_5_0).hashCode());
        assertNotEquals(source.hashCode(),
            new MacroContentWikiSource(new MacroContentSourceReference("type", "reference"), "content2",
                Syntax.PLAIN_1_0).hashCode());
        assertNotEquals(source.hashCode(),
            new MacroContentWikiSource(new MacroContentSourceReference("type", "reference2"), "content",
                Syntax.PLAIN_1_0).hashCode());
        assertNotEquals(source.hashCode(),
            new MacroContentWikiSource(new MacroContentSourceReference("type2", "reference"), "content",
                Syntax.PLAIN_1_0).hashCode());
    }
}

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
package org.xwiki.rendering.wikimodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link WikiParameters}.
 * 
 * @version $Id$
 */
class WikiParametersTest
{
    @Test
    void testParametersValuewithoutEndingDoubleQuote()
    {
        WikiParameters wikiParameters = WikiParameters.newWikiParameters("key=\"value");

        assertEquals(1, wikiParameters.getSize());
        assertEquals("value", wikiParameters.getParameter("key").getValue());
    }

    @Test
    void testParametersOnSeveralLines()
    {
        WikiParameters wikiParameters = WikiParameters.newWikiParameters("key1='value1'\n  key2='value2'");

        assertEquals(2, wikiParameters.getSize());
        assertEquals("value1", wikiParameters.getParameter("key1").getValue());
        assertEquals("value2", wikiParameters.getParameter("key2").getValue());
    }
}

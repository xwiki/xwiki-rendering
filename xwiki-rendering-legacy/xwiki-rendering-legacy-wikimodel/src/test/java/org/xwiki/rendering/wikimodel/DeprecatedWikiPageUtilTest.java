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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate the retro compatibility of the char based methods of {@link WikiPageUtil}.
 *
 * @version $Id$
 */
class DeprecatedWikiPageUtilTest
{
    @Test
    void isValidXmlNameStartChar()
    {
        assertTrue(WikiPageUtil.isValidXmlNameStartChar('a', false));
        assertTrue(WikiPageUtil.isValidXmlNameStartChar('_', false));
        assertFalse(WikiPageUtil.isValidXmlNameStartChar('1', false));
        assertTrue(WikiPageUtil.isValidXmlNameStartChar(':', true));
        assertFalse(WikiPageUtil.isValidXmlNameStartChar(':', false));
    }

    @Test
    void isValidXmlNameChar()
    {
        assertTrue(WikiPageUtil.isValidXmlNameChar('a', false));
        assertTrue(WikiPageUtil.isValidXmlNameChar('1', false));
        assertTrue(WikiPageUtil.isValidXmlNameChar('-', false));
        assertFalse(WikiPageUtil.isValidXmlNameChar(' ', false));
        assertTrue(WikiPageUtil.isValidXmlNameChar(':', true));
        assertFalse(WikiPageUtil.isValidXmlNameChar(':', false));
    }

    /**
     * A surrogate is only half of a supplementary character, so on its own it is not a valid name character. This is
     * why the char based signatures were replaced by code point based ones.
     */
    @Test
    void isValidXmlNameCharWithSurrogate()
    {
        assertFalse(WikiPageUtil.isValidXmlNameChar('\uD840', false));
        assertFalse(WikiPageUtil.isValidXmlNameChar('\uDC00', false));
    }
}

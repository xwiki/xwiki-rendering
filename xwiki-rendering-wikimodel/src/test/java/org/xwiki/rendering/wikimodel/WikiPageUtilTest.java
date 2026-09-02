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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link WikiPageUtil}.
 *
 * @version $Id$
 */
class WikiPageUtilTest
{
    /**
     * U+20000, the first code point of CJK Unified Ideographs Extension B. It is written with a surrogate pair, so
     * it's the simplest way to check that a supplementary character isn't validated one half at a time.
     */
    private static final String CJK_EXTENSION_B_FIRST = new String(Character.toChars(0x20000));

    @ParameterizedTest
    @ValueSource(ints = {'A', 'Z', '_', 'a', 'z', 0xC0, 0xF8, 0x370, 0x200C, 0x2C00, 0x3001, 0xF900, 0xFDF0, 0xFFFD,
        0x10000, 0x20000, 0xEFFFF})
    void isValidXmlNameStartCharWithValidCodePoint(int codePoint)
    {
        assertTrue(WikiPageUtil.isValidXmlNameStartChar(codePoint, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {'-', '.', '0', '9', 0xB7, 0xD7, 0xD800, 0xDC00, 0xF0000, 0x10FFFF})
    void isValidXmlNameStartCharWithInvalidCodePoint(int codePoint)
    {
        assertFalse(WikiPageUtil.isValidXmlNameStartChar(codePoint, false));
    }

    @Test
    void isValidXmlNameStartCharWithColon()
    {
        assertTrue(WikiPageUtil.isValidXmlNameStartChar(':', true));
        assertFalse(WikiPageUtil.isValidXmlNameStartChar(':', false));
    }

    @ParameterizedTest
    @ValueSource(ints = {'A', '_', 'a', '-', '.', '0', '9', 0xB7, 0x0300, 0x203F, 0x10000, 0x20000, 0xEFFFF})
    void isValidXmlNameCharWithValidCodePoint(int codePoint)
    {
        assertTrue(WikiPageUtil.isValidXmlNameChar(codePoint, false));
    }

    @ParameterizedTest
    @ValueSource(ints = {' ', '/', 0xD7, 0xD800, 0xDC00, 0xF0000, 0x10FFFF})
    void isValidXmlNameCharWithInvalidCodePoint(int codePoint)
    {
        assertFalse(WikiPageUtil.isValidXmlNameChar(codePoint, false));
    }

    @Test
    void isValidXmlNameCharWithColon()
    {
        assertTrue(WikiPageUtil.isValidXmlNameChar(':', true));
        assertFalse(WikiPageUtil.isValidXmlNameChar(':', false));
    }

    @ParameterizedTest
    @CsvSource({
        "foo, false, true",
        "_foo-1.2, false, true",
        "1foo, false, false",
        "-foo, false, false",
        "foo bar, false, false",
        "'', false, false",
        "ns:foo, true, true",
        "ns:foo, false, false",
        ":foo, true, true"
    })
    void isValidXmlName(String tagName, boolean colonEnabled, boolean expected)
    {
        assertEquals(expected, WikiPageUtil.isValidXmlName(tagName, colonEnabled));
    }

    @Test
    void isValidXmlNameWithNullName()
    {
        assertFalse(WikiPageUtil.isValidXmlName(null, true));
    }

    @Test
    void isValidXmlNameWithSupplementaryCharacter()
    {
        assertTrue(WikiPageUtil.isValidXmlName(CJK_EXTENSION_B_FIRST, false));
        assertTrue(WikiPageUtil.isValidXmlName(CJK_EXTENSION_B_FIRST + "foo", false));
        assertTrue(WikiPageUtil.isValidXmlName("foo" + CJK_EXTENSION_B_FIRST, false));
    }

    @Test
    void isValidXmlNameWithLoneSurrogate()
    {
        assertFalse(WikiPageUtil.isValidXmlName("\uD840", false));
        assertFalse(WikiPageUtil.isValidXmlName("foo\uDC00", false));
    }
}

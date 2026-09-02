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

import java.util.Arrays;

import org.xwiki.stability.Unstable;

/**
 * This class contains some utility methods used for escaping xml strings as
 * well as for encoding/decoding http parameters.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiPageUtil
{
    /**
     * The code point ranges of the XML NameStartChar production, as inclusive bounds. ':' is left out since it is
     * only accepted when the caller enables it.
     */
    private static final int[][] NAME_START_CHAR_RANGES = {
        {'A', 'Z'},
        {'_', '_'},
        {'a', 'z'},
        {0xC0, 0xD6},
        {0xD8, 0xF6},
        {0xF8, 0x2FF},
        {0x370, 0x37D},
        {0x37F, 0x1FFF},
        {0x200C, 0x200D},
        {0x2070, 0x218F},
        {0x2C00, 0x2FEF},
        {0x3001, 0xD7FF},
        {0xF900, 0xFDCF},
        {0xFDF0, 0xFFFD},
        {0x10000, 0xEFFFF}};

    /**
     * The code point ranges the XML NameChar production adds to NameStartChar, as inclusive bounds.
     */
    private static final int[][] NAME_CHAR_RANGES = {
        {'-', '.'},
        {'0', '9'},
        {0xB7, 0xB7},
        {0x0300, 0x036F},
        {0x203F, 0x2040}};

    /**
     * Reserved symbols - see RFC 2396 (http://www.ietf.org/rfc/rfc2396.txt)
     */
    private static final char[] HTTP_RESERVED_SYMBOLS = {
        ';',
        '/',
        '?',
        ':',
        '@',
        '&',
        '=',
        '+',
        '$',
        ','};

    /**
     * Unreserved symbols - see RFC 2396 (http://www.ietf.org/rfc/rfc2396.txt)
     */
    private static final char[] HTTP_UNRESERVED_SYMBOLS = {
        '-',
        '_',
        '.',
        '!',
        '~',
        '*',
        '\'',
        '(',
        ')',
        '#'};

    static {
        Arrays.sort(HTTP_RESERVED_SYMBOLS);
        Arrays.sort(HTTP_UNRESERVED_SYMBOLS);
    }

    /**
     * Returns the decoded http string - all special symbols, replaced by
     * replaced by the %[HEX HEX] sequence, where [HEX HEX] is the hexadecimal
     * code of the escaped symbol will be restored to its original characters
     * (see RFC-2616 http://www.w3.org/Protocols/rfc2616/).
     *
     * @param str the string to decode
     * @return the decoded string.
     */
    public static String decodeHttpParams(String str)
    {
        if (str == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char ch = array[i];
            if (ch == '%') {
                if (i + 2 >= array.length) {
                    break;
                }
                int val = (array[++i] - '0');
                val <<= 4;
                val |= (array[++i] - '0');
                ch = (char) val;
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * Returns the encoded string - all special symbols will be replaced by
     * %[HEX HEX] sequence, where [HEX HEX] is the hexadecimal code of the
     * escaped symbol (see RFC-2616
     * http://www.w3.org/Protocols/rfc2616/rfc2616.html).
     *
     * @param str the string to encode
     * @return the encoded string.
     */
    public static String encodeHttpParams(String str)
    {
        if (str == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            char ch = array[i];
            if ((ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= '0' && ch <= '9')
                || Character.isDigit(ch)
                || Arrays.binarySearch(HTTP_RESERVED_SYMBOLS, ch) >= 0
                || Arrays.binarySearch(HTTP_UNRESERVED_SYMBOLS, ch) >= 0)
            {
                buf.append(array[i]);
            } else {
                buf.append('%').append(Integer.toHexString(array[i]));
            }
        }
        return buf.toString();
    }

    /**
     * Returns the escaped attribute string.
     *
     * @param str the string to escape
     * @return the escaped string.
     */
    public static String escapeXmlAttribute(String str)
    {
        return escapeXmlString(str, true);
    }

    /**
     * Returns the escaped string.
     *
     * @param str the string to escape
     * @return the escaped string.
     */
    public static String escapeXmlString(String str)
    {
        return escapeXmlString(str, false);
    }

    /**
     * Returns the escaped string.
     *
     * @param str the string to escape
     * @param escapeQuots if this flag is <code>true</code> then "'" and "\""
     * symbols also will be escaped
     * @return the escaped string.
     */
    public static String escapeXmlString(String str, boolean escapeQuots)
    {
        if (str == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        char[] array = str.toCharArray();
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '>'
                || array[i] == '&'
                || array[i] == '<'
                || (escapeQuots && (array[i] == '\'' || array[i] == '"')))
            {
                buf.append("&#x").append(Integer.toHexString(array[i])).append(';');
            } else {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * @return CDATA block corresponding to the given text
     */
    public static String getCDATA(String text)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<![CDATA[");
        int startPos = 0;
        while (startPos >= 0 && startPos < text.length()) {
            int id = text.indexOf("]]>", startPos);
            if (id >= 0) {
                buf.append(text.substring(startPos, id));
                buf.append("]]]]><![CDATA[>");
                startPos += id + "]]>".length();
            } else {
                buf.append(text.substring(startPos));
                startPos = -1;
            }
        }
        buf.append("]]>");
        return buf.toString();
    }

    /**
     * Returns <code>true</code> if the given value is a valid XML character.
     * <p>
     * See http://www.w3.org/TR/xml/#charsets
     * </p>
     *
     * @param ch the value to check
     * @return <code>true</code> if the given value is a valid XML character.
     */
    public static boolean isValidXmlChar(int ch)
    {
        return (ch == 0x9)
            || (ch == 0xA)
            || (ch == 0xD)
            || (ch >= 0x20 && ch <= 0xD7FF)
            || (ch >= 0xE000 && ch <= 0xFFFD)
            || (ch >= 0x10000 && ch <= 0x10FFFF);
    }

    /**
     * This method checks the given string and returns <code>true</code> if it is a valid XML name
     * <p>
     * See http://www.w3.org/TR/xml/#NT-Name.
     * </p>
     *
     * @param tagName the name to check
     * @param colonEnabled if this flag is <code>true</code> then this method accepts the ':' symbol in the name
     * @return <code>true</code> if the given string is a valid XML name
     */
    public static boolean isValidXmlName(String tagName, boolean colonEnabled)
    {
        boolean valid = false;
        int len = tagName != null ? tagName.length() : 0;
        // A supplementary character is made of two chars, so walk the name by code point rather than by char,
        // otherwise each half of a surrogate pair would be checked on its own and rejected.
        int i = 0;
        while (i < len) {
            int codePoint = tagName.codePointAt(i);
            if (i == 0) {
                valid = isValidXmlNameStartChar(codePoint, colonEnabled);
            } else {
                valid = isValidXmlNameChar(codePoint, colonEnabled);
            }
            if (!valid) {
                break;
            }
            i += Character.charCount(codePoint);
        }
        return valid;
    }

    /**
     * Returns <code>true</code> if the given value is a valid XML name
     * character.
     * <p>
     * See http://www.w3.org/TR/xml/#NT-NameChar.
     * </p>
     *
     * @param codePoint the code point to check
     * @param colonEnabled if this flag is <code>true</code> then this method
     * accepts the ':' symbol.
     * @return <code>true</code> if the given value is a valid XML name
     *         character
     * @since 18.8.0RC1
     */
    @Unstable
    public static boolean isValidXmlNameChar(int codePoint, boolean colonEnabled)
    {
        return isValidXmlNameStartChar(codePoint, colonEnabled) || isInRanges(codePoint, NAME_CHAR_RANGES);
    }

    /**
     * Returns <code>true</code> if the given value is a valid first character
     * of an XML name.
     * <p>
     * See http://www.w3.org/TR/xml/#NT-NameStartChar.
     * </p>
     *
     * @param codePoint the code point to check
     * @param colonEnabled if this flag is <code>true</code> then this method
     * accepts the ':' symbol.
     * @return <code>true</code> if the given value is a valid first character
     *         for an XML name
     * @since 18.8.0RC1
     */
    @Unstable
    public static boolean isValidXmlNameStartChar(int codePoint, boolean colonEnabled)
    {
        if (codePoint == ':') {
            return colonEnabled;
        }
        return isInRanges(codePoint, NAME_START_CHAR_RANGES);
    }

    private static boolean isInRanges(int codePoint, int[][] ranges)
    {
        for (int[] range : ranges) {
            if (codePoint >= range[0] && codePoint <= range[1]) {
                return true;
            }
        }
        return false;
    }
}

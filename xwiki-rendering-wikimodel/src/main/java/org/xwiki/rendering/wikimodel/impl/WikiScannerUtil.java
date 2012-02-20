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
package org.xwiki.rendering.wikimodel.impl;

import java.util.List;

import org.xwiki.rendering.wikimodel.WikiParameter;

/**
 * This class contains some utility methods used by scanners.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiScannerUtil
{
    /**
     * The default character to use has escaping char.
     */
    private static final char DEFAULT_ESCAPECHAR = '\u005c\u005c';

    /**
     * Extracts and returns a substring of the given string starting from the
     * given open sequence and finishing by the specified close sequence. This
     * method unescapes all symbols prefixed by the given escape symbol.
     *
     * @param str from this string the substring framed by the specified open
     * and close sequence will be returned
     * @param open the start substring sequence
     * @param close the closing substring sequence
     * @return a substring of the given string starting from the given open
     *         sequence and finishing by the specified close sequence
     */
    public static String extractSubstring(String str, String open, String close)
    {
        return extractSubstring(str, open, close, DEFAULT_ESCAPECHAR, true);
    }

    /**
     * Extracts and returns a substring of the given string starting from the
     * given open sequence and finishing by the specified close sequence. This
     * method unescapes all symbols prefixed by the given escape symbol.
     *
     * @param str from this string the substring framed by the specified open
     * and close sequence will be returned
     * @param open the start substring sequence
     * @param close the closing substring sequence
     * @param escape the escape symbol
     * @return a substring of the given string starting from the given open
     *         sequence and finishing by the specified close sequence
     */
    public static String extractSubstring(
        String str,
        String open,
        String close,
        char escape)
    {
        return extractSubstring(str, open, close, escape, true);
    }

    /**
     * Extracts and returns a substring of the given string starting from the
     * given open sequence and finishing by the specified close sequence. This
     * method unescapes all symbols prefixed by the given escape symbol.
     *
     * @param str from this string the substring framed by the specified open
     * and close sequence will be returned
     * @param open the start substring sequence
     * @param close the closing substring sequence
     * @param escape the escape symbol
     * @param cleanEscape indicate if the escaping char has to be removed.
     * Useful when the substring use the same escaping that the string.
     * @return a substring of the given string starting from the given open
     *         sequence and finishing by the specified close sequence
     */
    public static String extractSubstring(
        String str,
        String open,
        String close,
        char escape,
        boolean cleanEscape)
    {
        int i;
        StringBuffer buf = new StringBuffer();
        int len = str.length();
        for (i = 0; i < len; i++) {
            if (str.startsWith(open, i)) {
                i += open.length();
                break;
            }
        }

        boolean escaped = false;
        for (; i < len; i++) {
            if (escaped) {
                char ch = str.charAt(i);
                buf.append(ch);
                escaped = false;
            } else {
                if (str.startsWith(close, i)) {
                    break;
                }
                char ch = str.charAt(i);
                escaped = ch == escape;
                if (!escaped || !cleanEscape) {
                    buf.append(ch);
                }
            }
        }

        return buf.toString();
    }

    /**
     * This method copies to the given buffer all characters from the specified
     * position of the character array to the next delimiter position. This
     * method returns the next position just after the delimiter (or the length
     * of the array if no delimiters was found).
     *
     * @param array the array of character used as a source of chars
     * @param pos the start position in the array
     * @param delimiter the delimiter; this method copies all character from the
     * current position to the first delimiter sequence
     * @param buf the buffer where the content should be appended
     * @return the next position just after the delimiter of the end of the
     *         sequence
     */
    public static int getNextSequence(
        char[] array,
        int pos,
        char[] delimiter,
        StringBuffer buf)
    {
        buf.delete(0, buf.length());
        char quot = 0;
        for (; pos < array.length; pos++) {
            char ch = array[pos];
            if (quot != 0) {
                if (ch == quot) {
                    quot = 0;
                }
            } else {
                if (ch == '\"') {
                    quot = ch;
                } else {
                    // Checks if a new delimiter sequence was found in the
                    // current position
                    int i = skipSequence(array, pos, delimiter);
                    if (i > pos) {
                        pos = i;
                        break;
                    }
                }
            }
            buf.append(ch);
        }
        return pos;
    }

    /**
     * @param array from this array of bytes the next token will be returned
     * @param pos the current position in the array of bytes
     * @param buf to this buffer the extracted token value will be appended
     * @param trim this array is used to return the boolean flag specifying if
     * the value collected in the buffer should be trimmed or not
     * @param escapeChar the escaping character
     * @return the new position in the array after extracting of a new token
     */
    private static int getNextToken(
        char[] array,
        int pos,
        char[] delimiter,
        StringBuffer buf,
        boolean[] trim,
        char escapeChar)
    {
        buf.delete(0, buf.length());
        boolean escaped = false;
        if (pos < array.length && (array[pos] == '\'' || array[pos] == '"')) {
            trim[0] = false;
            char endChar = array[pos];
            pos++;
            for (; pos < array.length && (escaped || array[pos] != endChar); pos++) {
                if (escaped) {
                    buf.append(array[pos]);
                    escaped = false;
                } else {
                    escaped = array[pos] == escapeChar;
                    if (!escaped) {
                        buf.append(array[pos]);
                    }
                }
            }
            if (pos < array.length) {
                pos++;
            }
        } else {
            trim[0] = true;
            for (; pos < array.length; pos++) {
                if (escaped) {
                    buf.append(array[pos]);
                    escaped = false;
                } else {
                    if ((array[pos] == '=' || skipSequence(
                        array,
                        pos,
                        delimiter) > pos))
                    {
                        break;
                    }
                    if (array[pos] == '\'' || array[pos] == '"') {
                        break;
                    }

                    escaped = array[pos] == escapeChar;
                    if (!escaped) {
                        buf.append(array[pos]);
                    }
                }
            }
        }

        return pos;
    }

    /**
     * Indicate if the specified sequence starts from the given position in the
     * character array.
     *
     * @param array the array of characters
     * @param arrayPos the position of the first character in the array;
     * starting from this position the sequence should be skipped
     * @param sequence the sequence of characters to match
     * @return true if the sequence is found, false otherwise
     */
    public static boolean matchesSequence(
        char[] array,
        int arrayPos,
        char[] sequence)
    {
        int i;
        int j;
        for (i = arrayPos, j = 0; i < array.length && j < sequence.length; i++, j++) {
            if (array[i] != sequence[j]) {
                break;
            }
        }
        return j == sequence.length;
    }

    /**
     * Moves forward the current position in the array until the first not empty
     * character is found.
     *
     * @param array the array of characters where the spaces are searched
     * @param pos the current position in the array; starting from this position
     * the spaces will be searched
     * @param buf to this buffer all not empty characters will be added
     * @return the new position int the array of characters
     */
    private static int removeSpaces(char[] array, int pos, StringBuffer buf)
    {
        buf.delete(0, buf.length());
        for (; pos < array.length
            && (array[pos] == '=' || Character.isSpaceChar(array[pos])); pos++)
        {
            if (array[pos] == '=') {
                buf.append(array[pos]);
            }
        }
        return pos;
    }

    /**
     * Skips the specified sequence if it starts from the given position in the
     * character array.
     *
     * @param array the array of characters
     * @param arrayPos the position of the first character in the array;
     * starting from this position the sequence should be skipped
     * @param sequence the sequence of characters to skip
     * @return a new value of the character counter
     */
    public static int skipSequence(char[] array, int arrayPos, char[] sequence)
    {
        int i;
        int j;
        for (i = arrayPos, j = 0; i < array.length && j < sequence.length; i++, j++) {
            if (array[i] != sequence[j]) {
                break;
            }
        }
        return j == sequence.length ? i : arrayPos;
    }

    /**
     * Splits the given string into a set of key-value pairs; all extracted
     * values will be added to the given list
     *
     * @param str the string to split
     * @param list to this list all extracted values will be added
     */
    public static int splitToPairs(String str, List<WikiParameter> list)
    {
        return splitToPairs(str, list, null);
    }

    public static int splitToPairs(
        String str,
        List<WikiParameter> list,
        char escapeChar)
    {
        return splitToPairs(str, list, null, null, escapeChar);
    }

    /**
     * Splits the given string into a set of key-value pairs; all extracted
     * values will be added to the given list
     *
     * @param str the string to split
     * @param list to this list all extracted values will be added
     * @param delimiter a delimiter for individual key/value pairs
     */
    public static int splitToPairs(
        String str,
        List<WikiParameter> list,
        String delimiter)
    {
        return splitToPairs(str, list, delimiter, null);
    }

    public static int splitToPairs(
        String str,
        List<WikiParameter> list,
        String delimiter,
        String end)
    {
        return splitToPairs(str, list, delimiter, end, DEFAULT_ESCAPECHAR);
    }

    /**
     * Splits the given string into a set of key-value pairs; all extracted
     * values will be added to the given list
     *
     * @param str the string to split
     * @param list to this list all extracted values will be added
     * @param delimiter a delimiter for individual key/value pairs
     * @param end the ending sequence, if null it's not taken into account
     * @param escapeChar the escaping character
     * @return the index where parser stopped
     */
    public static int splitToPairs(
        String str,
        List<WikiParameter> list,
        String delimiter,
        String end,
        char escapeChar)
    {
        if (str == null) {
            return 0;
        }
        char[] array = str.toCharArray();
        if (delimiter == null) {
            delimiter = " ";
        }
        char[] delimiterArray = delimiter.toCharArray();
        char[] endArray = end != null ? end.toCharArray() : new char[0];
        StringBuffer buf = new StringBuffer();
        int i = 0;
        boolean[] trim = {false};
        for (; i < array.length; ) {
            String key = null;
            String value = null;
            i = removeSpaces(array, i, buf);
            if (i >= array.length) {
                break;
            }
            int prev = i;
            i = skipSequence(array, i, delimiterArray);
            if (i >= array.length) {
                break;
            }
            if (i > prev) {
                i = removeSpaces(array, i, buf);
                if (i >= array.length) {
                    break;
                }
            }
            // if provided ending sequence is found, we stop parsing
            if (end != null && matchesSequence(array, i, endArray)) {
                break;
            }

            i = getNextToken(array, i, delimiterArray, buf, trim, escapeChar);
            key = buf.toString().trim();

            i = removeSpaces(array, i, buf);
            if (buf.indexOf("=") >= 0) {
                i = getNextToken(
                    array,
                    i,
                    delimiterArray,
                    buf,
                    trim,
                    escapeChar);
                value = buf.toString();
                if (trim[0]) {
                    value = value.trim();
                }
            }

            WikiParameter entry = new WikiParameter(key, value);
            list.add(entry);
        }

        return i;
    }

    /**
     * Unescapes the given string and returns the result. This method uses the
     * default escape symbol (see {@link #DEFAULT_ESCAPECHAR}).
     *
     * @param str the string to unescape
     * @return an unescaped string
     */
    public static String unescape(String str)
    {
        return unescape(str, DEFAULT_ESCAPECHAR);
    }

    /**
     * Unescapes the given string and returns the result.
     *
     * @param str the string to unescape
     * @param escape the symbol used to escape characters
     * @return an unescaped string
     */
    public static String unescape(String str, char escape)
    {
        if (str == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        char[] array = str.toCharArray();
        boolean escaped = false;
        for (int i = 0; i < array.length; i++) {
            char ch = array[i];
            if (escaped) {
                buf.append(ch);
                escaped = false;
            } else {
                escaped = (ch == escape);
                if (!escaped) {
                    buf.append(ch);
                }
            }
        }
        return buf.toString();
    }
}

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
package org.xwiki.rendering.wikimodel.xwiki.xwiki20;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XWikiScannerUtil
{
    public static final char ESCAPECHAR = '~';

    /**
     * To have }}} or {{{ inside inline block we need to escape it in some
     * condition. This method remove this escaping to send the correct text to
     * the event.
     */
    public static String unescapeVerbatim(String content)
    {
        StringBuffer unescapedContent = new StringBuffer();

        boolean escaped = false;
        char[] buff = content.toCharArray();
        for (int i = 0; i < buff.length; ++i) {
            if (!escaped) {
                if (buff[i] == '~') {
                    escaped = true;
                    continue;
                }
            } else {
                if (i < (i = matchVerbatimSyntax(buff, i, '{'))) {
                    unescapedContent.append("{{{");
                    escaped = false;
                    continue;
                } else if (i < (i = matchVerbatimSyntax(buff, i, '}'))) {
                    unescapedContent.append("}}}");
                    escaped = false;
                    continue;
                } else {
                    unescapedContent.append('~');
                }

                escaped = false;
            }

            unescapedContent.append(buff[i]);
        }

        return unescapedContent.toString();
    }

    private static int matchVerbatimSyntax(char buff[], int currentIndex,
        char syntax)
    {

        int i = currentIndex;
        boolean escaped = true;
        for (int j = 0; i < buff.length && j < 3; ++i) {
            if (!escaped) {
                if (buff[i] == syntax) {
                    if (++j == 3) {
                        return i;
                    }
                } else if (buff[i] == '~') {
                    escaped = true;
                }
            } else {
                if (buff[i] == syntax) {
                    if (++j == 3) {
                        return i;
                    }
                } else {
                    break;
                }

                escaped = false;
            }
        }

        return currentIndex;
    }
}

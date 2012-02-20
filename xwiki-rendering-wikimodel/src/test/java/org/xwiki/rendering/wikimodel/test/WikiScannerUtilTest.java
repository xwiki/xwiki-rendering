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
package org.xwiki.rendering.wikimodel.test;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerUtil;

import junit.framework.TestCase;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WikiScannerUtilTest extends TestCase
{
    /**
     * @param name
     */
    public WikiScannerUtilTest(String name)
    {
        super(name);
    }

    public void testParams()
    {
        testParams("a b c d", " ", "a", null, "b", null, "c", null, "d", null);
        testParams("a=b c=d", " ", "a", "b", "c", "d");
        testParams("a=b", "|", "a", "b");
        testParams(" a = b ", "|", "a", "b");
        testParams(" a = b c ", "|", "a", "b c");
        testParams("a=b|c=d", "|", "a", "b", "c", "d");
        testParams("a b c d", "|", "a b c d", null);
        testParams("x=b d e | y= f g h ", "|", "x", "b d e", "y", "f g h");
        testParams(" x = b d e | y = f g h ", "|", "x", "b d e", "y", "f g h");
        testParams(
            " x = ' b d e ' | y = ' f g h ' ",
            "|",
            "x",
            " b d e ",
            "y",
            " f g h ");
        testParams(
            "   x    =    ' b d e '     y =    ' f g h '    ",
            " ",
            "x",
            " b d e ",
            "y",
            " f g h ");
    }

    private void testParams(String str, String delim, String... pairs)
    {
        WikiParameters params = new WikiParameters(str, delim);
        int size = params.getSize();
        assertEquals(pairs.length / 2, size);
        for (int i = 0; i < size; i++) {
            String key = pairs[i * 2];
            String value = pairs[i * 2 + 1];
            WikiParameter pair = params.getParameter(i);
            assertNotNull(pair);
            assertEquals(key, pair.getKey());
            assertEquals(value, pair.getValue());
        }
    }

    /**
     *
     */
    public void testSubstringExtract()
    {
        testSubstringExtract1("123", "");
        testSubstringExtract1("123()", "");
        testSubstringExtract1("()", "");
        testSubstringExtract1("(abc)", "abc");
        testSubstringExtract1("123(abc)456", "abc");
        testSubstringExtract1("123(a\\(b\\)c)456", "a(b)c");
        testSubstringExtract1("123(a\\(b\\)c)456", "a\\(b\\)c", false);

        testSubstringExtract2("123{{}}", "");
        testSubstringExtract2("{{}}", "");
        testSubstringExtract2("{{abc}}", "abc");
        testSubstringExtract2("123{{abc}}456", "abc");
        testSubstringExtract2("123{{a\\(b\\)c}}456", "a(b)c");
        testSubstringExtract2("123{{a\\(b\\)c}}456", "a\\(b\\)c", false);
        testSubstringExtract2("123{{a\\{{b\\}}c}}456", "a{{b}}c");
        testSubstringExtract2("123{{a\\{{b\\}}c}}456", "a\\{{b\\}}c", false);
    }

    private void testSubstringExtract1(String str, String result)
    {
        String test = WikiScannerUtil.extractSubstring(str, "(", ")", '\\');
        assertEquals(result, test);
    }

    private void testSubstringExtract1(
        String str,
        String result,
        boolean cleanEscape)
    {
        String test = WikiScannerUtil.extractSubstring(
            str,
            "(",
            ")",
            '\\',
            cleanEscape);
        assertEquals(result, test);
    }

    private void testSubstringExtract2(String str, String result)
    {
        String test = WikiScannerUtil.extractSubstring(str, "{{", "}}", '\\');
        assertEquals(result, test);
    }

    private void testSubstringExtract2(
        String str,
        String result,
        boolean cleanEscape)
    {
        String test = WikiScannerUtil.extractSubstring(
            str,
            "{{",
            "}}",
            '\\',
            cleanEscape);
        assertEquals(result, test);
    }
}

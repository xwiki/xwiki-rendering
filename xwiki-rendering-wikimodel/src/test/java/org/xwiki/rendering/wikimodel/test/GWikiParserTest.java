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

import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.gwiki.GWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class GWikiParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public GWikiParserTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new GWikiParser();
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("*bold*");
        test("~~strike~~");
        test("_italic_");
        test("^superscript^");
        test(",,subscript,,");

        test("normal*bold_bold-italic*italic_normal");

        test("*bold");
        test("~~strike");
        test("_italic");
        test("^superscript");
        test(",,subscript");

        test("bold*");
        test("strike~~");
        test("italic_");
        test("superscript^");
        test("subscript,,");
    }

    /**
     * @throws WikiParserException
     */
    public void testHeaders() throws WikiParserException
    {
        test("===Header===");
        test("\n===Header===\n * list item");
        test("before\n=== Header ===\nafter");
        test("before\n=== Header \nafter");
        test("This is not a header: ===");
    }

    /**
     * @throws WikiParserException
     */
    public void testLists() throws WikiParserException
    {
        test(" * item one\n"
            + " * item two\n"
            + "   # item three\n"
            + "   # item four\n"
            + " * item five - first line\n"
            + "   item five - second line\n"
            + " * item six\n"
            + "   is on multiple\n"
            + "   lines");
    }

    /**
     * @throws WikiParserException
     */
    public void testParagraphs() throws WikiParserException
    {
        test("First paragraph.\n"
            + "Second line of the same paragraph.\n"
            + "\n"
            + "The second paragraph");
    }

    /**
     * @throws WikiParserException
     */
    public void testProperties() throws WikiParserException
    {
        test(
            "#toto hello  world\n123",
            "<div class='wikimodel-property' url='toto'><p>hello  world</p>\n</div>\n<p>123</p>");
        test(
            "#prop1 value1\n#prop2 value2",
            ""
                + "<div class='wikimodel-property' url='prop1'><p>value1</p>\n</div>\n"
                + "<div class='wikimodel-property' url='prop2'><p>value2</p>\n</div>");
        test(
            "#prop1 value1\nparagraph\n#prop2 value2",
            ""
                + "<div class='wikimodel-property' url='prop1'><p>value1</p>\n</div>\n"
                + "<p>paragraph</p>\n"
                + "<div class='wikimodel-property' url='prop2'><p>value2</p>\n</div>");
    }

    /**
     * @throws WikiParserException
     */
    public void testQuot() throws WikiParserException
    {
        test("This is a paragraph\n"
            + "\n"
            + " and this is a quotations\n"
            + " the second line");
        test(" First\n" + "  Second\n" + "   Third");
        test("             \n\n"
            + "      First\n"
            + "      Second\n"
            + "      Third");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
        test("before http://www.foo.bar/com after");
        test("before [toto] after");
    }

    /**
     * @throws WikiParserException
     */
    public void testTables() throws WikiParserException
    {
        test("|| cell1.1 || cell1.2\n" + "|| cell 2.1 || cell 2.2");
        test("abc || cde");
        test("||a\nb\nc || c\nd\ne\n{{{\n Hello\n * World}}} \n x ");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimBlocks() throws WikiParserException
    {
        test("{{{verbatim}}}");
        test("{{{verbatim");
        test("{{{{{{verbatim");
        test("{{{{{{verbatim}}}");
        test("{{{{{{verbatim}}}}}}");
        test("{{{before{{{verbatim}}}after}}}");
        test("{{{before{{{123{{{verbatim}}}456}}}after}}}");
        test("{{{verbatim}}}}}} - the three last symbols should be in a paragraph");

        test("before{{{verbatim}}}after");

        test("abc \n{{{ 123\n  CDE\n   345 }}} efg");
        test("abc {{{ 123\n  CDE\n   345 }}} efg");
        test("abc\n{{{before{{{ inside }}}after}}} efg");
        test("`verbatim`");
        test("before`verbatim`after");
        test("`just like this...");
    }
}

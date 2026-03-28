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

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.creole.CreoleWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
class CreoleWikiParserTest extends AbstractWikiParserTest
{
    @Override
    protected IWikiParser newWikiParser()
    {
        return new CreoleWikiParser();
    }

    @Test
    void testFormats() throws WikiParserException
    {
        test("**bold**", "<p><strong>bold</strong></p>");
        test("//italic//", "<p><em>italic</em></p>");
        test("before{{{inside}}}after", "<p>before<tt class=\"wikimodel-verbatim\">inside</tt>after</p>");

        // Mixed styles
        test(
            "normal**bold//bold-italic**italic//normal",
            "<p>normal<strong>bold</strong><strong><em>bold-italic</em></strong><em>italic</em>normal</p>");

        // Not formatting
        test("_nothing special_", "<p>_nothing special_</p>");
        test(
            "http://www.foo.bar",
            "<p><a href='http://www.foo.bar' class='wikimodel-freestanding'>http://www.foo.bar</a></p>");
    }

    @Test
    void testHeaders() throws WikiParserException
    {
        test("=Header1=", "<h1>Header1</h1>");
        test("==Header2==", "<h2>Header2</h2>");
        test("===Header3===", "<h3>Header3</h3>");
        test("====Header4====", "<h4>Header4</h4>");
        test("=====Header5====", "<h5>Header5</h5>");
        test("======Header6======", "<h6>Header6</h6>");
        test("=======Header6=======", "<h6>Header6</h6>");

        test("\n===Header===\n * list item");
        test("before\n=== Header ===\nafter");
        test("before\n=== Header \nafter");
        test("This is not a header: ===");
        test("== Header**bold** //italic// ==");
    }

    @Test
    void testLineBreaks() throws WikiParserException
    {
        test("line\\\\break");
        test("not\\a\\break");
    }

    @Test
    void testLists() throws WikiParserException
    {
        test("\n**item one**", "<p><strong>item one</strong></p>");
        test("**item one**\n\n**item two**", """
                <p><strong>item one</strong></p>
                <p><strong>item two</strong></p>""");

        test(" * item one", "<ul>\n  <li>item one</li>\n</ul>");
        test("* item one\n** item two", """
                <ul>
                  <li>item one<ul>
                  <li>item two</li>
                </ul>
                </li>
                </ul>""");

        test("** item one", """
                <ul>
                  <li><ul>
                  <li>item one</li>
                </ul>
                </li>
                </ul>""");
        test("*** item one", """
                <ul>
                  <li><ul>
                  <li><ul>
                  <li>item one</li>
                </ul>
                </li>
                </ul>
                </li>
                </ul>""");
        test("*## item one", """
                <ul>
                  <li><ol>
                  <li><ol>
                  <li>item one</li>
                </ol>
                </li>
                </ol>
                </li>
                </ul>""");
        test("*##item one", """
                <ul>
                  <li><ol>
                  <li><ol>
                  <li>item one</li>
                </ol>
                </li>
                </ol>
                </li>
                </ul>""");

        // Two "**" symbols at the beginning of the line are interpreted as a bold!!!
        test("**item one", "<p><strong>item one</strong></p>");
        test(
            " **item one",
            "<blockquote>\n<strong>item one</strong>\n</blockquote>");
    }

    @Test
    void testParagraphs() throws WikiParserException
    {
        test("""
            First paragraph.
            Second line of the same paragraph.

            The second paragraph""");
    }

    @Test
    void testProperties() throws WikiParserException
    {
        test("#toto hello  world\n123");
        test("#prop1 value1\n#prop2 value2");
    }

    @Test
    void testQuot() throws WikiParserException
    {
        test("This is a paragraph\n\n and this is a quotations\n the second line");
    }

    @Test
    void testReferences() throws WikiParserException
    {
        test(
            "before http://www.foo.bar/com after",
            "<p>before <a href='http://www.foo.bar/com' class='wikimodel-freestanding'>http://www.foo.bar/com</a> after</p>");
        test(
            "before this+is+a+reference:to_here after",
            "<p>before <a href='this+is+a+reference:to_here' class='wikimodel-freestanding'>this+is+a+reference:to_here</a> after</p>");
        test(
            "before [[toto]] after",
            "<p>before <a href='toto' class='wikimodel-freestanding'>toto</a> after</p>");

        test("[[link]]", "<p><a href='link' class='wikimodel-freestanding'>link</a></p>");
        test(
            "[[MyBigPage|Go to my page]]",
            "<p><a href='MyBigPage'>Go to my page</a></p>");
        test(
            "[[http://www.wikicreole.org/]]",
            "<p><a href='http://www.wikicreole.org/' class='wikimodel-freestanding'>http://www.wikicreole.org/</a></p>");
        test(
            "http://www.rawlink.org/, http://www.another.rawlink.org",
            "<p><a href='http://www.rawlink.org/' class='wikimodel-freestanding'>http://www.rawlink.org/</a>, <a href='http://www.another.rawlink.org' class='wikimodel-freestanding'>http://www.another.rawlink.org</a></p>");
        test(
            "[[http://www.wikicreole.org/|Visit the WikiCreole website]]",
            "<p><a href='http://www.wikicreole.org/'>Visit the WikiCreole website</a></p>");
        test("[[Weird Stuff|**Weird** // Stuff//]]");
        test(
            "[[Ohana:WikiFamily]]",
            "<p><a href='Ohana:WikiFamily' class='wikimodel-freestanding'>Ohana:WikiFamily</a></p>");

        // Not a reference
        test("before [toto] after", "<p>before [toto] after</p>");

        test("before this+is+a+reference:to_here after");
        test("before this+is+not+a+reference: to_here after");
        test("before|foo:bar|after");
        test("before main:wiki after");
        test("before main:**wiki** after");
        test("before http://www.google.com:8080 after");
        test("before http://www.google.com?q=Hello+World!#fragmenté~~ after");

        test("Bad reference: http://923.43.23.11:8080/toto ");
        test("Bad reference: http:|sdf after");
    }

    @Test
    void testTables() throws WikiParserException
    {
        test("""
            |= Header 1.1 |= Header 1.2
            | Cell 2.1 | Cell 2.2
            | Cell 3.1 |= Head 3.2""", """
            <table><tbody>
              <tr><th> Header 1.1 </th><th> Header 1.2</th></tr>
              <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>
              <tr><td> Cell 3.1 </td><th> Head 3.2</th></tr>
            </tbody></table>""");
        test("abc || cde");
    }

    @Test
    void testVerbatimeBlocks() throws WikiParserException
    {
        test("abc \n{{{ 123\n  CDE\n   345 }}} efg");
        test("abc {{{ 123\n  CDE\n   345 }}} efg");
        test("abc\n{{{\n{{{\n 123 \n }}}\n}}} efg");
        test("`verbatime`");
        test("`just like this...");
    }
}
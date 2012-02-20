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

import java.io.StringReader;

import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.IWikiPrinter;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.mediawiki.MediaWikiParser;
import org.xwiki.rendering.wikimodel.xhtml.PrintListener;

/**
 * @author MikhailKotelnikov
 */
public class MediawikiParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public MediawikiParserTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new MediaWikiParser();
    }

    /**
     * @param string
     * @param control
     * @throws WikiParserException
     */
    protected void doCustomTest(String string, String control) throws WikiParserException
    {
        println("==================================================");
        StringReader reader = new StringReader(string);
        IWikiParser parser = newWikiParser();
        final StringBuffer buf = new StringBuffer();

        IWikiPrinter printer = newPrinter(buf);
        IWemListener listener = new PrintListener(printer)
        {
            @Override
            public void onSpace(String str)
            {
                print("[" + str + "]");
            }

            @Override
            public void onWord(String str)
            {
                print("{" + str + "}");
            }
        };
        parser.parse(reader, listener);
        String test = buf.toString();
        println(test);
        checkResults(control, test);
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("before '''''bold-italic'''''  after", "<p>before <strong><em>bold-italic</em></strong>  after</p>");
        test("before '''bold''' after", "<p>before <strong>bold</strong> after</p>");
        test("before ''italic'' after", "<p>before <em>italic</em> after</p>");
        test("2H<sup>+</sup> + SO<sup>-</sup><sub>4</sub>", "<p>2H<sup>+</sup> + SO<sup>-</sup><sub>4</sub></p>");
        test("before <s>strike</s> after", "<p>before <strike>strike</strike> after</p>");
        test("before <sup>sup</sup> after", "<p>before <sup>sup</sup> after</p>");
        test("before <sub>sub</sub> after", "<p>before <sub>sub</sub> after</p>");
        test("before <tt>Fixed width text</tt> after", "<p>before <tt>Fixed width text</tt> after</p>");
        test("<del>Strikethrough</del>", "<p><del>Strikethrough</del></p>");
        test("<code>source code</code>", "<p><code>source code</code></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testTables() throws WikiParserException
    {

        test("{|\n|table cell\n|}", "<table><tbody>\n  <tr><td>table cell</td></tr>\n</tbody></table>");
        test("{|\n|cell1||cell2||cell3\n|}", "<table><tbody>\n"
            + "  <tr><td>cell1</td><td>cell2</td><td>cell3</td></tr>\n" + "</tbody></table>");
        test("{|\n|cell1\n|cell2\n|cell3\n|}", "<table><tbody>\n"
            + "  <tr><td>cell1</td><td>cell2</td><td>cell3</td></tr>\n" + "</tbody></table>");
        test("{|\n" + "| Cell 1.1 || Cell 1.2 \n" + "|-\n" + "| Cell 2.1 || Cell 2.2 \n" + "|-\n"
            + "| Cell 3.1 || Cell 3.2 \n" + "|}", "<table><tbody>\n"
            + "  <tr><td> Cell 1.1 </td><td> Cell 1.2 </td></tr>\n"
            + "  <tr><td> Cell 2.1 </td><td> Cell 2.2 </td></tr>\n"
            + "  <tr><td> Cell 3.1 </td><td> Cell 3.2 </td></tr>\n" + "</tbody></table>");
        test("before\n" + "{|\n" + "| Cell 1.1 || Cell 1.2 \n" + "|-\n" + "| Cell 2.1 || Cell 2.2 \n" + "|-\n"
            + "| Cell 3.1 || Cell 3.2 \n" + "|}\n" + "after", "<p>before</p>\n" + "<table><tbody>\n"
            + "  <tr><td> Cell 1.1 </td><td> Cell 1.2 </td></tr>\n"
            + "  <tr><td> Cell 2.1 </td><td> Cell 2.2 </td></tr>\n"
            + "  <tr><td> Cell 3.1 </td><td> Cell 3.2 </td></tr>\n" + "</tbody></table>\n" + "<p>after</p>");

        test("{| border=\"1\"\n" + "|Orange\n" + "|Apple\n" + "|12,333.00\n" + "|-\n" + "|Bread\n" + "|Pie\n"
            + "|500.00\n" + "|-\n" + "|Butter\n" + "|Ice cream\n" + "|1.00\n" + "|}\n", "<table border='1'><tbody>\n"
            + "  <tr><td>Orange</td><td>Apple</td><td>12,333.00</td></tr>\n"
            + "  <tr><td>Bread</td><td>Pie</td><td>500.00</td></tr>\n"
            + "  <tr><td>Butter</td><td>Ice cream</td><td>1.00</td></tr>\n" + "</tbody></table>");
    }

    /**
     * @throws WikiParserException
     */
    public void testEscape() throws WikiParserException
    {
        test("[htt://abcd reference]");
        test("[[not a reference]");

        test("~First letter is escaped", "<p><span class='wikimodel-escaped'>F</span>irst letter is escaped</p>");
        test("~[not a reference]", "<p><span class='wikimodel-escaped'>[</span>not a reference]</p>");
        test("~~escaped tilda", "<p><span class='wikimodel-escaped'>~</span>escaped tilda</p>");
        test("~ just a tilda because there is an espace after this tilda...",
            "<p>~ just a tilda because there is an espace after this tilda...</p>");

        test("!Heading\n~!Not a heading\n!Heading again!");
    }

    /**
     * @throws WikiParserException
     */
    public void testHeaders() throws WikiParserException
    {
        test("=Header1=", "<h1>Header1</h1>");
        test("==Header2==", "<h2>Header2</h2>");
        test("===Header3===", "<h3>Header3</h3>");
        test("====Header4====", "<h4>Header4</h4>");
        test("=====Header5====", "<h5>Header5</h5>");
        test("======Header6======", "<h6>Header6</h6>");
        test("=======Header6(?)=======", "<h6>Header6(?)</h6>");

        test("\n===Header===\n * list item", "<h3>Header</h3>\n<blockquote>\n* list item\n</blockquote>");
        test("before\n=== Header ===\nafter", "<p>before</p>\n<h3>Header </h3>\n<p>after</p>");
        test("before\n=== Header \nafter", "<p>before</p>\n<h3>Header </h3>\n<p>after</p>");
        test("This is not a header: ===", "<p>This is not a header: ===</p>");
        test("== Header'''bold''' ''italic'' ==", "<h2>Header<strong>bold</strong> <em>italic</em> </h2>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHorLine() throws WikiParserException
    {
        test("----", "<hr />");
        test("-------", "<hr />");
        test("-----------", "<hr />");
        test(" -----------", "<blockquote>\n-----------\n</blockquote>");
        test("---abc", "<p>---abc</p>");
    }

    public void testImage() throws WikiParserException
    {

        test(
            "[[Image:Yahoo Headquarters.jpg|thumb|right|250px|Yahoo headquarters in Sunnyvale]]",
            "<p><img src='Yahoo Headquarters.jpg' format='thumb' align='right' width='250px' alt='Yahoo headquarters in Sunnyvale' title='Yahoo headquarters in Sunnyvale'/></p>");
        test(
            "[[Image:Jerry Yang and David Filo.jpg|thumb|right|250px|Yahoo! co-founders [[Jerry Yang (entrepreneur)|Jerry Yang]] (left) and [[David Filo]] (right)]]");
        test("[[File:example.jpg|frameless|border|caption]]",
            "<p><img src='example.jpg' format='frameless' format='border' alt='caption' title='caption'/></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testLineBreak() throws WikiParserException
    {
        test("abc\\\\def", "<p>abc\\\\def</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testLists() throws WikiParserException
    {

        test("*first", "<ul>\n  <li>first</li>\n</ul>");
        test("* first", "<ul>\n  <li>first</li>\n</ul>");
        test("** second", "<ul>\n  <li><ul>\n  <li>second</li>\n</ul>\n</li>\n</ul>");
        test("*item one\n" + "* item two\n" + "*#item three\n" + "*# item four\n" + "* item five - first line\n"
            + "   item five - second line\n" + "* item six\n" + "  is on multiple\n" + " lines", "<ul>\n"
            + "  <li>item one</li>\n" + "  <li>item two<ol>\n" + "  <li>item three</li>\n" + "  <li>item four</li>\n"
            + "</ol>\n" + "</li>\n" + "  <li>item five - first line</li>\n" + "</ul>\n" + "<blockquote>\n"
            + "item five - second line\n" + "</blockquote>\n" + "<ul>\n" + "  <li>item six</li>\n" + "</ul>\n"
            + "<blockquote>\n" + "is on multiple\n" + "</blockquote>\n" + "<blockquote>\n" + "lines\n"
            + "</blockquote>");

        test(";a: b\n;c: d", "<dl>\n" + "  <dt>a</dt>\n" + "  <dd>b</dd>\n" + "  <dt>c</dt>\n" + "  <dd>d</dd>\n"
            + "</dl>");

        test(";term:  definition", "<dl>\n  <dt>term</dt>\n  <dd>definition</dd>\n</dl>");
        test(";:just definition", "<dl>\n  <dd>just definition</dd>\n</dl>");
        test(";just term", "<dl>\n  <dt>just term</dt>\n</dl>");
        test(";:", "<dl>\n  <dd></dd>\n</dl>");
        test("*#;:", "<ul>\n" + "  <li><ol>\n" + "  <li><dl>\n" + "  <dd></dd>\n" + "</dl>\n" + "</li>\n" + "</ol>\n"
            + "</li>\n" + "</ul>");
        test("*unordered\n*#ordered\n*#;term\n*#:definition", "<ul>\n" + "  <li>unordered<ol>\n"
            + "  <li>ordered<dl>\n" + "  <dt>term</dt>\n" + "  <dd>definition</dd>\n" + "</dl>\n" + "</li>\n"
            + "</ol>\n" + "</li>\n" + "</ul>");

        test(";term one: definition one\n" + ";term two: definition two\n" + ";term three: definition three", "<dl>\n"
            + "  <dt>term one</dt>\n" + "  <dd>definition one</dd>\n" + "  <dt>term two</dt>\n"
            + "  <dd>definition two</dd>\n" + "  <dt>term three</dt>\n" + "  <dd>definition three</dd>\n" + "</dl>");

        test(";One,\ntwo,\nbucle my shoes...:\n" + "...Three\nfour,\nClose the door\n"
            + ";Five,\nSix: Pick up\n sticks\n\ntam-tam, pam-pam...", "<dl>\n" + "  <dt>One,\n" + "two,\n"
            + "bucle my shoes...</dt>\n" + "  <dd>\n" + "...Three\n" + "four,\n" + "Close the door</dd>\n"
            + "  <dt>Five,\n" + "Six</dt>\n" + "  <dd>Pick up</dd>\n" + "</dl>\n" + "<blockquote>\n" + "sticks\n"
            + "</blockquote>\n" + "<p>tam-tam, pam-pam...</p>");

        test(":definition: ''definition''", "<dl>\n  <dd>definition: <em>definition</em></dd>\n</dl>");

        test(":a\n::b\n:::c\nnot a definition: ''definition''", "<dl>\n" + "  <dd>a<dl>\n" + "  <dd>b<dl>\n"
            + "  <dd>c\n" + "not a definition: <em>definition</em></dd>\n" + "</dl>\n" + "</dd>\n" + "</dl>\n"
            + "</dd>\n" + "</dl>");

        test(";term: definition: but this is just a text...\n:and this is a definition again.\n*: and this as well",
            "<dl>\n" + "  <dt>term</dt>\n" + "  <dd>definition: but this is just a text...</dd>\n"
                + "  <dd>and this is a definition again.</dd>\n" + "</dl>\n" + "<ul>\n" + "  <li><dl>\n"
                + "  <dd>and this as well</dd>\n" + "</dl>\n" + "</li>\n" + "</ul>");

        test(";term: ''definition''", "<dl>\n" + "  <dt>term</dt>\n" + "  <dd><em>definition</em></dd>\n" + "</dl>");

        // using an inlined macro as term
        test("; __term__ : ''definition''", "<dl>\n"
            + "  <dt><span class='wikimodel-macro' macroName='unhandled'><![CDATA[__term__]]></span></dt>\n"
            + "  <dd><em>definition</em></dd>\n" +
            "</dl>");

        // again using an inlined macro
        test("this is not a definition --\n" + "<p>this is not a definition --</p>\n"
            + "<blockquote>\n"
            +
            ";<span class='wikimodel-macro' macroName='unhandled'><![CDATA[__not__]]></span> a term: <em>not</em> a definition\n"
            + "</blockquote>\n"
            + "<hr />\n"
            + "<p>toto</p>\n");

        test("#list item A1\n" + "##list item B1\n" + "##list item B2\n" + "#:continuing list item A1\n"
            + "#list item A2", "<ol>\n" + "  <li>list item A1<ol>\n" + "  <li>list item B1</li>\n"
            + "  <li>list item B2</li>\n" + "</ol>\n" + "<dl>\n" + "  <dd>continuing list item A1</dd>\n" + "</dl>\n"
            + "</li>\n" + "  <li>list item A2</li>\n" + "</ol>");

        test("* ''Unordered lists'' are easy to do:\n" + "** Start every line with a star.\n"
            + "*** More stars indicate a deeper level.\n" + "*: Previous item continues.\n" + "** A newline\n"
            + "* in a list\n" + "marks the end of the list.\n" + "* Of course you can start again.", "<ul>\n"
            + "  <li><em>Unordered lists</em> are easy to do:<ul>\n" + "  <li>Start every line with a star.<ul>\n"
            + "  <li>More stars indicate a deeper level.</li>\n" + "</ul>\n" + "</li>\n" + "</ul>\n" + "<dl>\n"
            + "  <dd>Previous item continues.</dd>\n" + "</dl>\n" + "<ul>\n" + "  <li>A newline</li>\n" + "</ul>\n"
            + "</li>\n" + "  <li>in a list\n" + "marks the end of the list.</li>\n"
            + "  <li>Of course you can start again.</li>\n" + "</ul>");

        test("* You can even do mixed lists\r\n" + "*# and nest them\r\n" + "*# inside each other\r\n"
            + "*#* or break lines<br>in lists.\r\n" + "*#; definition lists\r\n" + "*#: can be \r\n"
            + "*#:; nested : too", "<ul>\n" + "  <li>You can even do mixed lists<ol>\n" + "  <li>and nest them</li>\n"
            + "  <li>inside each other<ul>\n" + "  <li>or break lines<br />in lists.</li>\n" + "</ul>\n" + "<dl>\n"
            + "  <dt>definition lists</dt>\n" + "  <dd>can be <dl>\n" + "  <dt>nested</dt>\n" + "  <dd>too</dd>\n"
            + "</dl>\n" + "</dd>\n" + "</dl>\n" + "</li>\n" + "</ol>\n" + "</li>\n" + "</ul>");
    }

    /**
     * @throws WikiParserException
     */
    public void testParagraphs() throws WikiParserException
    {
        test("First paragraph.\n" + "Second line of the same paragraph.\n" + "\n" + "The second paragraph",
            "<p>First paragraph.\nSecond line of the same paragraph.</p>\n<p>The second paragraph</p>");

        test("\n<toto", "<p>&lt;toto</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testQuot() throws WikiParserException
    {
        test("This is a paragraph\n\n and this is a quotations\n the second line", "<p>This is a paragraph</p>\n"
            + "<blockquote>\n" + "and this is a quotations\n" + "</blockquote>\n" + "<blockquote>\n"
            + "the second line\n" + "</blockquote>");

        // Verify that an empty quot line works fine
        test(" first quote line\n \n", "<blockquote>\n" + "first quote line\n" + "</blockquote>\n" + "<blockquote>\n"
            + "\n" + "</blockquote>");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
        test("before http://www.foo.bar/com after",
            "<p>before <a href='http://www.foo.bar/com' class='wikimodel-freestanding'>http://www.foo.bar/com</a> after</p>");
        test("before [toto] after", "<p>before <a href='toto' class='wikimodel-freestanding'>toto</a> after</p>");
        test("before wiki:Hello after",
            "<p>before <a href='wiki:Hello' class='wikimodel-freestanding'>wiki:Hello</a> after</p>");
        test("before wiki~:Hello after", "<p>before wiki<span class='wikimodel-escaped'>:</span>Hello after</p>");
        test("before [#local ancor] after",
            "<p>before <a href='#local ancor' class='wikimodel-freestanding'>#local ancor</a> after</p>");
        test("not [[a reference] at all!", "<p>not <span class='wikimodel-escaped'>[</span>a reference] at all!</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testInternalLinks() throws WikiParserException
    {
        test("[[Main Page]]", "<p><a href='Main Page' class='wikimodel-freestanding'>Main Page</a></p>");
        test("[[Main Page|different text]]", "<p><a href='Main Page'>different text</a></p>");
        test("[[Internationalisation]]s",
            "<p><a href='Internationalisation' class='wikimodel-freestanding'>Internationalisation</a>s</p>");
        test("[[/example]]", "<p><a href='/example' class='wikimodel-freestanding'>/example</a></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testExternalLinks() throws WikiParserException
    {
        test("http://mediawiki.org",
            "<p><a href='http://mediawiki.org' class='wikimodel-freestanding'>http://mediawiki.org</a></p>");
        test("[http://mediawiki.org MediaWiki]", "<p><a href='http://mediawiki.org'>MediaWiki</a></p>");
        test("[http://mediawiki.org]",
            "<p><a href='http://mediawiki.org' class='wikimodel-freestanding'>http://mediawiki.org</a></p>");
        test("[mailto:info@example.org email me]", "<p><a href='mailto:info@example.org'>email me</a></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testCitation() throws WikiParserException
    {
        test("<ref>simple citation</ref>", "<p><ref>simple citation</ref></p>");
        test("<ref name=\"CITE\"> Citation With text</ref>", "<p><ref> Citation With text</ref></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testSpecialSymbols() throws WikiParserException
    {
        test(";abcd:{", "<dl>\n  <dt>abcd</dt>\n  <dd>{</dd>\n</dl>");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimBlocks() throws WikiParserException
    {
        test("abc \n<pre> 123\n  CDE\n   345 </pre> efg", "<p>abc </p>\n" + "<pre> 123\n" + "  CDE\n"
            + "   345 </pre>\n" + "<p> efg</p>");
        test("abc <nowiki> 123\n  CDE\n   345 </nowiki> efg", "<p>abc </p>\n" + "<pre> 123\n" + "  CDE\n"
            + "   345 </pre>\n" + "<p> efg</p>");
        test("<pre>First '''''Block'''''</pre> abcdef <pre> Second ''Block''</pre>",
            "<pre>First '''''Block'''''</pre>\n" + "<p> abcdef </p>\n" + "<pre> Second ''Block''</pre>");
        test("<pre style=\"CSS text\">this way, all markups are '''ignored''' and formatted with a CSS text</pre>",
            "<pre style='CSS text'>this way, all markups are '''ignored''' and formatted with a CSS text</pre>");
        test("abc\n<math>\n {{{ 123 \n}\\}} \n</math> efg", "<p>abc\n" + "<tt class=\"wikimodel-verbatim\">\n"
            + " {{{ 123 \n" + "}\\}} \n" + "</tt> efg</p>");
        test("inline<math>verbatime</math>block", "<p>inline<tt class=\"wikimodel-verbatim\">verbatime</tt>block</p>");
        test("</math>just like this...", "<p>&lt;/math&gt;just like this...</p>");
        test("<math>just like this...", "<p>&lt;math&gt;just like this...</p>");
        test("just like this...</math>", "<p>just like this...&lt;/math&gt;</p>");
        test("just like this...<math>", "<p>just like this...&lt;math&gt;</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testMacros() throws WikiParserException
    {
        test("__TOC__", "<pre class='wikimodel-macro' macroName='toc' numbered='true'><![CDATA[__TOC__]]></pre>");
        test("__FORCETOC__", "<pre class='wikimodel-macro' macroName='forcetoc'><![CDATA[__FORCETOC__]]></pre>");
        test("{{MAGICWORD}}", "<pre class='wikimodel-macro' macroName='MAGICWORD'><![CDATA[{{MAGICWORD}}]]></pre>");
        test("{{TestMacro|paramA|paramB}}",
            "<pre class='wikimodel-macro' macroName='TestMacro' 1='paramA' 2='paramB'><![CDATA[{{TestMacro|paramA|paramB}}]]></pre>");
        test("{{TestMacro|1=paramA|2=paramB}}",
            "<pre class='wikimodel-macro' macroName='TestMacro' 1='paramA' 2='paramB'><![CDATA[{{TestMacro|1=paramA|2=paramB}}]]></pre>");
        test("{{TestMacro|param1=X|param2=Y|lastparam}}",
            "<pre class='wikimodel-macro' macroName='TestMacro' param1='X' param2='Y' 3='lastparam'><![CDATA[{{TestMacro|param1=X|param2=Y|lastparam}}]]></pre>");
        test("{{NameSpace:TestMacro|paramA|paramB}}",
            "<pre class='wikimodel-macro' macroName='NameSpace:TestMacro' 1='paramA' 2='paramB'><![CDATA[{{NameSpace:TestMacro|paramA|paramB}}]]></pre>");
        test("<references />", "<pre class='wikimodel-macro' macroName='footnotes'><![CDATA[<references />]]></pre>");
    }

    /**
     * @throws WikiParserException
     */
    public void testEmptyLines() throws WikiParserException
    {
        test("\nthis is one\nparagraph\n\n\n\nthis is another one\n", "<p>this is one\n" + "paragraph</p>\n"
            + "<div style='height:3em;'></div>\n" + "<p>this is another one</p>");
        test("*first\n**second\n\n\n\n\n\n**third\n\n\n\n", "<ul>\n" + "  <li>first<ul>\n" + "  <li>second</li>\n"
            + "</ul>\n" + "</li>\n" + "</ul>\n" + "<div style='height:4em;'></div>\n" + "<ul>\n" + "  <li><ul>\n"
            + "  <li>third</li>\n" + "</ul>\n" + "</li>\n" + "</ul>\n" + "<div style='height:3em;'></div>");
    }

    /**
     * @throws WikiParserException
     */
    public void testTableCaption() throws WikiParserException
    {
        test("before\n"
            + "{|\n"
            + "|+ THIS IS MY CAPTION"
            + "| Cell a.1 || Cell a.2 \n"
            + "|-\n"
            + "| Cell b.1 || Cell b.2 \n"
            + "|-\n"
            + "| Cell c.1 || Cell c.2 \n"
            + "|}\n"
            + "after");
    }
}

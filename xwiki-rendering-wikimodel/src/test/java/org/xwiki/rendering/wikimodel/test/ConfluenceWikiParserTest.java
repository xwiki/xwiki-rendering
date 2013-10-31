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
import org.xwiki.rendering.wikimodel.confluence.ConfluenceWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class ConfluenceWikiParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public ConfluenceWikiParserTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new ConfluenceWikiParser();
    }

    public void testMacroInTextLine() throws WikiParserException
    {
        test(
            "this is no format text: {noformat}text no format{noformat}",
            "<p>this is no format text: <span class='wikimodel-macro' macroName='noformat'><![CDATA[text no format]]></span></p>");
    }

    public void testConfluenceHeadings() throws WikiParserException
    {
        test("h1. Biggest heading", "<h1>Biggest heading</h1>");
        test("h2. Bigger heading", "<h2>Bigger heading</h2>");
        test("h3. Big Heading", "<h3>Big Heading</h3>");
        test("h4. Normal Heading", "<h4>Normal Heading</h4>");
        test("h5. Small Heading", "<h5>Small Heading</h5>");
        test("h6. Smallest Heading", "<h6>Smallest Heading</h6>");
    }

    /**
     * http://confluence.atlassian.com/renderer/notationhelp.action?section=
     * breaks
     */
    public void testConfluenceTextBreaks() throws WikiParserException
    {
        test("first\n\nsecond", "<p>first</p>\n<p>second</p>");
        test("first\\\\second", "<p>first<br />second</p>");
        test("first\n----second", "<p>first</p>\n<hr />\n<p>second</p>");
        test("first --- second", "<p>first --- second</p>");
        test("first -- second", "<p>first -- second</p>");
    }

    public void testConfluenceTextEffects() throws WikiParserException
    {
        test(
            "Makes text *strong*.",
            "<p>Makes text <strong>strong</strong>.</p>");
        test("Makes text _emphasis_.", "<p>Makes text <em>emphasis</em>.</p>");
        test(
            "Makes text ??citation??.",
            "<p>Makes text <cite>citation</cite>.</p>");
        test(
            "Makes text -strikethrough-.",
            "<p>Makes text <strike>strikethrough</strike>.</p>");

        // Underlined was replaced by "tt": I consider the underline formatting
        // harmful - this pure visual tag mocks up a very important logical tag
        // - "a". So it is not a "bug", it is a "feature".
        // (mkotelnikov)
        test(
            "Makes text +underlined+.",
            "<p>Makes text <ins>underlined</ins>.</p>");
        test(
            "Makes text ^superscript^.",
            "<p>Makes text <sup>superscript</sup>.</p>");
        test(
            "Makes text ~subscript~.",
            "<p>Makes text <sub>subscript</sub>.</p>");
        test(
            "Makes text as {{code text}}.",
            "<p>Makes text as <mono>code text</mono>.</p>");
        test(
            "bq. Some block quoted text.",
            "<blockquote>\n Some block quoted text.\n</blockquote>");
        test(
            "{quote}Some block quoted text.\n{quote}",
            "<blockquote>\nSome block quoted text.\n</blockquote>");

        // {color:red}
        // look ma, red text!
        // {color} Changes the color of a block of text.
        //
        // Example: look ma, red text!
    }

    /**
     * @throws WikiParserException
     */
    public void testEscape() throws WikiParserException
    {
        // "a" and "b" symbols are just escaped
        test("not\\a\\break", "<p>not"
            + "<span class='wikimodel-escaped'>a</span>"
            + "<span class='wikimodel-escaped'>b</span>"
            + "reak</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("a-b", "<p>a-b</p>");
        test("-a-", "<p><strike>a</strike></p>");

        // STRONG
        test("*bold*", "<p><strong>bold</strong></p>");
        test(
            "*bold* *bold* *bold*",
            "<p><strong>bold</strong> <strong>bold</strong> <strong>bold</strong></p>");
        // EM
        test("_italic_", "<p><em>italic</em></p>");
        test(
            "_italic_ _italic_ _italic_",
            "<p><em>italic</em> <em>italic</em> <em>italic</em></p>");
        // CITE
        test("??citation??", "<p><cite>citation</cite></p>");
        test(
            "??citation?? ??citation?? ??citation??",
            "<p><cite>citation</cite> <cite>citation</cite> <cite>citation</cite></p>");

        // STRIKE
        test("-abc-", "<p><strike>abc</strike></p>");
        test(" -abc-", "<p> <strike>abc</strike></p>");
        test("abc -cde- efg", "<p>abc <strike>cde</strike> efg</p>");
        test(
            "abc -cde- -efg-",
            "<p>abc <strike>cde</strike> <strike>efg</strike></p>");
        // not a STRIKE
        test("abc - cde", "<p>abc - cde</p>");

        // INS
        test("+abc+", "<p><ins>abc</ins></p>");
        test(" +abc+", "<p> <ins>abc</ins></p>");
        test("abc +cde+ efg", "<p>abc <ins>cde</ins> efg</p>");
        test("abc +cde+ +efg+", "<p>abc <ins>cde</ins> <ins>efg</ins></p>");
        // not a TT
        test("abc + cde", "<p>abc + cde</p>");

        // SUB
        test("~abc~", "<p><sub>abc</sub></p>");
        test(" ~abc~", "<p> <sub>abc</sub></p>");
        test("abc ~cde~ efg", "<p>abc <sub>cde</sub> efg</p>");
        test("abc ~cde~ ~efg~", "<p>abc <sub>cde</sub> <sub>efg</sub></p>");

        // SUP
        test("^abc^", "<p><sup>abc</sup></p>");
        test(" ^abc^", "<p> <sup>abc</sup></p>");
        test("abc ^cde^ efg", "<p>abc <sup>cde</sup> efg</p>");
        test("abc ^cde^ ^efg^", "<p>abc <sup>cde</sup> <sup>efg</sup></p>");

        test("before{{{inside}}}after", "<p>before<tt class=\"wikimodel-verbatim\">inside</tt>after</p>");

        // Mixed styles
        test(
            "normal*bold_bold-italic*italic_normal",
            "<p>normal<strong>bold</strong><strong><em>bold-italic</em></strong><em>italic</em>normal</p>");

        // Not formatting
        test(
            "http://www.foo.bar",
            "<p>http://www.foo.bar</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHeaders() throws WikiParserException
    {
        // Headers without spaces after the header symbol
        test("h1.Header1", "<h1>Header1</h1>");
        test("h2.Header2", "<h2>Header2</h2>");
        test("h3.Header3", "<h3>Header3</h3>");
        test("h4.Header4", "<h4>Header4</h4>");
        test("h5.Header5", "<h5>Header5</h5>");
        test("h6.Header6", "<h6>Header6</h6>");
        test("h7.Not a header", "<p>h7.Not a header</p>");
        // Headers: ignoring spaces just after the header sign
        test("h1. Header1", "<h1>Header1</h1>");
        test("h2. Header2", "<h2>Header2</h2>");
        test("h3. Header3", "<h3>Header3</h3>");
        test("h4. Header4", "<h4>Header4</h4>");
        test("h5. Header5", "<h5>Header5</h5>");
        test("h6. Header6", "<h6>Header6</h6>");
        test("h7. Not a header", "<p>h7. Not a header</p>");

        test(
            "\nh1.Header\n* list item",
            "<h1>Header</h1>\n<ul>\n  <li>list item</li>\n</ul>");
        test(
            "before\nh3. Header \nafter",
            "<p>before</p>\n<h3>Header </h3>\n<p>after</p>");
    }

    public void testImages() throws WikiParserException
    {
        test(
            "before !http://www.host.com/image.gif! after",
            "<p>before <img src='http://www.host.com/image.gif'/> after</p>");
        test(
            "!spaceKey:pageTitle^image.gif!",
            "<p><img src='spaceKey:pageTitle^image.gif'/></p>");
        test(
            "!/2007/05/23/My Blog Post^image.gif!",
            "<p><img src='/2007/05/23/My Blog Post^image.gif'/></p>");
        test(
            "!image.gif|a=b!",
            "<p><img src='image.gif' a='b' title='image.gif'/></p>");
        test(
            "!image.gif|a=b,c=d!",
            "<p><img src='image.gif' a='b' c='d' title='image.gif'/></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testLineBreaks() throws WikiParserException
    {
        test("line\\\\break", "<p>line<br />break</p>");
        test("not\\a\\break", "<p>not"
            + "<span class='wikimodel-escaped'>a</span>"
            + "<span class='wikimodel-escaped'>b</span>"
            + "reak</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testLists() throws WikiParserException
    {
        test("# Item 1\n"
            + "## Item 2\n"
            + "##* Item 3\n"
            + "## Item 4\n"
            + "# Item 5\n", ""
            + "<ol>\n"
            + "  <li>Item 1<ol>\n"
            + "  <li>Item 2<ul>\n"
            + "  <li>Item 3</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "  <li>Item 4</li>\n"
            + "</ol>\n"
            + "</li>\n"
            + "  <li>Item 5</li>\n"
            + "</ol>");
        test("* item one", "<ul>\n  <li>item one</li>\n</ul>");
        test("* item one\n** item two", ""
            + "<ul>\n"
            + "  <li>item one<ul>\n"
            + "  <li>item two</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>");
        test("- item one\n-- item two", ""
            + "<ul>\n"
            + "  <li>item one<ul>\n"
            + "  <li>item two</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>");
        test("** item one", ""
            + "<ul>\n"
            + "  <li><ul>\n"
            + "  <li>item one</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>");
        test("*** item one", ""
            + "<ul>\n"
            + "  <li><ul>\n"
            + "  <li><ul>\n"
            + "  <li>item one</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "</ul>");
        test("*## item one", ""
            + "<ul>\n"
            + "  <li><ol>\n"
            + "  <li><ol>\n"
            + "  <li>item one</li>\n"
            + "</ol>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</li>\n"
            + "</ul>");
        test("*## item one", ""
            + "<ul>\n"
            + "  <li><ol>\n"
            + "  <li><ol>\n"
            + "  <li>item one</li>\n"
            + "</ol>\n"
            + "</li>\n"
            + "</ol>\n"
            + "</li>\n"
            + "</ul>");
        test("# Item 1\n"
            + "## Item 2\n"
            + "##* Item 3\n"
            + "## Item 4\n"
            + "# Item 5\n"
            + "* Item 1\n"
            + "** Item 2\n"
            + "*** Item 3\n"
            + "** Item 4\n"
            + "* Item 5\n"
            + "* Item 6", ""
            + "<ol>\n"
            + "  <li>Item 1<ol>\n"
            + "  <li>Item 2<ul>\n"
            + "  <li>Item 3</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "  <li>Item 4</li>\n"
            + "</ol>\n"
            + "</li>\n"
            + "  <li>Item 5</li>\n"
            + "</ol>\n"
            + "<ul>\n"
            + "  <li>Item 1<ul>\n"
            + "  <li>Item 2<ul>\n"
            + "  <li>Item 3</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "  <li>Item 4</li>\n"
            + "</ul>\n"
            + "</li>\n"
            + "  <li>Item 5</li>\n"
            + "  <li>Item 6</li>\n"
            + "</ul>");
    }

    public void testMacro() throws WikiParserException
    {
        testMacro(
            // "quote" // DONE - {quote} quoted block {quote}
            "section",
            "column",
            "csv",
            // TODO: {table-plus:width=100..} table {table-plus}
            "table-plus",
            "composition-setup",
            // TODO: {float:xx} paragraph {float}
            "float",
            "cloak",
            "deck",
            "card",
            "color",
            "show-card",
            // TODO: {chart: params} table {charŧ}
            "chart",
            "slideshow",
            "slide",
            "note",
            "warning",
            "info",
            "tip",
            "cache",
            "sql",
            "panel",
            "sub-section",
            "clickable",
            // TODO: {tm} inline text {tm}
            "tm",
            // TODO: {sm} inline text {sm}
            "sm",
            // TODO: {reg-tm} inline text {reg-tm}
            "reg-tm",
            // TODO: {copyright} inline text {copyright}
            "copyright",
            // TODO: {span} inline text {span}
            "span",
            "lozenge",
            "style",
            "div",
            "bgcolor",
            "center",
            "strike",
            "privacy-policy",
            "roundrect",
            "align",
            "iframe",
            "table",
            "table-row",
            "table-cell",
            "th",
            "tbody",
            "thead",
            "ul",
            "li",
            "rollover",
            "fancy-bullets",
            "contentformattingtest",
            "toc-zone",
            "excerpt",
            "code",
            "html");

        test(
            ""
                + "{section}\n"
                + "{column:width=30%}\n"
                + "Column one text goes here\n"
                + "{column}\n"
                + "{column:width=70%}\n"
                + "Column two text goes here\n"
                + "{column}\n"
                + "{section}\n",
            "<pre class='wikimodel-macro' macroName='section'><![CDATA[\n"
                + "{column:width=30%}\n"
                + "Column one text goes here\n"
                + "{column}\n"
                + "{column:width=70%}\n"
                + "Column two text goes here\n"
                + "{column}\n"
                + "]]></pre>");

        // Macro parameters
        test(
            "{float}x{float}",
            "<pre class='wikimodel-macro' macroName='float'><![CDATA[x]]></pre>");
        test(
            "{float:left}align to left{float}",
            "<pre class='wikimodel-macro' macroName='float' value='left'><![CDATA[align to left]]></pre>");
        test(
            "{column:width=50%}\n" + "Text in this column.\n" + "{column}",
            "<pre class='wikimodel-macro' macroName='column' width='50%'><![CDATA[\nText in this column.\n]]></pre>");
        test(
            "{csv:output=wiki|width=900|border=15|delimiter=whitespace}\n"
                + "Month Max Min Average\n"
                + "January 25.5 *6.3* 15.9\n{csv}",
            "<pre class='wikimodel-macro' macroName='csv' output='wiki' width='900' border='15' delimiter='whitespace'>"
                + "<![CDATA[\n"
                + "Month Max Min Average\n"
                + "January 25.5 *6.3* 15.9\n]]></pre>");
    }

    private void testMacro(String... names) throws WikiParserException
    {
        for (String name : names) {
            test(
                "{" + name + "}a{" + name + "}",
                "<pre class='wikimodel-macro' macroName='"
                    + name
                    + "'><![CDATA[a]]></pre>");
            test("before\n{" + name + "}a{" + name + "}\nafter", ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='"
                + name
                + "'><![CDATA[a]]></pre>\n"
                + "<p>after</p>");
        }
    }

    public void testMacroEmpty() throws WikiParserException
    {
        test(
            "{code}a{code}b",
            ""
                + "<pre class='wikimodel-macro' macroName='code'><![CDATA[a]]></pre>\n"
                + "<p>b</p>");

        // Empty macros with parameters
        test(
            "{code:x=X|y=Y}a{code}b",
            ""
                + "<pre class='wikimodel-macro' macroName='code' x='X' y='Y'><![CDATA[a]]></pre>\n"
                + "<p>b</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testParagraphs() throws WikiParserException
    {
        test("First paragraph.\n"
            + "Second line of the same paragraph.\n"
            + "\n"
            + "The second paragraph", "<p>First paragraph.\n"
            + "Second line of the same paragraph.</p>\n"
            + "<p>The second paragraph</p>");
    }

    /**
     */
    public void testQuot() throws WikiParserException
    {
        test(
            "first\nbq.second\n\nthird",
            "<p>first</p>\n<blockquote>\nsecond\n</blockquote>\n<p>third</p>");
        test(
            "bq. Some block quoted text.",
            "<blockquote>\n Some block quoted text.\n</blockquote>");
        test(
            "first\n{quote}second\n{quote}\nthird",
            "<p>first</p>\n<blockquote>\nsecond\n</blockquote>\n<p>third</p>");
        test(
            "{quote}Some block quoted text.\n{quote}",
            "<blockquote>\nSome block quoted text.\n</blockquote>");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
//      The Confluence does not support this syntax as a reference.    
        test(
            "before http://www.foo.bar/com after",
            "<p>before http://www.foo.bar/com after</p>");

//        The Confluence does not support the uri format: "scheme:hier_path" 
        test(
            "before this+is+a+reference:to_here after",
            "<p>before this+is+a+reference:to_here after</p>");
        test(
            "before [toto] after",
            "<p>before <a href='toto' class='wikimodel-freestanding'>toto</a> after</p>");

        // Tests from
        // http://wikicreole.org/wiki/Creole1.0#section-Creole1.0-
        // LinksInternalExternalAndInterwiki
        test("[link]", "<p><a href='link' class='wikimodel-freestanding'>link</a></p>");
        test(
            "[Go to my page|MyBigPage]",
            "<p><a href='MyBigPage'>Go to my page</a></p>");
        test(
            "[http://www.wikicreole.org/]",
            "<p><a href='http://www.wikicreole.org/' class='wikimodel-freestanding'>http://www.wikicreole.org/</a></p>");
        test(
            "http://www.rawlink.org/, http://www.another.rawlink.org",
            "<p>http://www.rawlink.org/, http://www.another.rawlink.org</p>");
        test(
            "[ Visit the WikiCreole website | http://www.wikicreole.org/ ]",
            "<p><a href='http://www.wikicreole.org/'>Visit the WikiCreole website</a></p>");
        test(
            "[Ohana:WikiFamily]",
            "<p><a href='Ohana:WikiFamily' class='wikimodel-freestanding'>Ohana:WikiFamily</a></p>");

        test("[#anchor]", "<p><a href='#anchor' class='wikimodel-freestanding'>#anchor</a></p>");
        test(
            "[^attachment.ext]",
            "<p><a href='^attachment.ext' class='wikimodel-freestanding'>^attachment.ext</a></p>");
        test("[pagetitle]", "<p><a href='pagetitle' class='wikimodel-freestanding'>pagetitle</a></p>");
        test(
            "[pagetitle#anchor]",
            "<p><a href='pagetitle#anchor' class='wikimodel-freestanding'>pagetitle#anchor</a></p>");
        test(
            "[pagetitle^attachment.ext]",
            "<p><a href='pagetitle^attachment.ext' class='wikimodel-freestanding'>pagetitle^attachment.ext</a></p>");
        test(
            "[spacekey:pagetitle]",
            "<p><a href='spacekey:pagetitle' class='wikimodel-freestanding'>spacekey:pagetitle</a></p>");
        test(
            "[spacekey:pagetitle#anchor]",
            "<p><a href='spacekey:pagetitle#anchor' class='wikimodel-freestanding'>spacekey:pagetitle#anchor</a></p>");
        test(
            "[spacekey:pagetitle^attachment.ext]",
            "<p><a href='spacekey:pagetitle^attachment.ext' class='wikimodel-freestanding'>spacekey:pagetitle^attachment.ext</a></p>");
        test(
            "[link alias|#anchor|link tip]",
            "<p><a href='#anchor' title='link tip'>link alias</a></p>");
        test(
            "[link alias|^attachment.ext|link tip]",
            "<p><a href='^attachment.ext' title='link tip'>link alias</a></p>");
        test(
            "[link alias|pagetitle|link tip]",
            "<p><a href='pagetitle' title='link tip'>link alias</a></p>");
        test(
            "[link alias|pagetitle#anchor|link tip]",
            "<p><a href='pagetitle#anchor' title='link tip'>link alias</a></p>");
        test(
            "[link alias|pagetitle^attachment.ext|link tip]",
            "<p><a href='pagetitle^attachment.ext' title='link tip'>link alias</a></p>");
        test(
            "[link alias|spacekey:pagetitle|link tip]",
            "<p><a href='spacekey:pagetitle' title='link tip'>link alias</a></p>");
        test(
            "[link alias|spacekey:pagetitle#anchor|link tip]",
            "<p><a href='spacekey:pagetitle#anchor' title='link tip'>link alias</a></p>");
        test(
            "[link alias|spacekey:pagetitle^attachment.ext|link tip]",
            "<p><a href='spacekey:pagetitle^attachment.ext' title='link tip'>link alias</a></p>");
        test(
            "[/2004/01/12/My Blog Post]",
            "<p><a href='/2004/01/12/My Blog Post' class='wikimodel-freestanding'>/2004/01/12/My Blog Post</a></p>");
        test(
            "[spacekey:/2004/01/12/My Blog Post]",
            "<p><a href='spacekey:/2004/01/12/My Blog Post' class='wikimodel-freestanding'>spacekey:/2004/01/12/My Blog Post</a></p>");
        test("[/2004/01/12]", "<p><a href='/2004/01/12' class='wikimodel-freestanding'>/2004/01/12</a></p>");
        test(
            "[spacekey:/2004/01/12]",
            "<p><a href='spacekey:/2004/01/12' class='wikimodel-freestanding'>spacekey:/2004/01/12</a></p>");
        test(
            "[my link name|/2004/01/12]",
            "<p><a href='/2004/01/12'>my link name</a></p>");
        test(
            "[my link name|spacekey:/2004/01/12]",
            "<p><a href='spacekey:/2004/01/12'>my link name</a></p>");
        test("[$12345]", "<p><a href='$12345' class='wikimodel-freestanding'>$12345</a></p>");
        test(
            "[my link name|$12345]",
            "<p><a href='$12345'>my link name</a></p>");
        test("[spacekey:]", "<p><a href='spacekey:' class='wikimodel-freestanding'>spacekey:</a></p>");
        test(
            "[custom link title|spacekey:]",
            "<p><a href='spacekey:'>custom link title</a></p>");
        test("[~username]", "<p><a href='~username' class='wikimodel-freestanding'>~username</a></p>");
        test(
            "[custom link title|~username]",
            "<p><a href='~username'>custom link title</a></p>");
        test(
            "[phrase@shortcut]",
            "<p><a href='phrase@shortcut' class='wikimodel-freestanding'>phrase@shortcut</a></p>");
        test(
            "[custom link text|phrase@shortcut]",
            "<p><a href='phrase@shortcut'>custom link text</a></p>");
        test(
            "[http://confluence.atlassian.com]",
            "<p><a href='http://confluence.atlassian.com' class='wikimodel-freestanding'>http://confluence.atlassian.com</a></p>");
        test(
            "[Atlassian|http://atlassian.com]",
            "<p><a href='http://atlassian.com'>Atlassian</a></p>");
        test(
            "[mailto:legendaryservice@atlassian.com]",
            "<p><a href='mailto:legendaryservice@atlassian.com' class='wikimodel-freestanding'>mailto:legendaryservice@atlassian.com</a></p>");
        test(
            "[file://c:/temp/foo.txt]",
            "<p><a href='file://c:/temp/foo.txt' class='wikimodel-freestanding'>file://c:/temp/foo.txt</a></p>");
        test(
            "[file://z:/file/on/network/share.txt]",
            "<p><a href='file://z:/file/on/network/share.txt' class='wikimodel-freestanding'>file://z:/file/on/network/share.txt</a></p>");
        // Not a reference
        test(
            "before \\[toto] after",
            "<p>before <span class='wikimodel-escaped'>[</span>toto] after</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testTable() throws WikiParserException
    {
        test(
            "||Header",
            "<table><tbody>\n  <tr><th>Header</th></tr>\n</tbody></table>");
        test(
            "|Cell",
            "<table><tbody>\n  <tr><td>Cell</td></tr>\n</tbody></table>");
        test(
            "||Header||\n",
            "<table><tbody>\n  <tr><th>Header</th></tr>\n</tbody></table>");
        test(
            "|Cell|\n",
            "<table><tbody>\n  <tr><td>Cell</td></tr>\n</tbody></table>");
        test("||Header1||Header2", ""
            + "<table><tbody>\n"
            + "  <tr><th>Header1</th><th>Header2</th></tr>\n"
            + "</tbody></table>");
        test("||Header1||Header2||", ""
            + "<table><tbody>\n"
            + "  <tr><th>Header1</th><th>Header2</th></tr>\n"
            + "</tbody></table>");

        test("|Cell1|Cell2", ""
            + "<table><tbody>\n"
            + "  <tr><td>Cell1</td><td>Cell2</td></tr>\n"
            + "</tbody></table>");
        test("||Header1\n||Header2", ""
            + "<table><tbody>\n"
            + "  <tr><th>Header1</th></tr>\n"
            + "  <tr><th>Header2</th></tr>\n"
            + "</tbody></table>");
        test("|Cell1\n|Cell2", ""
            + "<table><tbody>\n"
            + "  <tr><td>Cell1</td></tr>\n"
            + "  <tr><td>Cell2</td></tr>\n"
            + "</tbody></table>");
        test("|| Header | Cell", ""
            + "<table><tbody>\n"
            + "  <tr><th> Header </th><td> Cell</td></tr>\n"
            + "</tbody></table>");

        test("|| Header 1.1 || Header 1.2\n"
            + "| Cell 2.1 | Cell 2.2\n"
            + "| Cell 3.1 || Head 3.2", ""
            + "<table><tbody>\n"
            + "  <tr><th> Header 1.1 </th><th> Header 1.2</th></tr>\n"
            + "  <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>\n"
            + "  <tr><td> Cell 3.1 </td><th> Head 3.2</th></tr>\n"
            + "</tbody></table>");

        test("||heading 1||heading 2||heading 3||\n"
            + "|col A1|col A2|col A3|\n"
            + "|col B1|col B2|col B3| ");
        // Not a table
        test("abc || cde", "<p>abc || cde</p>");

        // Check that the table accept only single lines
        test("before\n|cell1|cell2\nafter", ""
            + "<p>before</p>\n"
            + "<table><tbody>\n"
            + "  <tr><td>cell1</td><td>cell2\nafter"
            + "</td></tr>\n"
            + "</tbody></table>");
        test("|* Item 1|* Item 2",
            "<table><tbody>\n  <tr><td><ul>\n  <li>Item 1</li>\n</ul>\n</td><td><ul>\n  <li>Item 2</li>\n</ul>\n</td></tr>\n</tbody></table>");
        test("|* Item 1\n|* Item 2",
            "<table><tbody>\n  <tr><td><ul>\n  <li>Item 1</li>\n</ul>\n</td></tr>\n  <tr><td><ul>\n  <li>Item 2</li>\n</ul>\n</td></tr>\n</tbody></table>");
    }

    public void testVerbatimBlock() throws WikiParserException
    {
        test("{{{xxx}}}", "<pre>xxx</pre>");
        test(
            "before\n{{{xxx}}}after",
            "<p>before</p>\n<pre>xxx</pre>\n<p>after</p>");
        test("{{{\n"
            + "h1. xxx\n"
            + "* item\n"
            + "* item\n"
            + "}}}", ""
            + "<pre>\n"
            + "h1. xxx\n"
            + "* item\n"
            + "* item\n"
            + "</pre>");
        test("before\n{{{\n"
            + "h1. xxx\n"
            + "* item\n"
            + "* item\n"
            + "}}}after", ""
            + "<p>before</p>\n"
            + "<pre>\n"
            + "h1. xxx\n"
            + "* item\n"
            + "* item\n"
            + "</pre>\n"
            + "<p>after</p>");

        // Inline verbatim blocks
        test(
            "before{{{xxx}}}after",
            "<p>before<tt class=\"wikimodel-verbatim\">xxx</tt>after</p>");
        test("before{{{\n"
            + "h1. xxx\n"
            + "* item\n"
            + "* item\n"
            + "}}}after", ""
            + "<p>before<tt class=\"wikimodel-verbatim\">\n"
            + "h1. xxx\n"
            + "* item\n"
            + "* item\n"
            + "</tt>after</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimeBlocks() throws WikiParserException
    {
        test("abc \n{{{ 123\n  CDE\n   345 }}} efg");
        test("abc {{{ 123\n  CDE\n   345 }}} efg");
        test("abc\n{{{\n{{{\n 123 \n }}}\n}}} efg");
        test("`verbatime`");
        test("`just like this...");
    }
}

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
import org.xwiki.rendering.wikimodel.common.CommonWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class CommonWikiParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public CommonWikiParserTest(String name)
    {
        super(name, true, true);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new CommonWikiParser();
    }

    public void test() throws Exception
    {
        showSections(true);
        test("((()))", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + "<div class='wikimodel-document'>\n"
            + "<section-2-0>\n"
            + "<sectionContent-2-0>\n"
            + "</sectionContent-2-0>\n"
            + "</section-2-0>\n"
            + "</div>\n"
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
        test("=Header1=\nabc((()))\ncde", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + "<section-1-1>\n"
            + "<h1>Header1</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>abc</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<section-2-0>\n"
            + "<sectionContent-2-0>\n"
            + "</sectionContent-2-0>\n"
            + "</section-2-0>\n"
            + "</div>\n"
            + "<p>cde</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
    }

    public void testComplexFormatting() throws WikiParserException
    {
        test("%rdf:type toto:Document\n"
            + "\n"
            + "%title Hello World\n"
            + "\n"
            + "%summary This is a short description\n"
            + "%locatedIn (((\n"
            + "    %type [City]\n"
            + "    %name [Paris]\n"
            + "    %address (((\n"
            + "      %building 10\n"
            + "      %street Cité Nollez\n"
            + "    ))) \n"
            + ")))\n"
            + "= Hello World =\n"
            + "\n"
            + "* item one\n"
            + "  * sub-item a\n"
            + "  * sub-item b\n"
            + "    + ordered X \n"
            + "    + ordered Y\n"
            + "  * sub-item c\n"
            + "* item two\n"
            + "\n"
            + "\n"
            + "The table below contains \n"
            + "an %seeAlso(embedded document). \n"
            + "It can contain the same formatting \n"
            + "elements as the root document.\n"
            + "\n"
            + "\n"
            + "!! Table Header 1.1 !! Table Header 1.2\n"
            + ":: Cell 2.1 :: Cell 2.2 (((\n"
            + "== Embedded document ==\n"
            + "This is an embedded document:\n"
            + "* item X\n"
            + "* item Y\n"
            + "))) The text goes after the embedded\n"
            + " document\n"
            + ":: Cell 3.1 :: Cell 3.2");
        test("----------------------------------------------\n"
            + "= Example1 =\n"
            + "\n"
            + "The table below contains an embedded document.\n"
            + "Using such embedded documents you can insert table\n"
            + "in a list or a list in a table. And embedded documents\n"
            + "can contain their own embedded documents!!!\n"
            + "\n"
            + "!! Header 1.1 !! Header 1.2\n"
            + ":: Cell 2.1 :: Cell 2.2 with an embedded document: (((\n"
            + "== This is an embedded document! ==\n"
            + "* list item one\n"
            + "* list item two\n"
            + "  * sub-item A\n"
            + "  * sub-item B\n"
            + "* list item three\n"
            + ")))\n"
            + ":: Cell 3.1 :: Cell 3.2\n"
            + "\n"
            + "This is a paragraphs after the table...\n"
            + "----------------------------------------------\n"
            + "");
    }

    /**
     * @throws WikiParserException
     */
    public void testDocuments() throws WikiParserException
    {
        test("before ((( inside ))) after ", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n"
            + "</div>\n"
            + "<p>after </p>");
        test("before inside ))) after ", "<p>before inside</p>\n"
            + "<p>after </p>");
        test("before (((\ninside ))) after ", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n"
            + "</div>\n"
            + "<p>after </p>");
        test("before (((\n inside ))) after ", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p> inside</p>\n"
            + "</div>\n"
            + "<p>after </p>");
        test(
            "| Line One | First doc: (((\n inside ))) after \n"
                + "|Line Two | Second doc: (((lkjlj))) skdjg",
            ""
                + "<table><tbody>\n"
                + "  <tr><td> Line One </td><td> First doc:<div class='wikimodel-document'>\n"
                + "<p> inside</p>\n"
                + "</div>\n"
                + "after </td></tr>\n"
                + "  <tr><td>Line Two </td><td> Second doc:<div class='wikimodel-document'>\n"
                + "<p>lkjlj</p>\n"
                + "</div>\n"
                + "skdjg</td></tr>\n"
                + "</tbody></table>");
        test(
            "| This is a table: | (((* item one\n"
                + "* item two\n"
                + " * subitem 1\n"
                + " * subitem 2\n"
                + "* item three))) ",
            ""
                + "<table><tbody>\n"
                + "  <tr><td> This is a table: </td><td><div class='wikimodel-document'>\n"
                + "<ul>\n"
                + "  <li>item one</li>\n"
                + "  <li>item two<ul>\n"
                + "  <li>subitem 1</li>\n"
                + "  <li>subitem 2</li>\n"
                + "</ul>\n"
                + "</li>\n"
                + "  <li>item three</li>\n"
                + "</ul>\n"
                + "</div>\n"
                + "</td></tr>\n"
                + "</tbody></table>");
        test("before ((( opened and not closed", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>opened and not closed</p>\n"
            + "</div>");
        test("before ((( one ((( two ((( three ", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>one</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>two</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>three </p>\n"
            + "</div>\n"
            + "</div>\n"
            + "</div>");
    }

    public void testDocumentSections() throws WikiParserException
    {
        showSections(true);
        test("abc", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + "<p>abc</p>\n"
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
        test("=Header=\n" + "abc", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + "<section-1-1>\n"
            + "<h1>Header</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>abc</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
        test("=Header 1=\nabc\n=Header 2=\ncde", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + ""
            // The first section is formed by the first header
            + "<section-1-1>\n"
            + "<h1>Header 1</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>abc</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + ""
            // The section section
            + "<section-1-1>\n"
            + "<h1>Header 2</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>cde</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + ""
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
        test(""
            + "=Header 1=\nabc\n"
            + "==Header 1.1==\ncde\n"
            + "==Header 1.2==\nefg\n"
            + "=Header 2=\nghk", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + ""
            // The first section is formed by the first header
            + "<section-1-1>\n"
            + "<h1>Header 1</h1>\n"
            + "<sectionContent-1-1>\n"
            // First section of the second level
            + "<p>abc</p>\n"
            + "<section-1-2>\n"
            + "<h2>Header 1.1</h2>\n"
            + "<sectionContent-1-2>\n"
            + "<p>cde</p>\n"
            + "</sectionContent-1-2>\n"
            + "</section-1-2>\n"
            // Second section of the second level
            + "<section-1-2>\n"
            + "<h2>Header 1.2</h2>\n"
            + "<sectionContent-1-2>\n"
            + "<p>efg</p>\n"
            + "</sectionContent-1-2>\n"
            + "</section-1-2>\n"
            + ""
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + ""
            // The section section on the first level
            + "<section-1-1>\n"
            + "<h1>Header 2</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>ghk</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + ""
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");

        // Embedded document

        test("((()))", ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            + "<div class='wikimodel-document'>\n"
            + "<section-2-0>\n"
            + "<sectionContent-2-0>\n"
            + "</sectionContent-2-0>\n"
            + "</section-2-0>\n"
            + "</div>\n"
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
        test(""
            + "=Header 1=\nabc\n"
            + "((("
            + "=Header 1.1=\ncde\n"
            + "=Header 1.2=\nefg\n"
            + ")))\n"
            + "xyz\n"
            + "=Header 2=\nghk", ""
            + ""
            + "<section-1-0>\n"
            + "<sectionContent-1-0>\n"
            // The first section is formed by the first header
            + "<section-1-1>\n"
            + "<h1>Header 1</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>abc</p>\n"
            + ""
            // Embedded document
            + "<div class='wikimodel-document'>\n"
            + "<section-2-0>\n"
            + "<sectionContent-2-0>\n"
            // Headers in the embedded document
            + "<section-2-1>\n"
            + "<h1>Header 1.1</h1>\n"
            + "<sectionContent-2-1>\n"
            + "<p>cde</p>\n"
            + "</sectionContent-2-1>\n"
            + "</section-2-1>\n"
            // The second embedded header
            + "<section-2-1>\n"
            + "<h1>Header 1.2</h1>\n"
            + "<sectionContent-2-1>\n"
            + "<p>efg</p>\n"
            + "</sectionContent-2-1>\n"
            + "</section-2-1>\n"
            // End of the embedded document
            + "</sectionContent-2-0>\n"
            + "</section-2-0>\n"
            + "</div>\n"
            + "<p>xyz</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + ""
            // The section section at the first level
            + "<section-1-1>\n"
            + "<h1>Header 2</h1>\n"
            + "<sectionContent-1-1>\n"
            + "<p>ghk</p>\n"
            + "</sectionContent-1-1>\n"
            + "</section-1-1>\n"
            + ""
            + "</sectionContent-1-0>\n"
            + "</section-1-0>");
    }

    /**
     * @throws WikiParserException
     */
    public void testEscape() throws WikiParserException
    {
        test("[a reference]");
        test("\\[not a reference]");

        test("\\First letter is escaped");
        test("\\[not a reference]");
        test("\\\\escaped backslash");
        test("\\ a line break because it is followed by a space");

        test("= Heading =\n\\= Not a heading =\n= Heading again! =");
    }

    /**
     * @throws WikiParserException
     */
    public void testExtensions() throws WikiParserException
    {
        // Inline extensions
        test(
            " $abc ",
            "<p> <span class='wikimodel-extension' extension='abc'/> </p>");
        test(
            "abc $abc after",
            "<p>abc <span class='wikimodel-extension' extension='abc'/> after</p>");
        test(
            "abc $abc() after",
            "<p>abc <span class='wikimodel-extension' extension='abc'/> after</p>");
        test(
            "abc $abc(a=b c=d) after",
            "<p>abc <span class='wikimodel-extension' extension='abc' a='b' c='d'/> after</p>");
        test(
            "before$abc(hello)after",
            "<p>before<span class='wikimodel-extension' extension='abc' hello=''/>after</p>");

        // Block extensions
        test("$abc", "<div class='wikimodel-extension' extension='abc'/>");
        test("$abc()", "<div class='wikimodel-extension' extension='abc'/>");
        test(
            "$abc(a=b c=d)",
            "<div class='wikimodel-extension' extension='abc' a='b' c='d'/>");
        test(
            "$abc(a)",
            "<div class='wikimodel-extension' extension='abc' a=''/>");

        test("before\n$abc after", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-extension' extension='abc'/>\n"
            + "<p> after</p>");
        test("before\n$abc() after", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-extension' extension='abc'/>\n"
            + "<p> after</p>");
        test(
            "before\n$abc(a=b c=d) after",
            ""
                + "<p>before</p>\n"
                + "<div class='wikimodel-extension' extension='abc' a='b' c='d'/>\n"
                + "<p> after</p>");
        test("before\n$abc(hello)after", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-extension' extension='abc' hello=''/>\n"
            + "<p>after</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("*bold* ", "<p><strong>bold</strong> </p>");
        test(" **bold** ", "<p> <strong>bold</strong> </p>");
        test("__italic__", "<p><em>italic</em></p>");

        test("*strong*", "<p><strong>strong</strong></p>");
        test(" *strong*", "<p> <strong>strong</strong></p>");
        test("__em__", "<p><em>em</em></p>");
        test("$$code$$", "<p><code>code</code></p>");
        test("^^sup^^", "<p><sup>sup</sup></p>");
        test("~~sub~~", "<p><sub>sub</sub></p>");

        // These special symbols ("--" and "++") at the begining of the line are
        // interpreted as list markers (see {@link #testLists()} method)
        test("before++big++after", "<p>before<big>big</big>after</p>");
        test("before--small--after", "<p>before<small>small</small>after</p>");

        test("@@ins@@", "<p><ins>ins</ins></p>");
        test("##del##", "<p><del>del</del></p>");

        test(
            "before*bold*__italic__^^superscript^^~~subscript~~value after",
            "<p>before<strong>bold</strong><em>italic</em>"
                + "<sup>superscript</sup><sub>subscript</sub>"
                + "value after</p>");

        // "Bad-formed" formatting
        test("normal*bold__bold-italic*italic__normal", "<p>"
            + "normal<strong>bold</strong>"
            + "<strong><em>bold-italic</em></strong>"
            + "<em>italic</em>normal"
            + "</p>");

        // Auto-closing (non used) style formatting at the end of lines.
        test("not a bold__", "<p>not a bold</p>");
        test("not an italic__", "<p>not an italic</p>");

        test("text*", "<p>text</p>");
        test("text**", "<p>text</p>");
        test("text__", "<p>text</p>");
        test("text$$", "<p>text</p>");
        test("text^^", "<p>text</p>");
        test("text~~", "<p>text</p>");
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
        test("=Header1", "<h1>Header1</h1>");
        test("==Header2", "<h2>Header2</h2>");
        test("===Header3", "<h3>Header3</h3>");
        test("====Header4", "<h4>Header4</h4>");
        test("before\n= Header =\nafter", "<p>before</p>\n"
            + "<h1>Header </h1>\n"
            + "<p>after</p>");

        test("This is not a header: ==", "<p>This is not a header: ==</p>");

        test("{{a=b}}\n=Header1", "<h1 a='b'>Header1</h1>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHorLine() throws WikiParserException
    {
        test("----", "<hr />");
        test("-------", "<hr />");
        test("-----------", "<hr />");
        test("----\nabc", "<hr />\n<p>abc</p>");
        test("before\n----\nafter", "<p>before</p>\n<hr />\n<p>after</p>");
        test("{{a=b}}\n----", "<hr a='b' />");

        // Not lines
        test(" -----------", "<p> ---------</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testInfo() throws WikiParserException
    {
        test("/i\\ item {{{formatted block}}} {macro}123{/macro} after");
        test("before\n"
            + "/i\\Information block:\n"
            + "{{{pre\n"
            + "  formatted\n"
            + " block}}} sdlkgj\n"
            + "qsdg\n\n"
            + "after");
        test("/!\\");
        test("/i\\info");
        test("/i\\Information block:\n"
            + "first line\n"
            + "second line\n"
            + "third  line");
        test("{{a=b}}\n/!\\");
        test("{{a=b}}\n/i\\info");
    }

    /**
     * @throws WikiParserException
     */
    public void testLineBreak() throws WikiParserException
    {
        test("abc\\\ndef");
        test("abc\\  \ndef");
        test("abc\\ x \ndef");
        test("abc x \ndef");
    }

    /**
     * @throws WikiParserException
     */
    public void testLists() throws WikiParserException
    {
        test(
            "*this is a bold, and not a list",
            "<p><strong>this is a bold, and not a list</strong></p>");
        test("**bold**", "<p><strong>bold</strong></p>");

        test("* first", "<ul>\n  <li>first</li>\n</ul>");
        test(
            "** second",
            "<ul>\n  <li><ul>\n  <li>second</li>\n</ul>\n</li>\n</ul>");

        test("* item one\n"
            + "* item two\n"
            + "*+item three\n"
            + "*+ item four\n"
            + "* item five - first line\n"
            + "   item five - second line\n"
            + "* item six\n"
            + "  is on multiple\n"
            + " lines");

        test(
            "* item {{{formatted block}}} {macro}123{/macro} after",
            "<ul>\n"
                + "  <li>item <tt class=\"wikimodel-verbatim\">formatted block</tt>"
                + " <span class='wikimodel-macro' macroName='macro'><![CDATA[123]]></span> after</li>\n</ul>");

        test("? term:  definition");
        test("?just term");
        test(":just definition");
        test(";:just definition");
        test(":just definition");
        test(";:");
        test(": Indenting is stripped out.\n" + " : Includes double indenting");

        test(";term one: definition one\n"
            + ";term two: definition two\n"
            + ";term three: definition three");
        test(":Term definition");
        test(";:Term definition");

        test(";One,\ntwo,\nbucle my shoes...:\n"
            + "...Three\nfour,\nClose the door\n"
            + ";Five,\nSix: Pick up\n sticks\n\ntam-tam, pam-pam...");

        test(";__term__: *definition*");

        test("this is not a definition --\n"
            + " ;__not__ a term: ''not'' a definition\n"
            + "----toto");

        test("{{a='b'}}\n* item one");
    }

    public void testMacro() throws WikiParserException
    {
        test(
            "{toto}a{/toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>");
        test(
            "{toto}a{toto}b{/toto}c{/toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a{toto}b{/toto}c]]></pre>");
        test(
            "before\n{toto}a{/toto}\nafter",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>\n"
                + "<p>after</p>");
        test(
            "before\n{toto}a{/toto}after",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>\n"
                + "<p>after</p>");

        // URIs as macro names
        test(
            "{x:toto}a{/x:toto}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>");
        test(
            "{x:toto}a{x:toto}b{/x:toto}c{/x:toto}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a{x:toto}b{/x:toto}c]]></pre>");
        test(
            "before\n{x:toto}a{/x:toto}\nafter",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>\n"
                + "<p>after</p>");
        test(
            "before\n{x:toto}a{/x:toto}after",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>\n"
                + "<p>after</p>");

        // Empty macros
        test(
            "{x:toto /}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[]]></pre>");
        test(
            "{x:toto a=b c=d /}",
            "<pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'><![CDATA[]]></pre>");
        test(
            "before\n{x:toto  a=b c=d/}\nafter",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'><![CDATA[]]></pre>\n"
                + "<p>after</p>");
        test(
            "before\n{x:toto  a='b' c='d'/}after",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'><![CDATA[]]></pre>\n"
                + "<p>after</p>");
        test(
            "before{x:toto /}after",
            "<p>before<span class='wikimodel-macro' macroName='x:toto'><![CDATA[]]></span>after</p>");

        // Bad-formed block macros (not-closed)
        test(
            "{toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[]]></pre>");
        test(
            "{toto}a{toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a{toto}]]></pre>");

        // 
        test(
            "{toto}a{/toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>");
        test(
            "before{toto}macro{/toto}after",
            "<p>before<span class='wikimodel-macro' macroName='toto'><![CDATA[macro]]></span>after</p>");

        test(
            "before{toto a=b c=d}toto macro tata {/toto}after",
            ""
                + "<p>before<span class='wikimodel-macro' macroName='toto' a='b' c='d'>"
                + "<![CDATA[toto macro tata ]]>"
                + "</span>after</p>");

        test(
            "before{toto a=b c=d}toto {x qsdk} macro {sd} tata {/toto}after",
            ""
                + "<p>before<span class='wikimodel-macro' macroName='toto' a='b' c='d'>"
                + "<![CDATA[toto {x qsdk} macro {sd} tata ]]>"
                + "</span>after</p>");

        // Macros in other block elements (tables and lists)
        test(
            "- before\n{code a=b c=d}this is a code{/code}after",
            ""
                + "<ul>\n"
                + "  <li>before<pre class='wikimodel-macro' macroName='code' a='b' c='d'>"
                + "<![CDATA[this is a code]]></pre>\n"
                + "after</li>\n"
                + "</ul>");
        test(
            "- before{code a=b c=d}this is a code{/code}after",
            ""
                + "<ul>\n"
                + "  <li>before<span class='wikimodel-macro' macroName='code' a='b' c='d'>"
                + "<![CDATA[this is a code]]></span>after</li>\n"
                + "</ul>");

        // Not a macro
        test("{ toto a=b c=d}", "<p>{ toto a=b c=d}</p>");

        // Macro and its usage
        test(
            "This is a macro: {toto x:a=b x:c=d}\n"
                + "<table>\n"
                + "#foreach ($x in $table)\n"
                + "  <tr>hello, $x</tr>\n"
                + "#end\n"
                + "</table>\n"
                + "{/toto}\n\n"
                + "And this is a usage of this macro: $toto(a=x b=y)",
            "<p>This is a macro: <span class='wikimodel-macro' macroName='toto' x:a='b' x:c='d'><![CDATA[\n"
                + "<table>\n"
                + "#foreach ($x in $table)\n"
                + "  <tr>hello, $x</tr>\n"
                + "#end\n"
                + "</table>\n"
                + "]]></span></p>\n"
                +
                "<p>And this is a usage of this macro: <span class='wikimodel-extension' extension='toto' a='x' b='y'/></p>");

        test(
            "!!Header:: Cell with a macro: \n"
                + "{code}this is a code{/code} \n"
                + " this is afer the code...",
            ""
                + "<table><tbody>\n"
                + "  <tr><th>Header</th><td> Cell with a macro: "
                + "<pre class='wikimodel-macro' macroName='code'><![CDATA[this is a code]]></pre>\n \n"
                + " this is afer the code...</td></tr>\n"
                + "</tbody></table>");
        test(
            ""
                + "* item one\n"
                + "* item two\n"
                + "  * subitem with a macro:\n"
                + "  {code} this is a code{/code} \n"
                + "  the same item (continuation)\n"
                + "  * subitem two\n"
                + "* item three",
            ""
                + "<ul>\n"
                + "  <li>item one</li>\n"
                + "  <li>item two<ul>\n"
                + "  <li>subitem with a macro:\n"
                + "  <span class='wikimodel-macro' macroName='code'><![CDATA[ this is a code]]></span> \n"
                + "  the same item (continuation)</li>\n"
                + "  <li>subitem two</li>\n"
                + "</ul>\n"
                + "</li>\n"
                + "  <li>item three</li>\n"
                + "</ul>");

        // Macros with URIs as names
        test(
            "{x:y a=b c=d}",
            "<pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[]]></pre>");
        test(
            "before{x:y a=b c=d}macro content",
            "<p>before<span class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[macro content]]></span></p>");
        test(
            "before\n{x:y a=b c=d}macro content",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[macro content]]></pre>");
        test(
            "before\n{x:y a=b c=d/}\nafter",
            ""
                + "<p>before</p>\n"
                + "<pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[]]></pre>\n"
                + "<p>after</p>");

        // Not closed and bad-formed macros
        test(
            "a{a}{b}",
            "<p>a<span class='wikimodel-macro' macroName='a'><![CDATA[{b}]]></span></p>");
        test(
            "a{a}{b}{",
            "<p>a<span class='wikimodel-macro' macroName='a'><![CDATA[{b}{]]></span></p>");
        test(
            "a {{x:}} b",
            "<p>a {<span class='wikimodel-macro' macroName='x:'><![CDATA[} b]]></span></p>");
        test(
            "a {{x:}} }b",
            "<p>a {<span class='wikimodel-macro' macroName='x:'><![CDATA[} }b]]></span></p>");

        test(
            "a {{x:}} {}b",
            "<p>a {<span class='wikimodel-macro' macroName='x:'><![CDATA[} {}b]]></span></p>");
        test(
            "a {{x:}}, {{y:}} b",
            "<p>a {<span class='wikimodel-macro' macroName='x:'><![CDATA[}, {{y:}} b]]></span></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testParagraphs() throws WikiParserException
    {
        test("{{background='blue'}}", "<p background='blue'></p>");
        test(""
            + "{{background='blue'}}\n"
            + "{{background='red'}}\n"
            + "{{background='green'}}", ""
            + "<p background='blue'></p>\n"
            + "<p background='red'></p>\n"
            + "<p background='green'></p>");
        test(""
            + "{{background='blue'}}first\n"
            + "{{background='red'}}second\n"
            + "{{background='green'}}third", ""
            + "<p background='blue'>first</p>\n"
            + "<p background='red'>second</p>\n"
            + "<p background='green'>third</p>");
        test(""
            + "{{background='blue'}}\nfirst\n"
            + "{{background='red'}}\nsecond\n"
            + "{{background='green'}}\nthird", ""
            + "<p background='blue'>first</p>\n"
            + "<p background='red'>second</p>\n"
            + "<p background='green'>third</p>");

        test("{{background='blue'}}hello", "<p background='blue'>hello</p>");
        test("{{background='blue'}}\n"
            + "First paragraph\n"
            + "\n"
            + "\n"
            + "\n"
            + "", ""
            + "<p background='blue'>First paragraph</p>\n"
            + "<div style='height:3em;'></div>");

        test("First paragraph\n" + "\n" + "\n" + "\n" + "");
        test("First paragraph.\n"
            + "Second line of the same paragraph.\n"
            + "\n"
            + "The second paragraph");

        test("\n<toto");
    }

    /**
     * @throws WikiParserException
     */
    public void testPropertiesBlock() throws WikiParserException
    {
        test(
            "%toto hello  world\n123",
            "<div class='wikimodel-property' url='toto'><p>hello  world</p>\n</div>\n<p>123</p>");
        test(
            "%prop1 value1\n%prop2 value2",
            ""
                + "<div class='wikimodel-property' url='prop1'><p>value1</p>\n</div>\n"
                + "<div class='wikimodel-property' url='prop2'><p>value2</p>\n</div>");
        test(
            "%prop1 value1\nparagraph\n%prop2 value2",
            ""
                + "<div class='wikimodel-property' url='prop1'><p>value1</p>\n</div>\n"
                + "<p>paragraph</p>\n"
                + "<div class='wikimodel-property' url='prop2'><p>value2</p>\n</div>");

        test("%prop1 (((embedded)))next paragraph\n%prop2 value2", ""
            + "<div class='wikimodel-property' url='prop1'>\n"
            + "<p>embedded</p>\n"
            + "</div>\n"
            + "<p>next paragraph</p>\n"
            + "<div class='wikimodel-property' url='prop2'><p>value2</p>\n"
            + "</div>");
        test(
            "%prop1 (((=Header\n- item 1\n- item 2)))next paragraph\n%prop2 value2",
            ""
                + "<div class='wikimodel-property' url='prop1'>\n"
                + "<h1>Header</h1>\n"
                + "<ul>\n"
                + "  <li>item 1</li>\n"
                + "  <li>item 2</li>\n"
                + "</ul>\n"
                + "</div>\n"
                + "<p>next paragraph</p>\n"
                + "<div class='wikimodel-property' url='prop2'><p>value2</p>\n"
                + "</div>");

        test(
            "before\n"
                + "\n"
                + "%company (((\n"
                + "    %name Cognium Systems\n"
                + "    %addr (((\n"
                + "        %country [France]\n"
                + "        %city [Paris]\n"
                + "        %street Cité Nollez\n"
                + "        This is just a description...\n"
                + "    )))\n"
                + ")))\n"
                + "\n"
                + "after",
            ""
                + "<p>before</p>\n"
                + "<div class='wikimodel-property' url='company'>\n"
                + "<div class='wikimodel-property' url='name'><p>Cognium Systems</p>\n"
                + "</div>\n"
                + "<div class='wikimodel-property' url='addr'>\n"
                +
                "<div class='wikimodel-property' url='country'><p><a href='France' class='wikimodel-freestanding'>France</a></p>\n"
                + "</div>\n"
                +
                "<div class='wikimodel-property' url='city'><p><a href='Paris' class='wikimodel-freestanding'>Paris</a></p>\n"
                + "</div>\n"
                + "<div class='wikimodel-property' url='street'><p>Cité Nollez</p>\n"
                + "</div>\n"
                + "<p>        This is just a description...</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "<p>after</p>");
        // Bad formed block properties

        // No closing brackets
        test(
            "before\n"
                + "\n"
                + "%company (((\n"
                + "    %name Cognium Systems\n"
                + "    %addr (((\n"
                + "        %country [France]\n"
                + "        %city Paris\n"
                + "        %street Cité Nollez\n"
                + "        This is just a description...\n"
                + "after",
            "<p>before</p>\n"
                + "<div class='wikimodel-property' url='company'>\n"
                + "<div class='wikimodel-property' url='name'><p>Cognium Systems</p>\n"
                + "</div>\n"
                + "<div class='wikimodel-property' url='addr'>\n"
                +
                "<div class='wikimodel-property' url='country'><p><a href='France' class='wikimodel-freestanding'>France</a></p>\n"
                + "</div>\n"
                + "<div class='wikimodel-property' url='city'><p>Paris</p>\n"
                + "</div>\n"
                + "<div class='wikimodel-property' url='street'><p>Cité Nollez</p>\n"
                + "</div>\n"
                + "<p>        This is just a description...\n"
                + "after</p>\n"
                + "</div>\n"
                + "</div>");
    }

    public void testPropertiesInline() throws WikiParserException
    {
        test(
            "before %prop(value) after",
            "<p>before <span class='wikimodel-property' url='prop'>value</span> after</p>");
        test(
            "before %foo:bar:toto.com/titi/tata?query=x#ancor(value) after",
            "<p>before <span class='wikimodel-property' url='foo:bar:toto.com/titi/tata?query=x#ancor'>value</span> after</p>");
        test(
            "before %prop(before*bold*__italic__^^superscript^^~~subscript~~value) after",
            "<p>before <span class='wikimodel-property' url='prop'>before<strong>bold</strong><em>italic</em><sup>superscript</sup><sub>subscript</sub>value</span> after</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testQuot() throws WikiParserException
    {
        test("Q: Quotation", "<blockquote>\n Quotation\n</blockquote>");

        test(">This is a message\n"
            + ">>and this is a response to the message \n"
            + "> This is a continuation of the same message", ""
            + ""
            + "<blockquote>\n"
            + "This is a message"
            + "<blockquote>\n"
            + "and this is a response to the message \n"
            + "</blockquote>\n" // The new line of the blockquote element
            + "\n" // The new line from the end of the previous quoteline
            + " This is a continuation of the same message\n"
            + "</blockquote>");

        test("This is a paragraph\n"
            + ">and this is a quotations\n"
            + "> the second line", "<p>This is a paragraph</p>\n"
            + "<blockquote>\n"
            + "and this is a quotations\n"
            + " the second line\n"
            + "</blockquote>");

        test("        This is just a description...\n" + "    \n" + "\n" + "\n");
        test("> first\n"
            + ">> second\n"
            + ">> third\n"
            + ">>> subquot1\n"
            + ">>> subquot2\n"
            + ">> fourth");
        test("{{a='b'}}\n"
            + "  first\n"
            + "  second\n"
            + "  third\n"
            + "    subquot1\n"
            + "    subquot2"
            + "  fourth");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
        test(
            "Это (=ссылка=) на внешний документ...",
            "<p>Это <a href='ссылка' class='wikimodel-freestanding'>ссылка</a> на внешний документ...</p>");
        test(
            "Это (=http://www.google.com ссылка=) на внешний документ...",
            "<p>Это <a href='http://www.google.com'>ссылка</a> на внешний документ...</p>");
        test(
            "This is a (=reference=) to an external document...",
            "<p>This is a <a href='reference' class='wikimodel-freestanding'>reference</a> to an external document...</p>");
        test(
            "This is a (=http://www.google.com reference=) to an external document...",
            "<p>This is a <a href='http://www.google.com'>reference</a> to an external document...</p>");

        test(
            "before http://www.foo.bar/com after",
            "<p>before <a href='http://www.foo.bar/com' class='wikimodel-freestanding'>http://www.foo.bar/com</a> after</p>");
        test(
            "before http://www.foo.bar/com?q=abc#ancor after",
            "<p>before <a href='http://www.foo.bar/com?q=abc#ancor' class='wikimodel-freestanding'>http://www.foo.bar/com?q=abc#ancor</a> after</p>");
        test(
            "before wiki:Hello after",
            "<p>before <a href='wiki:Hello' class='wikimodel-freestanding'>wiki:Hello</a> after</p>");
        test(
            "before abc:cde#efg after",
            "<p>before <a href='abc:cde#efg' class='wikimodel-freestanding'>abc:cde#efg</a> after</p>");
        // Opaque URIs
        test(
            "before first:second:third:anonymous@hello/path/?query=value#ancor after",
            "<p>before <a href='first:second:third:anonymous@hello/path/?query=value#ancor' class='wikimodel-freestanding'>first:second:third:anonymous@hello/path/?query=value#ancor</a> after</p>");
        test(
            "http://123.234.245.34/toto/titi/MyDoc.pdf",
            "<p><a href='http://123.234.245.34/toto/titi/MyDoc.pdf' class='wikimodel-freestanding'>http://123.234.245.34/toto/titi/MyDoc.pdf</a></p>");

        // "Magic" references (starting with "image:", "download:", ...)
        test(
            "before image:http://www.foo.com/bar.gif after",
            "<p>before <img src='http://www.foo.com/bar.gif' class='wikimodel-freestanding'/> after</p>");
        test(
            "before download:http://www.foo.com/bar.zip after",
            "<p>before <a href='http://www.foo.com/bar.zip' class='wikimodel-freestanding'>http://www.foo.com/bar.zip</a> after</p>");
        test("download:MyDoc.pdf", "<p><a href='MyDoc.pdf' class='wikimodel-freestanding'>MyDoc.pdf</a></p>");
        test(
            "Reference: download:MyDoc.pdf :not a reference",
            "<p>Reference: <a href='MyDoc.pdf' class='wikimodel-freestanding'>MyDoc.pdf</a> :not a reference</p>");

        // Escaped reference
        test(
            "before wiki\\:Hello after",
            "<p>before wiki<span class='wikimodel-escaped'>:</span>Hello after</p>");

        // Not references
        test("download::MyDoc.pdf", "<p>download::MyDoc.pdf</p>");
        test("before abc::after", "<p>before abc::after</p>");
        test("before abc: after", "<p>before abc: after</p>");
        test("before abc# after", "<p>before abc# after</p>");
        test("before abc:#cde after", "<p>before abc:#cde after</p>");

        // Explicit references.
        test(
            "before [toto] after",
            "<p>before <a href='toto' class='wikimodel-freestanding'>toto</a> after</p>");
        test(
            "before (=toto=) after",
            "<p>before <a href='toto' class='wikimodel-freestanding'>toto</a> after</p>");
        test(
            "before [#local ancor] after",
            "<p>before <a href='#local'>ancor</a> after</p>");

        test("before (((doc-before(=toto=)doc-after))) after", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>doc-before<a href='toto' class='wikimodel-freestanding'>toto</a>doc-after</p>\n"
            + "</div>\n"
            + "<p>after</p>");
        test("before ((((=toto=)))) after", ""
            + "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p><a href='toto' class='wikimodel-freestanding'>toto</a></p>\n"
            + "</div>\n"
            + "<p>after</p>");
        test(" ((((=toto=))))", ""
            + "<div class='wikimodel-document'>\n"
            + "<p><a href='toto' class='wikimodel-freestanding'>toto</a></p>\n"
            + "</div>");
        test("((((=toto=))))", ""
            + "<div class='wikimodel-document'>\n"
            + "<p><a href='toto' class='wikimodel-freestanding'>toto</a></p>\n"
            + "</div>");

        test("((((((toto))))))", ""
            + "<div class='wikimodel-document'>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>toto</p>\n"
            + "</div>\n"
            + "</div>");
        test("(((a(((toto)))b)))", ""
            + "<div class='wikimodel-document'>\n"
            + "<p>a</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>toto</p>\n"
            + "</div>\n"
            + "<p>b</p>\n"
            + "</div>");
    }

    /**
     * @throws WikiParserException
     */
    public void testSpecialSymbols() throws WikiParserException
    {
        test(":)");
    }

    /**
     * @throws WikiParserException
     */
    public void testTables() throws WikiParserException
    {
        // "!!" and "::" markup
        test("!! Header :: Cell ", ""
            + "<table><tbody>\n"
            + "  <tr><th> Header </th><td> Cell </td></tr>\n"
            + "</tbody></table>");
        test("!!   Header    ::    Cell    ", ""
            + "<table><tbody>\n"
            + "  <tr><th>   Header    </th><td>    Cell    </td></tr>\n"
            + "</tbody></table>");
        test("!! Header 1.1 !! Header 1.2\n"
            + ":: Cell 2.1 :: Cell 2.2\n"
            + ":: Cell 3.1 !! Head 3.2", ""
            + "<table><tbody>\n"
            + "  <tr><th> Header 1.1 </th><th> Header 1.2</th></tr>\n"
            + "  <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>\n"
            + "  <tr><td> Cell 3.1 </td><th> Head 3.2</th></tr>\n"
            + "</tbody></table>");

        test("::Cell 1 :: Cell 2", "<table><tbody>\n"
            + "  <tr><td>Cell 1 </td><td> Cell 2</td></tr>\n"
            + "</tbody></table>");
        test("Not a Header :: Not a Cell", "<p>Not a Header :: Not a Cell</p>");
        test("Not a Header::Not a Cell", "<p>Not a Header::Not a Cell</p>");

        // Creole syntax
        test("|= Header 1.1 |= Header 1.2\n"
            + "| Cell 2.1 | Cell 2.2\n"
            + "| Cell 3.1 |= Head 3.2", ""
            + "<table><tbody>\n"
            + "  <tr><th> Header 1.1 </th><th> Header 1.2</th></tr>\n"
            + "  <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>\n"
            + "  <tr><td> Cell 3.1 </td><th> Head 3.2</th></tr>\n"
            + "</tbody></table>");
        test("|={{a=b}} Header 1.1 |= Header 1.2\n"
            + "| Cell 2.1 | Cell 2.2\n"
            + "| Cell 3.1 |={{c=d}} Head 3.2", ""
            + "<table><tbody>\n"
            + "  <tr><th a='b'> Header 1.1 </th><th> Header 1.2</th></tr>\n"
            + "  <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>\n"
            + "  <tr><td> Cell 3.1 </td><th c='d'> Head 3.2</th></tr>\n"
            + "</tbody></table>");
        test(
            "{{x=y}}|={{a=b}} Header 1.1 |={{n=m}} Header 1.2",
            ""
                + "<table><tbody>\n"
                + "  <tr x='y'><th a='b'> Header 1.1 </th><th n='m'> Header 1.2</th></tr>\n"
                + "</tbody></table>");
        test(
            "{{A=B}}\n{{x=y}}|={{a=b}} Header 1.1 |={{n=m}} Header 1.2",
            ""
                + "<table A='B'><tbody>\n"
                + "  <tr x='y'><th a='b'> Header 1.1 </th><th n='m'> Header 1.2</th></tr>\n"
                + "</tbody></table>");

        // "||" and "|" markup
        test("|| Header | Cell ", ""
            + "<table><tbody>\n"
            + "  <tr><th> Header </th><td> Cell </td></tr>\n"
            + "</tbody></table>");
        test("||   Header    |    Cell    ", ""
            + "<table><tbody>\n"
            + "  <tr><th>   Header    </th><td>    Cell    </td></tr>\n"
            + "</tbody></table>");

        test("|| cell 1.1 || cell 1.2\n" + "|| cell 2.1|| cell 2.2", ""
            + "<table><tbody>\n"
            + "  <tr><th> cell 1.1 </th><th> cell 1.2</th></tr>\n"
            + "  <tr><th> cell 2.1</th><th> cell 2.2</th></tr>\n"
            + "</tbody></table>");
        test("|| Head 1.1 || Head 1.2\n" + "| cell 2.1| cell 2.2", ""
            + "<table><tbody>\n"
            + "  <tr><th> Head 1.1 </th><th> Head 1.2</th></tr>\n"
            + "  <tr><td> cell 2.1</td><td> cell 2.2</td></tr>\n"
            + "</tbody></table>");
        test("|| Multi \nline  \nheader \n"
            + "| Multi\nline\ncell\n"
            + "\n"
            + "One,two,three", ""
            + "<table><tbody>\n"
            + "  <tr><th> Multi \nline  \nheader </th></tr>\n"
            + "  <tr><td> Multi\nline\ncell</td></tr>\n"
            + "</tbody></table>\n"
            + "<p>One,two,three</p>");
        test("this is not || a table", "<p>this is not || a table</p>");
        test("this is not | a table", "<p>this is not | a table</p>");
        test(
            "|| __Italic header__ || *Bold header*\n"
                + "| __Italic cell__ | *Bold cell*\n",
            ""
                + "<table><tbody>\n"
                + "  <tr><th> <em>Italic header</em> </th><th> <strong>Bold header</strong></th></tr>\n"
                + "  <tr><td> <em>Italic cell</em> </td><td> <strong>Bold cell</strong></td></tr>\n"
                + "</tbody></table>");
        test(
            "|| __Italic header || *Bold header \n"
                + "| __Italic cell | *Bold cell \n",
            ""
                + "<table><tbody>\n"
                + "  <tr><th> <em>Italic header </em></th><th> <strong>Bold header </strong></th></tr>\n"
                + "  <tr><td> <em>Italic cell </em></td><td> <strong>Bold cell </strong></td></tr>\n"
                + "</tbody></table>");

        // Table parameters
        test("{{a=b}}\n|| Header ", ""
            + "<table a='b'><tbody>\n"
            + "  <tr><th> Header </th></tr>\n"
            + "</tbody></table>");
        test("{{a=b}}\n!! Header ", ""
            + "<table a='b'><tbody>\n"
            + "  <tr><th> Header </th></tr>\n"
            + "</tbody></table>");
        test("{{a=b}}\n| cell ", ""
            + "<table a='b'><tbody>\n"
            + "  <tr><td> cell </td></tr>\n"
            + "</tbody></table>");
        test("{{a=b}}\n:: cell ", ""
            + "<table a='b'><tbody>\n"
            + "  <tr><td> cell </td></tr>\n"
            + "</tbody></table>");

        // Row parameters
        test("{{a=b}}||cell");
        test("{{a=b}}::cell1\n{{c=d}}::cell2");

        test("{{a=b}}\n{{c=d}}||{{e=f}} cell");
        test("{{a=b}}\n{{c=d}}::{{e=f}} cell ::{{g=h}}");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimeBlocks() throws WikiParserException
    {
        test("{{{verbatim}}}", "<pre>verbatim</pre>");
        test("{{{ver\\}}}batim}}}", "<pre>ver}}}batim</pre>");
        test("{{{verbatim", "<pre>verbatim</pre>");
        test("{{{{{{verbatim", "<pre>{{{verbatim</pre>");
        test("{{{{{{verbatim}}}", "<pre>{{{verbatim</pre>");
        test("{{{{{{verbatim}}}}}}", "<pre>{{{verbatim}}}</pre>");
        test(
            "{{{before{{{verbatim}}}after}}}",
            "<pre>before{{{verbatim}}}after</pre>");

        test(
            "{{{before{{{123{{{verbatim}}}456}}}after}}}",
            "<pre>before{{{123{{{verbatim}}}456}}}after</pre>");
        test(
            "{{{verbatim}}}}}} - the three last symbols should be in a paragraph",
            "<pre>verbatim</pre>\n"
                + "<p>}}} - the three last symbols should be in a paragraph</p>");

        // inline verbatim blocks
        test(" {{{abc}}}", "<p> <tt class=\"wikimodel-verbatim\">abc</tt></p>");
        test("before{{{abc}}}after", "<p>before<tt class=\"wikimodel-verbatim\">abc</tt>after</p>");
        test(" {{{{{{abc}}}}}}", "<p> <tt class=\"wikimodel-verbatim\">{{{abc}}}</tt></p>");

        test(" {{{verbatim}}}", "<p> <tt class=\"wikimodel-verbatim\">verbatim</tt></p>");
        test(" {{{{{{verbatim}}}", "<p> <tt class=\"wikimodel-verbatim\">{{{verbatim</tt></p>");
        test(" {{{{{{verbatim}}}}}}", "<p> <tt class=\"wikimodel-verbatim\">{{{verbatim}}}</tt></p>");
        test(
            "before{{{xxx{{{verbatim}}}after",
            "<p>before<tt class=\"wikimodel-verbatim\">xxx{{{verbatim}}}after</tt></p>");
        test(
            "before{{{verbatim}}}after",
            "<p>before<tt class=\"wikimodel-verbatim\">verbatim</tt>after</p>");
        test(
            "before{{{verbatim}}}}}}after",
            "<p>before<tt class=\"wikimodel-verbatim\">verbatim</tt>}}}after</p>");

        test(" `verbatim`", "<p> <tt class=\"wikimodel-verbatim\">verbatim</tt></p>");
        test(" `{{{verbatim`", "<p> <tt class=\"wikimodel-verbatim\">{{{verbatim</tt></p>");
        test(" `{{{verbatim}}}`", "<p> <tt class=\"wikimodel-verbatim\">{{{verbatim}}}</tt></p>");
        test("before`verbatim`after", "<p>before<tt class=\"wikimodel-verbatim\">verbatim</tt>after</p>");
        test(
            "before`verbatim`}}}after",
            "<p>before<tt class=\"wikimodel-verbatim\">verbatim</tt>}}}after</p>");
        // Broken inline verbatim
        test(
            "before`xxx{{{verbatim}}}after",
            "<p>before`xxx<tt class=\"wikimodel-verbatim\">verbatim</tt>after</p>");

        // Complex formatting
        test("!! Syntax !! Results\n"
            + ":: {{{\n"
            + "!! Header 1 !! Header 2\n"
            + ":: Cell 1 :: Cell 2\n"
            + "}}} :: (((\n"
            + "!! Header 1 !! Header 2\n"
            + ":: Cell 1 :: Cell 2\n"
            + ")))\n"
            + ":: {{{\n"
            + "|| Header 1 || Header 2\n"
            + "| Cell 1 | Cell 2\n"
            + "}}} :: (((\n"
            + "|| Header 1 || Header 2\n"
            + "| Cell 1 | Cell 2\n"
            + ")))\n"
            + "");
    }

    public void testVerbatimInlineElements() throws WikiParserException
    {
        test("`verbatim`", "<p><tt class=\"wikimodel-verbatim\">verbatim</tt></p>");
        test("before`verbatim`after", "<p>before<tt class=\"wikimodel-verbatim\">verbatim</tt>after</p>");

        // Bad formed elements
        test("`verbatim", "<p>`verbatim</p>");
        test("before`after", "<p>before`after</p>");
        test("before`after\nnext line", "<p>before`after\nnext line</p>");
    }
}
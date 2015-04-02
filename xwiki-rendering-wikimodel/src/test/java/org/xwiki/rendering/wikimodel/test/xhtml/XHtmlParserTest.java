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
package org.xwiki.rendering.wikimodel.test.xhtml;

import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.test.AbstractWikiParserTest;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XHtmlParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public XHtmlParserTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new XhtmlParser();
    }

    /**
     * @throws WikiParserException
     */
    public void testDefinitionLists() throws WikiParserException
    {
        test("<html><dl><dt>term</dt><dd>definition</dd></dl></html>", ""
            + "<dl>\n"
            + "  <dt>term</dt>\n"
            + "  <dd>definition</dd>\n"
            + "</dl>");
        test(
            "<html><dl><dt>term1</dt><dt>term2</dt><dd>definition</dd></dl></html>",
            ""
                + "<dl>\n"
                + "  <dt>term1</dt>\n"
                + "  <dt>term2</dt>\n"
                + "  <dd>definition</dd>\n"
                + "</dl>");
        // FIXME: this test generates an invalid structure (should be an
        // embedded document with an internal definition list);
        test("<html>"
            + "<dl>"
            + "<dt>term</dt>"
            + "<dd>definition"
            + "<dl>"
            + "<dt>term</dt>"
            + "<dd>definition</dd>"
            + "</dl>"
            + "</dd>"
            + "</dl></html>");
    }

    /**
     * @throws WikiParserException
     */
    public void testDocuments() throws WikiParserException
    {
        test("<html><p>before</p>\n"
            + "<div>\n"
            + "<p>inside</p>\n"
            + "</div>\n"
            + "<p>after</p></html>", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n"
            + "</div>\n"
            + "<p>after</p>");
        test("<html><p>before</p>\n"
            + "<div>\n"
            + "<p>inside</p>\n"
            + "</div>\n"
            + "<p>after</p></html>", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n"
            + "</div>\n"
            + "<p>after</p>");
        test(
            "<html><table><tbody>\n"
                + " <tr><td> Line One </td><td> First doc:<div>\n"
                + "<p>inside</p>\n"
                + "</div>\n"
                + "after</td></tr>\n"
                + "   <tr><td>Line Two</td><td>Second doc:<div>\n"
                + "<p>lkjlj</p>\n"
                + "</div>\n"
                + "skdjg</td></tr>\n"
                + "</tbody></table></html>",
            "<table><tbody>\n"
                + "  <tr><td>Line One</td><td>First doc:<div class='wikimodel-document'>\n"
                + "<p>inside</p>\n"
                + "</div>\n"
                + "after</td></tr>\n"
                + "  <tr><td>Line Two</td><td>Second doc:<div class='wikimodel-document'>\n"
                + "<p>lkjlj</p>\n"
                + "</div>\n"
                + "skdjg</td></tr>\n"
                + "</tbody></table>");
        test(
            "<html><table><tbody>\n"
                + "  <tr><td>This is a table:</td><td><div>\n"
                + "<ul>\n"
                + "  <li>item one</li>\n"
                + "  <li>item two</li>\n"
                + "  <li>subitem 1</li>\n"
                + "  <li>subitem 2</li>\n"
                + "  <li>item three</li>\n"
                + "</ul>\n"
                + "</div>\n"
                + "</td></tr>\n"
                + "</tbody></table></html>",
            "<table><tbody>\n"
                + "  <tr><td>This is a table:</td><td><div class='wikimodel-document'>\n"
                + "<ul>\n"
                + "  <li>item one</li>\n"
                + "  <li>item two</li>\n"
                + "  <li>subitem 1</li>\n"
                + "  <li>subitem 2</li>\n"
                + "  <li>item three</li>\n"
                + "</ul>\n"
                + "</div>\n"
                + "</td></tr>\n"
                + "</tbody></table>");

        test("<html><p>before</p>\n"
            + "<div>\n"
            + "<p>opened and not closed</p>\n"
            + "</div></html>", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>opened and not closed</p>\n"
            + "</div>");
        test("<html><p>before</p>\n"
            + "<div>\n"
            + "<p>one</p>\n"
            + "<div>\n"
            + "<p>two</p>\n"
            + "<div>\n"
            + "<p>three</p>\n"
            + "</div>\n"
            + "</div>\n"
            + "</div></html>", "<p>before</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>one</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>two</p>\n"
            + "<div class='wikimodel-document'>\n"
            + "<p>three</p>\n"
            + "</div>\n"
            + "</div>\n"
            + "</div>");

        test(
            "<html><ul><li>Item 1<div><ul><li>Item 2</li></ul></div></li></ul></html>",
            "<ul>\n"
                + "  <li>Item 1<div class='wikimodel-document'>\n"
                + "<ul>\n"
                + "  <li>Item 2</li>\n"
                + "</ul>\n"
                + "</div>\n"
                + "</li>\n"
                + "</ul>");
        test(
            "<html><ul><li>Item 1<p>Before</p><ul><li>Item 2</li></ul><p>After</p></li></ul></html>",
            "<ul>\n"
                + "  <li>Item 1<div class='wikimodel-document'>\n"
                + "<p>Before</p>\n"
                + "<ul>\n"
                + "  <li>Item 2</li>\n"
                + "</ul>\n"
                + "<p>After</p>\n"
                + "</div>\n"
                + "</li>\n"
                + "</ul>");

        test("<html><div class='toto'>\n"
            + "<p>inside</p>\n"
            + "</div></html>",
            "<div class='wikimodel-document' class='toto'>\n"
                + "<p>inside</p>\n"
                + "</div>");
    }

    /**
     * @throws WikiParserException
     */
    public void testEscape() throws WikiParserException
    {
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("<html><b>bold</b></html>", "<p><strong>bold</strong></p>");
        test(
            "<html><strong>bold</strong></html>",
            "<p><strong>bold</strong></p>");

        test("<html><s>strike</s></html>", "<p><strike>strike</strike></p>");
        test(
            "<html><strike>strike</strike></html>",
            "<p><strike>strike</strike></p>");
        test("<html><del>strike</del></html>", "<p><strike>strike</strike></p>");

        test("<html><em>italic</em></html>", "<p><em>italic</em></p>");

        test("<html><u>underline</u></html>", "<p><ins>underline</ins></p>");
        test("<html><ins>underline</ins></html>", "<p><ins>underline</ins></p>");

        test("<html><sup>sup</sup></html>", "<p><sup>sup</sup></p>");
        test("<html><sub>sub</sub></html>", "<p><sub>sub</sub></p>");

        test("<html><tt>mono</tt></html>", "<p><mono>mono</mono></p>");

        test(
            "<html>a<strong><em>b</em></strong>c</html>",
            "<p>a<strong><em>b</em></strong>c</p>");

        test("<html>a<em><em>b</em></em>c</html>", "<p>a<em>b</em>c</p>");
        test(
            "<html>a<em><strong><em>b</em></strong></em>c</html>",
            "<p>a<em><strong>b</strong></em>c</p>");

        test(
            "<html><p>12<strong>34<span param='value'>56</span>78</strong>90</p></html>",
            "<p>12<strong>34</strong><strong><span class='wikimodel-parameters'[param='value']>56</span></strong><strong>78</strong>90</p>");

        test(
            "<html><p><span param1='value1'><span param2='value2'>word</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[param1='value1', param2='value2']>word</span></p>");

        // merging

        // style

        test(
            "<html><p><span param1='value1'><span param2='value2'>word</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[param1='value1', param2='value2']>word</span></p>");

        test(
            "<html><p><span param1='value1'><strong param2='value2'>word</strong></span></p></html>",
            "<p><strong><span class='wikimodel-parameters'[param1='value1', param2='value2']>word</span></strong></p>");

        test(
            "<html><p><strong param1='value1'><span param2='value2'>word</span>word2</strong></p></html>",
            "<p><strong><span class='wikimodel-parameters'[param1='value1', param2='value2']>word</span></strong>" +
                "<strong><span class='wikimodel-parameters'[param1='value1']>word2</span></strong></p>");

        test(
            "<html><p><span style='color: red;'><span style='background-color: yellow;'>word</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[style='color: red; background-color: yellow']>word</span></p>");

        test(
            "<html><p><span style='color: red;'>word1<span style='background-color: yellow;'>word2</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[style='color: red;']>word1</span><span class='wikimodel-parameters'[style='color: red; background-color: yellow']>word2</span></p>");

        test(
            "<html><p><span style='font-family: times new roman'>x<span style='font-family: arial black'>y</span>z</span></p></html>",
            "<p><span class='wikimodel-parameters'[style='font-family: times new roman']>x</span>" +
                "<span class='wikimodel-parameters'[style='font-family: times new roman; font-family: arial black']>y</span>" +
                "<span class='wikimodel-parameters'[style='font-family: times new roman']>z</span></p>");

        // class

        test(
            "<html><p><span class='class1'><span class='class2'>word</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[class='class1 class2']>word</span></p>");

        test(
            "<html><p><span class='class1'>word1<span class='class2'>word2</span>word3</span></p></html>",
            "<p><span class='wikimodel-parameters'[class='class1']>word1</span>" +
                "<span class='wikimodel-parameters'[class='class1 class2']>word2</span>" +
                "<span class='wikimodel-parameters'[class='class1']>word3</span></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHeaders() throws WikiParserException
    {
        test("<html><h1>header1</h1></html>");
        test("<html><h2>header2</h2></html>");
        test("<html><h3>header3</h3></html>");
        test("<html><h4>header4</h4></html>");
        test("<html><h5>header5</h5></html>");
        test("<html><h6>header6</h6></html>");

        test("<html>before<h1>header1</h1>after</html>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHorLine() throws WikiParserException
    {
        test("<html>before<hr />after</html>", ""
            + "<p>before</p>\n"
            + "<hr />\n"
            + "<p>after</p>");
        test("<html><hr a='b' /></html>", "<hr a='b' />");
    }

    public void testImages() throws WikiParserException
    {
        test(
            "<html><img src=\"target\" alt=\"some description\"/></html>",
            "<p><img src='target' title='some description'/></p>");

        test(
            "<html><img src=\"target\" alt=\"some description\" class=\"wikimodel-freestanding\" param1=\"value1\"/></html>",
            "<p><img src='target' param1='value1' title='some description'/></p>");

        test(
            "<html><img src=\"target\" alt=\"some description\" class=\"wikimodel-freestanding\"/></html>",
            "<p><img src='target' class='wikimodel-freestanding'/></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testLineBreak() throws WikiParserException
    {
        test("<html>before<br />after</html>", "<p>before\nafter</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testLists() throws WikiParserException
    {
        // TODO: add management of embedded block elements.
        test(
            "<html><ul><li>a<ul><li>b</li></ul></li><li>c</li></ul></html>",
            ""
                + "<ul>\n"
                + "  <li>a<ul>\n"
                + "  <li>b</li>\n"
                + "</ul>\n"
                + "</li>\n"
                + "  <li>c</li>\n"
                + "</ul>");

        test("<html><ul>"
            + "<li>item one</li>"
            + "<li>before<hr />after</li>"
            + "</ul></html>");
        test("<html><ul>"
            + "<li>item one</li>"
            + "<li>before"
            + " <ul>"
            + "  <li>item one</li>"
            + " </ul>"
            + "after</li>"
            + "</ul></html>");

        test("<html><ol><li>Item 1<ol><li>Item 2<ul class=\"star\"><li>Item\r\n"
            + "3</li></ul></li><li>Item 4</li></ol></li><li>Item 5</li></ol><ul\r\n"
            + "class=\"star\"><li>Item 1<ul class=\"star\"><li>Item 2<ul class=\"star\"><li>Item\r\n"
            + "3</li></ul></li><li>Item 4</li></ul></li><li>Item 5</li><li>Item\r\n"
            + "6</li></ul></html>\r\n"
            + "");

        test(
            "<html><ol><li>item one<ul><li>item two</li></ul></li></ol></html>",
            "<ol>\n"
                + "  <li>item one<ul>\n"
                + "  <li>item two</li>\n"
                + "</ul>\n"
                + "</li>\n"
                + "</ol>");
    }

    /**
     * @throws WikiParserException
     */
    public void testParagraphs() throws WikiParserException
    {
        test("<html><p>paragraph</p></html>", "<p>paragraph</p>");
        test(
            "<p>hello <em class=\"italic\">beautiful</em> <strong>world</strong></p>",
            "<p>hello <em><span class='wikimodel-parameters'[class='italic']>beautiful</span></em> <strong>world</strong></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testQuot() throws WikiParserException
    {
        test(
            "<html><blockquote>quote</blockquote></html>",
            "<blockquote>\nquote\n</blockquote>");
        test(
            "<html><blockquote>line1<blockquote>line2<blockquote>line3</blockquote>"
                + "line4</blockquote></blockquote></html>",
            "<blockquote>\nline1<blockquote>\nline2<blockquote>\nline3\n</blockquote>"
                + "\n\nline4\n</blockquote>\n\n</blockquote>");

        test(
            "<html><blockquote><p>quote</p></blockquote></html>",
            "<blockquote>\nquote\n</blockquote>");
        test(
            "<html><blockquote><p>line1</p><blockquote><p>line2</p><blockquote><p>line3</p></blockquote>"
                + "<p>line4</p></blockquote></blockquote></html>",
            "<blockquote>\nline1<blockquote>\nline2<blockquote>\nline3\n</blockquote>"
                + "\n\nline4\n</blockquote>\n\n</blockquote>");

        test(
            "<html><blockquote><span param='vale'>line1<br/>line2</span></blockquote></html>",
            "<blockquote>\n" +
                "<span class='wikimodel-parameters'[param='vale']>line1</span>\n" +
                "<span class='wikimodel-parameters'[param='vale']>line2</span>\n" +
                "</blockquote>");

        test(
            "<html><blockquote><span param1='value1'><span param2='vale2'>line1<br/>line2</span></span></blockquote></html>",
            "<blockquote>\n" +
                "<span class='wikimodel-parameters'[param1='value1', param2='vale2']>line1</span>\n" +
                "<span class='wikimodel-parameters'[param1='value1', param2='vale2']>line2</span>\n" +
                "</blockquote>");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
        test("<html><a href=\"reference\">label</a></html>", "<p><a href='reference'>label</a></p>");
        test("<html><a href=\"reference\" param=\"value\">label</a></html>",
            "<p><a href='reference' param='value'>label</a></p>");
        test("<html><a href=\"reference\" class=\"wikimodel-freestanding\" param=\"value\">label</a></html>",
            "<p><a href='reference' param='value'>label</a></p>");
        test("<html><a href=\"reference\" class=\"wikimodel-freestanding\">label</a></html>",
            "<p><a href='reference' class='wikimodel-freestanding'>reference</a></p>");
        test("<html><a href='#foo'>test</a></html>", "<p><a href='#foo'>test</a></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testTables() throws WikiParserException
    {
        test("<html><table><tr><td>first cell</td><td>second cell</td></tr></table></html>",
            "<table><tbody>\n"
                + "  <tr><td>first cell</td><td>second cell</td></tr>\n"
                + "</tbody></table>");
        test("<html><table><tr><td>first cell</td></tr></table></html>",
            "<table><tbody>\n"
                + "  <tr><td>first cell</td></tr>\n"
                + "</tbody></table>");
        test("<html><table>"
            + "<tr><th>first header</th><th>second header</th></tr>"
            + "<tr><td>first cell</td><td>second cell</td></tr>"
            + "</table></html>",
            "<table><tbody>\n"
                + "  <tr><th>first header</th><th>second header</th></tr>\n"
                + "  <tr><td>first cell</td><td>second cell</td></tr>\n"
                + "</tbody></table>");
        test("<html><table>"
            + "<tr><th>first row</th><td>first cell</td></tr>"
            + "<tr><th>second row</th><td>before <table><tr><td>first cell</td></tr></table> after</td></tr>"
            + "<tr><th>third row</th><td>third cell</td></tr>"
            + "</table></html>",
            "<table><tbody>\n"
                + "  <tr><th>first row</th><td>first cell</td></tr>\n"
                + "  <tr><th>second row</th><td>before<div class='wikimodel-document'>\n"
                + "<table><tbody>\n"
                + "  <tr><td>first cell</td></tr>\n"
                + "</tbody></table>\n"
                + "<p>after</p>\n"
                + "</div>\n"
                + "</td></tr>\n"
                + "  <tr><th>third row</th><td>third cell</td></tr>\n"
                + "</tbody></table>");

        // Automatic sub document

        test("<html><table><tr><td><p>text</p></td></tr></table></html>",
            "<table><tbody>\n"
                + "  <tr><td><div class='wikimodel-document'>\n"
                + "<p>text</p>\n"
                + "</div>\n"
                + "</td></tr>\n"
                + "</tbody></table>");

        test("<html><table><tr><td><div class='wikimodel-emptyline'/><p>text</p></td></tr></table></html>",
            "<table><tbody>\n"
                + "  <tr><td><div class='wikimodel-document'>\n"
                + "<p>text</p>\n"
                + "</div>\n"
                + "</td></tr>\n"
                + "</tbody></table>");

        // "Bad-formed" tables...

        // The content is completely ignored.
        test("<html><table>first cell</table></html>", "<table><tbody>\n</tbody></table>");

        // A "td" element directly in the table
        test("<html><table><td>first cell</td></table></html>", "<table><tbody>\n"
            + "  <tr><td>first cell</td></tr>\n"
            + "</tbody></table>");

        // Not a table at all
        test("<html><td>first cell</td></html>", "<table><tbody>\n"
            + "  <tr><td>first cell</td></tr>\n"
            + "</tbody></table>");

        test("<table><tr><td><tt class=\"wikimodel-verbatim\">x</tt></td><td><tt>y</tt></td></tr></table>",
            "<table><tbody>\n"
                + "  <tr><td><tt class=\"wikimodel-verbatim\">x</tt></td><td><mono>y</mono></td></tr>\n"
                + "</tbody></table>");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimBlocks() throws WikiParserException
    {
        test(
            "<html><pre>one\ntwo</pre></html>",
            "<pre>one\ntwo</pre>");
        test(
            "<html><pre>one<br/>two</pre></html>",
            "<pre>one\ntwo</pre>");
        test(
            "<html><pre><b>one</b><br/>two</pre></html>",
            "<pre>one\ntwo</pre>");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimInline() throws WikiParserException
    {
        test(
            "<html><p><tt class=\"wikimodel-verbatim\">one\ntwo</tt></p></html>",
            "<p><tt class=\"wikimodel-verbatim\">one\ntwo</tt></p>");
        test(
            "<html><p><tt class=\"wikimodel-verbatim\">one<br/>two</tt></p></html>",
            "<p><tt class=\"wikimodel-verbatim\">one\ntwo</tt></p>");
        test(
            "<html><p><tt class=\"wikimodel-verbatim\"><b>one</b><br/>two</tt></p></html>",
            "<p><tt class=\"wikimodel-verbatim\">one\ntwo</tt></p>");
    }

    public void testMacro() throws WikiParserException
    {
        test(
            "<html><!--startmacro:name|-|param=\"value\"|-|content--><!--stopmacro--></html>",
            "<pre class='wikimodel-macro' macroName='name' param='value'><![CDATA[content]]></pre>");
        test(
            "<html><!--startmacro:name|-|title=\"value\"-->macro<!--stopmacro--></html>",
            "<pre class='wikimodel-macro' macroName='name' title='value'/>");

        test(
            "<html><!--startmacro:name|-|param=\"va|-|lue\"|-|content-->macro<!--stopmacro--></html>",
            "<pre class='wikimodel-macro' macroName='name' param='va|-|lue'><![CDATA[content]]></pre>");
        test(
            "<html><!--startmacro:name|-|title=\"va|-|lue\"-->macro<!--stopmacro--></html>",
            "<pre class='wikimodel-macro' macroName='name' title='va|-|lue'/>");
        test(
            "<html><!--startmacro:name|-|param=\"value\"|-|content--><!--startmacro:subname|-|subparam=\"subvalue\"|-|subcontent--><!--stopmacro--><!--stopmacro--></html>",
            "<pre class='wikimodel-macro' macroName='name' param='value'><![CDATA[content]]></pre>");
    }

    public void testMisc() throws WikiParserException
    {
        test(
            "<html><p><a name=\"foo\">bar</a></p></html>",
            "<p></p>");
        test(
            "<html><p><a id=\"foo\">bar</a></p></html>",
            "<p></p>");
    }

    /**
     * Test that unsupported tags, like HTML5 ones, are simply ignored
     * @throws WikiParserException
     */
    public void testUnsupportedTags() throws WikiParserException
    {
        test("<html>This is <unsupported>tags</unsupported> ignored</html>",
            "<p>This is tags ignored</p>");

        test("<html><dl><dt>term</dt><dd>definition<unsupported><dl><dt>term</dt><dd>definition</dd></dl></unsupported></dd></dl></html>",
            "<dl>\n"
                + "  <dt>term</dt>\n"
                + "  <dd>definition<dl>\n"
                + "  <dt>term</dt>\n"
                + "  <dd>definition</dd>\n"
                + "</dl>\n"
                + "</dd>\n"
                + "</dl>");

        test(
            "<html><ul><li>a<unsupported><ul><li>b</li></ul></unsupported></li></ul></html>",
            ""
                + "<ul>\n"
                + "  <li>a<ul>\n"
                + "  <li>b</li>\n"
                + "</ul>\n"
                + "</li>\n"
                + "</ul>");

        test(
            "<html><blockquote>line1<unsupported><blockquote>line2<blockquote>line3</blockquote>"
                + "line4</blockquote></unsupported></blockquote></html>",
            "<blockquote>\nline1<blockquote>\nline2<blockquote>\nline3\n</blockquote>"
                + "\n\nline4\n</blockquote>\n\n</blockquote>");
    }

    /**
     * Test malformed constructs created by the HTML cleaner not supporting HTML5
     * @throws WikiParserException
     */
    public void testUnsupportedHTML5Tags() throws WikiParserException
    {
        test(
            "<html><p><section><p>test</p></section></p></html>",
            "<p></p>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>test</p>\n"
                + "</div>");
        test(
            "<html><p><header><h1>test</h1></header></p></html>",
            "<p></p>\n"
                + "<div class='wikimodel-document'>\n"
                + "<h1>test</h1>\n"
                + "</div>");

        test(
            "<html><p><header><h1>test</h1></header></p></html>",
            "<p></p>\n"
                + "<div class='wikimodel-document'>\n"
                + "<h1>test</h1>\n"
                + "</div>");
    }
    
    /**
     * Test basic HTML5 support
     * @throws WikiParserException
     */
    public void testBasicHTML5Tags() throws WikiParserException
    {
        test(
            "<html><header>Header<nav>Navigation</nav></header><main><article><section><h1>section 1</h1><summary>Summary 1</summary><details>Text 1<figure><img src='target'/><figcaption>caption</figcaption></figure></details><aside>Aside</aside></section><section><h1>section 2</h1><p>text 2</p></section></article></main><footer>Footer</footer></html>",
            "<div class='wikimodel-document'>\n"
                + "<p>Header</p>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>Navigation</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class='wikimodel-document'>\n"
                + "<div class='wikimodel-document'>\n"
                + "<div class='wikimodel-document'>\n"
                + "<h1>section 1</h1>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>Summary 1</p>\n"
                + "</div>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>Text 1</p>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p><img src='target'/></p>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>caption</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>Aside</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class='wikimodel-document'>\n"
                + "<h1>section 2</h1>\n"
                + "<p>text 2</p>\n"
                + "</div>\n"
                + "</div>\n"
                + "</div>\n"
                + "<div class='wikimodel-document'>\n"
                + "<p>Footer</p>\n"
                + "</div>");
    }
}

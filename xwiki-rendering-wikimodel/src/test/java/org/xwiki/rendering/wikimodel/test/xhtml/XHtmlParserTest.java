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

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.test.AbstractWikiParserTest;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
class XHtmlParserTest extends AbstractWikiParserTest
{
    @Override
    protected IWikiParser newWikiParser()
    {
        return new XhtmlParser();
    }

    @Test
    void testDefinitionLists() throws WikiParserException
    {
        test("<html><dl><dt>term</dt><dd>definition</dd></dl></html>", """
                <dl>
                  <dt>term</dt>
                  <dd>definition</dd>
                </dl>""");
        test("<html><dl><dt>term1</dt><dt>term2</dt><dd>definition</dd></dl></html>", """
                <dl>
                  <dt>term1</dt>
                  <dt>term2</dt>
                  <dd>definition</dd>
                </dl>""");
        test("<html><dl><dt>term</dt><dd>definition<dl><dt>term</dt><dd>definition</dd></dl></dd></dl></html>",
            """
                <dl>
                  <dt>term</dt>
                  <dd>definition<dl>
                  <dt>term</dt>
                  <dd>definition</dd>
                </dl>
                </dd>
                </dl>""");
    }

    @Test
    void testDocuments() throws WikiParserException
    {
        test("""
                <html><p>before</p>
                <div>
                <p>inside</p>
                </div>
                <p>after</p></html>""", """
                <p>before</p>
                <div class='wikimodel-document'>
                <p>inside</p>
                </div>
                <p>after</p>""");
        test("""
                <html><p>before</p>
                <div>
                <p>inside</p>
                </div>
                <p>after</p></html>""", """
                <p>before</p>
                <div class='wikimodel-document'>
                <p>inside</p>
                </div>
                <p>after</p>""");
        test("""
                <html><table><tbody>
                 <tr><td> Line One </td><td> First doc:<div>
                <p>inside</p>
                </div>
                after</td></tr>
                   <tr><td>Line Two</td><td>Second doc:<div>
                <p>lkjlj</p>
                </div>
                skdjg</td></tr>
                </tbody></table></html>""",
            """
                <table><tbody>
                  <tr><td>Line One</td><td>First doc:<div class='wikimodel-document'>
                <p>inside</p>
                </div>
                after</td></tr>
                  <tr><td>Line Two</td><td>Second doc:<div class='wikimodel-document'>
                <p>lkjlj</p>
                </div>
                skdjg</td></tr>
                </tbody></table>""");
        test("""
                <html><table><tbody>
                  <tr><td>This is a table:</td><td><div>
                <ul>
                  <li>item one</li>
                  <li>item two</li>
                  <li>subitem 1</li>
                  <li>subitem 2</li>
                  <li>item three</li>
                </ul>
                </div>
                </td></tr>
                </tbody></table></html>""",
            """
                <table><tbody>
                  <tr><td>This is a table:</td><td><div class='wikimodel-document'>
                <ul>
                  <li>item one</li>
                  <li>item two</li>
                  <li>subitem 1</li>
                  <li>subitem 2</li>
                  <li>item three</li>
                </ul>
                </div>
                </td></tr>
                </tbody></table>""");

        test("""
                <html><p>before</p>
                <div>
                <p>opened and not closed</p>
                </div></html>""", """
                <p>before</p>
                <div class='wikimodel-document'>
                <p>opened and not closed</p>
                </div>""");
        test("""
                <html><p>before</p>
                <div>
                <p>one</p>
                <div>
                <p>two</p>
                <div>
                <p>three</p>
                </div>
                </div>
                </div></html>""", """
                <p>before</p>
                <div class='wikimodel-document'>
                <p>one</p>
                <div class='wikimodel-document'>
                <p>two</p>
                <div class='wikimodel-document'>
                <p>three</p>
                </div>
                </div>
                </div>""");

        test(
            "<html><ul><li>Item 1<div><ul><li>Item 2</li></ul></div></li></ul></html>",
            """
                <ul>
                  <li>Item 1<div class='wikimodel-document'>
                <ul>
                  <li>Item 2</li>
                </ul>
                </div>
                </li>
                </ul>""");
        test(
            "<html><ul><li>Item 1<p>Before</p><ul><li>Item 2</li></ul><p>After</p></li></ul></html>",
            """
                <ul>
                  <li>Item 1<div class='wikimodel-document'>
                <p>Before</p>
                <ul>
                  <li>Item 2</li>
                </ul>
                <p>After</p>
                </div>
                </li>
                </ul>""");

        test("""
                <html><div class='toto'>
                <p>inside</p>
                </div></html>""",
            """
                <div class='wikimodel-document' class='toto'>
                <p>inside</p>
                </div>""");

        test("<html><ul><li><div>document</div><p>para</p></li></ul></html>",
            """
                <ul>
                  <li><div class='wikimodel-document'>
                <p>document</p>
                </div>
                <div class='wikimodel-document'>
                <p>para</p>
                </div>
                </li>
                </ul>""");
    }

    @Test
    void testFormats() throws WikiParserException
    {
        test("<html><b>bold</b></html>", "<p><strong>bold</strong></p>");
        test("<html><strong>bold</strong></html>", "<p><strong>bold</strong></p>");

        test("<html><s>strike</s></html>", "<p><strike>strike</strike></p>");
        test("<html><strike>strike</strike></html>", "<p><strike>strike</strike></p>");
        test("<html><del>strike</del></html>", "<p><strike>strike</strike></p>");

        test("<html><em>italic</em></html>", "<p><em>italic</em></p>");

        test("<html><u>underline</u></html>", "<p><ins>underline</ins></p>");
        test("<html><ins>underline</ins></html>", "<p><ins>underline</ins></p>");

        test("<html><sup>sup</sup></html>", "<p><sup>sup</sup></p>");
        test("<html><sub>sub</sub></html>", "<p><sub>sub</sub></p>");

        test("<html><tt>mono</tt></html>", "<p><mono>mono</mono></p>");

        test("<html>a<strong><em>b</em></strong>c</html>", "<p>a<strong><em>b</em></strong>c</p>");

        test("<html>a<em><em>b</em></em>c</html>", "<p>a<em>b</em>c</p>");
        test("<html>a<em><strong><em>b</em></strong></em>c</html>",
            "<p>a<em><strong>b</strong></em>c</p>");

        test(
            "<html><p>12<strong>34<span param='value'>56</span>78</strong>90</p></html>",
            "<p>12<strong>34</strong><strong><span class='wikimodel-parameters'[param='value']>56</span></strong>"
                + "<strong>78</strong>90</p>");

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
            "<p><strong><span class='wikimodel-parameters'[param1='value1', param2='value2']>word</span></strong>"
                + "<strong><span class='wikimodel-parameters'[param1='value1']>word2</span></strong></p>");

        test(
            "<html><p><span style='color: red;'><span style='background-color: yellow;'>word</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[style='color: red; background-color: yellow']>word</span></p>");

        test(
            "<html><p><span style='color: red;'>word1<span style='background-color: yellow;'>word2</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[style='color: red;']>word1</span>"
                + "<span class='wikimodel-parameters'[style='color: red; background-color: yellow']>word2</span></p>");

        test(
            "<html><p><span style='font-family: times new roman'>x<span style='font-family: arial black'>y</span>z</span></p></html>",
            "<p><span class='wikimodel-parameters'[style='font-family: times new roman']>x</span>"
                + "<span class='wikimodel-parameters'[style='font-family: times new roman; font-family: arial black']>y</span>"
                + "<span class='wikimodel-parameters'[style='font-family: times new roman']>z</span></p>");

        // class

        test(
            "<html><p><span class='class1'><span class='class2'>word</span></span></p></html>",
            "<p><span class='wikimodel-parameters'[class='class1 class2']>word</span></p>");

        test(
            "<html><p><span class='class1'>word1<span class='class2'>word2</span>word3</span></p></html>",
            "<p><span class='wikimodel-parameters'[class='class1']>word1</span>"
                + "<span class='wikimodel-parameters'[class='class1 class2']>word2</span>"
                + "<span class='wikimodel-parameters'[class='class1']>word3</span></p>");
    }

    @Test
    void testHeaders() throws WikiParserException
    {
        test("<html><h1>header1</h1></html>");
        test("<html><h2>header2</h2></html>");
        test("<html><h3>header3</h3></html>");
        test("<html><h4>header4</h4></html>");
        test("<html><h5>header5</h5></html>");
        test("<html><h6>header6</h6></html>");

        test("<html>before<h1>header1</h1>after</html>");
    }

    @Test
    void testHorLine() throws WikiParserException
    {
        test("<html>before<hr />after</html>", """
                <p>before</p>
                <hr />
                <p>after</p>""");
        test("<html><hr a='b' /></html>", "<hr a='b' />");
    }

    @Test
    void testImages() throws WikiParserException
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

    @Test
    void testLineBreak() throws WikiParserException
    {
        test("<html>before<br />after</html>", "<p>before\nafter</p>");
    }

    @Test
    void testLists() throws WikiParserException
    {
        test(
            "<html><ul><li>a<ul><li>b</li></ul></li><li>c</li></ul></html>",
            """
                <ul>
                  <li>a<ul>
                  <li>b</li>
                </ul>
                </li>
                  <li>c</li>
                </ul>""");

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
            + "6</li></ul></html>\r\n");

        test(
            "<html><ol><li>item one<ul><li>item two</li></ul></li></ol></html>",
            """
                <ol>
                  <li>item one<ul>
                  <li>item two</li>
                </ul>
                </li>
                </ol>""");
    }

    @Test
    void testParagraphs() throws WikiParserException
    {
        test("<html><p>paragraph</p></html>", "<p>paragraph</p>");
        test(
            "<p>hello <em class=\"italic\">beautiful</em> <strong>world</strong></p>",
            "<p>hello <em><span class='wikimodel-parameters'[class='italic']>beautiful</span></em> <strong>world</strong></p>");
    }

    @Test
    void testQuot() throws WikiParserException
    {
        test(
            "<html><blockquote>quote</blockquote></html>",
            """
                <blockquote>
                quote
                </blockquote>""");
        test(
            "<html><blockquote>line1<blockquote>line2<blockquote>line3</blockquote>"
                + "line4</blockquote></blockquote></html>",
            """
                <blockquote>
                line1<blockquote>
                line2<blockquote>
                line3
                </blockquote>

                line4
                </blockquote>

                </blockquote>""");

        test(
            "<html><blockquote><p>quote</p></blockquote></html>",
            """
                <blockquote>
                quote
                </blockquote>""");
        test(
            "<html><blockquote><p>line1</p><blockquote><p>line2</p><blockquote><p>line3</p></blockquote>"
                + "<p>line4</p></blockquote></blockquote></html>",
            """
                <blockquote>
                line1<blockquote>
                line2<blockquote>
                line3
                </blockquote>

                line4
                </blockquote>

                </blockquote>""");

        test(
            "<html><blockquote><span param='vale'>line1<br/>line2</span></blockquote></html>",
            """
                <blockquote>
                <span class='wikimodel-parameters'[param='vale']>line1</span>
                <span class='wikimodel-parameters'[param='vale']>line2</span>
                </blockquote>""");

        test(
            "<html><blockquote><span param1='value1'><span param2='vale2'>line1<br/>line2</span></span></blockquote></html>",
            """
                <blockquote>
                <span class='wikimodel-parameters'[param1='value1', param2='vale2']>line1</span>
                <span class='wikimodel-parameters'[param1='value1', param2='vale2']>line2</span>
                </blockquote>""");
    }

    @Test
    void testReferences() throws WikiParserException
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

    @Test
    void testTables() throws WikiParserException
    {
        test("<html><table><tr><td>first cell</td><td>second cell</td></tr></table></html>",
            """
                <table><tbody>
                  <tr><td>first cell</td><td>second cell</td></tr>
                </tbody></table>""");
        test("<html><table><tr><td>first cell</td></tr></table></html>",
            """
                <table><tbody>
                  <tr><td>first cell</td></tr>
                </tbody></table>""");
        test("<html><table>"
            + "<tr><th>first header</th><th>second header</th></tr>"
            + "<tr><td>first cell</td><td>second cell</td></tr>"
            + "</table></html>",
            """
                <table><tbody>
                  <tr><th>first header</th><th>second header</th></tr>
                  <tr><td>first cell</td><td>second cell</td></tr>
                </tbody></table>""");
        test("<html><table>"
            + "<tr><th>first row</th><td>first cell</td></tr>"
            + "<tr><th>second row</th><td>before <table><tr><td>first cell</td></tr></table> after</td></tr>"
            + "<tr><th>third row</th><td>third cell</td></tr>"
            + "</table></html>",
            """
                <table><tbody>
                  <tr><th>first row</th><td>first cell</td></tr>
                  <tr><th>second row</th><td>before<div class='wikimodel-document'>
                <table><tbody>
                  <tr><td>first cell</td></tr>
                </tbody></table>
                <p>after</p>
                </div>
                </td></tr>
                  <tr><th>third row</th><td>third cell</td></tr>
                </tbody></table>""");

        // Automatic sub document

        test("<html><table><tr><td><p>text</p></td></tr></table></html>",
            """
                <table><tbody>
                  <tr><td><div class='wikimodel-document'>
                <p>text</p>
                </div>
                </td></tr>
                </tbody></table>""");

        test("<html><table><tr><td><div class='wikimodel-emptyline'/><p>text</p></td></tr></table></html>",
            """
                <table><tbody>
                  <tr><td><div class='wikimodel-document'>
                <p>text</p>
                </div>
                </td></tr>
                </tbody></table>""");

        // "Bad-formed" tables...

        // The content is completely ignored.
        test("<html><table>first cell</table></html>", "<table><tbody>\n</tbody></table>");

        // A "td" element directly in the table
        test("<html><table><td>first cell</td></table></html>", """
                <table><tbody>
                  <tr><td>first cell</td></tr>
                </tbody></table>""");

        // Not a table at all
        test("<html><td>first cell</td></html>", """
                <table><tbody>
                  <tr><td>first cell</td></tr>
                </tbody></table>""");

        test("<table><tr><td><tt class=\"wikimodel-verbatim\">x</tt></td><td><tt>y</tt></td></tr></table>",
            """
                <table><tbody>
                  <tr><td><tt class="wikimodel-verbatim">x</tt></td><td><mono>y</mono></td></tr>
                </tbody></table>""");
    }

    @Test
    void testVerbatimBlocks() throws WikiParserException
    {
        test("<html><pre>one\ntwo</pre></html>", "<pre>one\ntwo</pre>");
        test("<html><pre>one<br/>two</pre></html>", "<pre>one\ntwo</pre>");
        test("<html><pre><b>one</b><br/>two</pre></html>", "<pre>one\ntwo</pre>");
    }

    @Test
    void testVerbatimInline() throws WikiParserException
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

    @Test
    void testMacro() throws WikiParserException
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

    @Test
    void testMisc() throws WikiParserException
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
     */
    @Test
    void testUnsupportedTags() throws WikiParserException
    {
        test("<html>This is <unsupported>tags</unsupported> ignored</html>",
            "<p>This is tags ignored</p>");

        test("<html><dl><dt>term</dt><dd>definition<unsupported><dl><dt>term</dt><dd>definition</dd></dl></unsupported></dd></dl></html>",
            """
                <dl>
                  <dt>term</dt>
                  <dd>definition<dl>
                  <dt>term</dt>
                  <dd>definition</dd>
                </dl>
                </dd>
                </dl>""");

        test(
            "<html><ul><li>a<unsupported><ul><li>b</li></ul></unsupported></li></ul></html>",
            """
                <ul>
                  <li>a<ul>
                  <li>b</li>
                </ul>
                </li>
                </ul>""");

        test(
            "<html><blockquote>line1<unsupported><blockquote>line2<blockquote>line3</blockquote>"
                + "line4</blockquote></unsupported></blockquote></html>",
            """
                <blockquote>
                line1<blockquote>
                line2<blockquote>
                line3
                </blockquote>

                line4
                </blockquote>

                </blockquote>""");
    }

    /**
     * Test malformed constructs created by the HTML cleaner not supporting HTML5
     */
    @Test
    void testUnsupportedHTML5Tags() throws WikiParserException
    {
        test(
            "<html><p><section><p>test</p></section></p></html>",
            """
                <p></p>
                <div class='wikimodel-document'>
                <p>test</p>
                </div>""");
        test(
            "<html><p><header><h1>test</h1></header></p></html>",
            """
                <p></p>
                <div class='wikimodel-document'>
                <h1>test</h1>
                </div>""");

        test(
            "<html><p><header><h1>test</h1></header></p></html>",
            """
                <p></p>
                <div class='wikimodel-document'>
                <h1>test</h1>
                </div>""");
    }

    /**
     * Test basic HTML5 support.
     * While invalid in XHTML 1.0, if these tags ever appear in the submitted content, it could cause some text nodes
     * to be merged, which is usually not nice from an end user POV. Therefore Wikimodel provide some minimal support
     * for them, by considering those tags as simple divs.
     */
    @Test
    void testBasicHTML5Tags() throws WikiParserException
    {
        test(
            "<html><header>Header<nav>Navigation</nav></header><main><article><section><h1>section 1</h1>"
                + "<summary>Summary 1</summary><details>Text 1<figure><img src='target'/>"
                + "<figcaption>caption</figcaption></figure></details><aside>Aside</aside></section>"
                + "<section><h1>section 2</h1><p>text 2</p></section></article></main><footer>Footer</footer></html>",
            """
                <div class='wikimodel-document'>
                <p>Header</p>
                <div class='wikimodel-document'>
                <p>Navigation</p>
                </div>
                </div>
                <div class='wikimodel-document'>
                <div class='wikimodel-document'>
                <div class='wikimodel-document'>
                <h1>section 1</h1>
                <div class='wikimodel-document'>
                <p>Summary 1</p>
                </div>
                <div class='wikimodel-document'>
                <p>Text 1</p>
                <div class='wikimodel-document'>
                <p><img src='target'/></p>
                <div class='wikimodel-document'>
                <p>caption</p>
                </div>
                </div>
                </div>
                <div class='wikimodel-document'>
                <p>Aside</p>
                </div>
                </div>
                <div class='wikimodel-document'>
                <h1>section 2</h1>
                <p>text 2</p>
                </div>
                </div>
                </div>
                <div class='wikimodel-document'>
                <p>Footer</p>
                </div>""");
    }
}
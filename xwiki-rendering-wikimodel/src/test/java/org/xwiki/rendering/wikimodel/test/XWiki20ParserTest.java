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
import org.xwiki.rendering.wikimodel.xhtml.PrintListener;
import org.xwiki.rendering.wikimodel.xwiki.xwiki20.XWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XWiki20ParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public XWiki20ParserTest(String name)
    {
        super(name);
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

    @Override
    protected IWikiParser newWikiParser()
    {
        return new XWikiParser();
    }

    public void test() throws Exception
    {
        test("before **bold** after", "<p>before <strong>bold</strong> after</p>");

        doCustomTest("before **bold** after", "<p>{before}[ ]<strong>{bold}</strong>[ ]{after}</p>");
        doCustomTest("before \n* bold after", "<p>{before}[ ]</p>\n" + "<ul>\n" + "  <li>{bold}[ ]{after}</li>\n"
            + "</ul>" + "");
    }

    /**
     * @throws WikiParserException
     */
    public void testDefinitionLists() throws WikiParserException
    {
        test("; term: definition", "<dl>\n  <dt>term: definition</dt>\n</dl>");
        test(";: just definition", "<dl>\n  <dd>just definition</dd>\n</dl>");
        test("; just term", "<dl>\n  <dt>just term</dt>\n</dl>");
        test(";: ", "<dl>\n  <dd></dd>\n</dl>");

        test(";not: definition", "<p>;not: definition</p>");
        test("; this:is_not_a_term : it is an uri", "<dl>\n" + "  <dt>this:is_not_a_term : it is an uri</dt>\n"
            + "</dl>");

        test("; term one\n: definition one\n" + "; term two\n: definition two\n" + "; term three\n: definition three",
            "<dl>\n" + "  <dt>term one</dt>\n" + "  <dd>definition one</dd>\n" + "  <dt>term two</dt>\n"
                + "  <dd>definition two</dd>\n" + "  <dt>term three</dt>\n" + "  <dd>definition three</dd>\n"
                + "</dl>");

        test("; One,\ntwo,\nbucle my shoes...\n: " + "...Three\nfour,\nClose the door\n"
            + "; Five,\nSix\n: Pick up\n sticks\n\ntam-tam, pam-pam...", "<dl>\n" + "  <dt>One,\n" + "two,\n"
            + "bucle my shoes...</dt>\n" + "  <dd>...Three\n" + "four,\n" + "Close the door</dd>\n"
            + "  <dt>Five,\n" + "Six</dt>\n" + "  <dd>Pick up\n" + " sticks</dd>\n" + "</dl>\n"
            + "<p>tam-tam, pam-pam...</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testDocuments() throws WikiParserException
    {
        test("before ((( inside ))) after ", "<p>before</p>\n" + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n" + "</div>\n" + "<p>after </p>");
        test("before\n((( inside ))) after ", "<p>before</p>\n" + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n" + "</div>\n" + "<p>after </p>");
        test("before inside ))) after ", "<p>before inside ))) after </p>");
        test("before (((\ninside ))) after ", "<p>before</p>\n" + "<div class='wikimodel-document'>\n"
            + "<p>inside</p>\n" + "</div>\n" + "<p>after </p>");
        test("| Line One | First doc: (((\n inside ))) after \n" + "|Line Two | Second doc: (((lkjlj))) skdjg",
            "<table><tbody>\n" + "  <tr><td> Line One </td><td> First doc:<div class='wikimodel-document'>\n"
                + "<p> inside</p>\n" + "</div>\n" + "after </td></tr>\n"
                + "  <tr><td>Line Two </td><td> Second doc:<div class='wikimodel-document'>\n" + "<p>lkjlj</p>\n"
                + "</div>\n" + "skdjg</td></tr>\n" + "</tbody></table>");
        test("| This is a table: | (((* item one\n" + "* item two\n" + " * subitem 1\n" + " * subitem 2\n"
            + "* item three))) ", "<table><tbody>\n"
            + "  <tr><td> This is a table: </td><td><div class='wikimodel-document'>\n" + "<ul>\n"
            + "  <li>item one</li>\n" + "  <li>item two</li>\n" + "  <li>subitem 1</li>\n"
            + "  <li>subitem 2</li>\n" + "  <li>item three</li>\n" + "</ul>\n" + "</div>\n" + "</td></tr>\n"
            + "</tbody></table>");

        test("before ((( opened and not closed", "<p>before</p>\n" + "<div class='wikimodel-document'>\n"
            + "<p>opened and not closed</p>\n" + "</div>");
        test("before ((( one ((( two ((( three ", "<p>before</p>\n" + "<div class='wikimodel-document'>\n"
            + "<p>one</p>\n" + "<div class='wikimodel-document'>\n" + "<p>two</p>\n"
            + "<div class='wikimodel-document'>\n" + "<p>three </p>\n" + "</div>\n" + "</div>\n" + "</div>");
        test("before\n(% param=\"value\" %)((( inside ))) after ", "<p>before</p>\n"
            + "<div class='wikimodel-document' param='value'>\n" + "<p>inside</p>\n" + "</div>\n" + "<p>after </p>");
        test("before\n|(% param=\"value\" %)((( inside ))) after ", "<p>before</p>\n" + "<table><tbody>\n"
            + "  <tr><td param='value'><div class='wikimodel-document'>\n" + "<p>inside</p>\n" + "</div>\n"
            + "after </td></tr>\n" + "</tbody></table>");
        test("before\n|(% param=\"value\" %)(% docparam=\"docvalue\" %)((( inside ))) after ", "<p>before</p>\n"
            + "<table><tbody>\n" + "  <tr><td param='value'><div class='wikimodel-document' docparam='docvalue'>\n"
            + "<p>inside</p>\n" + "</div>\n" + "after </td></tr>\n" + "</tbody></table>");

        test("(% param=\"value\" %)\n ((( inside ))) after ", "<div class='wikimodel-document' param='value'>\n"
            + "<p>inside</p>\n" + "</div>\n" + "<p>after </p>");
        test("(% param=value %)(((inside)))", "<div class='wikimodel-document' param='value'>\n" + "<p>inside</p>\n"
            + "</div>");

        test("((( {{macro}} hello world! {{/macro}} )))", "<div class='wikimodel-document'>\n"
            + "<pre class='wikimodel-macro' macroName='macro'><![CDATA[ hello world! ]]></pre>\n" + "</div>");
        test("((( {{{ hello world! }}} )))", "<div class='wikimodel-document'>\n" + "<pre> hello world! </pre>\n"
            + "</div>");
        test("((( {{macro/}} )))", "<div class='wikimodel-document'>\n"
            + "<pre class='wikimodel-macro' macroName='macro'/>\n" + "</div>");

        test("|(% param=\"value\" %)(% docparam=\"docvalue\" %)(((  )))", "<table><tbody>\n"
            + "  <tr><td param='value'><div class='wikimodel-document' docparam='docvalue'>\n" + "</div>\n"
            + "</td></tr>\n" + "</tbody></table>");
        test("| |(% param=\"value\" %)(% docparam=\"docvalue\" %)(((  )))", "<table><tbody>\n"
            + "  <tr><td> </td><td param='value'><div class='wikimodel-document' docparam='docvalue'>\n"
            + "</div>\n" + "</td></tr>\n" + "</tbody></table>");
        test("((({{macro/}}\n)))", "<div class='wikimodel-document'>\n"
            + "<pre class='wikimodel-macro' macroName='macro'/>\n" + "</div>");
        test("((({{macro/}})))", "<div class='wikimodel-document'>\n"
            + "<pre class='wikimodel-macro' macroName='macro'/>\n" + "</div>");
        test("((({{{verbatim}}}\n)))", "<div class='wikimodel-document'>\n" + "<pre>verbatim</pre>\n" + "</div>");
        test("((({{{verbatim}}})))", "<div class='wikimodel-document'>\n<pre>verbatim</pre>\n</div>");
    }

    /**
     * @throws WikiParserException
     */
    public void testEscape() throws WikiParserException
    {
        test("~First letter is escaped", "<p>First letter is escaped</p>");
        test("~ A space just after a tilda the tilda should desapear",
            "<p> A space just after a tilda the tilda should desapear</p>");
        test("~~escaped tilda", "<p>~escaped tilda</p>");
        test("tilda at the end~", "<p>tilda at the end</p>");
    }

    public void testFormat() throws Exception
    {
        // test("**bold**", "<p><strong>bold</strong></p>");
        test("before **bold** after", "<p>before <strong>bold</strong> after</p>");
        test("before //italic// after", "<p>before <em>italic</em> after</p>");
        test("before --strike-- after", "<p>before <strike>strike</strike> after</p>");
        test("before __underline__ after", "<p>before <ins>underline</ins> after</p>");
        test("before ^^sup^^ after", "<p>before <sup>sup</sup> after</p>");
        test("before ,,sub,, after", "<p>before <sub>sub</sub> after</p>");
        test("before ##mono## after", "<p>before <mono>mono</mono> after</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("(% param1='value1' param2='value2' %)");
        test("xxx (% param1='value1' param2='value2' %) xxx ");
        // (% param3="value3" %)hello(%%) world
        //
        // (% param3="valueA" %)hello (% param3="valueB" %)world

        test("**bold**", "<p><strong>bold</strong></p>");
        test("//italic//", "<p><em>italic</em></p>");
        test("--strike--", "<p><strike>strike</strike></p>");
        test("^^sup^^", "<p><sup>sup</sup></p>");
        test(",,sub,,", "<p><sub>sub</sub></p>");
        test("##mono##", "<p><mono>mono</mono></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHeaders() throws WikiParserException
    {
        test("= Heading 1", "<h1>Heading 1</h1>");
        test("== Heading 2", "<h2>Heading 2</h2>");
        test("=== Heading 3", "<h3>Heading 3</h3>");
        test("==== Heading 4", "<h4>Heading 4</h4>");
        test("===== Heading 5", "<h5>Heading 5</h5>");
        test("====== Heading 6", "<h6>Heading 6</h6>");
        test("= Heading 1 =some", "<h1>Heading 1</h1>\n<p>some</p>");
        test("= Heading\n1 =", "<h1>Heading\n1</h1>");
        test("= Heading 1 =\nsome text", "<h1>Heading 1</h1>\n<p>some text</p>");
        test("= Heading 1 = \nsome text", "<h1>Heading 1</h1>\n<p> \nsome text</p>");
        test("= Heading 1 =\n\n\n= Heading 1 =", "<h1>Heading 1</h1>\n<h1>Heading 1</h1>");
        test("= Heading 1 {{macro}}{{/macro}}=",
            "<h1>Heading 1 <span class='wikimodel-macro' macroName='macro'><![CDATA[]]></span></h1>");
        test("= Title level 1\n== Title level 2", "<h1>Title level 1</h1>\n<h2>Title level 2</h2>");
        test("= Title level 1\n\n== Title level 2", "<h1>Title level 1</h1>\n<h2>Title level 2</h2>");
        test("=== Heading 3===\ntext1\n\ntext2", "<h3>Heading 3</h3>\n<p>text1</p>\n<p>text2</p>");
        test("= Heading 1 =\n= Heading 2 =", "<h1>Heading 1</h1>\n<h1>Heading 2</h1>");
        test("= Heading 1\n\n= Heading 2 =", "<h1>Heading 1</h1>\n<h1>Heading 2</h1>");
        test("= Heading 1 ={{macro/}}", "<h1>Heading 1</h1>\n<pre class='wikimodel-macro' macroName='macro'/>");
        test("= Heading 1 =\n{{macro/}}", "<h1>Heading 1</h1>\n<pre class='wikimodel-macro' macroName='macro'/>");
        test("=\n\nnot header", "<h1></h1>\n<p>not header</p>");

        test("= Header = \nParagraph\n\n", "<h1>Header</h1>\n<p> \nParagraph</p>");

        test("paragraph\n\n= header =", "<p>paragraph</p>\n<h1>header</h1>");

        test("= {{macro/}}text =", "<h1><span class='wikimodel-macro' macroName='macro'/>text</h1>");
        test("= {{macro/}} text =", "<h1><span class='wikimodel-macro' macroName='macro'/> text</h1>");
        test("= {{macro/}}\ntext =", "<h1><span class='wikimodel-macro' macroName='macro'/>\ntext</h1>");

        test("= header\n* list\n \n\n", "<h1>header</h1>\n<ul>\n  <li>list\n </li>\n</ul>");
        test("= header\n>quote\n \n\n", "<h1>header</h1>\n<blockquote>\nquote\n</blockquote>\n<p> </p>");
        test("= header\n; term: definition\n \n\n", "<h1>header</h1>\n<dl>\n  <dt>term: definition\n </dt>\n</dl>");

        test("(% param='vale' %)\n= header =", "<h1 param='vale'>header</h1>");
    }

    public void testImages() throws WikiParserException
    {
        test("image:reference", "<p><img src='reference' class='wikimodel-freestanding'/></p>");
        test("image:reference ", "<p><img src='reference' class='wikimodel-freestanding'/> </p>");
        test("image:reference~ ", "<p><img src='reference~ ' class='wikimodel-freestanding'/></p>");
        test("image:wiki:space.page", "<p><img src='wiki:space.page' class='wikimodel-freestanding'/></p>");
        test("image:wiki:space.page@file.ext",
            "<p><img src='wiki:space.page@file.ext' class='wikimodel-freestanding'/></p>");
        test("[[image:reference]]", "<p><img src='reference'/></p>");
    }

    public void testAttach() throws WikiParserException
    {
        test("attach:reference",
            "<p><a href='attach:reference' class='wikimodel-freestanding'>attach:reference</a></p>");
        test("attach:wiki:space.page",
            "<p><a href='attach:wiki:space.page' class='wikimodel-freestanding'>attach:wiki:space.page</a></p>");
        test("attach:wiki:space.page@file.ext",
            "<p><a href='attach:wiki:space.page@file.ext' class='wikimodel-freestanding'>attach:wiki:space.page@file.ext</a></p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testHorLine() throws WikiParserException
    {
        test("----");
        test("-------");
        test("-----------");
        test(" -----------");
        test("----abc");
        test("(%a=b%)\n----", "<hr a='b' />");
    }

    /**
     * @throws WikiParserException
     */
    public void testLineBreak() throws WikiParserException
    {
        test("abc\\\\def");
    }

    /**
     * @throws WikiParserException
     */
    public void testLists() throws WikiParserException
    {
        test("* first", "<ul>\n" + "  <li>first</li>\n" + "</ul>");
        test("** second", "<ul>\n" + "  <li><ul>\n" + "  <li>second</li>\n" + "</ul>\n" + "</li>\n" + "</ul>");
        test("  ** second", "<ul>\n" + "  <li><ul>\n" + "  <li>second</li>\n" + "</ul>\n" + "</li>\n" + "</ul>");
        test("* first\n** second", "<ul>\n" + "  <li>first<ul>\n" + "  <li>second</li>\n" + "</ul>\n" + "</li>\n"
            + "</ul>");
        test("*1. second", "<ul>\n" + "  <li><ol>\n" + "  <li>second</li>\n" + "</ol>\n" + "</li>\n" + "</ul>");
        test("  11. second", "<ol>\n" + "  <li><ol>\n" + "  <li>second</li>\n" + "</ol>\n" + "</li>\n" + "</ol>");

        test("* item one\n" + "* item two\n" + "*1. item three\n" + "*1. item four\n" + "* item five - first line\n"
            + "   item five - second line\n" + "* item six\n" + "  is on multiple\n" + " lines", "<ul>\n"
            + "  <li>item one</li>\n" + "  <li>item two<ol>\n" + "  <li>item three</li>\n"
            + "  <li>item four</li>\n" + "</ol>\n" + "</li>\n" + "  <li>item five - first line\n"
            + "   item five - second line</li>\n" + "  <li>item six\n" + "  is on multiple\n" + " lines</li>\n"
            + "</ul>");

        test("* item one", "<ul>\n  <li>item one</li>\n</ul>");
        test("*item one", "<p>*item one</p>");
        test("(%param=\"value\"%)\n* item one\n" + "** item two", "<ul param='value'>\n" + "  <li>item one<ul>\n"
            + "  <li>item two</li>\n" + "</ul>\n" + "</li>\n" + "</ul>");
        test("(%param=\"value\"%)\n* item one\n" + "(%param2=\"value2\"%)\n** item two", "<ul param='value'>\n"
            + "  <li>item one<ul param2='value2'>\n" + "  <li>item two</li>\n" + "</ul>\n" + "</li>\n" + "</ul>");
        test("(%param=\"value\"%)\n* item one\n" + "(%param2=\"value2\"%)\n** item two"
            + "(%param3=\"value3\"%)\n** item three", "<ul param='value'>\n"
            + "  <li>item one<ul param2='value2'>\n" + "  <li>item two</li>\n" + "  <li>item three</li>\n"
            + "</ul>\n" + "</li>\n" + "</ul>");

        test("* \n((( group )))", "<ul>\n" + "  <li>\n" + "<div class='wikimodel-document'>\n" + "<p>group</p>\n"
            + "</div>\n" + "</li>\n" + "</ul>");

        test("* \n((( group ))) not group\n* item", "<ul>\n" + "  <li>\n" + "<div class='wikimodel-document'>\n"
            + "<p>group</p>\n" + "</div>\n" + "not group</li>\n" + "  <li>item</li>\n" + "</ul>");
        test("* \n((( group )))\n* item", "<ul>\n" + "  <li>\n" + "<div class='wikimodel-document'>\n"
            + "<p>group</p>\n" + "</div>\n" + "</li>\n" + "  <li>item</li>\n" + "</ul>");
    }

    public void testMacro() throws WikiParserException
    {

        test("{{macro/}}{{macro/}}",
            "<p><span class='wikimodel-macro' macroName='macro'/><span class='wikimodel-macro' macroName='macro'/></p>");

        test("{{macro/}}\n{{macro/}}", "<p><span class='wikimodel-macro' macroName='macro'/>\n"
            + "<span class='wikimodel-macro' macroName='macro'/></p>");

        test("{{macro/}}\ntext", "<p><span class='wikimodel-macro' macroName='macro'/>\n" + "text</p>");

        test("{{macro/}}\n", "<pre class='wikimodel-macro' macroName='macro'/>");

        test("{{macro/}}\n\n{{macro/}}",
            "<pre class='wikimodel-macro' macroName='macro'/>\n<pre class='wikimodel-macro' macroName='macro'/>");

        test("{{macro/}}\n\n{{macro/}}\n\n{{macro/}}", "<pre class='wikimodel-macro' macroName='macro'/>\n"
            + "<pre class='wikimodel-macro' macroName='macro'/>\n"
            + "<pre class='wikimodel-macro' macroName='macro'/>");

        test("{{macro/}}\n\n{{macro/}}\n\n{{macro/}}\n\n{{macro/}}",
            "<pre class='wikimodel-macro' macroName='macro'/>\n" + "<pre class='wikimodel-macro' macroName='macro'/>\n"
                + "<pre class='wikimodel-macro' macroName='macro'/>\n"
                + "<pre class='wikimodel-macro' macroName='macro'/>");

        test("some text {{macro/}}\n\n{{macro/}}",
            "<p>some text <span class='wikimodel-macro' macroName='macro'/></p>\n<pre class='wikimodel-macro' macroName='macro'/>");

        test(
            "{{toto1}}a{{/toto1}}{{toto2/}}",
            "<p><span class='wikimodel-macro' macroName='toto1'><![CDATA[a]]></span><span class='wikimodel-macro' macroName='toto2'/></p>");

        test(
            "{{toto}}a{{/toto}}{{toto}}{{/toto}}",
            "<p><span class='wikimodel-macro' macroName='toto'><![CDATA[a]]></span><span class='wikimodel-macro' macroName='toto'><![CDATA[]]></span></p>");

        test("{{toto}}a{{/toto}}", "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>");

        test("{{x:toto y:param=value1 z:param2='value two'}}a{{/x:toto}}",
            "<pre class='wikimodel-macro' macroName='x:toto' y:param='value1' z:param2='value two'><![CDATA[a]]></pre>");

        test("{{toto}}a{{toto}}b{{/toto}}c{{/toto}}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a{{toto}}b{{/toto}}c]]></pre>");

        test("{{macro}}{{macro1}}{{/macro1}}{{/macro}}",
            "<pre class='wikimodel-macro' macroName='macro'><![CDATA[{{macro1}}{{/macro1}}]]></pre>");

        test("{{toto}}a{{tata}}b{{/tata}}c{{/toto}}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a{{tata}}b{{/tata}}c]]></pre>");

        test("before\n{{toto}}a{{/toto}}\nafter",
            "<p>before\n<span class='wikimodel-macro' macroName='toto'><![CDATA[a]]></span>\nafter</p>");

        test("before\n{{toto}}a{{/toto}}after",
            "<p>before\n<span class='wikimodel-macro' macroName='toto'><![CDATA[a]]></span>after</p>");

        // URIs as macro names
        test("{{x:toto}}a{{/x:toto}}", "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>");
        test("{{x:toto}}a{{x:toto}}b{{/x:toto}}c{{/x:toto}}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a{{x:toto}}b{{/x:toto}}c]]></pre>");
        test("{{x:toto}}a{{tata}}b{{/tata}}c{{/x:toto}}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a{{tata}}b{{/tata}}c]]></pre>");

        test("before\n{{x:toto}}a{{/x:toto}}\nafter",
            "<p>before\n<span class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></span>\nafter</p>");

        test("before\n{{x:toto}}a{{/x:toto}}after", "<p>before\n"
            + "<span class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></span>after</p>");

        // Empty macros
        test("{{x:toto /}}", "<pre class='wikimodel-macro' macroName='x:toto'/>");

        test("{{x:toto a=b c=d /}}", "<pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'/>");

        test("before\n{{x:toto  a=b c=d/}}\nafter",
            "<p>before\n<span class='wikimodel-macro' macroName='x:toto' a='b' c='d'/>\nafter</p>");

        test("before\n{{x:toto  a='b' c='d'/}}after", "<p>before\n"
            + "<span class='wikimodel-macro' macroName='x:toto' a='b' c='d'/>after</p>");

        test("before{{x:toto /}}after", "<p>before<span class='wikimodel-macro' macroName='x:toto'/>after</p>");

        test("**{{macro/}}** text", "<p><strong><span class='wikimodel-macro' macroName='macro'/></strong> text</p>");

        // Bad-formed block macros (not-closed)
        test("{{toto}}", "<pre class='wikimodel-macro' macroName='toto'><![CDATA[]]></pre>");
        test("{{toto}}a{{toto}}", "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a{{toto}}]]></pre>");
        test("{{/x}}", "<p>{{/x}}</p>");
        test("before{{a}}x{{b}}y{{c}}z\n" + "new line in the same  macro",

            "<p>before<span class='wikimodel-macro' macroName='a'><![CDATA[x{{b}}y{{c}}z\n"
                + "new line in the same  macro]]></span></p>");

        test("before{{a}}x{{b}}y{{c}}z{{/a}}after",
            "<p>before<span class='wikimodel-macro' macroName='a'><![CDATA[x{{b}}y{{c}}z]]></span>after</p>");

        // 
        test("{{toto}}a{{/toto}}", "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>");
        test("before{{toto}}macro{{/toto}}after",
            "<p>before<span class='wikimodel-macro' macroName='toto'><![CDATA[macro]]></span>after</p>");

        test("before{{toto a=b c=d}}toto macro tata {{/toto}}after",
            "<p>before<span class='wikimodel-macro' macroName='toto' a='b' c='d'>" + "<![CDATA[toto macro tata ]]>"
                + "</span>after</p>");

        test("before{{toto a=b c=d}}toto {{x qsdk}} macro {{sd}} tata {{/toto}}after",
            "<p>before<span class='wikimodel-macro' macroName='toto' a='b' c='d'>"
                + "<![CDATA[toto {{x qsdk}} macro {{sd}} tata ]]>" + "</span>after</p>");

        // New lines formating
        test("{{macro}}\n{{/macro}}", "<pre class='wikimodel-macro' macroName='macro'><![CDATA[]]></pre>");
        test("{{macro}}\r\n{{/macro}}", "<pre class='wikimodel-macro' macroName='macro'><![CDATA[]]></pre>");
        test("{{macro}}\n\n{{/macro}}", "<pre class='wikimodel-macro' macroName='macro'><![CDATA[]]></pre>");
        test("{{macro}}\r\n\r\n{{/macro}}", "<pre class='wikimodel-macro' macroName='macro'><![CDATA[]]></pre>");
        test("{{macro}}\ncontent\n{{/macro}}",
            "<pre class='wikimodel-macro' macroName='macro'><![CDATA[content]]></pre>");
        test("text {{macro}}\n{{/macro}} text",
            "<p>text <span class='wikimodel-macro' macroName='macro'><![CDATA[]]></span> text</p>");
        test("text {{macro}}\n\n{{/macro}} text",
            "<p>text <span class='wikimodel-macro' macroName='macro'><![CDATA[]]></span> text</p>");
        test("text {{macro}}\ncontent\n{{/macro}} text",
            "<p>text <span class='wikimodel-macro' macroName='macro'><![CDATA[content]]></span> text</p>");

        // Not a macro
        test("{{ toto a=b c=d}}", "<p>{{ toto a=b c=d}}</p>");

        test("This is a macro: {{toto x:a=b x:c=d}}\n" + "<table>\n" + "#foreach ($x in $table)\n"
            + "  <tr>hello, $x</tr>\n" + "#end\n" + "</table>\n" + "{{/toto}}",

            "<p>This is a macro: <span class='wikimodel-macro' macroName='toto' x:a='b' x:c='d'><![CDATA[" + "<table>\n"
                + "#foreach ($x in $table)\n" + "  <tr>hello, $x</tr>\n" + "#end\n" + "</table>" + "]]></span></p>");

        test("* item one\n" + "* item two\n" + "  {{code}} this is a code{{/code}} \n"
            + "  the same item (continuation)\n" + "* item three",

            "<ul>\n" + "  <li>item one</li>\n" + "  <li>item two\n"
                + "  <span class='wikimodel-macro' macroName='code'><![CDATA[ this is a code]]></span> \n"
                + "  the same item (continuation)</li>\n" + "  <li>item three</li>\n" + "</ul>");

        // Macros with URIs as names
        test("{{x:y a=b c=d}}", "<pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[]]></pre>");
        test("before{{x:y a=b c=d}}macro content",
            "<p>before<span class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[macro content]]></span></p>");
        test("before\n{{x:y a=b c=d}}macro content", "<p>before\n"
            + "<span class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[macro content]]></span></p>");

        test("before\n{{x:y a=b c=d/}}\nafter",
            "<p>before\n<span class='wikimodel-macro' macroName='x:y' a='b' c='d'/>\nafter</p>");

        // Not closed and bad-formed macros
        test("a{{a}}{{b}}", "<p>a<span class='wikimodel-macro' macroName='a'><![CDATA[{{b}}]]></span></p>");
        test("a{{a}}{{b}}{", "<p>a<span class='wikimodel-macro' macroName='a'><![CDATA[{{b}}{]]></span></p>");

        // escaping
        test("{{toto param1=\"val~\"ue1\" param2=\"v~~al~}}ue2\"}}a{{/toto}}",
            "<pre class='wikimodel-macro' macroName='toto' param1='val&#x22;ue1' param2='v~al}}ue2'><![CDATA[a]]></pre>");

        // parameters on different lines
        test("{{macro\n   param1=\"val1\"\n   param2=\"val2\"\n/}}",
             "<pre class='wikimodel-macro' macroName='macro' param1='val1' param2='val2'/>");
        test("{{macro\n   param1=\"val1\"\n   param2=\"val2\"\n}}foo{{/macro}}",
             "<pre class='wikimodel-macro' macroName='macro' param1='val1' param2='val2'><![CDATA[foo]]></pre>");
    }

    public void testMacroParameterEscaping() throws WikiParserException
    {
        test("{{macro a={{b /}}  /}}", "<p><span class='wikimodel-macro' macroName='macro' a='{{b'/>  /}}</p>");

        test("{{macro a=\"{{b /}}\" /}}", "<pre class='wikimodel-macro' macroName='macro' a='{{b /}}'/>");

        test("{{macro a='{{b /}}' /}}", "<pre class='wikimodel-macro' macroName='macro' a='{{b /}}'/>");

        test("{{macro \"{{b /}}\"=a /}}", "<pre class='wikimodel-macro' macroName='macro'/>");

        test("{{macro '{{b /}}'=a /}}", "<pre class='wikimodel-macro' macroName='macro'/>");

        test("{{macro a={{b/~}} /}}", "<pre class='wikimodel-macro' macroName='macro' a='{{b/}}'/>");

        test("{{macro a={{b/}~} /}}", "<pre class='wikimodel-macro' macroName='macro' a='{{b/}}'/>");

        test("{{macro {{b/~}}=a /}}", "<pre class='wikimodel-macro' macroName='macro'/>");

        test("{{macro a=\"{{b /~}}\" /}}", "<pre class='wikimodel-macro' macroName='macro' a='{{b /}}'/>");

        test("{{macro a='{{b /~}}' /}}", "<pre class='wikimodel-macro' macroName='macro' a='{{b /}}'/>");

        test("{{macro a='~'' /}}", "<pre class='wikimodel-macro' macroName='macro' a='&#x27;'/>");

        test("{{macro a=\"~\"\" /}}", "<pre class='wikimodel-macro' macroName='macro' a='&#x22;'/>");

        test("{{macro a=\"foo\" b=\"bar\" }}content{{/macro}}\n\n{{macro a=\"foo\" b=\"bar\" }}content{{/macro}}",
             "<pre class='wikimodel-macro' macroName='macro' a='foo' b='bar'><![CDATA[content]]></pre>\n" 
             + "<pre class='wikimodel-macro' macroName='macro' a='foo' b='bar'><![CDATA[content]]></pre>" );

        test("{{box title =  \"1~\"2|-|3=~~~\"4~~\" }}=~\"|-|~~{{/box}}", "<pre class='wikimodel-macro' macroName='box' title='1&#x22;2|-|3=~&#x22;4~'><![CDATA[=~\"|-|~~]]></pre>");
    }

    /**
     * @throws WikiParserException
     */
    public void testParagraphs() throws WikiParserException
    {
        test("First paragraph.\n" + "Second line of the same paragraph.\n" + "\n" + "The second paragraph");
        test("(% a='b' %)\nparagraph1\n\nparagraph2", "<p a='b'>paragraph1</p>\n<p>paragraph2</p>");

        test("(% param='value' %)\n{{macro}}content{{/macro}}inline",
            "<p param='value'><span class='wikimodel-macro' macroName='macro'><![CDATA[content]]></span>inline</p>");

        test("(% param='value' %)\n", "<p param='value'></p>");

        test("(% param='value' %)\n" + "{{macro}}content{{/macro}}\n\n" + "after",
            "<p param='value'><span class='wikimodel-macro' macroName='macro'><![CDATA[content]]></span></p>\n"
                + "<p>after</p>");

        test("(% param='value' %)\n\n", "<p param='value'></p>");

        test("(% param='value' %)\n\na", "<p param='value'></p>\n<p>a</p>");

        test("(% param='value' %)\n\n\n", "<p param='value'></p>\n<div style='height:2em;'></div>");

        test("(% param='value' %)\ntext\n(% param2='value2' %)text",
            "<p param='value'>text\n<span class='wikimodel-parameters'[param2='value2']>text</span></p>");

        // TODO: this is a bug but too old to be fixed now, should be fixed for 2.1
        test("toto\n(% param='value' %)\ntiti", "<p>toto</p>\n<p param='value'>titi</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testQuot() throws WikiParserException
    {
        test(">line", "<blockquote>\nline\n</blockquote>");
        test(">line1\n>line2", "<blockquote>\nline1\nline2\n</blockquote>");
        test("This is a paragraph" + "\n" + "\n" + ">and this is a quotations\n" + ">the second line",
            "<p>This is a paragraph</p>\n" + "<blockquote>\n" + "and this is a quotations\n" + "the second line\n"
                + "</blockquote>");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
        test("before [[xx[[image:img.gif]]yy]] after",
            "<p>before <a href='xx[[image:img.gif]]yy' class='wikimodel-freestanding'>xx[[image:img.gif]]yy</a> after</p>");
        test("before [[xx[[image:img.gif bqdf]]yy]] after",
            "<p>before <a href='xx[[image:img.gif bqdf]]yy' class='wikimodel-freestanding'>xx[[image:img.gif bqdf]]yy</a> after</p>");
        test("before http://www.foo.bar/com after",
            "<p>before <a href='http://www.foo.bar/com' class='wikimodel-freestanding'>http://www.foo.bar/com</a> after</p>");
        test("before [[toto]] after", "<p>before <a href='toto' class='wikimodel-freestanding'>toto</a> after</p>");
        test("before [[ [toto] [tata] ]] after",
            "<p>before <a href=' [toto] [tata] ' class='wikimodel-freestanding'> [toto] [tata] </a> after</p>");
        test("before wiki:Hello after", "<p>before wiki:Hello after</p>");
        test("before mailto:Hello after",
            "<p>before <a href='mailto:Hello' class='wikimodel-freestanding'>mailto:Hello</a> after</p>");
        test("before wiki~:Hello after", "<p>before wiki:Hello after</p>");

        // Not a reference
        test("before [toto] after", "<p>before [toto] after</p>");
        test("not [[a reference] at all!", "<p>not [[a reference] at all!</p>");
        test("before [#local ancor] after", "<p>before [#local ancor] after</p>");
    }

    /**
     * @throws WikiParserException
     */
    public void testTables() throws WikiParserException
    {
        // "!=" and "!!" markup
        test("!= Header !! Cell ", "" + "<table><tbody>\n" + "  <tr><th> Header </th><td> Cell </td></tr>\n"
            + "</tbody></table>");
        test("!=   Header    !!    Cell    ", "" + "<table><tbody>\n"
            + "  <tr><th>   Header    </th><td>    Cell    </td></tr>\n" + "</tbody></table>");

        test("!!Cell 1 !! Cell 2", "<table><tbody>\n" + "  <tr><td>Cell 1 </td><td> Cell 2</td></tr>\n"
            + "</tbody></table>");
        test("Not a Header !! Not a Cell", "<p>Not a Header !! Not a Cell</p>");
        test("Not a Header!!Not a Cell", "<p>Not a Header!!Not a Cell</p>");

        // "|=" and "|" markup
        test("|= Header | Cell ", "" + "<table><tbody>\n" + "  <tr><th> Header </th><td> Cell </td></tr>\n"
            + "</tbody></table>");
        test("|=   Header    |    Cell    ", "" + "<table><tbody>\n"
            + "  <tr><th>   Header    </th><td>    Cell    </td></tr>\n" + "</tbody></table>");

        test("|Cell 1 | Cell 2", "<table><tbody>\n" + "  <tr><td>Cell 1 </td><td> Cell 2</td></tr>\n"
            + "</tbody></table>");
        test("Not a Header | Not a Cell", "<p>Not a Header | Not a Cell</p>");
        test("Not a Header|Not a Cell", "<p>Not a Header|Not a Cell</p>");

        test("|= cell 1.1 |= cell 1.2\n" + "|= cell 2.1|= cell 2.2", "" + "<table><tbody>\n"
            + "  <tr><th> cell 1.1 </th><th> cell 1.2</th></tr>\n"
            + "  <tr><th> cell 2.1</th><th> cell 2.2</th></tr>\n" + "</tbody></table>");
        test("|= Head 1.1 |= Head 1.2\n" + "| cell 2.1| cell 2.2", "" + "<table><tbody>\n"
            + "  <tr><th> Head 1.1 </th><th> Head 1.2</th></tr>\n"
            + "  <tr><td> cell 2.1</td><td> cell 2.2</td></tr>\n" + "</tbody></table>");
        test("|= Multi \nline  \nheader \n" + "| Multi\nline\ncell\n" + "\n" + "One,two,three", "" + "<table><tbody>\n"
            + "  <tr><th> Multi \nline  \nheader </th></tr>\n" + "  <tr><td> Multi\nline\ncell</td></tr>\n"
            + "</tbody></table>\n" + "<p>One,two,three</p>");
        test("this is not |= a table", "<p>this is not |= a table</p>");
        test("this is not | a table", "<p>this is not | a table</p>");
        test("|Hi|Hello|\n\nSome Text", "<table><tbody>\n" + "  <tr><td>Hi</td><td>Hello</td><td></td></tr>\n"
            + "</tbody></table>\n" + "<p>Some Text</p>");
        test("|cell\n\n(% name='value' %)\ntext", "<table><tbody>\n" + "  <tr><td>cell</td></tr>\n"
            + "</tbody></table>\n" + "<p name='value'>text</p>");

        test("|= //Italic header// |= **Bold header**\n" + "| //Italic cell// | **Bold cell**\n", ""
            + "<table><tbody>\n"
            + "  <tr><th> <em>Italic header</em> </th><th> <strong>Bold header</strong></th></tr>\n"
            + "  <tr><td> <em>Italic cell</em> </td><td> <strong>Bold cell</strong></td></tr>\n"
            + "</tbody></table>");
        test("|= //Italic header |= **Bold header \n" + "| //Italic cell | **Bold cell \n", "" + "<table><tbody>\n"
            + "  <tr><th> <em>Italic header </em></th><th> <strong>Bold header </strong></th></tr>\n"
            + "  <tr><td> <em>Italic cell </em></td><td> <strong>Bold cell </strong></td></tr>\n"
            + "</tbody></table>");

        // Empty cells
        test("|||cell13", "<table><tbody>\n" + "  <tr><td></td><td></td><td>cell13</td></tr>\n" + "</tbody></table>");

        // Table parameters
        test("(%a=b%)\n|= Header ", "" + "<table a='b'><tbody>\n" + "  <tr><th> Header </th></tr>\n"
            + "</tbody></table>");
        test("(%a=b%)\n!= Header ", "" + "<table a='b'><tbody>\n" + "  <tr><th> Header </th></tr>\n"
            + "</tbody></table>");
        test("(%a=b%)\n| cell ", "" + "<table a='b'><tbody>\n" + "  <tr><td> cell </td></tr>\n" + "</tbody></table>");
        test("(%a=b%)\n| cell ", "" + "<table a='b'><tbody>\n" + "  <tr><td> cell </td></tr>\n" + "</tbody></table>");

        // Row parameters
        test("(%a=b%)|=cell");
        test("(%a=b%)!!cell1\n(%c=d%)!!cell2");

        test("(%a=b%)\n(%c=d%)|=(%e=f%) cell");
        test("(%a=b%)\n(%c=d%)!!(%e=f%) cell !!(%g=h%)");

        // Cell content
        // TODO: this is a bug but too old to be fixed now, should be fixed for 2.1
        test("|Bla\n(% param='value' %)\nBla Bla", "<table><tbody>\n" +
            "  <tr><td>Bla</td></tr>\n" +
            "</tbody></table>\n" +
            "<p param='value'>Bla Bla</p>");

        // Misc
        test("|cell\n(% param='value' %)\nparagraph|", "<table><tbody>\n" +
            "  <tr><td>cell</td></tr>\n" +
            "</tbody></table>\n" +
            "<p param='value'>paragraph|</p>");
        test("|Bla\n(% parm='value' %)\nBla Bla\n\n", "<table><tbody>\n" +
            "  <tr><td>Bla</td></tr>\n" +
            "</tbody></table>\n" +
            "<p parm='value'>Bla Bla</p>");
    }

    public void testVerbatim() throws WikiParserException
    {

        test("{{{verbatim}}}\n* not really", "<pre>verbatim</pre>\n<ul>\n  <li>not really</li>\n</ul>");

        test("this is {{{verbatim", "<p>this is <tt class=\"wikimodel-verbatim\">verbatim</tt></p>");
        test("{{{abc}}}", "<pre>abc</pre>");
        test("{{{{{{abc}}}}}}", "<pre>{{{abc}}}</pre>");

        // Inline verbatim // test(" {{{abc}}}",
        // "<p> <tt class=\"wikimodel-verbatim\">abc</tt></p>");
        test("{{{abc}}}{{{cde}}}",
            "<p><tt class=\"wikimodel-verbatim\">abc</tt><tt class=\"wikimodel-verbatim\">cde</tt></p>");
        test("{{{abc}}}after", "<p><tt class=\"wikimodel-verbatim\">abc</tt>after</p>");
        test("before{{{abc}}}after", "<p>before<tt class=\"wikimodel-verbatim\">abc</tt>after</p>");
        test("before{{{{{{abc}}}}}}after", "<p>before<tt class=\"wikimodel-verbatim\">{{{abc}}}</tt>after</p>");
        test("}}}", "<p>}}}</p>");
        test("{{{", "<pre></pre>");
        test("{{{}}}\n\n{{{}}}", "<pre></pre>\n<pre></pre>");
        test("some text {{{}}}\n\n{{{}}}", "<p>some text <tt class=\"wikimodel-verbatim\"></tt></p>\n<pre></pre>");
        test(
            "{{{}}}\n{{{}}}\n{{{}}}",
            "<p><tt class=\"wikimodel-verbatim\"></tt>\n<tt class=\"wikimodel-verbatim\"></tt>\n<tt class=\"wikimodel-verbatim\"></tt></p>");
        test("{{{}}}\n\n{{{}}}\n\n{{{}}}\n\n{{{}}}", "<pre></pre>\n<pre></pre>\n<pre></pre>\n<pre></pre>");
        test("{{{}}}\nsome text", "<p><tt class=\"wikimodel-verbatim\"></tt>\nsome text</p>");

        test("some text {{{}}}\nsome text", "<p>some text <tt class=\"wikimodel-verbatim\"></tt>\nsome text</p>");

        // Escaping
        test("{{{ ~}~}~} }}}", "<pre> }}} </pre>");
        test("{{{ ~}}} }}}", "<pre> }}} </pre>");
        test("{{{ ~}}~} }}}", "<pre> }}} </pre>");
        test("{{{ ~}~}} }}}", "<pre> }}} </pre>");
        test("{{{ ~{~{~{ }}}", "<pre> {{{ </pre>");
        test("{{{ ~{{{ }}}", "<pre> {{{ </pre>");
        test("{{{ ~{{~{ }}}", "<pre> {{{ </pre>");
        test("{{{ ~{~{{ }}}", "<pre> {{{ </pre>");
        test("{{{ ~ }}}", "<pre> ~ </pre>");
        test("{{{ ~}~}~}", "<pre> }}}</pre>");
    }

    public void testLink() throws WikiParserException
    {
        test("[[label>>reference||param=\"value ~\"value~\"\"]]",
            "<p><a href='reference' param='value &#x22;value&#x22;'>label</a></p>");

        test("~[~[nolink]]\n[[[[>>reference]]\n~[~[whatever\n~[[[link]]\n[http://reference]",
             "<p>[[nolink]]\n<a href='reference'>[[</a>\n[[whatever\n"
             + "[<a href='link' class='wikimodel-freestanding'>link</a>\n"
             + "[<a href='http://reference' class='wikimodel-freestanding'>http://reference</a>]</p>");

    }

    public void testEmptyLine() throws WikiParserException
    {
        test("paragraph\n\nparagraph", "<p>paragraph</p>\n<p>paragraph</p>");
        // The following doe snot seems right
        test("paragraph\n\n\nparagraph", "<p>paragraph</p>\n<p>paragraph</p>");
        test("paragraph\n\n\n\nparagraph", "<p>paragraph</p>\n<div style='height:2em;'></div>\n<p>paragraph</p>");
        test("paragraph\n\n\n\n\nparagraph", "<p>paragraph</p>\n<div style='height:3em;'></div>\n<p>paragraph</p>");

        test("paragraph\n\n\n\n----", "<p>paragraph</p>\n<div style='height:2em;'></div>\n<hr />");
        test("paragraph\n\n\n\n\n----", "<p>paragraph</p>\n<div style='height:3em;'></div>\n<hr />");
    }
}

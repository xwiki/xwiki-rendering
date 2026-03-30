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
import org.xwiki.rendering.wikimodel.common.CommonWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
class CommonWikiParserTest extends AbstractWikiParserTest
{
    @Override
    protected IWikiParser newWikiParser()
    {
        return new CommonWikiParser();
    }

    CommonWikiParserTest()
    {
        this.supportDownload = true;
        this.supportImage = true;
    }

    @Test
    void test() throws Exception
    {
        showSections(true);
        test("((()))", """
            <section-1-0>
            <sectionContent-1-0>
            <div class='wikimodel-document'>
            <section-2-0>
            <sectionContent-2-0>
            </sectionContent-2-0>
            </section-2-0>
            </div>
            </sectionContent-1-0>
            </section-1-0>""");
        test("=Header1=\nabc((()))\ncde", """
            <section-1-0>
            <sectionContent-1-0>
            <section-1-1>
            <h1>Header1</h1>
            <sectionContent-1-1>
            <p>abc</p>
            <div class='wikimodel-document'>
            <section-2-0>
            <sectionContent-2-0>
            </sectionContent-2-0>
            </section-2-0>
            </div>
            <p>cde</p>
            </sectionContent-1-1>
            </section-1-1>
            </sectionContent-1-0>
            </section-1-0>""");
    }

    @Test
    void testComplexFormatting() throws WikiParserException
    {
        test("""
            %rdf:type toto:Document

            %title Hello World

            %summary This is a short description
            %locatedIn (((
                %type [City]
                %name [Paris]
                %address (((
                  %building 10
                  %street Cité Nollez
                )))\s
            )))
            = Hello World =

            * item one
              * sub-item a
              * sub-item b
                + ordered X\s
                + ordered Y
              * sub-item c
            * item two


            The table below contains\s
            an %seeAlso(embedded document).\s
            It can contain the same formatting\s
            elements as the root document.


            !! Table Header 1.1 !! Table Header 1.2
            :: Cell 2.1 :: Cell 2.2 (((
            == Embedded document ==
            This is an embedded document:
            * item X
            * item Y
            ))) The text goes after the embedded
             document
            :: Cell 3.1 :: Cell 3.2""");
        test("""
            ----------------------------------------------
            = Example1 =

            The table below contains an embedded document.
            Using such embedded documents you can insert table
            in a list or a list in a table. And embedded documents
            can contain their own embedded documents!!!

            !! Header 1.1 !! Header 1.2
            :: Cell 2.1 :: Cell 2.2 with an embedded document: (((
            == This is an embedded document! ==
            * list item one
            * list item two
              * sub-item A
              * sub-item B
            * list item three
            )))
            :: Cell 3.1 :: Cell 3.2

            This is a paragraphs after the table...
            ----------------------------------------------
            """);
    }

    @Test
    void testDocuments() throws WikiParserException
    {
        test("before ((( inside ))) after ", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p>inside</p>
            </div>
            <p>after </p>""");
        test("before inside ))) after ", """
            <p>before inside</p>
            <p>after </p>""");
        test("before (((\ninside ))) after ", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p>inside</p>
            </div>
            <p>after </p>""");
        test("before (((\n inside ))) after ", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p> inside</p>
            </div>
            <p>after </p>""");
        test("""
            | Line One | First doc: (((
             inside ))) after\s
            |Line Two | Second doc: (((lkjlj))) skdjg""",
            """
            <table><tbody>
              <tr><td> Line One </td><td> First doc:<div class='wikimodel-document'>
            <p> inside</p>
            </div>
            after </td></tr>
              <tr><td>Line Two </td><td> Second doc:<div class='wikimodel-document'>
            <p>lkjlj</p>
            </div>
            skdjg</td></tr>
            </tbody></table>""");
        test(
            """
            | This is a table: | (((* item one
            * item two
             * subitem 1
             * subitem 2
            * item three)))\s""",
            """
            <table><tbody>
              <tr><td> This is a table: </td><td><div class='wikimodel-document'>
            <ul>
              <li>item one</li>
              <li>item two<ul>
              <li>subitem 1</li>
              <li>subitem 2</li>
            </ul>
            </li>
              <li>item three</li>
            </ul>
            </div>
            </td></tr>
            </tbody></table>""");
        test("before ((( opened and not closed", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p>opened and not closed</p>
            </div>""");
        test("before ((( one ((( two ((( three ", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p>one</p>
            <div class='wikimodel-document'>
            <p>two</p>
            <div class='wikimodel-document'>
            <p>three </p>
            </div>
            </div>
            </div>""");
    }

    @Test
    void testDocumentSections() throws WikiParserException
    {
        showSections(true);
        test("abc", """
            <section-1-0>
            <sectionContent-1-0>
            <p>abc</p>
            </sectionContent-1-0>
            </section-1-0>""");
        test("=Header=\nabc", """
            <section-1-0>
            <sectionContent-1-0>
            <section-1-1>
            <h1>Header</h1>
            <sectionContent-1-1>
            <p>abc</p>
            </sectionContent-1-1>
            </section-1-1>
            </sectionContent-1-0>
            </section-1-0>""");
        // section-1-1 for Header 1, section-1-1 for Header 2
        test("=Header 1=\nabc\n=Header 2=\ncde", """
            <section-1-0>
            <sectionContent-1-0>
            <section-1-1>
            <h1>Header 1</h1>
            <sectionContent-1-1>
            <p>abc</p>
            </sectionContent-1-1>
            </section-1-1>
            <section-1-1>
            <h1>Header 2</h1>
            <sectionContent-1-1>
            <p>cde</p>
            </sectionContent-1-1>
            </section-1-1>
            </sectionContent-1-0>
            </section-1-0>""");
        // section-1-1 for Header 1 (with sub-sections 1.1 and 1.2), section-1-1 for Header 2
        test("=Header 1=\nabc\n==Header 1.1==\ncde\n==Header 1.2==\nefg\n=Header 2=\nghk", """
            <section-1-0>
            <sectionContent-1-0>
            <section-1-1>
            <h1>Header 1</h1>
            <sectionContent-1-1>
            <p>abc</p>
            <section-1-2>
            <h2>Header 1.1</h2>
            <sectionContent-1-2>
            <p>cde</p>
            </sectionContent-1-2>
            </section-1-2>
            <section-1-2>
            <h2>Header 1.2</h2>
            <sectionContent-1-2>
            <p>efg</p>
            </sectionContent-1-2>
            </section-1-2>
            </sectionContent-1-1>
            </section-1-1>
            <section-1-1>
            <h1>Header 2</h1>
            <sectionContent-1-1>
            <p>ghk</p>
            </sectionContent-1-1>
            </section-1-1>
            </sectionContent-1-0>
            </section-1-0>""");

        // Embedded document

        test("((()))", """
            <section-1-0>
            <sectionContent-1-0>
            <div class='wikimodel-document'>
            <section-2-0>
            <sectionContent-2-0>
            </sectionContent-2-0>
            </section-2-0>
            </div>
            </sectionContent-1-0>
            </section-1-0>""");
        // section-1-1 for Header 1 with embedded doc (section-2-1 for 1.1 and 1.2), section-1-1 for Header 2
        test("=Header 1=\nabc\n(((=Header 1.1=\ncde\n=Header 1.2=\nefg\n)))\nxyz\n=Header 2=\nghk", """
            <section-1-0>
            <sectionContent-1-0>
            <section-1-1>
            <h1>Header 1</h1>
            <sectionContent-1-1>
            <p>abc</p>
            <div class='wikimodel-document'>
            <section-2-0>
            <sectionContent-2-0>
            <section-2-1>
            <h1>Header 1.1</h1>
            <sectionContent-2-1>
            <p>cde</p>
            </sectionContent-2-1>
            </section-2-1>
            <section-2-1>
            <h1>Header 1.2</h1>
            <sectionContent-2-1>
            <p>efg</p>
            </sectionContent-2-1>
            </section-2-1>
            </sectionContent-2-0>
            </section-2-0>
            </div>
            <p>xyz</p>
            </sectionContent-1-1>
            </section-1-1>
            <section-1-1>
            <h1>Header 2</h1>
            <sectionContent-1-1>
            <p>ghk</p>
            </sectionContent-1-1>
            </section-1-1>
            </sectionContent-1-0>
            </section-1-0>""");
    }

    @Test
    void testEscape() throws WikiParserException
    {
        test("[a reference]");
        test("\\[not a reference]");

        test("\\First letter is escaped");
        test("\\[not a reference]");
        test("\\\\escaped backslash");
        test("\\ a line break because it is followed by a space");

        test("= Heading =\n\\= Not a heading =\n= Heading again! =");
    }

    @Test
    void testExtensions() throws WikiParserException
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

        test("before\n$abc after", """
            <p>before</p>
            <div class='wikimodel-extension' extension='abc'/>
            <p> after</p>""");
        test("before\n$abc() after", """
            <p>before</p>
            <div class='wikimodel-extension' extension='abc'/>
            <p> after</p>""");
        test("before\n$abc(a=b c=d) after", """
            <p>before</p>
            <div class='wikimodel-extension' extension='abc' a='b' c='d'/>
            <p> after</p>""");
        test("before\n$abc(hello)after", """
            <p>before</p>
            <div class='wikimodel-extension' extension='abc' hello=''/>
            <p>after</p>""");
    }

    @Test
    void testFormats() throws WikiParserException
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

    @Test
    void testHeaders() throws WikiParserException
    {
        test("=Header1=", "<h1>Header1</h1>");
        test("==Header2==", "<h2>Header2</h2>");
        test("===Header3===", "<h3>Header3</h3>");
        test("====Header4====", "<h4>Header4</h4>");
        test("=Header1", "<h1>Header1</h1>");
        test("==Header2", "<h2>Header2</h2>");
        test("===Header3", "<h3>Header3</h3>");
        test("====Header4", "<h4>Header4</h4>");
        test("before\n= Header =\nafter", """
            <p>before</p>
            <h1>Header </h1>
            <p>after</p>""");

        test("This is not a header: ==", "<p>This is not a header: ==</p>");

        test("{{a=b}}\n=Header1", "<h1 a='b'>Header1</h1>");
    }

    @Test
    void testHorLine() throws WikiParserException
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

    @Test
    void testInfo() throws WikiParserException
    {
        test("/i\\ item {{{formatted block}}} {macro}123{/macro} after");
        test("""
            before
            /i\\Information block:
            {{{pre
              formatted
             block}}} sdlkgj
            qsdg

            after""");
        test("/!\\");
        test("/i\\info");
        test("""
            /i\\Information block:
            first line
            second line
            third  line""");
        test("{{a=b}}\n/!\\");
        test("{{a=b}}\n/i\\info");
    }

    @Test
    void testLineBreak() throws WikiParserException
    {
        test("abc\\\ndef");
        test("abc\\  \ndef");
        test("abc\\ x \ndef");
        test("abc x \ndef");
    }

    @Test
    void testLists() throws WikiParserException
    {
        test(
            "*this is a bold, and not a list",
            "<p><strong>this is a bold, and not a list</strong></p>");
        test("**bold**", "<p><strong>bold</strong></p>");

        test("* first", "<ul>\n  <li>first</li>\n</ul>");
        test(
            "** second",
            "<ul>\n  <li><ul>\n  <li>second</li>\n</ul>\n</li>\n</ul>");

        test("""
            * item one
            * item two
            *+item three
            *+ item four
            * item five - first line
               item five - second line
            * item six
              is on multiple
             lines""");

        test(
            "* item {{{formatted block}}} {macro}123{/macro} after",
            """
            <ul>
              <li>item <tt class="wikimodel-verbatim">formatted block</tt> \
            <span class='wikimodel-macro' macroName='macro'><![CDATA[123]]></span> after</li>
            </ul>""");

        test("? term:  definition");
        test("?just term");
        test(":just definition");
        test(";:just definition");
        test(":just definition");
        test(";:");
        test("""
            : Indenting is stripped out.
             : Includes double indenting""");

        test("""
            ;term one: definition one
            ;term two: definition two
            ;term three: definition three""");
        test(":Term definition");
        test(";:Term definition");

        test("""
            ;One,
            two,
            bucle my shoes...:
            ...Three
            four,
            Close the door
            ;Five,
            Six: Pick up
             sticks

            tam-tam, pam-pam...""");

        test(";__term__: *definition*");

        test("""
            this is not a definition --
             ;__not__ a term: ''not'' a definition
            ----toto""");

        test("{{a='b'}}\n* item one");
    }

    @Test
    void testMacro() throws WikiParserException
    {
        test(
            "{toto}a{/toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>");
        test(
            "{toto}a{toto}b{/toto}c{/toto}",
            "<pre class='wikimodel-macro' macroName='toto'><![CDATA[a{toto}b{/toto}c]]></pre>");
        test(
            "before\n{toto}a{/toto}\nafter",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>
            <p>after</p>""");
        test(
            "before\n{toto}a{/toto}after",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='toto'><![CDATA[a]]></pre>
            <p>after</p>""");

        // URIs as macro names
        test(
            "{x:toto}a{/x:toto}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>");
        test(
            "{x:toto}a{x:toto}b{/x:toto}c{/x:toto}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a{x:toto}b{/x:toto}c]]></pre>");
        test(
            "before\n{x:toto}a{/x:toto}\nafter",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>
            <p>after</p>""");
        test(
            "before\n{x:toto}a{/x:toto}after",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='x:toto'><![CDATA[a]]></pre>
            <p>after</p>""");

        // Empty macros
        test(
            "{x:toto /}",
            "<pre class='wikimodel-macro' macroName='x:toto'><![CDATA[]]></pre>");
        test(
            "{x:toto a=b c=d /}",
            "<pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'><![CDATA[]]></pre>");
        test(
            "before\n{x:toto  a=b c=d/}\nafter",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'><![CDATA[]]></pre>
            <p>after</p>""");
        test(
            "before\n{x:toto  a='b' c='d'/}after",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='x:toto' a='b' c='d'><![CDATA[]]></pre>
            <p>after</p>""");
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
            "<p>before<span class='wikimodel-macro' macroName='toto' a='b' c='d'>"
                + "<![CDATA[toto macro tata ]]></span>after</p>");

        test(
            "before{toto a=b c=d}toto {x qsdk} macro {sd} tata {/toto}after",
            "<p>before<span class='wikimodel-macro' macroName='toto' a='b' c='d'>"
                + "<![CDATA[toto {x qsdk} macro {sd} tata ]]></span>after</p>");

        // Macros in other block elements (tables and lists)
        test(
            "- before\n{code a=b c=d}this is a code{/code}after",
            """
            <ul>
              <li>before<pre class='wikimodel-macro' macroName='code' a='b' c='d'><![CDATA[this is a code]]></pre>
            after</li>
            </ul>""");
        test(
            "- before{code a=b c=d}this is a code{/code}after",
            """
            <ul>
              <li>before<span class='wikimodel-macro' macroName='code' a='b' c='d'>\
            <![CDATA[this is a code]]></span>after</li>
            </ul>""");

        // Not a macro
        test("{ toto a=b c=d}", "<p>{ toto a=b c=d}</p>");

        // Macro and its usage
        test(
            """
            This is a macro: {toto x:a=b x:c=d}
            <table>
            #foreach ($x in $table)
              <tr>hello, $x</tr>
            #end
            </table>
            {/toto}

            And this is a usage of this macro: $toto(a=x b=y)""",
            """
            <p>This is a macro: <span class='wikimodel-macro' macroName='toto' x:a='b' x:c='d'><![CDATA[
            <table>
            #foreach ($x in $table)
              <tr>hello, $x</tr>
            #end
            </table>
            ]]></span></p>
            <p>And this is a usage of this macro: <span class='wikimodel-extension' extension='toto' a='x' b='y'/></p>""");

        test("""
            !!Header:: Cell with a macro:\s
            {code}this is a code{/code}\s
             this is afer the code...""", """
            <table><tbody>
              <tr><th>Header</th><td> Cell with a macro: <pre class='wikimodel-macro' macroName='code'>\
            <![CDATA[this is a code]]></pre>
            \s
             this is afer the code...</td></tr>
            </tbody></table>""");
        test("""
            * item one
            * item two
              * subitem with a macro:
              {code} this is a code{/code}\s
              the same item (continuation)
              * subitem two
            * item three""", """
            <ul>
              <li>item one</li>
              <li>item two<ul>
              <li>subitem with a macro:
              <span class='wikimodel-macro' macroName='code'><![CDATA[ this is a code]]></span>\s
              the same item (continuation)</li>
              <li>subitem two</li>
            </ul>
            </li>
              <li>item three</li>
            </ul>""");

        // Macros with URIs as names
        test(
            "{x:y a=b c=d}",
            "<pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[]]></pre>");
        test(
            "before{x:y a=b c=d}macro content",
            "<p>before<span class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[macro content]]></span></p>");
        test(
            "before\n{x:y a=b c=d}macro content",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[macro content]]></pre>""");
        test(
            "before\n{x:y a=b c=d/}\nafter",
            """
            <p>before</p>
            <pre class='wikimodel-macro' macroName='x:y' a='b' c='d'><![CDATA[]]></pre>
            <p>after</p>""");

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

    @Test
    void testParagraphs() throws WikiParserException
    {
        test("{{background='blue'}}", "<p background='blue'></p>");
        test("""
            {{background='blue'}}
            {{background='red'}}
            {{background='green'}}""", """
            <p background='blue'></p>
            <p background='red'></p>
            <p background='green'></p>""");
        test("""
            {{background='blue'}}first
            {{background='red'}}second
            {{background='green'}}third""", """
            <p background='blue'>first</p>
            <p background='red'>second</p>
            <p background='green'>third</p>""");
        test("""
            {{background='blue'}}
            first
            {{background='red'}}
            second
            {{background='green'}}
            third""", """
            <p background='blue'>first</p>
            <p background='red'>second</p>
            <p background='green'>third</p>""");

        test("{{background='blue'}}hello", "<p background='blue'>hello</p>");
        test("""
            {{background='blue'}}
            First paragraph



            """, """
            <p background='blue'>First paragraph</p>
            <div style='height:3em;'></div>""");

        test("First paragraph\n\n\n\n");
        test("""
            First paragraph.
            Second line of the same paragraph.

            The second paragraph""");

        test("\n<toto");
    }

    @Test
    void testPropertiesBlock() throws WikiParserException
    {
        test(
            "%toto hello  world\n123",
            "<div class='wikimodel-property' url='toto'><p>hello  world</p>\n</div>\n<p>123</p>");
        test(
            "%prop1 value1\n%prop2 value2",
            """
            <div class='wikimodel-property' url='prop1'><p>value1</p>
            </div>
            <div class='wikimodel-property' url='prop2'><p>value2</p>
            </div>""");
        test(
            "%prop1 value1\nparagraph\n%prop2 value2",
            """
            <div class='wikimodel-property' url='prop1'><p>value1</p>
            </div>
            <p>paragraph</p>
            <div class='wikimodel-property' url='prop2'><p>value2</p>
            </div>""");

        test("%prop1 (((embedded)))next paragraph\n%prop2 value2", """
            <div class='wikimodel-property' url='prop1'>
            <p>embedded</p>
            </div>
            <p>next paragraph</p>
            <div class='wikimodel-property' url='prop2'><p>value2</p>
            </div>""");
        test(
            "%prop1 (((=Header\n- item 1\n- item 2)))next paragraph\n%prop2 value2",
            """
            <div class='wikimodel-property' url='prop1'>
            <h1>Header</h1>
            <ul>
              <li>item 1</li>
              <li>item 2</li>
            </ul>
            </div>
            <p>next paragraph</p>
            <div class='wikimodel-property' url='prop2'><p>value2</p>
            </div>""");

        test(
            """
            before

            %company (((
                %name Cognium Systems
                %addr (((
                    %country [France]
                    %city [Paris]
                    %street Cité Nollez
                    This is just a description...
                )))
            )))

            after""",
            """
            <p>before</p>
            <div class='wikimodel-property' url='company'>
            <div class='wikimodel-property' url='name'><p>Cognium Systems</p>
            </div>
            <div class='wikimodel-property' url='addr'>
            <div class='wikimodel-property' url='country'><p><a href='France' class='wikimodel-freestanding'>France</a></p>
            </div>
            <div class='wikimodel-property' url='city'><p><a href='Paris' class='wikimodel-freestanding'>Paris</a></p>
            </div>
            <div class='wikimodel-property' url='street'><p>Cité Nollez</p>
            </div>
            <p>        This is just a description...</p>
            </div>
            </div>
            <p>after</p>""");
        // Bad formed block properties

        // No closing brackets
        test(
            """
            before

            %company (((
                %name Cognium Systems
                %addr (((
                    %country [France]
                    %city Paris
                    %street Cité Nollez
                    This is just a description...
            after""",
            """
            <p>before</p>
            <div class='wikimodel-property' url='company'>
            <div class='wikimodel-property' url='name'><p>Cognium Systems</p>
            </div>
            <div class='wikimodel-property' url='addr'>
            <div class='wikimodel-property' url='country'><p><a href='France' class='wikimodel-freestanding'>France</a></p>
            </div>
            <div class='wikimodel-property' url='city'><p>Paris</p>
            </div>
            <div class='wikimodel-property' url='street'><p>Cité Nollez</p>
            </div>
            <p>        This is just a description...
            after</p>
            </div>
            </div>""");
    }

    @Test
    void testPropertiesInline() throws WikiParserException
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

    @Test
    void testQuot() throws WikiParserException
    {
        test("Q: Quotation", "<blockquote>\n Quotation\n</blockquote>");

        test("""
            >This is a message
            >>and this is a response to the message\s
            > This is a continuation of the same message""", """
            <blockquote>
            This is a message<blockquote>
            and this is a response to the message\s
            </blockquote>

             This is a continuation of the same message
            </blockquote>""");

        test("""
            This is a paragraph
            >and this is a quotations
            > the second line""", """
            <p>This is a paragraph</p>
            <blockquote>
            and this is a quotations
             the second line
            </blockquote>""");

        test("""
                        This is just a description...
               \s


            """);
        test("""
            > first
            >> second
            >> third
            >>> subquot1
            >>> subquot2
            >> fourth""");
        test("""
            {{a='b'}}
              first
              second
              third
                subquot1
                subquot2  fourth""");
    }

    @Test
    void testReferences() throws WikiParserException
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

        test("before (((doc-before(=toto=)doc-after))) after", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p>doc-before<a href='toto' class='wikimodel-freestanding'>toto</a>doc-after</p>
            </div>
            <p>after</p>""");
        test("before ((((=toto=)))) after", """
            <p>before</p>
            <div class='wikimodel-document'>
            <p><a href='toto' class='wikimodel-freestanding'>toto</a></p>
            </div>
            <p>after</p>""");
        test(" ((((=toto=))))", """
            <div class='wikimodel-document'>
            <p><a href='toto' class='wikimodel-freestanding'>toto</a></p>
            </div>""");
        test("((((=toto=))))", """
            <div class='wikimodel-document'>
            <p><a href='toto' class='wikimodel-freestanding'>toto</a></p>
            </div>""");

        test("((((((toto))))))", """
            <div class='wikimodel-document'>
            <div class='wikimodel-document'>
            <p>toto</p>
            </div>
            </div>""");
        test("(((a(((toto)))b)))", """
            <div class='wikimodel-document'>
            <p>a</p>
            <div class='wikimodel-document'>
            <p>toto</p>
            </div>
            <p>b</p>
            </div>""");
    }

    @Test
    void testSpecialSymbols() throws WikiParserException
    {
        test(":)");
    }

    @Test
    void testTables() throws WikiParserException
    {
        // "!!" and "::" markup
        test("!! Header :: Cell ", """
            <table><tbody>
              <tr><th> Header </th><td> Cell </td></tr>
            </tbody></table>""");
        test("!!   Header    ::    Cell    ", """
            <table><tbody>
              <tr><th>   Header    </th><td>    Cell    </td></tr>
            </tbody></table>""");
        test("""
            !! Header 1.1 !! Header 1.2
            :: Cell 2.1 :: Cell 2.2
            :: Cell 3.1 !! Head 3.2""", """
            <table><tbody>
              <tr><th> Header 1.1 </th><th> Header 1.2</th></tr>
              <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>
              <tr><td> Cell 3.1 </td><th> Head 3.2</th></tr>
            </tbody></table>""");

        test("::Cell 1 :: Cell 2", """
            <table><tbody>
              <tr><td>Cell 1 </td><td> Cell 2</td></tr>
            </tbody></table>""");
        test("Not a Header :: Not a Cell", "<p>Not a Header :: Not a Cell</p>");
        test("Not a Header::Not a Cell", "<p>Not a Header::Not a Cell</p>");

        // Creole syntax
        test("""
            |= Header 1.1 |= Header 1.2
            | Cell 2.1 | Cell 2.2
            | Cell 3.1 |= Head 3.2""", """
            <table><tbody>
              <tr><th> Header 1.1 </th><th> Header 1.2</th></tr>
              <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>
              <tr><td> Cell 3.1 </td><th> Head 3.2</th></tr>
            </tbody></table>""");
        test("""
            |={{a=b}} Header 1.1 |= Header 1.2
            | Cell 2.1 | Cell 2.2
            | Cell 3.1 |={{c=d}} Head 3.2""", """
            <table><tbody>
              <tr><th a='b'> Header 1.1 </th><th> Header 1.2</th></tr>
              <tr><td> Cell 2.1 </td><td> Cell 2.2</td></tr>
              <tr><td> Cell 3.1 </td><th c='d'> Head 3.2</th></tr>
            </tbody></table>""");
        test(
            "{{x=y}}|={{a=b}} Header 1.1 |={{n=m}} Header 1.2",
            """
            <table><tbody>
              <tr x='y'><th a='b'> Header 1.1 </th><th n='m'> Header 1.2</th></tr>
            </tbody></table>""");
        test(
            "{{A=B}}\n{{x=y}}|={{a=b}} Header 1.1 |={{n=m}} Header 1.2",
            """
            <table A='B'><tbody>
              <tr x='y'><th a='b'> Header 1.1 </th><th n='m'> Header 1.2</th></tr>
            </tbody></table>""");

        // "||" and "|" markup
        test("|| Header | Cell ", """
            <table><tbody>
              <tr><th> Header </th><td> Cell </td></tr>
            </tbody></table>""");
        test("||   Header    |    Cell    ", """
            <table><tbody>
              <tr><th>   Header    </th><td>    Cell    </td></tr>
            </tbody></table>""");

        test("""
            || cell 1.1 || cell 1.2
            || cell 2.1|| cell 2.2""", """
            <table><tbody>
              <tr><th> cell 1.1 </th><th> cell 1.2</th></tr>
              <tr><th> cell 2.1</th><th> cell 2.2</th></tr>
            </tbody></table>""");
        test("""
            || Head 1.1 || Head 1.2
            | cell 2.1| cell 2.2""", """
            <table><tbody>
              <tr><th> Head 1.1 </th><th> Head 1.2</th></tr>
              <tr><td> cell 2.1</td><td> cell 2.2</td></tr>
            </tbody></table>""");
        test("""
            || Multi\s
            line \s
            header\s
            | Multi
            line
            cell

            One,two,three""", """
            <table><tbody>
              <tr><th> Multi\s
            line \s
            header </th></tr>
              <tr><td> Multi
            line
            cell</td></tr>
            </tbody></table>
            <p>One,two,three</p>""");
        test("this is not || a table", "<p>this is not || a table</p>");
        test("this is not | a table", "<p>this is not | a table</p>");
        test(
            """
            || __Italic header__ || *Bold header*
            | __Italic cell__ | *Bold cell*
            """,
            """
            <table><tbody>
              <tr><th> <em>Italic header</em> </th><th> <strong>Bold header</strong></th></tr>
              <tr><td> <em>Italic cell</em> </td><td> <strong>Bold cell</strong></td></tr>
            </tbody></table>""");
        test("""
            || __Italic header || *Bold header\s
            | __Italic cell | *Bold cell\s
            """,
            """
            <table><tbody>
              <tr><th> <em>Italic header </em></th><th> <strong>Bold header </strong></th></tr>
              <tr><td> <em>Italic cell </em></td><td> <strong>Bold cell </strong></td></tr>
            </tbody></table>""");

        // Table parameters
        test("{{a=b}}\n|| Header ", """
            <table a='b'><tbody>
              <tr><th> Header </th></tr>
            </tbody></table>""");
        test("{{a=b}}\n!! Header ", """
            <table a='b'><tbody>
              <tr><th> Header </th></tr>
            </tbody></table>""");
        test("{{a=b}}\n| cell ", """
            <table a='b'><tbody>
              <tr><td> cell </td></tr>
            </tbody></table>""");
        test("{{a=b}}\n:: cell ", """
            <table a='b'><tbody>
              <tr><td> cell </td></tr>
            </tbody></table>""");

        // Row parameters
        test("{{a=b}}||cell");
        test("{{a=b}}::cell1\n{{c=d}}::cell2");

        test("{{a=b}}\n{{c=d}}||{{e=f}} cell");
        test("{{a=b}}\n{{c=d}}::{{e=f}} cell ::{{g=h}}");
    }

    @Test
    void testVerbatimeBlocks() throws WikiParserException
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
            """
            <pre>verbatim</pre>
            <p>}}} - the three last symbols should be in a paragraph</p>""");

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
        test("""
            !! Syntax !! Results
            :: {{{
            !! Header 1 !! Header 2
            :: Cell 1 :: Cell 2
            }}} :: (((
            !! Header 1 !! Header 2
            :: Cell 1 :: Cell 2
            )))
            :: {{{
            || Header 1 || Header 2
            | Cell 1 | Cell 2
            }}} :: (((
            || Header 1 || Header 2
            | Cell 1 | Cell 2
            )))
            """);
    }

    @Test
    void testVerbatimInlineElements() throws WikiParserException
    {
        test("`verbatim`", "<p><tt class=\"wikimodel-verbatim\">verbatim</tt></p>");
        test("before`verbatim`after", "<p>before<tt class=\"wikimodel-verbatim\">verbatim</tt>after</p>");

        // Bad formed elements
        test("`verbatim", "<p>`verbatim</p>");
        test("before`after", "<p>before`after</p>");
        test("before`after\nnext line", "<p>before`after\nnext line</p>");
    }
}
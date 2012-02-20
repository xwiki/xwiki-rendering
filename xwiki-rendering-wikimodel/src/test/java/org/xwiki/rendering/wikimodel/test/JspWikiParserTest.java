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
import org.xwiki.rendering.wikimodel.jspwiki.JspWikiParser;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class JspWikiParserTest extends AbstractWikiParserTest
{
    /**
     * @param name
     */
    public JspWikiParserTest(String name)
    {
        super(name);
    }

    @Override
    protected IWikiParser newWikiParser()
    {
        return new JspWikiParser();
    }

    /**
     * @throws WikiParserException
     */
    public void testEscape() throws WikiParserException
    {
        test("[a reference]");
        test("[[not a reference]");

        test("~First letter is escaped");
        test("~[not a reference]");
        test("~~escaped tilda");
        test("~ just a tilda because there is an espace after this tilda...");

        test("!Heading\n~!Not a heading\n!Heading again!");
    }

    /**
     * @throws WikiParserException
     */
    public void testFormats() throws WikiParserException
    {
        test("__bold__");
        test("''italic''");
        test("__bold");
        test("''italic");

        test("not a bold__");
        test("not an italic''");
    }

    /**
     * @throws WikiParserException
     */
    public void testHeaders() throws WikiParserException
    {
        test("!!!Header");
        test("\n!!!Header\n* list item");
        test("before\n! Header\nafter");
        test("before\n!! Header \nafter");
        test("This is not a header: !!");
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
        test("---abc");
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
        test("*first");
        test("* first");
        test("** second");
        test("*item one\n"
            + "* item two\n"
            + "*#item three\n"
            + "*# item four\n"
            + "* item five - first line\n"
            + "   item five - second line\n"
            + "* item six\n"
            + "  is on multiple\n"
            + " lines");

        test(";term:  definition");
        test(";:just definition");
        test(";just term");
        test(";:");

        test(";term one: definition one\n"
            + ";term two: definition two\n"
            + ";term three: definition three");

        test(";One,\ntwo,\nbucle my shoes...:\n"
            + "...Three\nfour,\nClose the door\n"
            + ";Five,\nSix: Pick up\n sticks\n\ntam-tam, pam-pam...");

        test(";__term__: ''definition''");

        test("this is not a definition --\n"
            + " ;__not__ a term: ''not'' a definition\n"
            + "----toto");
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

        test("\n<toto");
    }

    /**
     * @throws ParseException
     */
    public void testQuot() throws WikiParserException
    {
        test("This is a paragraph\n\n and this is a quotations\n the second line");
    }

    /**
     * @throws WikiParserException
     */
    public void testReferences() throws WikiParserException
    {
        test("before http://www.foo.bar/com after");
        test("before [toto] after");
        test("before wiki:Hello after");
        test("before wiki~:Hello after");
        test("before [#local ancor] after");

        test("not [[a reference] at all!");
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
        test("|| cell 1.1 || cell 1.2\n" + "|| cell 2.1|| cell 2.2");
        test("|| Head 1.1 || Head 1.2\n" + "| cell 2.1| cell 2.2");
        test("|| Multi \nline  \nheader \n"
            + "| Multi\nline\ncell\n\nOne,two,three...");
        test("this is not || a table");
        test("this is not | a table");
        test("|| ''Italic header'' || __Bold header__\n"
            + "| ''Italic cell'' | __Bold cell__\n");
    }

    /**
     * @throws WikiParserException
     */
    public void testVerbatimeBlocks() throws WikiParserException
    {
        test("abc \n{{{ 123\n  CDE\n   345 }}} efg");
        test("abc {{{ 123\n  CDE\n   345 }}} efg");
        test("abc\n{{{\n {{{ 123 \n}\\}} \n}}} efg");
        test("inline{{verbatime}}block");
        test("{{just like this...");
    }
}

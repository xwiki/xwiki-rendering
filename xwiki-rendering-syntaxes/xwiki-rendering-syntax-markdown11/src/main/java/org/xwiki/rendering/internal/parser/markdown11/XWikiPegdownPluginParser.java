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
package org.xwiki.rendering.internal.parser.markdown11;

import org.parboiled.Rule;
import org.parboiled.annotations.Cached;
import org.parboiled.annotations.DontSkipActionsInPredicates;
import org.parboiled.support.StringBuilderVar;
import org.parboiled.support.Var;
import org.pegdown.Parser;
import org.pegdown.ast.TextNode;
import org.pegdown.plugins.BlockPluginParser;
import org.pegdown.plugins.InlinePluginParser;
import org.xwiki.rendering.internal.parser.markdown11.ast.MacroNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.MacroParameterNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.SubscriptNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.SuperscriptNode;

/**
 * Implements a pluggable parser for Pegdown that provides extended syntax for XWiki.
 *
 * @version $Id $
 * @since 5.2M1
 */
public class XWikiPegdownPluginParser extends Parser implements InlinePluginParser, BlockPluginParser
{
    /**
     * String to open the XWiki-style macro tag.
     */
    private static final String MACRO_TAG_OPEN_MARK = "{{";

    /**
     * String to close the XWiki-style macro tag.
     */
    private static final String MACRO_TAG_CLOSE_MARK = "}}";


    public XWikiPegdownPluginParser()
    {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }

    @Override
    public Rule[] blockPluginRules()
    {
        return new Rule[] { XWikiMacro(), MarkdownMacro(false) };
    }

    @Override
    public Rule[] inlinePluginRules()
    {
        return new Rule[] { Superscript(), Subscript(), MarkdownMacro(true) };
    }


    //////// Superscript and subscript ////////

    /**
     * Rule for a superscript formatting.
     * Example: <tt>This is in ^superscript^</tt>
     */
    public Rule Superscript()
    {
        StringBuilderVar text = new StringBuilderVar();
        Rule marker = Ch('^');

        return NodeSequence(
                marker,
                SuperSubScriptText(marker, text),
                push(new SuperscriptNode(text.getString())),
                marker
        );
    }

    /**
     * Rule for a subscript formatting.
     * Example: <tt>This is in ~subscript~</tt>
     */
    public Rule Subscript()
    {
        StringBuilderVar text = new StringBuilderVar();
        Rule marker = Ch('~');

        return NodeSequence(
                marker,
                SuperSubScriptText(marker, text),
                push(new SubscriptNode(text.getString())),
                marker
        );
    }

    /**
     * Common rule for superscript and subscript that parses any text without
     * the given marker ({@literal ^} for superscript or {@literal ~} for
     * subscript) and non-escaped spaces.
     *
     * @param marker <tt>Ch('^')</tt> for superscript, or <tt>Ch('~')</tt> for
     *               subscript
     * @param text StringBuilder variable to append parsed text into
     */
    public Rule SuperSubScriptText(Rule marker, StringBuilderVar text) {
        return OneOrMore(
                TestNot(marker),
                FirstOf(
                        Sequence(
                                Ch('\\'),  //will not append escape char
                                Spacechar(), text.append(match())),
                        Sequence(
                                Nonspacechar(), text.append(match())
                        )
                )
        );
    }


    //////// Macro ////////

    /**
     * Rule for Markdown-style macro syntax.
     *
     * Examples: <tt>#[mymacro par1=val1 par2="val 2"](content)</tt>,
     *           <tt>#[mymacro](content)</tt>,
     *           <tt>#[mymacro par1=val1]</tt>,
     *           <tt>#[mymacro]</tt>
     *
     * @param isInline if the rule is used in inline plugin
     */
    @Cached
    public Rule MarkdownMacro(boolean isInline) {
        return NodeSequence(
                "#[",
                Identifier(), push(new MacroNode(match(), isInline)),
                Sp(),
                ZeroOrMore(
                        MacroParameter(EMPTY),
                        Sp()
                ),
                ']',
                Optional(
                        '(',
                        OneOrMore(
                                TestNot(')'),
                                ANY
                        ), push(new TextNode(match())) && addAsChild(),
                        ')'
                )
        );
    }

    /**
     * Rule for XWiki-style macro syntax.
     *
     * Examples: <tt>{{mymacro par1=val1 par2="val 2"}}content{{/mymacro}}</tt>,
     *           <tt>{{mymacro}}content{{/mymacro}}</tt>,
     *           <tt>{{mymacro par1=val1 /}}</tt>,
     *           <tt>{{mymacro /}}</tt>
     */
    public Rule XWikiMacro()
    {
        Var<MacroNode> node = new Var<MacroNode>();
        return NodeSequence(
                MACRO_TAG_OPEN_MARK,
                Identifier(), push(node.setAndGet(new MacroNode(match(), false))),
                Spn1(),
                ZeroOrMore(
                        Test(Identifier()),
                        MacroParameter(Spn1()),
                        Spn1()
                ),
                FirstOf(
                        Sequence('/', MACRO_TAG_CLOSE_MARK),
                        Sequence(
                                MACRO_TAG_CLOSE_MARK,
                                Optional(Newline()),
                                XWikiMacroContent(node),
                                XWikiMacroCloseTag(node)
                        )
                )
        );
    }

    /**
     * Rule for a macro parameter.
     *
     * Example: <tt>par1="some value"</tt>,
     *          <tt>par2='some value'</tt>,
     *          <tt>par3=someValue</tt>
     *
     * @param space space rule around {@literal =}
     */
    @Cached
    public Rule MacroParameter(Rule space)
    {
        Var<MacroParameterNode> node = new Var<MacroParameterNode>();
        return Sequence(
                Identifier(), node.set(new MacroParameterNode(match())),
                space,
                '=',
                space,
                FirstOf(
                        MacroParameterValue('"', node),
                        MacroParameterValue('\'', node),
                        MacroParameterValue(node)
                ), ((MacroNode) peek()).addParameter(node.get())
        );
    }

    /**
     * Rule for a quoted value of the macro parameter.
     *
     * Example: <tt>"some value"</tt>,
     *          <tt>'some value'</tt>
     *
     * @param mark quotation character
     * @param node MacroParameterNode variable
     */
    public Rule MacroParameterValue(char mark, Var<MacroParameterNode> node)
    {
        return Sequence(
                mark,
                ZeroOrMore(
                        TestNot(mark),
                        ANY
                ), node.get().setValue(match()),
                mark
        );
    }

    /**
     * Rule for a value of the macro parameter without special chars, spaces
     * and newlines.
     *
     * Example: <tt>someValueWithoutSpaces</tt>
     *
     * @param node MacroParameterNode variable
     */
    public Rule MacroParameterValue(Var<MacroParameterNode> node)
    {
        return Sequence(
                OneOrMore(
                        TestNot(']'),
                        TestNot(MACRO_TAG_CLOSE_MARK),
                        TestNot('/'),
                        Nonspacechar()
                ), node.get().setValue(match())
        );
    }

    /**
     * Rule for a content of the XWiki macro syntax.
     *
     * @param node MacroNode variable
     */
    public Rule XWikiMacroContent(Var<MacroNode> node)
    {
        return Sequence(
                ZeroOrMore(
                        Sequence(TestNot(XWikiMacroCloseTag(node)), ANY)
                ), push(new TextNode(match())) && addAsChild()
        );
    }

    /**
     * Rule for a close tag of the XWiki macro syntax.
     * 
     * Example: <tt>{{/mymacro}}</tt>
     *
     * @param node MacroNode variable
     */
    @DontSkipActionsInPredicates
    public Rule XWikiMacroCloseTag(Var<MacroNode> node)
    {
        return Sequence(
                MACRO_TAG_OPEN_MARK,
                '/',
                Identifier(), match().equals(node.get().getMacroId()),
                Spn1(),
                MACRO_TAG_CLOSE_MARK
        );
    }

    /**
     * Rule for an alphanumeric identifier with dashes and underscores.
     */
    public Rule Identifier()
    {
        return OneOrMore(FirstOf(Alphanumeric(), '-', '_'));
    }
}

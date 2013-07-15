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
import org.parboiled.support.StringBuilderVar;
import org.pegdown.Parser;
import org.pegdown.plugins.BlockPluginParser;
import org.pegdown.plugins.InlinePluginParser;
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
    public XWikiPegdownPluginParser()
    {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }

    @Override
    public Rule[] blockPluginRules()
    {
        return new Rule[] { };
    }

    @Override
    public Rule[] inlinePluginRules()
    {
        return new Rule[] { Superscript(), Subscript() };
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
}

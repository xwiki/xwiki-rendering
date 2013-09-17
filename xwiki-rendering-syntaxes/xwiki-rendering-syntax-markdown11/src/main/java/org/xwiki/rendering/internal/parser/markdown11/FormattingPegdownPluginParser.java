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
import org.pegdown.Parser;
import org.pegdown.ast.TextNode;
import org.pegdown.plugins.InlinePluginParser;
import org.xwiki.rendering.internal.parser.markdown11.ast.SubscriptNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.SuperscriptNode;

/**
 * Implements a pluggable parser for Pegdown that provides superscript and
 * subscript syntax for XWiki.
 *
 * @version $Id $
 * @since 5.2RC1
 */
public class FormattingPegdownPluginParser extends Parser implements InlinePluginParser
{

    public FormattingPegdownPluginParser()
    {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }

    @Override
    public Rule[] inlinePluginRules()
    {
        return new Rule[] { Superscript(), Subscript() };
    }


    /**
     * Rule for a superscript formatting.
     * Example: <tt>This is in ^superscript^</tt>,
     *          <tt>This is in^superscript^</tt>
     */
    public Rule Superscript()
    {
        Rule marker = Ch('^');

        return NodeSequence(
            marker,
            push(new SuperscriptNode()),
            SuperscriptOrSubscript(marker),
            marker
        );
    }

    /**
     * Rule for a subscript formatting.
     * Example: <tt>This is in ~subscript~</tt>,
     *          <tt>This is in~subscript~</tt>
     */
    public Rule Subscript()
    {
        Rule marker = Ch('~');

        return NodeSequence(
            marker,
            push(new SubscriptNode()),
            SuperscriptOrSubscript(marker),
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
     */
    @Cached
    public Rule SuperscriptOrSubscript(Rule marker)
    {
        return OneOrMore(
            TestNot(marker),
            FirstOf(
                Sequence(
                    Ch('\\'),  //will not append escape char
                    Spacechar(), push(new TextNode(match())) && addAsChild()),
                Sequence(
                    Nonspacechar(), push(new TextNode(match())) && addAsChild()
                )
            )
        );
    }
}

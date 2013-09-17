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

import java.util.ArrayList;
import java.util.List;

import org.parboiled.Rule;
import org.parboiled.matchers.FirstOfMatcher;
import org.parboiled.matchers.Matcher;
import org.pegdown.Parser;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.plugins.InlinePluginParser;

/**
 * Pluggable parser for Pegdown that parsers an inline XHTML/XML.
 *
 * <p>This is intended to replace the Inline HTML feature of the Pegdown parser which is too lenient
 * and so very hard to escape correctly in renderer. XML syntax is more strict and so more suitable
 * for an inline (X)HTML in the Markdown syntax.</p>
 *
 * <p>There are some restrictions and one relaxation:</p>
 * <ul>
 *     <li>Only tags (and attributes) are supported.</li>
 *     <li>No new lines are permitted inside tags.</li>
 *     <li>An attribute value may not be quoted when it doesn't contain spaces or '>' characters.</li>
 * </ul>
 *
 * @version $Id $
 * @since 5.2RC1
 */
public class InlineXHtmlPegdownPluginParser extends Parser implements InlinePluginParser
{

    public InlineXHtmlPegdownPluginParser()
    {
        super(ALL, 1000l, DefaultParseRunnerProvider);
    }

    @Override
    public Rule[] inlinePluginRules()
    {
        // HideInlineHtmlRule must be after InlineXHtml and before InlineHtml
        return new Rule[]{ InlineXHtml(), HideInlineHtmlRule() };
    }

    /**
     * Rule for an inline XHTML/XML.
     */
    public Rule InlineXHtml()
    {
        return NodeSequence(
            XmlTag(), push(new InlineHtmlNode(match()))
        );
    }

    /**
     * Rule for a XML tag.
     * It must not contain new lines.
     */
    public Rule XmlTag()
    {
        return Sequence(
            '<', Optional('/'),
            OneOrMore(
                Alphanumeric()
            ),
            Sp(),
            ZeroOrMore(
                XmlAttribute(),
                Sp()
            ),
            Optional('/'), '>'
        );
    }

    /**
     * Rule for a XML attribute.
     * An attribute value may not be quoted when it doesn't contain spaces or '>' characters.
     */
    public Rule XmlAttribute()
    {
        return Sequence(
            OneOrMore(
                FirstOf(Alphanumeric(), '-', '_')
            ),
            Sp(), '=', Sp(),
            FirstOf(
                Quoted(),
                // Non-quoted value is not valid in XML, but we may not be so strict
                OneOrMore(
                    TestNot('>'),
                    Nonspacechar()
                )
            )
        );
    }

    /**
     * Rule that "hides" the {@link Parser#InlineHtml() InlineHtml} rule from the Pegdown parser.
     *
     * <p>This rule tests {@link Parser#HtmlTag() HtmlTag} against an input and when it succeeds,
     * then it tries all of the subrules from the {@link Parser#NonLinkInline() NonLinkInline}
     * except the {@code InlineHtml} rule. This means that the parser never reaches the
     * {@code InlineHtml} rule and an inline HTML is parsed as a normal text.</p>
     *
     * <p>Why so complicated? Parsing of an inline HTML cannot be simply disabled in Pegdown, only
     * suppressed, i.e. HTML is parsed and then dropped. The {@link Parser} class cannot be smoothly
     * subclassed (see <a href="https://github.com/sirthias/pegdown/issues/54"> #54</a>) so there's
     * no a straightforward way how to override the {@code InlineHtml} rule. Therefore this is
     * probably the most elegant way how to do it without patching the Pegdown parser.</p>
     */
    public Rule HideInlineHtmlRule()
    {
        FirstOfMatcher nonLinkInline = (FirstOfMatcher) NonLinkInline();
        List<Matcher> matchers = new ArrayList<Matcher>();

        // Copy all matchers but InlineHtml
        for (Matcher matcher : nonLinkInline.getChildren()) {
            if (! "InlineHtml".equals(matcher.getLabel())) {
                matchers.add(matcher);
            }
        }
        return NodeSequence(
            Test(InlineHtml()),
            // This is basically NonLinkInline rule but without InlineHtml
            FirstOf(matchers.toArray())
        );
    }
}

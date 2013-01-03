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
package org.xwiki.rendering.internal.parser.markdown;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Implements Pegdown Visitor's text events.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractTextPegdownVisitor extends AbstractPegdownVisitor
{
    /**
     * Id of the code macro.
     */
    private static final String CODE_MACRO_ID = "code";

    /**
     * Regex to recognize an HTML entity.
     */
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[^\\s]*;");

    /**
     * Abbreviation definitions.
     */
    private Map<String, AbbreviationNode> abbreviations = new HashMap<String, AbbreviationNode>();

    /**
     * Whether we're currently in the code that handles abbreviations or not. This is to prevent recursive abbreviation
     * handling.
     */
    private boolean isHandlingAbbreviations;

    @Override
    public void visit(ParaNode paraNode)
    {
        getListener().beginParagraph(Collections.EMPTY_MAP);
        visitChildren(paraNode);
        getListener().endParagraph(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(TextNode textNode)
    {
        boolean foundAbbreviation = false;
        if (!this.isHandlingAbbreviations) {
            for (Map.Entry<String, AbbreviationNode> abbreviationEntry : this.abbreviations.entrySet()) {
                int pos = textNode.getText().indexOf(abbreviationEntry.getKey());
                if (pos > -1) {
                    visit(new TextNode(textNode.getText().substring(0, pos)));

                    this.isHandlingAbbreviations = true;
                    String abbreviationDefinition = extractText(abbreviationEntry.getValue().getExpansion());
                    this.isHandlingAbbreviations = false;

                    String html;
                    if (StringUtils.isNotEmpty(abbreviationDefinition)) {
                        html = String.format("<abbr title=\"%s\">%s</abbr>", abbreviationDefinition,
                            abbreviationEntry.getKey());
                    } else {
                        html = String.format("<abbr>%s</abbr>", abbreviationEntry.getKey());
                    }
                    getListener().onRawText(html, Syntax.HTML_4_01);

                    visit(new TextNode(textNode.getText().substring(pos + abbreviationEntry.getKey().length())));
                    foundAbbreviation = true;
                    break;
                }
            }
        }

        if (!foundAbbreviation) {
            // Mardkown supports embedding HTML entities directly in the content. Thus we need to find them and replace
            // them with a RawBlock
            Matcher matcher = HTML_ENTITY_PATTERN.matcher(textNode.getText());
            if (matcher.find()) {
                if (matcher.start() > 0) {
                    visit(textNode.getText().substring(0, matcher.start()));
                }
                getListener().onRawText(matcher.group(), Syntax.HTML_4_01);
                if (matcher.end() < textNode.getText().length()) {
                    visit(new TextNode(textNode.getText().substring(matcher.end())));
                }
            } else {
                visit(textNode.getText());
            }
        }
    }

    /**
     * @param text the text to parse and for which to return XWiki events
     */
    private void visit(String text)
    {
        try {
            WrappingListener inlineListener = new InlineFilterListener();
            inlineListener.setWrappedListener(getListener());
            this.plainTextStreamParser.parse(new StringReader(text), inlineListener);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Error parsing content [%s]", text), e);
        }
    }

    @Override
    public void visit(SpecialTextNode specialTextNode)
    {
        visit(specialTextNode.getText());
    }

    @Override
    public void visit(CodeNode codeNode)
    {
        // Since XWiki doesn't have a Code Block we generate a Code Macro Block
        getListener().onMacro(CODE_MACRO_ID, Collections.EMPTY_MAP, codeNode.getText(), true);
    }

    @Override
    public void visit(VerbatimNode verbatimNode)
    {
        String text = StringUtils.removeEnd(verbatimNode.getText(), "\n");

        Map<String, String> parameters;
        if (verbatimNode.getType().length() > 0) {
            parameters = Collections.singletonMap("language", verbatimNode.getType());
        } else {
            parameters = Collections.EMPTY_MAP;
        }

        getListener().onMacro(CODE_MACRO_ID, parameters, text, false);
    }

    @Override
    public void visit(BlockQuoteNode blockQuoteNode)
    {
        getListener().beginQuotation(Collections.EMPTY_MAP);

        // XWiki only supports paragraph in quotations, see http://jira.xwiki.org/browse/XRENDERING-259.
        // We replace Paragraph events with QuotationLine events.
        QuoteListener quoteListener = new QuoteListener();
        quoteListener.setWrappedListener(getListener());
        this.listeners.push(quoteListener);
        visitChildren(blockQuoteNode);
        this.listeners.pop();
        quoteListener.closeOpenedQuotationLines();

        getListener().endQuotation(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(QuotedNode quotedNode)
    {
        // XWiki doesn't have a notion of Quote block and thus we can't store the quote in a manner independent of the
        // rendering... Thus in order to get the same kind of output as users would expect from Markdown we generate
        // "beautified" quotes in our AST. Ideally those should be get beautified by the renderer only.
        switch (quotedNode.getType()) {
            case DoubleAngle:
                visit("\u201C");
                visitChildren(quotedNode);
                visit("\u201D");
                break;
            case Single:
                visit("\u2018");
                visitChildren(quotedNode);
                visit("\u2019");
                break;
            case Double:
            default:
                visit("\u00AB");
                visitChildren(quotedNode);
                visit("\u00BB");
                break;
        }
    }

    @Override
    public void visit(SimpleNode simpleNode)
    {
        switch (simpleNode.getType()) {
            case Linebreak:
                getListener().onNewLine();
                break;
            case Apostrophe:
                visit("'");
                break;
            case HRule:
                getListener().onHorizontalLine(Collections.EMPTY_MAP);
                break;
            case Endash:
                visit("\u2013");
                break;
            case Emdash:
                visit("\u2014");
                break;
            case Ellipsis:
                visit("\u2026");
                break;
            case Nbsp:
            default:
                visit(" ");
                break;
        }
    }

    @Override
    public void visit(AbbreviationNode abbreviationNode)
    {
        // Since XWiki doesn't support abbreviations, we store abbreviation definitions in memory and when an
        // abbrevitation is used in the text we add the <abbr> HTML element around it.
        this.isHandlingAbbreviations = true;
        this.abbreviations.put(extractText(abbreviationNode), abbreviationNode);
        this.isHandlingAbbreviations = false;
    }
}

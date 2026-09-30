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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.rendering.renderer.printer.LookaheadWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * A Wiki printer that knows how to escape characters that would otherwise mean something different in XWiki wiki
 * syntax. For example if we have "**" as special symbols (and not as a Bold Format block) we need to escape them to
 * "~*~*" as otherwise they'd be considered bold after being rendered.
 *
 * @version $Id$
 * @since 1.7
 */
public class XWikiSyntaxEscapeWikiPrinter extends LookaheadWikiPrinter
{
    private static final Pattern VERBATIM_PATTERN = Pattern.compile("(\\{\\{\\{)|(\\}\\}\\})");

    private XWikiSyntaxListenerChain listenerChain;

    private XWikiSyntaxEscapeHandler escapeHandler;

    private boolean escapeLastChar;

    private Pattern escapeFirstIfMatching;

    private String lastPrinted;

    /**
     * @param printer the printer to send the escaped content to
     * @param listenerChain the listener chain, used by the escape handler to know in which kind of block the content
     *     is printed
     */
    public XWikiSyntaxEscapeWikiPrinter(WikiPrinter printer, XWikiSyntaxListenerChain listenerChain)
    {
        super(printer);

        this.escapeHandler = new XWikiSyntaxEscapeHandler();

        this.listenerChain = listenerChain;
    }

    /**
     * @return the handler that escapes the content printed through this printer
     */
    public XWikiSyntaxEscapeHandler getEscapeHandler()
    {
        return this.escapeHandler;
    }

    @Override
    protected void printInternal(String text)
    {
        super.printInternal(text);

        int length = text.length();

        if (length > 0) {
            this.escapeHandler.setOnNewLine(text.charAt(length - 1) == '\n');
        }

        this.lastPrinted = text;
    }

    @Override
    protected void printlnInternal(String text)
    {
        super.printlnInternal(text);

        this.escapeHandler.setOnNewLine(true);

        this.lastPrinted = "\n";
    }

    @Override
    public void flush()
    {
        if (!getBuffer().isEmpty()) {
            this.escapeHandler.escape(getBuffer(), this.listenerChain, this.escapeLastChar, this.escapeFirstIfMatching,
                this.lastPrinted);
            super.flush();
        }
        this.escapeLastChar = false;
        this.escapeFirstIfMatching = null;
    }

    /**
     * Prints the start of a bold format, escaping the "*" that follows it when it is at the start of a line and could
     * thus be parsed as a list item marker.
     */
    public void printBeginBold()
    {
        flush();

        boolean isOnNewLine = this.escapeHandler.isOnNewLine();

        print("**");

        if (isOnNewLine) {
            this.escapeFirstIfMatching = XWikiSyntaxEscapeHandler.STARLISTEND_PATTERN;
        }
    }

    /**
     * @param escapeLastChar {@code true} to escape the last character of the content that is currently buffered, to
     *     be used when the content printed next would otherwise combine with it into a syntax construct
     */
    public void setEscapeLastChar(boolean escapeLastChar)
    {
        this.escapeLastChar = escapeLastChar;
    }

    /**
     * @param onNewLine {@code true} if the content printed next starts at the beginning of a line
     */
    public void setOnNewLine(boolean onNewLine)
    {
        this.escapeHandler.setOnNewLine(onNewLine);
    }

    /**
     * @return {@code true} if the content printed next starts at the beginning of a line
     */
    public boolean isOnNewLine()
    {
        return this.escapeHandler.isOnNewLine();
    }

    /**
     * @return {@code true} if the last character printed is a white space or if nothing has been printed on the
     *     current line yet
     */
    public boolean isAfterWhiteSpace()
    {
        return isOnNewLine() || Character.isWhitespace(getLastPrinted().charAt(getLastPrinted().length() - 1));
    }

    /**
     * @return the last chunk of content that has been printed, or {@code null} if nothing has been printed yet
     */
    public String getLastPrinted()
    {
        return this.lastPrinted;
    }

    /**
     * Prints the start of an italic format, escaping a preceding ":" so that the result cannot be parsed as the "://"
     * of a URL.
     */
    public void printBeginItalic()
    {
        printItalicMarker();
    }

    /**
     * Prints the end of an italic format, escaping a preceding ":" so that the result cannot be parsed as the "://"
     * of a URL.
     */
    public void printEndItalic()
    {
        printItalicMarker();
    }

    private void printItalicMarker()
    {
        // If the lookahead buffer is not empty and the last character is ":" then we need to escape it
        // since otherwise we would get "://" which could be confused for a URL.
        if (!getBuffer().isEmpty() && getBuffer().charAt(getBuffer().length() - 1) == ':') {
            this.escapeLastChar = true;
        }

        print("//");
    }

    /**
     * Prints an already rendered inline macro, escaping a preceding "{" so that the result cannot be parsed as the
     * "{{{" of a verbatim block.
     *
     * @param xwikiSyntaxText the macro, already serialized into XWiki Syntax
     */
    public void printInlineMacro(String xwikiSyntaxText)
    {
        // If the lookahead buffer is not empty and the last character is "{" then we need to escape it
        // since otherwise we would get "{{{" which could be confused for a verbatim block.
        if (!getBuffer().isEmpty() && getBuffer().charAt(getBuffer().length() - 1) == '{') {
            this.escapeLastChar = true;
        }

        print(xwikiSyntaxText);
    }

    /**
     * Prints the content of a verbatim block, escaping the "{{{" and "}}}" it contains so that they cannot open or
     * close a verbatim block. Balanced pairs are left alone as the parser accepts nested verbatim blocks.
     *
     * @param verbatimContent the content of the verbatim block
     */
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:NPathComplexity", "checkstyle:JavaNCSS",
        "checkstyle:ExecutableStatementCount", "checkstyle:MultipleStringLiterals", "java:S3776"})
    public void printVerbatimContent(String verbatimContent)
    {
        StringBuffer result = new StringBuffer();

        Deque<StringBuffer> subVerbatimStack = new ArrayDeque<StringBuffer>();
        boolean printEndVerbatim = false;

        Matcher matcher = VERBATIM_PATTERN.matcher(verbatimContent);
        int currentIndex = 0;
        for (; matcher.find(); currentIndex = matcher.end()) {
            String before = verbatimContent.substring(currentIndex, matcher.start());

            if (printEndVerbatim) {
                if (before.startsWith("}")) {
                    result.append("~}~}~}");
                } else {
                    result.append("~}}}");
                }
            }

            if (subVerbatimStack.isEmpty()) {
                result.append(before);
            } else {
                subVerbatimStack.peek().append(before);
            }

            if (matcher.group(1) != null) {
                subVerbatimStack.push(new StringBuffer());
            } else {
                if (subVerbatimStack.isEmpty()) {
                    printEndVerbatim = true;
                } else {
                    StringBuffer subVerbatim = subVerbatimStack.pop();

                    if (subVerbatimStack.isEmpty()) {
                        result.append("{{{");
                        result.append(subVerbatim);
                        result.append("}}}");
                    } else {
                        subVerbatimStack.peek().append("{{{");
                        subVerbatimStack.peek().append(subVerbatim);
                        subVerbatimStack.peek().append("}}}");
                    }
                }
            }
        }

        if (currentIndex == 0) {
            print(verbatimContent);
            return;
        }

        String end = verbatimContent.substring(currentIndex);

        if (printEndVerbatim) {
            if (end.isEmpty() || end.charAt(0) == '}') {
                result.append("~}~}~}");
            } else {
                result.append("~}}}");
            }
        }

        if (!subVerbatimStack.isEmpty()) {
            // Append remaining string
            subVerbatimStack.peek().append(end);

            // Escape not closed verbatim blocks
            while (!subVerbatimStack.isEmpty()) {
                StringBuffer subVerbatim = subVerbatimStack.pop();

                if (subVerbatimStack.isEmpty()) {
                    if (!subVerbatim.isEmpty() && subVerbatim.charAt(0) == '{') {
                        result.append("~{~{~{");
                    } else {
                        result.append("~{{{");
                    }
                    result.append(subVerbatim);
                } else {
                    if (!subVerbatim.isEmpty() && subVerbatim.charAt(0) == '{') {
                        subVerbatimStack.peek().append("~{~{~{");
                    } else {
                        subVerbatimStack.peek().append("~{{{");
                    }
                    subVerbatimStack.peek().append(subVerbatim);
                }
            }
        } else {
            // Append remaining string
            result.append(end);
        }

        print(result.toString());
    }
}

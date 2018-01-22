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
package org.xwiki.rendering.wikimodel.xhtml.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlCharacter;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlCharacterType;
import org.xwiki.rendering.wikimodel.xhtml.handler.AbstractFormatTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.CommentHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;

/**
 * Provides context for the parsing.
 *
 * @version $Id$
 * @since 7.0RC1
 */
public class TagStack
{
    private static final String QUOTE_DEPTH = "quoteDepth";

    private static final String INSIDE_BLOCK_ELEMENT = "insideBlockElement";

    private static final String LIST_STYLES = "listStyles";

    private static final String DOCUMENT_PARENT = "documentParent";

    private final Map<String, TagHandler> fMap;

    private final CommentHandler fCommentHandler;

    private TagContext fPeek;

    private final Deque<WikiScannerContext> fScannerContext = new ArrayDeque<WikiScannerContext>();

    private final Deque<Map<String, Object>> fStackParameters = new ArrayDeque<Map<String, Object>>();

    private boolean fIgnoreElements;

    private int fEmptyLineCount;

    public TagStack(WikiScannerContext context, Map<String, TagHandler> handlers)
    {
        this(context, handlers, new CommentHandler());
    }

    public TagStack(WikiScannerContext context, Map<String, TagHandler> handlers, CommentHandler commentHandler)
    {
        fMap = handlers;
        fScannerContext.push(context);
        fCommentHandler = commentHandler;

        // init stack paramaters
        pushStackParameters();
    }

    public void beginElement(String name, WikiParameters params)
    {
        fPeek = new TagContext(fPeek, name, params, this);
        name = fPeek.getName();
        TagHandler handler = fMap.get(name);
        if (!shouldIgnoreElements()) {
            if (!(handler instanceof AbstractFormatTagHandler)) {
                //previousCharType = null;
            }

            fPeek.beginElement(handler);
        }
    }

    public void endElement()
    {
        boolean ignoreElements = shouldIgnoreElements();
        if (!ignoreElements) {
            fPeek.endElement();
        }
        fPeek = fPeek.getParentContext();
    }

    private XhtmlCharacterType getCharacterType(char ch, XhtmlCharacterType prevCharType, boolean isLast)
    {
        XhtmlCharacterType type = XhtmlCharacterType.CHARACTER;
        switch (ch) {
            case '!':
            case '\'':
            case '#':
            case '$':
            case '%':
            case '&':
            case '(':
            case ')':
            case '*':
            case '+':
            case ',':
            case '-':
            case '.':
            case '/':
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            case '{':
            case '|':
            case '}':
            case '~':
            case '\"':
                type = XhtmlCharacterType.SPECIAL_SYMBOL;
                break;
            case ' ':
            case '\t':
                type = XhtmlCharacterType.SPACE;
                break;
            case 160:
                // This is a &nbsp;
                // if previous character was a space as well, send it as a space, it will be rendered back in XHTML as a
                // space anyway. If it's in the middle of a word, it's a regular word character
                if (prevCharType == XhtmlCharacterType.SPACE || prevCharType == null || isLast) {
                    type = XhtmlCharacterType.SPACE;
                } else {
                    type = XhtmlCharacterType.CHARACTER;
                }
                break;
            case '\n':
            case '\r':
                type = XhtmlCharacterType.NEW_LINE;
                break;
            default:
                break;
        }
        return type;
    }

    public WikiScannerContext getScannerContext()
    {
        return fScannerContext.isEmpty() ? null : fScannerContext.peek();
    }

    public void setScannerContext(WikiScannerContext context)
    {
        if (!fScannerContext.isEmpty()) {
            fScannerContext.pop();
        }
        fScannerContext.push(context);
    }

    public void pushScannerContext(WikiScannerContext context)
    {
        fScannerContext.push(context);
    }

    public WikiScannerContext popScannerContext()
    {
        return fScannerContext.pop();
    }

    private void flushStack(Queue<XhtmlCharacter> stack)
    {
        while (!stack.isEmpty()) {
            XhtmlCharacter character = stack.poll();
            switch (character.getType()) {
                case ESCAPED:
                    getScannerContext().onEscape("" + character.getCharacter());
                    break;
                case SPECIAL_SYMBOL:
                    getScannerContext().onSpecialSymbol("" + character.getCharacter());
                    break;
                case NEW_LINE:
                    getScannerContext().onLineBreak();
                    break;
                case SPACE:
                    StringBuilder spaceBuffer = new StringBuilder(" ");
                    while (!stack.isEmpty() && (stack.element().getType() == XhtmlCharacterType.SPACE)) {
                        stack.poll();
                        spaceBuffer.append(' ');
                    }
                    getScannerContext().onSpace(spaceBuffer.toString());
                    break;
                default:
                    StringBuilder charBuffer = new StringBuilder();
                    charBuffer.append(character.getCharacter());
                    while (!stack.isEmpty() && (stack.element().getType() == XhtmlCharacterType.CHARACTER)) {
                        charBuffer.append(stack.poll().getCharacter());
                    }
                    getScannerContext().onWord(WikiPageUtil.escapeXmlString(charBuffer.toString()));
            }
        }
    }

    public void onCharacters(String content)
    {

        if (!fPeek.isContentContainer() || shouldIgnoreElements()) {
            return;
        }

        if (!fPeek.appendContent(content)) {
            Queue<XhtmlCharacter> stack = new ArrayDeque<XhtmlCharacter>();
            XhtmlCharacterType charType = null;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                charType = getCharacterType(c, charType, i == content.length() - 1);
                stack.offer(new XhtmlCharacter(c, charType));
            }

            // Now send the events.
            flushStack(stack);
        }
    }

    public void onComment(char[] array, int start, int length)
    {
        fCommentHandler.onComment(new String(array, start, length), this);
    }

    public void pushStackParameters()
    {
        fStackParameters.push(new HashMap<String, Object>());

        // Pre-initialize stack parameters for performance reason
        // (so that we don't have to check all the time if they're
        // initialized or not)
        setStackParameter(LIST_STYLES, new StringBuffer());
        setQuoteDepth(0);
        getStackParameters().put(INSIDE_BLOCK_ELEMENT, false);

        // Allow each handler to have some initialization
        for (TagHandler tagElementHandler : fMap.values()) {
            tagElementHandler.initialize(this);
        }
    }

    public void popStackParameters()
    {
        fStackParameters.pop();
    }

    private Map<String, Object> getStackParameters()
    {
        return fStackParameters.peek();
    }

    public void setStackParameter(String name, Object data)
    {
        Deque<Object> set = (Deque<Object>) getStackParameters().get(name);
        if (set != null && !set.isEmpty()) {
            set.pop();
        }
        pushStackParameter(name, data);
    }

    public Object getStackParameter(String name)
    {
        Deque<Object> set = (Deque<Object>) getStackParameters().get(name);
        return (set == null) ? null : set.peek();
    }

    @Deprecated
    public Object getStackParameter(String name, int index)
    {
        Deque<Object> set = (Deque<Object>) getStackParameters().get(name);
        if (set == null || set.size() <= index) {
            return null;
        }
        return set.toArray()[set.size() - index - 1];
    }

    public Iterator<Object> getStackParameterIterator(String name)
    {
        Deque<Object> set = (Deque<Object>) getStackParameters().get(name);
        return (set == null) ? null : set.descendingIterator();
    }

    public void pushStackParameter(String name, Object data)
    {
        Deque<Object> set = (Deque<Object>) getStackParameters().get(name);
        if (set == null) {
            getStackParameters().put(name, set = new LinkedList<Object>());
        }

        set.push(data);
    }

    public Object popStackParameter(String name)
    {
        return ((Deque<Object>) getStackParameters().get(name)).pop();
    }

    public void setQuoteDepth(int depth)
    {
        setStackParameter(QUOTE_DEPTH, depth);
    }

    public int getQuoteDepth()
    {
        return (int) getStackParameter(QUOTE_DEPTH);
    }

    public boolean isInsideBlockElement()
    {
        return (boolean) getStackParameters().get(INSIDE_BLOCK_ELEMENT);
    }

    public void setInsideBlockElement()
    {
        getStackParameters().put(INSIDE_BLOCK_ELEMENT, true);
    }

    public void unsetInsideBlockElement()
    {
        getStackParameters().put(INSIDE_BLOCK_ELEMENT, false);
    }

    public void setDocumentParent()
    {
        getStackParameters().put(DOCUMENT_PARENT, fPeek.getParent());
    }

    public TagContext getDocumentParent()
    {
        return (TagContext) getStackParameters().get(DOCUMENT_PARENT);
    }

    public String pushListStyle(char style)
    {
        StringBuffer listStyles = (StringBuffer) getStackParameter(LIST_STYLES);
        listStyles.append(style);
        return listStyles.toString();
    }

    public void popListStyle()
    {
        // We should always have a length greater than 0 but we handle
        // the case where the user has entered some badly formed HTML
        StringBuffer listStyles = (StringBuffer) getStackParameter(LIST_STYLES);
        if (listStyles.length() > 0) {
            listStyles.setLength(listStyles.length() - 1);
        }
    }

    public boolean isEndOfList()
    {
        return ((StringBuffer) getStackParameter(LIST_STYLES)).length() == 0;
    }

    public void resetEmptyLinesCount()
    {
        fEmptyLineCount = 0;
    }

    public void incrementEmptyLinesCount()
    {
        fEmptyLineCount += 1;
    }

    public int getEmptyLinesCount()
    {
        return fEmptyLineCount;
    }

    public boolean shouldIgnoreElements()
    {
        return fIgnoreElements;
    }

    public void setIgnoreElements()
    {
        fIgnoreElements = true;
    }

    public void unsetIgnoreElements()
    {
        fIgnoreElements = false;
    }
}

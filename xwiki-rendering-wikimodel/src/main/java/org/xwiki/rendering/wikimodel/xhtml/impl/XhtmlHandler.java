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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlCharacter;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlCharacterType;
import org.xwiki.rendering.wikimodel.xhtml.handler.BoldTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.BreakTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.CommentHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.DefinitionDescriptionTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.DefinitionTermTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.DivisionTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.HeaderTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.HorizontalLineTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.ImgTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.ItalicTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.ListItemTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.ListTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.ParagraphTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.PreserveTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.QuoteTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.ReferenceTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.SpanTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.StrikedOutTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.SubScriptTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.SuperScriptTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TableDataTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TableRowTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TableTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TeletypeTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.UnderlineTagHandler;

/**
 * @author kotelnikov
 * @author vmassol
 * @author thomas.mortagne
 */
public class XhtmlHandler extends DefaultHandler implements LexicalHandler
{
    public static class TagStack
    {
        public class TagContext
        {
            private final WikiParameters fParameters;

            private String fName;

            private StringBuffer fContent;

            public TagHandler fHandler;

            private final TagContext fParent;

            TagStack fTagStack;

            public TagContext(
                TagContext parent,
                String name,
                WikiParameters params,
                TagStack tagStack)
            {
                fName = name;
                fParent = parent;
                fParameters = params;
                fTagStack = tagStack;
            }

            public boolean appendContent(String content)
            {
                if (fHandler == null || !fHandler.isAccumulateContent()) {
                    return false;
                }
                if (fContent == null) {
                    fContent = new StringBuffer();
                }
                fContent.append(content);
                return true;
            }

            public void beginElement(TagHandler handler)
            {
                if (fParent == null) {
                    getScannerContext().beginDocument();
                }
                fHandler = handler;
                if (fHandler != null) {
                    fHandler.beginElement(this);
                }
            }

            public void endElement()
            {
                if (fHandler != null) {
                    fHandler.endElement(this);
                }
                if (fParent == null) {
                    getScannerContext().endDocument();
                }
            }

            public String getContent()
            {
                return fContent != null ? WikiPageUtil.escapeXmlString(fContent
                    .toString()) : "";
            }

            public String getName()
            {
                return fName;
            }

            public WikiParameters getParams()
            {
                return fParameters;
            }

            public TagContext getParent()
            {
                return fParent;
            }

            public WikiScannerContext getScannerContext()
            {
                return fScannerContext.isEmpty() ? null : fScannerContext
                    .peek();
            }

            public TagStack getTagStack()
            {
                return fTagStack;
            }

            public boolean isContentContainer()
            {
                return fHandler == null || fHandler.isContentContainer();
            }

            public boolean isTag(String string)
            {
                return string.equals(fName.toLowerCase());
            }
        }

        private Map<String, TagHandler> fMap = new HashMap<String, TagHandler>();

        private CommentHandler fCommentHandler;

        /**
         * Allow saving parameters. For example we save the number of br
         * elements if we're outside of a block element so that we can emit an
         * onEmptyLines event.
         */
        private Stack<Map<String, Object>> fStackParameters = new Stack<Map<String, Object>>();

        public void add(String tag, TagHandler handler)
        {
            fMap.put(tag, handler);
        }

        public void addAll(Map<String, TagHandler> handlers)
        {
            fMap.putAll(handlers);
        }

        public void setCommentHandler(CommentHandler handler)
        {
            fCommentHandler = handler;
        }

        private TagContext fPeek;

        private Stack<WikiScannerContext> fScannerContext = new Stack<WikiScannerContext>();

        public TagStack(WikiScannerContext context)
        {
            // init stack paramaters
            pushStackParameters();

            fScannerContext.push(context);
            fCommentHandler = new CommentHandler();
        }

        public void beginElement(String name, WikiParameters params)
        {
            fPeek = new TagContext(fPeek, name, params, this);
            name = fPeek.getName();
            TagHandler handler = fMap.get(name);
            boolean ignoreElements = (Boolean) getStackParameter("ignoreElements");
            if (!ignoreElements) {
                fPeek.beginElement(handler);
            }
        }

        public void endElement()
        {
            boolean ignoreElements = (Boolean) getStackParameter("ignoreElements");
            if (!ignoreElements) {
                fPeek.endElement();
            }
            fPeek = fPeek.fParent;
        }

        private XhtmlCharacterType getCharacterType(char ch)
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
                case 160: // This is a &nbsp;
                    type = XhtmlCharacterType.SPACE;
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
            if (fScannerContext.isEmpty()) {
                pushScannerContext(context);
            } else {
                fScannerContext.set(fScannerContext.size() - 1, context);
            }
        }

        public void pushScannerContext(WikiScannerContext context)
        {
            fScannerContext.push(context);
        }

        public WikiScannerContext popScannerContext()
        {
            return fScannerContext.pop();
        }

        private void flushStack(Stack<XhtmlCharacter> stack)
        {
            while (stack.size() > 0) {
                XhtmlCharacter character = stack.remove(0);
                switch (character.getType()) {
                    case ESCAPED:
                        getScannerContext().onEscape(
                            "" + character.getCharacter());
                        break;
                    case SPECIAL_SYMBOL:
                        getScannerContext().onSpecialSymbol(
                            "" + character.getCharacter());
                        break;
                    case NEW_LINE:
                        getScannerContext().onLineBreak();
                        break;
                    case SPACE:
                        StringBuffer spaceBuffer = new StringBuffer(" ");
                        while ((stack.size() > 0)
                            && (stack.firstElement().getType() == XhtmlCharacterType.SPACE))
                        {
                            stack.remove(0);
                            spaceBuffer.append(' ');
                        }
                        getScannerContext().onSpace(spaceBuffer.toString());
                        break;
                    default:
                        StringBuffer charBuffer = new StringBuffer();
                        charBuffer.append(character.getCharacter());
                        while ((stack.size() > 0)
                            && (stack.firstElement().getType() == XhtmlCharacterType.CHARACTER))
                        {
                            charBuffer.append(stack
                                .firstElement()
                                .getCharacter());
                            stack.remove(0);
                        }
                        getScannerContext()
                            .onWord(
                                WikiPageUtil.escapeXmlString(charBuffer
                                    .toString()));
                }
            }
        }

        public void onCharacters(String content)
        {

            if (!fPeek.isContentContainer()) {
                return;
            }
            boolean ignoreElements = (Boolean) getStackParameter("ignoreElements");
            if (ignoreElements) {
                return;
            }

            if (!fPeek.appendContent(content)) {
                Stack<XhtmlCharacter> stack = new Stack<XhtmlCharacter>();
                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    XhtmlCharacter current = new XhtmlCharacter(
                        c,
                        getCharacterType(c));
                    XhtmlCharacter result = current;
                    stack.push(result);
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
            setStackParameter("ignoreElements", false);
            setStackParameter("emptyLinesCount", 0);
            setStackParameter("listStyles", new StringBuffer());
            setStackParameter("quoteDepth", new Integer(0));
            setStackParameter("insideBlockElement", new Stack<Boolean>());

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
            Stack<Object> set = (Stack<Object>) getStackParameters().get(name);
            if (set == null || set.isEmpty()) {
                pushStackParameter(name, data);
            } else {
                set.setElementAt(data, set.size() - 1);
            }
        }

        public Object getStackParameter(String name)
        {
            Stack<Object> set = (Stack<Object>) getStackParameters().get(name);
            if (set == null || set.isEmpty()) {
                return null;
            } else {
                return set.peek();
            }
        }

        public Object getStackParameter(String name, int index)
        {
            Stack<Object> set = (Stack<Object>) getStackParameters().get(name);
            if (set == null || set.size() <= index) {
                return null;
            } else {
                return set.get(index);
            }
        }

        public void pushStackParameter(String name, Object data)
        {
            Stack<Object> set = (Stack<Object>) getStackParameters().get(name);
            if (set == null) {
                getStackParameters().put(name, set = new Stack<Object>());
            }

            set.push(data);
        }

        public Object popStackParameter(String name)
        {
            return ((Stack<Object>) getStackParameters().get(name)).pop();
        }
    }

    protected String fDocumentSectionUri;

    protected String fDocumentUri;

    protected String fDocumentWikiProperties;

    TagStack fStack;

    public XhtmlHandler(
        WikiScannerContext context,
        Map<String, TagHandler> extraHandlers)
    {
        this(context, extraHandlers, new CommentHandler());
    }

    /**
     * @param context
     */
    public XhtmlHandler(
        WikiScannerContext context,
        Map<String, TagHandler> extraHandlers,
        CommentHandler commentHandler)
    {
        fStack = new TagStack(context);
        fStack.setCommentHandler(commentHandler);

        // Register default handlers
        fStack.add("p", new ParagraphTagHandler());
        fStack.add("table", new TableTagHandler());
        fStack.add("tr", new TableRowTagHandler());
        TagHandler handler = new TableDataTagHandler();
        fStack.add("td", handler);
        fStack.add("th", handler);
        handler = new ListTagHandler();
        fStack.add("ul", handler);
        fStack.add("ol", handler);
        fStack.add("dl", handler);
        handler = new ListItemTagHandler();
        fStack.add("li", handler);
        fStack.add("dt", new DefinitionTermTagHandler());
        fStack.add("dd", new DefinitionDescriptionTagHandler());
        handler = new HeaderTagHandler();
        fStack.add("h1", handler);
        fStack.add("h2", handler);
        fStack.add("h3", handler);
        fStack.add("h4", handler);
        fStack.add("h5", handler);
        fStack.add("h6", handler);
        fStack.add("hr", new HorizontalLineTagHandler());
        fStack.add("pre", new PreserveTagHandler());
        handler = new ReferenceTagHandler();
        fStack.add("a", handler);
        handler = new ImgTagHandler();
        fStack.add("img", handler);
        handler = new BoldTagHandler();
        fStack.add("strong", handler);
        fStack.add("b", handler);
        handler = new UnderlineTagHandler();
        fStack.add("ins", handler);
        fStack.add("u", handler);
        handler = new StrikedOutTagHandler();
        fStack.add("del", handler);
        fStack.add("strike", handler);
        fStack.add("s", handler);
        handler = new ItalicTagHandler();
        fStack.add("em", handler);
        fStack.add("i", handler);
        fStack.add("sup", new SuperScriptTagHandler());
        fStack.add("sub", new SubScriptTagHandler());
        fStack.add("tt", new TeletypeTagHandler());
        fStack.add("br", new BreakTagHandler());
        fStack.add("div", new DivisionTagHandler());
        handler = new QuoteTagHandler();
        fStack.add("blockquote", handler);
        fStack.add("quote", handler);
        fStack.add("span", new SpanTagHandler());

        // Register extra handlers
        fStack.addAll(extraHandlers);

        // Allow each handler to have some initialization
        for (TagHandler tagElementHandler : fStack.fMap.values()) {
            tagElementHandler.initialize(fStack);
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] array, int start, int length)
        throws SAXException
    {
        fStack.onCharacters(new String(array, start, length));
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException
    {
        TagHandler.sendEmptyLines(fStack.fPeek);
        fStack.endElement();
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        fStack.endElement();
    }

    protected String getHref(Attributes attributes)
    {
        String value = attributes.getValue("HREF");
        if (value == null) {
            value = attributes.getValue("href");
        }
        if (value == null) {
            value = attributes.getValue("src");
        }
        if (value == null) {
            value = attributes.getValue("SRC");
        }
        return value;
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        fStack.beginElement(null, null);
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(
        String uri,
        String localName,
        String qName,
        Attributes attributes) throws SAXException
    {
        fStack.beginElement(
            getLocalName(uri, localName, qName, false),
            getParameters(attributes));
    }

    // Lexical handler methods

    public void comment(char[] array, int start, int length)
        throws SAXException
    {
        fStack.onComment(array, start, length);
    }

    public void endCDATA() throws SAXException
    {
        // Nothing to do
    }

    public void endDTD() throws SAXException
    {
        // Nothing to do
    }

    public void endEntity(String arg0) throws SAXException
    {
        // Nothing to do
    }

    public void startCDATA() throws SAXException
    {
        // Nothing to do
    }

    public void startDTD(String arg0, String arg1, String arg2)
        throws SAXException
    {
        // Nothing to do
    }

    public void startEntity(String arg0) throws SAXException
    {
        // Nothing to do
    }

    private String getLocalName(
        String uri,
        String localName,
        String name,
        boolean upperCase)
    {
        String result = (localName != null && !"".equals(localName))
            ? localName
            : name;
        return upperCase ? result.toUpperCase() : result;
    }

    private WikiParameters getParameters(Attributes attributes)
    {
        List<WikiParameter> params = new ArrayList<WikiParameter>();
        for (int i = 0; i < attributes.getLength(); i++) {
            String key = getLocalName(attributes.getURI(i), attributes
                .getQName(i), attributes.getLocalName(i), false);
            String value = attributes.getValue(i);
            WikiParameter param = new WikiParameter(key, value);

            // The XHTML DTD specifies some default value for some attributes.
            // For example for a TD element
            // it defines colspan=1 and rowspan=1. Thus we'll get a colspan and
            // rowspan attribute passed to
            // the current method even though they are not defined in the source
            // XHTML content.
            // However with SAX2 it's possible to check if an attribute is
            // defined in the source or not using
            // the Attributes2 class.
            // See
            // http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html#package_description
            if (attributes instanceof Attributes2) {
                Attributes2 attributes2 = (Attributes2) attributes;
                // If the attribute is present in the XHTML source file then add
                // it, otherwise skip it.
                if (attributes2.isSpecified(i)) {
                    params.add(param);
                }
            } else {
                params.add(param);
            }
        }
        return new WikiParameters(params);
    }
}

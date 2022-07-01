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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.handler.BlockTagHandler;
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
 * SAX2 event and extension handler to parse XHTML into wikimodel events
 *
 * @version $Id$
 * @since 4.0M1
 */
public class XhtmlHandler extends DefaultHandler implements LexicalHandler
{
    private TagStack fStack;

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
        Map<String, TagHandler> handlers = new HashMap<>();

        // Prepare default handlers
        handlers.put("p", new ParagraphTagHandler());
        handlers.put("table", new TableTagHandler());
        handlers.put("tr", new TableRowTagHandler());
        TagHandler handler = new TableDataTagHandler();
        handlers.put("td", handler);
        handlers.put("th", handler);
        handler = new ListTagHandler();
        handlers.put("ul", handler);
        handlers.put("ol", handler);
        handlers.put("dl", handler);
        handler = new ListItemTagHandler();
        handlers.put("li", handler);
        handlers.put("dt", new DefinitionTermTagHandler());
        handlers.put("dd", new DefinitionDescriptionTagHandler());
        handler = new HeaderTagHandler();
        handlers.put("h1", handler);
        handlers.put("h2", handler);
        handlers.put("h3", handler);
        handlers.put("h4", handler);
        handlers.put("h5", handler);
        handlers.put("h6", handler);
        handlers.put("hr", new HorizontalLineTagHandler());
        handlers.put("pre", new PreserveTagHandler());
        handlers.put("a", new ReferenceTagHandler());
        handlers.put("img", new ImgTagHandler());
        handler = new BoldTagHandler();
        handlers.put("strong", handler);
        handlers.put("b", handler);
        handler = new UnderlineTagHandler();
        handlers.put("ins", handler);
        handlers.put("u", handler);
        handler = new StrikedOutTagHandler();
        handlers.put("del", handler);
        handlers.put("strike", handler);
        handlers.put("s", handler);
        handler = new ItalicTagHandler();
        handlers.put("em", handler);
        handlers.put("i", handler);
        handlers.put("sup", new SuperScriptTagHandler());
        handlers.put("sub", new SubScriptTagHandler());
        handlers.put("tt", new TeletypeTagHandler());
        handlers.put("br", new BreakTagHandler());
        handlers.put("div", new DivisionTagHandler());
        handler = new QuoteTagHandler();
        handlers.put("blockquote", handler);
        handlers.put("quote", handler);
        handlers.put("span", new SpanTagHandler());

        handler = extraHandlers.get("div");
        if (handler != null) {
            handler = new BlockTagHandler(((BlockTagHandler) handler).getDocumentClass());
        } else {
            handler = new BlockTagHandler();
        }

        // Basic handling of HTML5 block tags
        // There is no intend here to provide real HTML5 support which deserve its own independent parser,
        // but only to handle some HTML5 tags like divs to avoid potentially unexpected merging of separate text nodes.
        handlers.put("aside", handler);
        handlers.put("section", handler);
        handlers.put("article", handler);
        handlers.put("main", handler);
        handlers.put("nav", handler);
        handlers.put("details", handler);
        handlers.put("summary", handler);
        handlers.put("figure", handler);
        handlers.put("figcaption", handler);
        handlers.put("header", handler);
        handlers.put("footer", handler);

        // Prepare extra handlers
        handlers.putAll(extraHandlers);

        // Initialize the TagStack and handlers
        fStack = new TagStack(context, handlers, commentHandler);
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
        TagHandler.sendEmptyLines(fStack);
        fStack.endElement();
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        fStack.endElement();
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
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *     org.xml.sax.Attributes)
     */
    @Override
    public void startElement(
        String uri,
        String localName,
        String qName,
        Attributes attributes) throws SAXException
    {
        fStack.beginElement(
            getLocalName(localName, qName, false),
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
        Set<String> keys = new HashSet<>();
        List<WikiParameter> params = new ArrayList<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            String key = getLocalName(attributes.getQName(i), attributes.getLocalName(i), false);
            keys.add(key);
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

        List<WikiParameter> translatedParameters = params.stream()
            // Remove prefixed attributes that also exist as non-prefixed version.
            .filter(param -> {
                // Kep all attributes without prefix.
                boolean keep = !param.getKey().startsWith(XHTMLWikiPrinter.TRANSLATED_ATTRIBUTE_PREFIX);
                if (!keep) {
                    String translatedKey =
                        param.getKey().substring(XHTMLWikiPrinter.TRANSLATED_ATTRIBUTE_PREFIX.length());
                    keep = !keys.contains(translatedKey);
                }
                return keep;
            })
            // Remove prefix.
            .map(param -> {
                if (param.getKey().startsWith(XHTMLWikiPrinter.TRANSLATED_ATTRIBUTE_PREFIX)) {
                    String translatedKey =
                        param.getKey().substring(XHTMLWikiPrinter.TRANSLATED_ATTRIBUTE_PREFIX.length());
                    return new WikiParameter(translatedKey, param.getValue());
                } else {
                    return param;
                }
            })
            .collect(Collectors.toList());
        return new WikiParameters(translatedParameters);
    }
}

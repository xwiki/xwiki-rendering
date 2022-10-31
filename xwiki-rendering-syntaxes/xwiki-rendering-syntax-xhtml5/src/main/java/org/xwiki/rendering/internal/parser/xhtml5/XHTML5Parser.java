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
package org.xwiki.rendering.internal.parser.xhtml5;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.xhtml.XHTMLParser;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiCommentHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiDivTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiHeaderTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiImageTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiReferenceTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiTableDataTagHandler;
import org.xwiki.rendering.internal.parser.xhtml5.wikimodel.XHTML5SpanTagHandler;
import org.xwiki.rendering.internal.parser.xhtml5.wikimodel.XWikiFigcaptionTagHandler;
import org.xwiki.rendering.internal.parser.xhtml5.wikimodel.XWikiFigureTagHandler;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlParser;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.stability.Unstable;
import org.xwiki.xml.XMLReaderFactory;

import static org.xwiki.rendering.internal.xhtml5.XHTML5SyntaxProvider.XHTML_5;

/**
 * This is an HTML5 parser that expects valid XML as input, i.e., doesn't run HTMLCleaner.
 *
 * @version $Id$
 * @since 14.1RC1
 */
@Component
@Named("xhtml/5")
@Unstable
public class XHTML5Parser extends XHTMLParser
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    @Named("xhtmlmarker")
    private ResourceReferenceParser xhtmlMarkerResourceReferenceParser;

    /**
     * A special factory that create foolproof XML reader that have the following characteristics:
     * <ul>
     * <li>Use DTD caching when the underlying XML parser is Xerces</li>
     * <li>Ignore SAX callbacks when the parser parses the DTD</li>
     * <li>Accumulate onCharacters() calls since SAX parser may normally call this event several times.</li>
     * <li>Remove non-semantic white spaces where needed</li>
     * <li>Resolve DTDs locally to speed DTD loading/validation</li>
     * </ul>
     */
    @Inject
    @Named("xwiki")
    private XMLReaderFactory xmlReaderFactory;

    @Override
    public Syntax getSyntax()
    {
        return XHTML_5;
    }

    @Override
    public IWikiParser createWikiModelParser() throws ParseException
    {
        // Override some of the WikiModel XHTML parser tag handlers to introduce our own logic.
        Map<String, TagHandler> handlers = new HashMap<>();
        TagHandler handler = new XWikiHeaderTagHandler();
        handlers.put("h1", handler);
        handlers.put("h2", handler);
        handlers.put("h3", handler);
        handlers.put("h4", handler);
        handlers.put("h5", handler);
        handlers.put("h6", handler);
        handlers.put("a", new XWikiReferenceTagHandler(this));
        handlers.put("img", new XWikiImageTagHandler());
        handlers.put("span", new XHTML5SpanTagHandler(this.componentManager, this));
        // Change the class value indicating that the division is an embedded document. We do this in order to be
        // independent of WikiModel in what we expose to the outside world. Thus if one day we need to change to
        // another implementation we won't be tied to WikiModel.
        TagHandler divHandler = new XWikiDivTagHandler("xwiki-document", this.componentManager, this);
        handlers.put("div", divHandler);
        handlers.put("th", new XWikiTableDataTagHandler());

        handlers.put("figure", new XWikiFigureTagHandler());
        handlers.put("figcaption", new XWikiFigcaptionTagHandler());

        XhtmlParser parser = new XhtmlParser();
        parser.setExtraHandlers(handlers);
        parser.setCommentHandler(
            new XWikiCommentHandler(this.componentManager, this, this.xhtmlMarkerResourceReferenceParser));

        // Construct our own XML filter chain since we want to use our own Comment filter.
        try {
            parser.setXmlReader(this.xmlReaderFactory.createXMLReader());
        } catch (Exception e) {
            throw new ParseException("Failed to create XML reader", e);
        }

        return parser;
    }
}

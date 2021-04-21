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
package org.xwiki.rendering.internal.parser.xhtml;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.wikimodel.AbstractWikiModelParser;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiCommentHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiDivTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiHeaderTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiImageTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiReferenceTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiSpanTagHandler;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiTableDataTagHandler;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.xhtml.XhtmlParser;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;
import org.xwiki.xml.XMLReaderFactory;

import static org.xwiki.rendering.internal.xhtml.XHTML10SyntaxProvider.XHTML_1_0;

/**
 * Parses XHTML and generate a {@link org.xwiki.rendering.block.XDOM} object.
 *
 * @version $Id$
 * @since 1.5M2
 */
@Component
@Named("xhtml/1.0")
@Singleton
public class XHTMLParser extends AbstractWikiModelParser
{
    /**
     * The parser used for the link label parsing. For (x)html parsing, this will be an xwiki 2.0 parser, since it's
     * more convenient to pass link labels in xwiki syntax. See referred resource for more details.
     *
     * @see XWikiCommentHandler#handleLinkCommentStop(TagStack)
     */
    @Inject
    @Named("xdom+xml/current")
    private StreamParser xmlParser;

    @Inject
    @Named("xdom+xml/current")
    private PrintRendererFactory xmlRenderer;

    /**
     * @see #getLinkReferenceParser()
     */
    @Inject
    @Named("link")
    private ResourceReferenceParser linkReferenceParser;

    /**
     * @see #getImageReferenceParser()
     */
    @Inject
    @Named("image")
    private ResourceReferenceParser imageReferenceParser;

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
        return XHTML_1_0;
    }

    @Override
    public StreamParser getLinkLabelParser()
    {
        return this.xmlParser;
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
        handlers.put("a", new XWikiReferenceTagHandler(this, this.xmlRenderer));
        handlers.put("img", new XWikiImageTagHandler());
        handlers.put("span", new XWikiSpanTagHandler(this.componentManager, this));
        // Change the class value indicating that the division is an embedded document. We do this in order to be
        // independent of WikiModel in what we expose to the outside world. Thus if one day we need to change to
        // another implementation we won't be tied to WikiModel.
        handlers.put("div", new XWikiDivTagHandler("xwiki-document", this.componentManager, this));
        handlers.put("th", new XWikiTableDataTagHandler());

        XhtmlParser parser = new XhtmlParser();
        parser.setExtraHandlers(handlers);
        parser.setCommentHandler(new XWikiCommentHandler(this.componentManager, this,
            this.xmlRenderer, this.xhtmlMarkerResourceReferenceParser));

        // Construct our own XML filter chain since we want to use our own Comment filter.
        try {
            parser.setXmlReader(this.xmlReaderFactory.createXMLReader());
        } catch (Exception e) {
            throw new ParseException("Failed to create XML reader", e);
        }

        return parser;
    }

    @Override
    public ResourceReferenceParser getLinkReferenceParser()
    {
        return this.linkReferenceParser;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.rendering.internal.parser.wikimodel.AbstractWikiModelParser#getImageReferenceParser()
     * @since 2.5RC1
     */
    @Override
    public ResourceReferenceParser getImageReferenceParser()
    {
        return this.imageReferenceParser;
    }

    @Override
    public XWikiGeneratorListener createXWikiGeneratorListener(Listener listener, IdGenerator idGenerator)
    {
        return new XHTMLXWikiGeneratorListener(getLinkLabelParser(), listener, getLinkReferenceParser(),
            getImageReferenceParser(), this.plainRendererFactory, idGenerator, getSyntax());
    }

    @Override
    public XDOM parse(Reader source) throws ParseException
    {
        Reader pushBackReader = getPushBackReader(source);
        if (pushBackReader != null) {
            return super.parse(pushBackReader);
        } else {
            return new XDOM(Collections.emptyList());
        }
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        Reader pushBackReader = getPushBackReader(source);
        if (pushBackReader != null) {
            super.parse(pushBackReader, listener);
        }
    }

    @Override
    protected void parse(Reader source, Listener listener, IdGenerator idGenerator) throws ParseException
    {
        Reader pushBackReader = getPushBackReader(source);
        if (pushBackReader != null) {
            super.parse(pushBackReader, listener, idGenerator);
        }
    }

    /**
     * In order to handle empty content we use a {@link PushbackReader} to try to read one character from the stream
     * and if we get -1 it means that the stream is empty and in this case we return an empty XDOM.
     */
    private Reader getPushBackReader(Reader source) throws ParseException
    {
        PushbackReader pushbackReader = new PushbackReader(source);
        int c;
        try {
            c = pushbackReader.read();
            if (c == -1) {
                return null;
            }
            pushbackReader.unread(c);
        } catch (IOException e) {
            throw new ParseException("Failed to find out if the source to parse is empty or not", e);
        }
        return  pushbackReader;
    }
}

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
package org.xwiki.rendering.wikimodel.xhtml;

import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.rendering.wikimodel.IWemListener;
import org.xwiki.rendering.wikimodel.IWikiParser;
import org.xwiki.rendering.wikimodel.WikiParserException;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.filter.AccumulationXMLFilter;
import org.xwiki.rendering.wikimodel.xhtml.filter.DTDXMLFilter;
import org.xwiki.rendering.wikimodel.xhtml.filter.XHTMLWhitespaceXMLFilter;
import org.xwiki.rendering.wikimodel.xhtml.handler.CommentHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler;
import org.xwiki.xml.internal.LocalEntityResolver;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XhtmlParser implements IWikiParser
{
    private Map<String, TagHandler> fExtraHandlers;

    private CommentHandler fCommentHandler;

    /**
     * Optional XML Reader that can be specified. This is the solution for
     * setting up custom XML filters.
     */
    private XMLReader fXmlReader;

    private boolean namespacesEnabled = true;

    public XhtmlParser()
    {
        fExtraHandlers = Collections.<String, TagHandler>emptyMap();
        fCommentHandler = new CommentHandler();
    }

    public boolean isNamespacesEnabled()
    {
        return this.namespacesEnabled;
    }

    public void setNamespacesEnabled(boolean namespacesEnabled)
    {
        this.namespacesEnabled = namespacesEnabled;
    }

    public void setExtraHandlers(Map<String, TagHandler> extraHandlers)
    {
        fExtraHandlers = extraHandlers;
    }

    public void setCommentHandler(CommentHandler commentHandler)
    {
        fCommentHandler = commentHandler;
    }

    public void setXmlReader(XMLReader xmlReader)
    {
        fXmlReader = xmlReader;
    }

    /**
     * @param listener the listener object wich will be used to report about all
     * structural elements on the wiki page.
     * @return a XHTML SAX handler wich can be used to generate well-formed
     *         sequence of WEM events; all events will be reported to the given
     *         listener object.
     */
    public DefaultHandler getHandler(IWemListener listener)
    {
        WikiScannerContext context = new WikiScannerContext(listener);
        XhtmlHandler handler = new XhtmlHandler(
            context,
            fExtraHandlers,
            fCommentHandler);
        return handler;
    }

    /**
     * @see org.xwiki.rendering.wikimodel.IWikiParser#parse(java.io.Reader,
     *      org.xwiki.rendering.wikimodel.IWemListener)
     */
    public void parse(Reader reader, IWemListener listener)
        throws WikiParserException
    {
        try {
            XMLReader xmlReader = getXMLReader();

            // The WikiModel-specific handler
            DefaultHandler handler = getHandler(listener);

            xmlReader
                .setFeature("http://xml.org/sax/features/namespaces", isNamespacesEnabled());
            xmlReader.setEntityResolver(new LocalEntityResolver());
            xmlReader.setContentHandler(handler);
            xmlReader.setProperty(
                "http://xml.org/sax/properties/lexical-handler",
                handler);

            InputSource source = new InputSource(reader);
            xmlReader.parse(source);
        } catch (Exception e) {
            throw new WikiParserException(e);
        }
    }

    private XMLReader getXMLReader() throws Exception
    {
        XMLReader reader;

        if (fXmlReader != null) {
            reader = fXmlReader;
        } else {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();

            // Instructs the implementation to process XML securely.
            try {
                parserFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
                // A really old parser is being used? Ignore the problem and continue.
            }

            SAXParser parser = parserFactory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();

            // Ignore SAX callbacks when the parser parses the DTD
            DTDXMLFilter dtdFilter = new DTDXMLFilter(xmlReader);

            // Add a XML Filter to accumulate onCharacters() calls since SAX
            // parser may call it several times.
            AccumulationXMLFilter accumulationFilter = new AccumulationXMLFilter(
                dtdFilter);

            // Add a XML Filter to remove non-semantic white spaces. We need to
            // do that since all WikiModel
            // events contain only semantic information.
            XHTMLWhitespaceXMLFilter whitespaceFilter = new XHTMLWhitespaceXMLFilter(
                accumulationFilter);

            reader = whitespaceFilter;
        }

        return reader;
    }
}

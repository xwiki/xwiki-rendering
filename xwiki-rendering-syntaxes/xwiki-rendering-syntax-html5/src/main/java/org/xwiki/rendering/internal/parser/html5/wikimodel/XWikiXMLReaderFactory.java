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
package org.xwiki.rendering.internal.parser.html5.wikimodel;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.wikimodel.xhtml.filter.AccumulationXMLFilter;
import org.xwiki.rendering.wikimodel.xhtml.filter.DTDXMLFilter;
import org.xwiki.xml.EntityResolver;
import org.xwiki.xml.XMLReaderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Creates XML Readers that have the following characteristics:
 * <ul>
 * <li>Use DTD caching when the underlying XML parser is Xerces</li>
 * <li>Ignore SAX callbacks when the parser parses the DTD</li>
 * <li>Accumulate onCharacters() calls since SAX parser may normally call this event several times.</li>
 * <li>Remove non-semantic white spaces where needed</li>
 * <li>Resolve DTDs locally to speed DTD loading/validation</li>
 * </ul>
 * 
 * @version $Id$
 * @since 2.1RC1
 */
@Component
@Named("xwiki")
@Singleton
public class XWikiXMLReaderFactory implements XMLReaderFactory
{
    /**
     * Used to create an optimized SAX XML Reader. In general SAX parsers don't cache DTD grammars and as a consequence
     * parsing a document with a grammar such as the XHTML DTD takes a lot more time than required.
     */
    @Inject
    private XMLReaderFactory xmlReaderFactory;

    /**
     * In order to speed up DTD loading/validation we use an entity resolver that can resolve DTDs locally.
     */
    @Inject
    protected EntityResolver entityResolver;

    @Override
    public XMLReader createXMLReader() throws SAXException, ParserConfigurationException
    {
        XMLReader xmlReader;

        try {
            // Use a performant XML Reader (which does DTD caching for Xerces)
            XMLReader xr = this.xmlReaderFactory.createXMLReader();

            // Ignore SAX callbacks when the parser parses the DTD
            DTDXMLFilter dtdFilter = new DTDXMLFilter(xr);

            // Add a XML Filter to accumulate onCharacters() calls since SAX
            // parser may call it several times.
            AccumulationXMLFilter accumulationFilter = new AccumulationXMLFilter(dtdFilter);

            // Add a XML Filter to remove non-semantic white spaces. We need to do that since all WikiModel
            // events contain only semantic information.
            XWikiXHTMLWhitespaceXMLFilter whitespaceFilter = new XWikiXHTMLWhitespaceXMLFilter(accumulationFilter);

            whitespaceFilter.setEntityResolver(this.entityResolver);

            xmlReader = whitespaceFilter;
        } catch (Exception e) {
            throw new SAXException("Failed to create XML reader", e);
        }

        return xmlReader;
    }
}

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
package org.xwiki.rendering.internal.renderer.html5;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.xml.html.HTMLElementSanitizer;

/**
 * Convert listener events to HTML5.
 *
 * @version $Id$
 * @since 6.4M3
 */
public class HTML5ChainingRenderer extends XHTMLChainingRenderer
{
    private static final String ELEM_SPAN = "span";

    private static final String ELEM_PRE = "pre";

    private static final String PROP_CLASS = "class";

    private static final String FIGURE_TAG = "figure";

    private static final String FIGURE_CAPTION_TAG = "figcaption";

    /**
     * @param linkRenderer the object to render link events into XHTML. This is done so that it's pluggable because link
     * rendering depends on how the underlying system wants to handle it. For example for XWiki we check if the document
     * exists, we get the document URL, etc.
     * @param imageRenderer the object to render image events into XHTML. This is done so that it's pluggable because
     * image rendering depends on how the underlying system wants to handle it. For example for XWiki we check if the
     * image exists as a document attachments, we get its URL, etc.
     * @param htmlElementSanitizer the sanitizer to use for sanitizing HTML elements and attributes
     * @param listenerChain the chain of listener filters used to compute various states
     */
    public HTML5ChainingRenderer(XHTMLLinkRenderer linkRenderer,
            XHTMLImageRenderer imageRenderer, HTMLElementSanitizer htmlElementSanitizer,
            ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, htmlElementSanitizer, listenerChain);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        // Right now, the only difference with the super class is about the "monospace" format
        if (format == Format.MONOSPACE) {
            Map<String, String> attributes = new LinkedHashMap<>(parameters);
            String cssClass = "monospace";
            // The element may already have a class
            if (attributes.containsKey(PROP_CLASS)) {
                cssClass = String.format("%s %s", cssClass, attributes.get(PROP_CLASS));
            }
            attributes.put(PROP_CLASS, cssClass);
            getXHTMLWikiPrinter().printXMLStartElement(ELEM_SPAN, attributes);
        } else {
            // Call the super class
            super.beginFormat(format, parameters);
        }

    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        if (!parameters.isEmpty()) {
            getXHTMLWikiPrinter().printXMLEndElement(ELEM_SPAN);
        }
        // Right now, the only difference with the super class is about the "monospace" format
        if (format == Format.MONOSPACE) {
            if (parameters.isEmpty()) {
                // if the parameters are not empty, the span element has already been closed
                getXHTMLWikiPrinter().printXMLEndElement(ELEM_SPAN);
            }
        } else {
            // Call the super class, with an empty parameters map to avoid closing the span element twice
            super.endFormat(format, new HashMap<String, String>());
        }
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (inline) {
            // Note: We generate a span element rather than a pre element since pre elements cannot be located inside
            // paragraphs for example.
            // The class is what is expected by wikimodel to understand the span as meaning a verbatim and not a
            // Monospace element.
            getXHTMLWikiPrinter().printXMLStartElement(ELEM_SPAN,
                new String[][] { { PROP_CLASS, "wikimodel-verbatim" } });
            getXHTMLWikiPrinter().printXML(content);
            getXHTMLWikiPrinter().printXMLEndElement(ELEM_SPAN);
        } else {
            getXHTMLWikiPrinter().printXMLStartElement(ELEM_PRE, parameters);
            getXHTMLWikiPrinter().printXML(content);
            getXHTMLWikiPrinter().printXMLEndElement(ELEM_PRE);
        }
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement(FIGURE_TAG, parameters);
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLStartElement(FIGURE_CAPTION_TAG, parameters);
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement(FIGURE_TAG);
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        getXHTMLWikiPrinter().printXMLEndElement(FIGURE_CAPTION_TAG);
    }
}

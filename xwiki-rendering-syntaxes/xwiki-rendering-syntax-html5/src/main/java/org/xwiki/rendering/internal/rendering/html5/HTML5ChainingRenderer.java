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
package org.xwiki.rendering.internal.rendering.html5;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Convert listener events to HTML5.
 *
 * @version $Id$
 * @since 6.4M3
 */
public class HTML5ChainingRenderer extends XHTMLChainingRenderer
{
    private static final String ELEM_STRONG = "strong";

    private static final String ELEM_EM = "em";

    private static final String ELEM_DEL = "del";

    private static final String ELEM_INS = "ins";

    private static final String ELEM_SUP = "sup";

    private static final String ELEM_SUB = "sub";

    private static final String ELEM_SPAN = "span";

    /**
     * @param linkRenderer the object to render link events into XHTML. This is done so that it's pluggable because link
     * rendering depends on how the underlying system wants to handle it. For example for XWiki we check if the document
     * exists, we get the document URL, etc.
     * @param imageRenderer the object to render image events into XHTML. This is done so that it's pluggable because
     * image rendering depends on how the underlying system wants to handle it. For example for XWiki we check if the
     * image exists as a document attachments, we get its URL, etc.
     * @param listenerChain the chain of listener filters used to compute various states
     */
    public HTML5ChainingRenderer(XHTMLLinkRenderer linkRenderer,
            XHTMLImageRenderer imageRenderer,
            ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, listenerChain);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        // Right now, the only difference with the super class is about the "monospace" format
        if (format == Format.MONOSPACE) {
            Map<String, String> attributes = new HashMap<>();
            attributes.putAll(parameters);
            attributes.put("class", "monospace");
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
            getXHTMLWikiPrinter().printXMLEndElement(ELEM_SPAN);
        } else {
            // Call the super class, with an empty parameters map to avoid closing the span element twice
            super.endFormat(format, new HashMap<String, String>());
        }
    }
}

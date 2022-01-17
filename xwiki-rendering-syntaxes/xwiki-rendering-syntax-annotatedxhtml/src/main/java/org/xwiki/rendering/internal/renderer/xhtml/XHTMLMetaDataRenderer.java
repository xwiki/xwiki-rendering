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
package org.xwiki.rendering.internal.renderer.xhtml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;

/**
 * Renders metadata into Annotated XHTML, i.e., a div or span with the metadata as attributes.
 *
 * @version $Id$
 * @since 14.0RC1
 */
public class XHTMLMetaDataRenderer
{
    /**
     * @return a span element if we are inside an inline macro. Else a div.
     */
    private String getMetadataContainerElement(boolean inline)
    {
        if (inline) {
            return "span";
        } else {
            return "div";
        }
    }

    /**
     * Render the open tag of the metadata.
     *
     * @param printer The output printer.
     * @param inline If the current context is an inline context, i.e., only phrasing content is allowed.
     * @param metaData The metadata to render.
     */
    public void beginRender(XHTMLWikiPrinter printer, boolean inline, MetaData metaData)
    {
        Map<String, String> attributes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> metadataPair : metaData.getMetaData().entrySet()) {
            attributes.put(XHTMLXWikiGeneratorListener.METADATA_ATTRIBUTE_PREFIX + metadataPair.getKey(),
                metadataPair.getValue().toString());
        }

        attributes.put("class", XHTMLXWikiGeneratorListener.METADATA_CONTAINER_CLASS);

        printer.printXMLStartElement(getMetadataContainerElement(inline), attributes);
    }

    /**
     * Render the closing tag of the metadata.
     *
     * @param printer The output print.
     * @param inline If the current context is an inline context, i.e., only phrasing content is allowed.
     */
    public void endRender(XHTMLWikiPrinter printer, boolean inline)
    {
        printer.printXMLEndElement(getMetadataContainerElement(inline));
    }
}

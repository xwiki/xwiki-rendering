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
package org.xwiki.rendering.internal.macro.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XHTMLXWikiGeneratorListener;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Renderer that generates XHTML from a XDOM resulting from the parsing of text containing HTML mixed with wiki syntax.
 * We override the default XHTML renderer since we want special behaviors, for example to not escape special symbols
 * (since we don't want to escape HTML tags for example).
 *
 * @version $Id $
 * @since 1.8.3
 */
public class HTMLMacroXHTMLChainingRenderer extends XHTMLChainingRenderer
{
    /**
     * @param linkRenderer the object to render link events into XHTML. This is done so that it's pluggable because link
     *            rendering depends on how the underlying system wants to handle it. For example for XWiki we check if
     *            the document exists, we get the document URL, etc.
     * @param imageRenderer the object to render image events into XHTML. This is done so that it's pluggable because
     *            image rendering depends on how the underlying system wants to handle it. For example for XWiki we
     *            check if the image exists as a document attachments, we get its URL, etc.
     * @param listenerChain the chain of listener filters used to compute various states
     * @since 2.0M3
     */
    public HTMLMacroXHTMLChainingRenderer(XHTMLLinkRenderer linkRenderer, XHTMLImageRenderer imageRenderer,
        ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, listenerChain);
    }

    /**
     * @return true if the current event is generated from a transformation.
     */
    private boolean isInGeneratedBlock()
    {
        // Since we're already inside the HTML macro, we check for a depth of 2 (macro inside of macro).
        return getBlockState().getMacroDepth() > 1;
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print(String.valueOf(symbol));
        } else {
            super.onSpecialSymbol(symbol);
        }
    }

    @Override
    public void onWord(String word)
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print(word);
        } else {
            super.onWord(word);
        }
    }

    @Override
    public void onNewLine()
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print("\n");
        } else {
            super.onNewLine();
        }
    }

    @Override
    public void onSpace()
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print(" ");
        } else {
            super.onSpace();
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        if (!isInGeneratedBlock()) {
            // Don't print anything.
        } else {
            super.onEmptyLines(count);
        }
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        if (!isInGeneratedBlock()) {
            // Don't print anything.
        } else {
            super.beginParagraph(parameters);
        }
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        if (!isInGeneratedBlock()) {
            // Don't print anything.
        } else {
            super.endParagraph(parameters);
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        // Don't print anything since we are already in the html macro.
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Don't print anything since we are already in the html macro.
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Don't print anything since we are already in the html macro.
    }

    protected BlockStateChainingListener getBlockState()
    {
        return (BlockStateChainingListener) getListenerChain().getListener(HTMLMacroBlockStateChainingListener.class);
    }

    /**
     * @return a span element if we are inside an inline macro. Else a div.
     */
    private String getMetadataContainerElement()
    {
        if (getBlockState().isInLine()) {
            return "span";
        } else {
            return "div";
        }
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        Map<String, String> attributes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> metadataPair : metadata.getMetaData().entrySet()) {
            attributes.put(XHTMLXWikiGeneratorListener.METADATA_ATTRIBUTE_PREFIX + metadataPair.getKey(),
                metadataPair.getValue().toString());
        }

        attributes.put("class", XHTMLXWikiGeneratorListener.METADATA_CONTAINER_CLASS);

        this.getXHTMLWikiPrinter().printXMLStartElement(getMetadataContainerElement(), attributes);
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        getXHTMLWikiPrinter().printXMLEndElement(getMetadataContainerElement());
    }
}

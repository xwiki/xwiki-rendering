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

import java.util.Map;

import org.xwiki.rendering.internal.renderer.xhtml.XHTMLMacroRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLMetaDataRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.xml.html.HTMLElementSanitizer;

/**
 * Convert listener events to annotated HTML5. See {@link AnnotatedHTML5Renderer} for more details on what Annotated
 * HTML5 is.
 *
 * @version $Id$
 * @since 6.4M3
 */
public class AnnotatedHTML5ChainingRenderer extends HTML5ChainingRenderer
{
    /**
     * Renders a Macro definition into Annotated XHTML.
     */
    private final XHTMLMacroRenderer macroRenderer;

    /**
     * Renders metadata into Annotated XHTML.
     */
    private final XHTMLMetaDataRenderer metaDataRenderer;

    /**
     * @param linkRenderer the object to render link events into XHTML. This is done so that it's pluggable because link
     *            rendering depends on how the underlying system wants to handle it. For example for XWiki we check if
     *            the document exists, we get the document URL, etc.
     * @param imageRenderer the object to render image events into XHTML. This is done so that it's pluggable because
     *            image rendering depends on how the underlying system wants to handle it. For example for XWiki we
     *            check if the image exists as a document attachments, we get its URL, etc.
     * @param htmlElementSanitizer the HTML element sanitizer to use
     * @param listenerChain the chain of listener filters used to compute various states
     */
    public AnnotatedHTML5ChainingRenderer(XHTMLLinkRenderer linkRenderer,
            XHTMLImageRenderer imageRenderer, HTMLElementSanitizer htmlElementSanitizer, ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, htmlElementSanitizer, listenerChain);

        this.macroRenderer = new XHTMLMacroRenderer();

        this.metaDataRenderer = new XHTMLMetaDataRenderer();
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean isInline)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.render(getXHTMLWikiPrinter(), id, parameters, content);
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.beginRender(getXHTMLWikiPrinter(), name, parameters, content);
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
        // so that the macro can be reconstructed when moving back from XHTML to XDOM.
        this.macroRenderer.endRender(getXHTMLWikiPrinter());
    }

    /**
     * {@inheritDoc}
     * @since 14.0RC1
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        this.metaDataRenderer.beginRender(getXHTMLWikiPrinter(), getBlockState().isInLine(), metadata);
    }

    /**
     * {@inheritDoc}
     * @since 14.0RC1
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        this.metaDataRenderer.endRender(getXHTMLWikiPrinter(), getBlockState().isInLine());
    }

    /**
     * Start of a figure caption.
     *
     * Only rendered as &lt;figcaption&gt; tag when the parent event is a figure event.
     *
     * @param parameters a generic list of parameters for the figure
     * @since 14.0RC1
     */
    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        if (this.getBlockState().getParentEvent() == BlockStateChainingListener.Event.FIGURE) {
            super.beginFigureCaption(parameters);
        }
    }

    /**
     * End of a figure caption.
     *
     * Only rendered as &lt;/figcaption&gt;-tag when the parent event is a figure event.
     *
     * @param parameters a generic list of parameters for the figure
     * @since 14.0RC1
     */
    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        if (this.getBlockState().getParentEvent() == BlockStateChainingListener.Event.FIGURE) {
            super.endFigureCaption(parameters);
        }
    }
}

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

import java.util.Map;

import org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkRenderer;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Convert listener events to annotated XHTML. See {@link AnnotatedXHTMLChainingRenderer} for more details on
 * what Annotated XHTML is.
 *
 * @version $Id$
 * @since 2.0M2
 */
public class AnnotatedXHTMLChainingRenderer extends XHTMLChainingRenderer
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
     * @param listenerChain the chain of listener filters used to compute various states
     */
    public AnnotatedXHTMLChainingRenderer(XHTMLLinkRenderer linkRenderer,
        XHTMLImageRenderer imageRenderer, ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, listenerChain);

        this.macroRenderer = new XHTMLMacroRenderer();

        this.metaDataRenderer = new XHTMLMetaDataRenderer();
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
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

    @Override
    public void beginMetaData(MetaData metadata)
    {
        this.metaDataRenderer.beginRender(getXHTMLWikiPrinter(), getBlockState().isInLine(), metadata);
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        this.metaDataRenderer.endRender(getXHTMLWikiPrinter(), getBlockState().isInLine());
    }
}

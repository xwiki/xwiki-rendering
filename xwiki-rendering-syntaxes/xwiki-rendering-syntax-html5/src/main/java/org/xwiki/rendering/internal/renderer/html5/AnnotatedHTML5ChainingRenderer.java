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

import org.xwiki.rendering.internal.renderer.html5.HTML5ChainingRenderer;
import org.xwiki.rendering.internal.renderer.html5.HTML5MacroRenderer;
import org.xwiki.rendering.internal.renderer.html5.image.HTML5ImageRenderer;
import org.xwiki.rendering.internal.renderer.html5.link.HTML5LinkRenderer;
import org.xwiki.rendering.listener.chaining.ListenerChain;

import java.util.Map;

/**
 * Convert listener events to annotated HTML5. See {@link org.xwiki.rendering.internal.renderer.html5.AnnotatedHTML5ChainingRenderer} for more details on
 * what Annotated XHTML is.
 *
 * @version $Id$
 * @since 4.4M1
 */
public class AnnotatedHTML5ChainingRenderer extends HTML5ChainingRenderer
{
    private HTML5MacroRenderer macroRenderer;

    /**
     * @param linkRenderer the object to render link events into XHTML. This is done so that it's pluggable because link
     *            rendering depends on how the underlying system wants to handle it. For example for XWiki we check if
     *            the document exists, we get the document URL, etc.
     * @param imageRenderer the object to render image events into XHTML. This is done so that it's pluggable because
     *            image rendering depends on how the underlying system wants to handle it. For example for XWiki we
     *            check if the image exists as a document attachments, we get its URL, etc.
     * @param listenerChain the chain of listener filters used to compute various states
     */
    public AnnotatedHTML5ChainingRenderer(HTML5LinkRenderer linkRenderer,
                                          HTML5ImageRenderer imageRenderer, ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, listenerChain);

        this.macroRenderer = new org.xwiki.rendering.internal.renderer.html5.HTML5MacroRenderer();
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
        if (getBlockState().getMacroDepth() == 1) {
            // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
            // so that the macro can be reconstructed when moving back from XHTML to XDOM.
            this.macroRenderer.beginRender(getXHTMLWikiPrinter(), name, parameters, content);
        }
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        if (getBlockState().getMacroDepth() == 1) {
            // Do not do any rendering but we still need to save the macro definition in some hidden XHTML
            // so that the macro can be reconstructed when moving back from XHTML to XDOM.
            this.macroRenderer.endRender(getXHTMLWikiPrinter());
        }
    }
}
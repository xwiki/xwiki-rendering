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
package org.xwiki.rendering.internal.renderer.xhtml.image;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.annotation.Default;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;

/**
 * Default implementation for rendering images as XHTML. The implementation is pluggable in the sense that the
 * implementation is done by {@link org.xwiki.rendering.internal.renderer.xhtml.image.XHTMLImageTypeRenderer}
 * implementation, each in charge of handling a given {@link org.xwiki.rendering.listener.reference.ResourceType}.
 *
 * @version $Id$
 * @since 2.0M3
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXHTMLImageRenderer implements XHTMLImageRenderer
{
    @Inject
    private XHTMLImageTypeRenderer defaultImageTypeRenderer;

    @Inject
    @Named("context")
    protected Provider<ComponentManager> componentManagerProvider;

    /**
     * The XHTML printer to use to output images as XHTML.
     */
    private XHTMLWikiPrinter xhtmlPrinter;

    @Override
    public void setXHTMLWikiPrinter(XHTMLWikiPrinter printer)
    {
        this.xhtmlPrinter = printer;
    }

    @Override
    public XHTMLWikiPrinter getXHTMLWikiPrinter()
    {
        return this.xhtmlPrinter;
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, @Default("") Map<String, String> parameters)
    {
        getXHTMLImageTypeRenderer(reference).onImage(reference, freestanding, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        getXHTMLImageTypeRenderer(reference).onImage(reference, freestanding, id, parameters);
    }

    private XHTMLImageTypeRenderer getXHTMLImageTypeRenderer(ResourceReference reference)
    {
        XHTMLImageTypeRenderer renderer;

        // TODO: This is probably not very performant since it's called at each onImage.
        try {
            renderer = this.componentManagerProvider.get().getInstance(XHTMLImageTypeRenderer.class,
                reference.getType().getScheme());
        } catch (ComponentLookupException e) {
            // There's no specific XHTML Image Type Renderer for the passed link type, use the default renderer.
            renderer = this.defaultImageTypeRenderer;
        }
        renderer.setXHTMLWikiPrinter(getXHTMLWikiPrinter());
        return renderer;
    }
}

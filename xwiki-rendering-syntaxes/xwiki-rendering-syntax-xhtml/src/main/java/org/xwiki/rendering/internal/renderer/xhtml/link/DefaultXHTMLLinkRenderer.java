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
package org.xwiki.rendering.internal.renderer.xhtml.link;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;

/**
 * Default implementation for rendering links as XHTML. The implementation is pluggable in the sense that the
 * implementation is done by {@link org.xwiki.rendering.internal.renderer.xhtml.link.XHTMLLinkTypeRenderer}
 * implementation, each in charge of handling a given {@link org.xwiki.rendering.listener.reference.ResourceType}.
 * 
 * @version $Id$
 * @since 2.0M3
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXHTMLLinkRenderer implements XHTMLLinkRenderer
{
    @Inject
    private XHTMLLinkTypeRenderer defaultLinkTypeRenderer;

    @Inject
    @Named("context")
    protected Provider<ComponentManager> componentManagerProvider;

    /**
     * The XHTML printer to use to output links as XHTML.
     */
    private XHTMLWikiPrinter xhtmlPrinter;

    /**
     * @see #setHasLabel(boolean)
     */
    private boolean hasLabel;

    @Override
    public void setHasLabel(boolean hasLabel)
    {
        this.hasLabel = hasLabel;
    }

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
    public void beginLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        getXHTMLLinkTypeRenderer(reference).beginLink(reference, isFreeStandingURI, parameters);
    }

    @Override
    public void endLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        getXHTMLLinkTypeRenderer(reference).endLink(reference, isFreeStandingURI, parameters);
    }

    private XHTMLLinkTypeRenderer getXHTMLLinkTypeRenderer(ResourceReference reference)
    {
        XHTMLLinkTypeRenderer renderer;

        // TODO: This is probably not very performant since it's called at each begin/endLink.
        try {
            renderer =
                this.componentManagerProvider.get().getInstance(XHTMLLinkTypeRenderer.class,
                    reference.getType().getScheme());
        } catch (ComponentLookupException e) {
            // There's no specific XHTML Link Type Renderer for the passed link type, use the default renderer.
            renderer = this.defaultLinkTypeRenderer;
        }
        renderer.setHasLabel(this.hasLabel);
        renderer.setXHTMLWikiPrinter(getXHTMLWikiPrinter());
        return renderer;
    }
}

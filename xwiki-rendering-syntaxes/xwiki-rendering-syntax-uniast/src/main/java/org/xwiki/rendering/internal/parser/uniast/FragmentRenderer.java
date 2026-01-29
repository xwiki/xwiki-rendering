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
package org.xwiki.rendering.internal.parser.uniast;

import java.util.Deque;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.RenderingContext;

/**
 * Used to render an XDOM fragment to the syntax specified by the XDOM metadata or the rendering context.
 * 
 * @version $Id$
 * @since 18.1.0RC1
 */
@Component(roles = FragmentRenderer.class)
@Singleton
public class FragmentRenderer
{
    /**
     * Used to lookup the renderer.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private RenderingContext renderingContext;

    /**
     * Begin rendering a fragment.
     *
     * @param contextStack the context stack
     * @throws ParseException if the print renderer factory matching the target syntax cannot be found
     */
    public void beginFragment(Deque<Context> contextStack) throws ParseException
    {
        PrintRenderer printRenderer = getPrintRendererFactory().createRenderer(new DefaultWikiPrinter());
        contextStack.push(contextStack.peek().withListener(printRenderer));
        printRenderer.beginDocument(MetaData.EMPTY);
    }

    /**
     * End rendering a fragment.
     *
     * @param contextStack the context stack
     * @return the rendered fragment
     */
    public String endFragment(Deque<Context> contextStack)
    {
        PrintRenderer printRenderer = (PrintRenderer) contextStack.pop().listener();
        printRenderer.endDocument(MetaData.EMPTY);
        return printRenderer.getPrinter().toString();
    }

    private PrintRendererFactory getPrintRendererFactory() throws ParseException
    {
        Syntax targetSyntax = this.renderingContext.getTargetSyntax();

        try {
            return this.componentManagerProvider.get().getInstance(PrintRendererFactory.class,
                targetSyntax.toIdString());
        } catch (ComponentLookupException e) {
            throw new ParseException("Failed to get the print renderer factory for syntax " + targetSyntax, e);
        }
    }
}

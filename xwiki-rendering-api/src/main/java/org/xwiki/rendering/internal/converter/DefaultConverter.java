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
package org.xwiki.rendering.internal.converter;

import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * Default implementation for {@link Converter}.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultConverter implements Converter
{
    /**
     * Used to lookup parser and renderer.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Used to execute transformations.
     */
    @Inject
    private TransformationManager transformationManager;

    @Override
    public void convert(Reader source, Syntax sourceSyntax, Syntax targetSyntax, WikiPrinter printer)
        throws ConversionException
    {
        // Step 1: Find the parser and generate a XDOM
        XDOM xdom;
        try {
            Parser parser = this.componentManagerProvider.get().getInstance(Parser.class, sourceSyntax.toIdString());
            xdom = parser.parse(source);
        } catch (ComponentLookupException e) {
            throw new ConversionException("Failed to locate Parser for syntax [" + sourceSyntax + "]", e);
        } catch (ParseException e) {
            throw new ConversionException("Failed to parse input source", e);
        }

        // Step 2: Run transformations
        try {
            TransformationContext context = new TransformationContext(xdom, sourceSyntax);
            context.setTargetSyntax(targetSyntax);
            this.transformationManager.performTransformations(xdom, context);
        } catch (TransformationException e) {
            throw new ConversionException("Failed to execute some transformations", e);
        }

        // Step 3: Locate the Renderer and render the content in the passed printer
        BlockRenderer renderer;
        try {
            renderer = this.componentManagerProvider.get().getInstance(BlockRenderer.class, targetSyntax.toIdString());
        } catch (ComponentLookupException e) {
            throw new ConversionException("Failed to locate Renderer for syntax [" + targetSyntax + "]", e);
        }
        renderer.render(xdom, printer);
    }
}

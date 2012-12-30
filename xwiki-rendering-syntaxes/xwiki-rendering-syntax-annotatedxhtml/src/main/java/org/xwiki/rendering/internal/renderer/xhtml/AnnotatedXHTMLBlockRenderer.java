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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.renderer.AbstractBlockRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;

/**
 * Block Renderer for Annotated XHTML syntax. To be used to convert XDOM into XHTML.
 *
 * @version $Id$
 * @since 2.0M3
 */
@Component
@Named("annotatedxhtml/1.0")
@Singleton
public class AnnotatedXHTMLBlockRenderer extends AbstractBlockRenderer
{
    /**
     * Factory to create Annotated XHTML Print Renderers.
     */
    @Inject
    @Named("annotatedxhtml/1.0")
    private PrintRendererFactory annotatedXHTMLRendererFactory;

    @Override
    protected PrintRendererFactory getPrintRendererFactory()
    {
        return this.annotatedXHTMLRendererFactory;
    }
}

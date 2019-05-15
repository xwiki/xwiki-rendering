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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.rendering.internal.renderer.AbstractPrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Create Renderers for the HTML Macro.
 * It computes the renderer to creates based on the given hint. By default it creates a {@link HTMLMacroXHTMLRenderer}.
 *
 * @version $Id$
 * @since 2.0M3
 */
@Component(hints = {"xhtmlmacro/1.0", "htmlmacro+annotatedxhtml/1.0"})
@Singleton
public class HTMLMacroXHTMLRendererFactory extends AbstractPrintRendererFactory
{
    /**
     * Prefix for supported syntaxes in HTML Macro.
     * @since 11.4RC1
     */
    public static final String PREFIX_SYNTAX = "htmlmacro+";

    /**
     * The special syntax to recognize the HTML Macro XHTML Renderer.
     */
    private static final Syntax XHTML_SYNTAX = new Syntax(new SyntaxType("xhtmlmacro", "XHTML Macro"), "1.0");

    /**
     * List of supported syntaxes in HTML Macro.
     */
    private static final List<Syntax> SUPPORTED_SYNTAXES = Arrays.asList(Syntax.ANNOTATED_XHTML_1_0);

    /**
     * List of HTML Macro syntaxes based on the {@link #SUPPORTED_SYNTAXES} with the {@link #PREFIX_SYNTAX} to use
     * the dedicated renderers.
     */
    private final List<Syntax> htmlMacroSyntaxes;

    /**
     * Used to retrieve the hint of the component.
     */
    @Inject
    private ComponentDescriptor componentDescriptor;

    /**
     * Constructor that builds the list of {@link #htmlMacroSyntaxes}.
     *
     * @since 11.4RC1
     */
    public HTMLMacroXHTMLRendererFactory()
    {
        this.htmlMacroSyntaxes = new ArrayList<>();
        for (Syntax existedAcceptedSyntax : SUPPORTED_SYNTAXES) {
            SyntaxType type = existedAcceptedSyntax.getType();
            this.htmlMacroSyntaxes.add(new Syntax(
                new SyntaxType(PREFIX_SYNTAX + type.getId(), "HTML Macro " + type.getName()),
                existedAcceptedSyntax.getVersion()));
        }
    }

    /**
     * {@inheritDoc}
     * @return the syntax based on the component hint. Fallback on {@link #XHTML_SYNTAX}.
     */
    @Override
    public Syntax getSyntax()
    {
        Syntax result = XHTML_SYNTAX;
        String roleHint = this.componentDescriptor.getRoleHint();

        for (Syntax acceptedSyntax : this.htmlMacroSyntaxes) {
            if (roleHint.equals(acceptedSyntax.toIdString())) {
                result = acceptedSyntax;
            }
        }

        return result;
    }
}

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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.internal.renderer.AbstractPrintRendererFactory;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;
import org.xwiki.rendering.syntax.SyntaxType;

/**
 * Create Renderers for the HTML Macro. It computes the renderer to create based on the given hint. By default, it
 * creates a {@link org.xwiki.rendering.internal.macro.html.renderers.html5.HTMLMacroHTML5Renderer}.
 *
 * @version $Id$
 * @since 2.0M3
 */
@Component(hints = { "xhtmlmacro/1.0", "htmlmacro+annotatedxhtml/1.0", "htmlmacro+annotatedhtml/5.0",
    "htmlmacro+html/5.0", "htmlmacro+xhtml/1.0" })
@Singleton
public class HTMLMacroXHTMLRendererFactory extends AbstractPrintRendererFactory implements Initializable
{
    /**
     * Prefix for supported syntaxes in HTML Macro.
     *
     * @since 11.4RC1
     */
    public static final String PREFIX_SYNTAX = "htmlmacro+";

    /**
     * HTML Macro syntax based on the component's role with the {@link #PREFIX_SYNTAX} to use the dedicated renderers.
     */
    private Syntax htmlMacroSyntax;

    /**
     * Used to retrieve the hint of the component.
     */
    @Inject
    private ComponentDescriptor<PrintRendererFactory> componentDescriptor;

    /**
     * Used to resolve the syntax.
     */
    @Inject
    private SyntaxRegistry syntaxRegistry;

    /**
     * Initialize the syntax of this rendering factory.
     */
    @Override
    public void initialize() throws InitializationException
    {
        String roleHint = this.componentDescriptor.getRoleHint();
        String originalRole;
        if (roleHint.startsWith(PREFIX_SYNTAX)) {
            originalRole = roleHint.substring(PREFIX_SYNTAX.length());
        } else {
            originalRole = "xhtml/1.0";
        }

        try {
            Syntax originalSyntax = this.syntaxRegistry.resolveSyntax(originalRole);
            this.htmlMacroSyntax = toMacroSyntax(originalSyntax);
        } catch (ParseException parseException) {
            throw new InitializationException("Couldn't resolve the original syntax " + originalRole, parseException);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the syntax based on the component hint.
     */
    @Override
    public Syntax getSyntax()
    {
        return this.htmlMacroSyntax;
    }

    /**
     * Transforms the given Syntax into a corresponding HTML macro syntax.
     * <p>
     * For the xhtml/1.0 syntax the special "xhtmlmacro" syntax is returned for compatibility reasons, all other
     * syntaxes have the "htmlmacro+" prefix added to their id.
     *
     * @param originalSyntax The syntax to transform.
     * @return The corresponding HTML macro syntax.
     * @since 14.1RC1
     */
    private Syntax toMacroSyntax(Syntax originalSyntax)
    {
        Syntax result;

        if (Syntax.XHTML_1_0.equals(originalSyntax)) {
            result = new Syntax(new SyntaxType("xhtmlmacro", "XHTML Macro"), "1.0");
        } else {
            SyntaxType type = originalSyntax.getType();
            result = new Syntax(new SyntaxType(PREFIX_SYNTAX + type.getId(), "HTML Macro " + type.getName()),
                originalSyntax.getVersion());
        }

        return result;
    }
}

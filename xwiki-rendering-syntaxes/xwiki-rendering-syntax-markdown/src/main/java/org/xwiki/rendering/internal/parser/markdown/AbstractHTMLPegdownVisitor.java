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
package org.xwiki.rendering.internal.parser.markdown;

import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Implements Pegdown Visitor's HTML events.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractHTMLPegdownVisitor extends AbstractTextPegdownVisitor
{
    @Override
    public void visit(HtmlBlockNode htmlBlockNode)
    {
        getListener().onRawText(htmlBlockNode.getText(), Syntax.HTML_4_01);
    }

    @Override
    public void visit(InlineHtmlNode inlineHtmlNode)
    {
        getListener().onRawText(inlineHtmlNode.getText(), Syntax.HTML_4_01);
    }
}

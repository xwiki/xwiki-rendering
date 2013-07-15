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
package org.xwiki.rendering.internal.parser.markdown11;

import java.util.Collections;

import org.pegdown.ast.Node;
import org.pegdown.ast.TextNode;
import org.xwiki.rendering.internal.parser.markdown.AbstractTablePegdownVisitor;
import org.xwiki.rendering.internal.parser.markdown11.ast.SubscriptNode;
import org.xwiki.rendering.internal.parser.markdown11.ast.SuperscriptNode;
import org.xwiki.rendering.listener.Format;

/**
 * Implements Pegdown Visitor's {@link org.pegdown.ast.Node} for Pegdown's plugins.
 *
 * @version $Id $
 * @since 5.2M1
 */
public abstract class AbstractPluginsPegdownVisitor extends AbstractTablePegdownVisitor
{
    @Override
    public void visit(Node node)
    {
        // this is kinda ugly but whole visitor's implementation should be refactored in future...
        if (node instanceof SuperscriptNode) {
            visit((SuperscriptNode) node);
        } else if (node instanceof SubscriptNode) {
            visit((SubscriptNode) node);
        } else {
            throw new RuntimeException("Don't know how to handle node " + node);
        }
    }

    /**
     * @param node a superscript node
     */
    public void visit(SuperscriptNode node)
    {
        handleFormatted(node, Format.SUPERSCRIPT);
    }

    /**
     * @param node a subscript node
     */
    public void visit(SubscriptNode node)
    {
        handleFormatted(node, Format.SUBSCRIPT);
    }

    /**
     * Handles formatted text node. This is common code for superscript and subscript visitor.
     *
     * @param node any text node
     * @param format formatting style
     */
    private void handleFormatted(TextNode node, Format format)
    {
        getListener().beginFormat(format, Collections.EMPTY_MAP);
        getListener().onWord(node.getText());
        getListener().endFormat(format, Collections.EMPTY_MAP);
    }
}

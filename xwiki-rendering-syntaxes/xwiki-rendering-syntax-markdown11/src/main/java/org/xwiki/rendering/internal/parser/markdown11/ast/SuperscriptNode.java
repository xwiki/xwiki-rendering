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
package org.xwiki.rendering.internal.parser.markdown11.ast;

import org.pegdown.ast.Node;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.Visitor;

/**
 * Represents text in a superscript formatting.
 *
 * @version $Id $
 * @since 5.2M1
 */
public class SuperscriptNode extends TextNode
{
    /**
     * @param text a text that should be formatted as a superscript
     */
    public SuperscriptNode(String text)
    {
        super(text);
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit((Node) this);
    }
}

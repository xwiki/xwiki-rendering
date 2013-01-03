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

import java.util.Collections;

import org.pegdown.ast.EmphNode;
import org.pegdown.ast.StrongNode;
import org.xwiki.rendering.listener.Format;

/**
 * Implements Pegdown Visitor's formatting events.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractFormattingPegdownVisitor extends AbstractTextPegdownVisitor
{
    @Override
    public void visit(StrongNode strongNode)
    {
        getListener().beginFormat(Format.BOLD, Collections.EMPTY_MAP);
        visitChildren(strongNode);
        getListener().endFormat(Format.BOLD, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(EmphNode emphNode)
    {
        getListener().beginFormat(Format.ITALIC, Collections.EMPTY_MAP);
        visitChildren(emphNode);
        getListener().endFormat(Format.ITALIC, Collections.EMPTY_MAP);
    }
}

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

import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.OrderedListNode;
import org.xwiki.rendering.listener.ListType;

/**
 * Implements Pegdown Visitor's list events.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractListPegdownVisitor extends AbstractHeadingPegdownVisitor
{
    @Override
    public void visit(BulletListNode bulletListNode)
    {
        getListener().beginList(ListType.BULLETED, Collections.EMPTY_MAP);
        visitChildren(bulletListNode);
        getListener().endList(ListType.BULLETED, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(ListItemNode listItemNode)
    {
        getListener().beginListItem();
        visitChildren(listItemNode);
        getListener().endListItem();
    }

    @Override
    public void visit(DefinitionListNode definitionListNode)
    {
        getListener().beginDefinitionList(Collections.EMPTY_MAP);
        visitChildren(definitionListNode);
        getListener().endDefinitionList(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(DefinitionNode definitionNode)
    {
        getListener().beginDefinitionDescription();

        DefinitionListListener listener = new DefinitionListListener();
        listener.setWrappedListener(getListener());
        this.listeners.push(listener);
        visitChildren(definitionNode);
        this.listeners.pop();

        getListener().endDefinitionDescription();
    }

    @Override
    public void visit(DefinitionTermNode definitionTermNode)
    {
        getListener().beginDefinitionTerm();
        visitChildren(definitionTermNode);
        getListener().endDefinitionTerm();
    }

    @Override
    public void visit(OrderedListNode orderedListNode)
    {
        getListener().beginList(ListType.NUMBERED, Collections.EMPTY_MAP);
        visitChildren(orderedListNode);
        getListener().endList(ListType.NUMBERED, Collections.EMPTY_MAP);
    }
}

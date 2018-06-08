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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.Map;

import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Stack table head cell blocks until there's some standalone elements or the {@link #endTableHeadCell(Map)} event is
 * called which marks the end of the handling of the table head cell content.
 *
 * @version $Id$
 * @since 10.5RC1
 */
public class TableHeadCellStackingInlineContentChainingListener extends AbstractStackingInlineContentChainingListener
{
    private int tableHeadCellDepth;

    /**
     * @param listenerChain the listener chain to save
     */
    public TableHeadCellStackingInlineContentChainingListener(ListenerChain listenerChain)
    {
        super(listenerChain);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginList(type, parameters);
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        super.endList(type, parameters);
        endStandaloneElement();
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginDefinitionList(parameters);
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        super.endDefinitionList(parameters);
        endStandaloneElement();
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.tableHeadCellDepth++;
        super.beginTableHeadCell(parameters);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        super.endTableHeadCell(parameters);

        // Should we stop stacking?
        if (this.tableHeadCellDepth == 0) {
            stopStacking();
        } else {
            this.tableHeadCellDepth--;
        }
    }
}

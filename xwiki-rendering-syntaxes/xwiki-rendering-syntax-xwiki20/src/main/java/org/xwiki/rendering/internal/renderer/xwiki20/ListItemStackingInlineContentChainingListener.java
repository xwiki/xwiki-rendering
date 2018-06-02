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

import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Stack list item blocks until there's some standalone elements or the {@link #endListItem(Map)} (Map)} event is called
 * which marks the end of the handling of the list item content.
 *
 * @version $Id$
 * @since 10.5RC1
 */
public class ListItemStackingInlineContentChainingListener extends AbstractStackingInlineContentChainingListener
{
    private int listItemDepth;

    /**
     * @param listenerChain the listener chain to save
     */
    public ListItemStackingInlineContentChainingListener(ListenerChain listenerChain)
    {
        super(listenerChain);
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        handleBeginListItem();
        super.beginListItem(parameters);
    }

    @Override
    public void beginListItem()
    {
        handleBeginListItem();
        super.beginListItem();
    }

    @Override
    public void endListItem()
    {
        super.endListItem();
        handleEndListItem();
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        super.endListItem(parameters);
        handleEndListItem();
    }

    private void handleBeginListItem()
    {
        this.listItemDepth++;
    }

    private void handleEndListItem()
    {
        // Should we stop stacking?
        if (this.listItemDepth == 0) {
            stopStacking();
        } else {
            this.listItemDepth--;
        }
    }
}

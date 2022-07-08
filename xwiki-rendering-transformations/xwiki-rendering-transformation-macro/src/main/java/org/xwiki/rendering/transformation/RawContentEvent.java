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
package org.xwiki.rendering.transformation;

import org.xwiki.observation.event.AbstractCancelableEvent;
import org.xwiki.observation.event.filter.EventFilter;
import org.xwiki.stability.Unstable;

/**
 * Event triggered when raw content supplied by the user as macro content (or parameter) shall be printed.
 *
 * When the event is cancelled, the raw content either won't be printed or it will be filtered to ensure it is safe.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Unstable
public class RawContentEvent extends AbstractCancelableEvent
{
    /** Serial version ID. Increment only if the <i>serialized</i> version of this class changes. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor initializing the event filter with an
     * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that this event will match any
     * other event of the same type.
     */
    public RawContentEvent()
    {
        super();
    }

    /**
     * Constructor initializing the event filter with a {@link org.xwiki.observation.event.filter.FixedNameEventFilter},
     * meaning that this event will match only events of the same type affecting the same passed name.
     *
     * @param macroName name of the macro to match, e.g. "html"
     */
    public RawContentEvent(String macroName)
    {
        super(macroName);
    }

    /**
     * Constructor using a custom {@link EventFilter}.
     *
     * @param eventFilter the filter to use for matching events
     */
    public RawContentEvent(EventFilter eventFilter)
    {
        super(eventFilter);
    }

}

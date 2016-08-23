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
package org.xwiki.rendering.listener;

import java.util.Map;

/**
 * Automatically generate section events from header events.
 * 
 * @version $Id$
 * @since 8.2RC1
 */
public class SectionGeneratorListener extends WrappingListener
{
    private int sectionDepth = -1;

    /**
     * @param listener the listener to wrapp
     */
    public SectionGeneratorListener(Listener listener)
    {
        setWrappedListener(listener);
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        // Close sections that need to be closed
        for (; this.sectionDepth >= level.ordinal(); --this.sectionDepth) {
            endSection(Listener.EMPTY_PARAMETERS);
        }

        // Open new sections
        for (; this.sectionDepth < level.ordinal(); ++this.sectionDepth) {
            beginSection(Listener.EMPTY_PARAMETERS);
        }

        super.beginHeader(level, id, parameters);
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        // Close sections that need to be closed
        for (; this.sectionDepth > -1; --this.sectionDepth) {
            endSection(Listener.EMPTY_PARAMETERS);
        }

        super.endDocument(metadata);
    }
}

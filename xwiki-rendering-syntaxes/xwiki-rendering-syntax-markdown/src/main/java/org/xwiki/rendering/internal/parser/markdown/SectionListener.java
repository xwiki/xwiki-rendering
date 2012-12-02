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
import java.util.Map;

import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.WrappingListener;

/**
 * Handles Sections since the notion of Section doesn't exist in Pegdown but XWiki requires it.
 *
 * @version $Id$
 * @since 4.4M1
 */
public class SectionListener extends WrappingListener
{
    /**
     * Depth of heading sections that will need to be closed.
     */
    private int depth;

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        if (level.getAsInt() - 1 < this.depth) {
            for (int i = 0; i < this.depth - level.getAsInt() + 1; i++) {
                super.endSection(Collections.EMPTY_MAP);
            }
            this.depth = level.getAsInt() - 1;
        }

        super.beginSection(Collections.EMPTY_MAP);
        super.beginHeader(level, id, parameters);
        this.depth++;
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        super.endHeader(level, id, parameters);
    }

    @Override
    public void endDocument(MetaData metaData)
    {
        if (this.depth > 0) {
            for (int i = 0; i < this.depth; i++) {
                super.endSection(Collections.EMPTY_MAP);
            }
            this.depth = 0;
        }
        super.endDocument(metaData);
    }
}

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
package org.xwiki.rendering.wikimodel.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This is an utility class which is used to build tables of content (TOCs).
 *
 * @version $Id$
 * @since 4.0M1
 */
public class TocBuilder
{
    protected int fBaseLevel;

    protected Deque<Integer> fBaseLevelStack = new ArrayDeque<Integer>();

    protected int fLevel;

    private ITocListener fListener;

    private final int fMaxHeaderDepth;

    private final int fMaxSectionDepth;

    private int fTotalDepth;

    public TocBuilder(ITocListener listener)
    {
        this(listener, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public TocBuilder(ITocListener listener, int totalDepth)
    {
        this(listener, Integer.MAX_VALUE, Integer.MAX_VALUE, totalDepth);
    }

    public TocBuilder(ITocListener listener, int documentDepth, int headerDepth)
    {
        this(listener, documentDepth, headerDepth, Integer.MAX_VALUE);
    }

    public TocBuilder(
        ITocListener listener,
        int documentDepth,
        int headerDepth,
        int totalDepth)
    {
        fListener = listener;
        fMaxSectionDepth = documentDepth;
        fMaxHeaderDepth = headerDepth;
        fTotalDepth = totalDepth;
    }

    public void beginDocument()
    {
        fBaseLevelStack.push(fBaseLevel);
        fBaseLevel = fLevel;
    }

    public void beginHeader(int level)
    {
        setHeaderLevel(level);
        if (checkDepth()) {
            fListener.beginItem();
        }
    }

    /**
     * @return <code>true</code> if the current element should be shown
     */
    public boolean checkDepth()
    {
        int documentDepth = fBaseLevelStack.size();
        int headerLevel = getHeaderLevel();
        return documentDepth <= fMaxSectionDepth
            && headerLevel <= fMaxHeaderDepth
            && (documentDepth + headerLevel) <= fTotalDepth;
    }

    public void endDocument()
    {
        setHeaderLevel(0);
        Integer level = fBaseLevelStack.pop();
        fBaseLevel = level.intValue();
    }

    public void endHeader()
    {
        if (checkDepth()) {
            fListener.endItem();
        }
    }

    /**
     * @return the current level of headers
     */
    protected int getHeaderLevel()
    {
        return fLevel - fBaseLevel;
    }

    protected void setHeaderLevel(int level)
    {
        while (fLevel > level + fBaseLevel) {
            if (checkDepth()) {
                fListener.endLevel(getHeaderLevel());
            }
            fLevel--;
        }
        while (fLevel < level + fBaseLevel) {
            fLevel++;
            if (checkDepth()) {
                fListener.beginLevel(getHeaderLevel());
            }
        }
    }
}

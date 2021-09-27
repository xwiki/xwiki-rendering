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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class SectionBuilder<T>
{
    protected class TocEntry implements TreeBuilder.IPos<TocEntry>,
        ISectionListener.IPos<T>
    {
        T fData;

        protected boolean fDoc;

        protected boolean fHeader;

        int fDocLevel;

        int fLevel;

        public TocEntry(int docLevel, int level, T data, boolean doc,
            boolean header)
        {
            fDocLevel = docLevel;
            fLevel = level;
            fData = data;
            fDoc = doc;
            fHeader = header;
        }

        public boolean equalsData(TocEntry pos)
        {
            return true;
        }

        public T getData()
        {
            return fData;
        }

        public int getDocumentLevel()
        {
            return fDocLevel;
        }

        public int getHeaderLevel()
        {
            return fLevel;
        }

        public int getPos()
        {
            return fDocLevel * 10 + fLevel + 1;
        }
    }

    Deque<TreeBuilder<TocEntry>> fBuilder = new ArrayDeque<TreeBuilder<TocEntry>>();

    private Deque<TocEntry> fDocEntries = new ArrayDeque<TocEntry>();

    ISectionListener<T> fListener;

    public SectionBuilder(ISectionListener<T> listener)
    {
        fListener = listener;
    }

    private void pushBuilder()
    {
        fBuilder.push(new TreeBuilder<TocEntry>(
            new TreeBuilder.ITreeListener<TocEntry>()
            {
                public void onBeginRow(TocEntry n)
                {
                    if (!n.fDoc) {
                        fListener.beginSection(n);
                        if (n.fHeader) {
                            fListener.beginSectionHeader(n);
                        } else {
                            fListener.beginSectionContent(n);
                        }
                    }
                }

                public void onBeginTree(TocEntry n)
                {
                    if (n.fDoc) {
                        fListener.beginDocument(n);
                    }
                }

                public void onEndRow(TocEntry n)
                {
                    if (!n.fDoc) {
                        fListener.endSectionContent(n);
                        fListener.endSection(n);
                    }
                }

                public void onEndTree(TocEntry n)
                {
                    if (n.fDoc) {
                        fListener.endDocument(n);
                    }
                }
            }));
    }

    private TreeBuilder<TocEntry> popBuilder()
    {
        return fBuilder.pop();
    }

    private TocEntry align(int docLevel, int level, T data, boolean doc)
    {
        TocEntry entry = null;
        List<TocEntry> entries = new ArrayList<TocEntry>();
        for (int i = 0; i <= level; ++i) {
            entry = new TocEntry(docLevel, i, data, doc, i == level);
            entries.add(entry);
        }
        fBuilder.peek().align(entries);

        return entry;
    }

    public void beginDocument(T data)
    {
        pushBuilder();

        TocEntry entry = align(getDocLevel() + 1, 0, data, true);

        fDocEntries.push(entry);
    }

    public void beginHeader(int level, T data)
    {
        int docLevel = getDocLevel();
        align(docLevel, level, data, false);
    }

    public void endDocument()
    {
        fDocEntries.pop();

        popBuilder().align(Collections.<TocEntry>emptyList());
    }

    public void endHeader()
    {
        TocEntry entry = fBuilder.peek().getPeek();
        fListener.endSectionHeader(entry);
        fListener.beginSectionContent(entry);
    }

    /**
     * @return
     */
    private int getDocLevel()
    {
        return fDocEntries.size();
    }
}

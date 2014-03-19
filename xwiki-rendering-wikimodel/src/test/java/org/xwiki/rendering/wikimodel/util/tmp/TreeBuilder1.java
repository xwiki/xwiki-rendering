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
package org.xwiki.rendering.wikimodel.util.tmp;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class TreeBuilder1<T>
{
    public interface ITreeBuilderListener<T>
    {
        void beginItem(int depth, T data);

        void beginLevel(int depth, T prev);

        void endItem(int depth, T data);

        void endLevel(int i, T prev);
    }

    private static class Slot<T>
    {
        private final T data;

        private final int val;

        public Slot(int pos, T data)
        {
            this.val = pos;
            this.data = data;
        }
    }

    private static final int MIN = Integer.MIN_VALUE;

    private boolean fIn;

    private boolean fInitialized;

    private ITreeBuilderListener<T> fListener;

    private T fPrev;

    private Deque<Slot<T>> fStack = new ArrayDeque<Slot<T>>();

    public TreeBuilder1(ITreeBuilderListener<T> listener)
    {
        fListener = listener;
    }

    public void align(int val, T data)
    {
        trim(val, true);
        if (!fInitialized) {
            fInitialized = true;
            fIn = true;
            val = MIN;
        }
        Slot<T> slot = new Slot<T>(val, data);
        if (fIn) {
            fListener.beginLevel(fStack.size(), data);
        }
        fStack.push(slot);
        fListener.beginItem(fStack.size(), data);
        fPrev = data;
        fIn = true;
    }

    public void finish()
    {
        trim(MIN, true);
    }

    /**
     * @param val
     */
    public void trim(int val)
    {
        trim(val, true);
    }

    /**
     * @param val
     */
    public void trim(int val, boolean includeValue)
    {
        while (!fStack.isEmpty()) {
            Slot<T> peek = fStack.peek();
            if (peek.val < val || peek.val == val && !includeValue) {
                break;
            }
            if (!fIn) {
                fListener.endLevel(fStack.size(), fPrev);
            }
            fStack.pop();
            fListener.endItem(fStack.size(), peek.data);
            fIn = false;
            fPrev = peek.data;

            if (peek.val == val) {
                break;
            }
        }
        if (fInitialized && fStack.isEmpty()) {
            fListener.endLevel(fStack.size(), fPrev);
        }
    }
}

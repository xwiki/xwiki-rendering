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

import java.util.ArrayList;
import java.util.List;

/**
 * This is an internal utility class used as a context to keep in memory the
 * current state of parsed trees (list items).
 *
 * @version $Id$
 * @since 4.0M1
 */
public final class TreeBuilder<X extends TreeBuilder.IPos<X>>
{
    /**
     * This interface identifies position of elements in rows.
     *
     * @author MikhailKotelnikov
     */
    public interface IPos<X extends IPos<X>>
    {
        /**
         * @return <code>true</code> if the underlying data in both positions
         *         are the same
         */
        boolean equalsData(X pos);

        /**
         * @return the position of the node
         */
        int getPos();
    }

    public interface ITreeListener<X extends IPos<X>>
    {
        void onBeginRow(X n);

        void onBeginTree(X n);

        void onEndRow(X n);

        void onEndTree(X n);
    }

    private static <X extends IPos<X>> void addTail(
        ITreeListener<X> listener,
        List<X> firstArray,
        List<X> secondArray,
        int secondPos,
        boolean openTree)
    {
        X n = getNode(secondArray, secondPos);
        if (n == null) {
            return;
        }
        if (openTree) {
            listener.onBeginTree(n);
        }
        listener.onBeginRow(n);
        firstArray.add(n);
        addTail(listener, firstArray, secondArray, secondPos + 1, true);
    }

    private static <X extends IPos<X>> void doAlign(
        ITreeListener<X> listener,
        List<X> firstArray,
        List<X> secondArray,
        boolean expand)
    {
        boolean newTree = true;
        int f;
        int s;
        int firstLen = firstArray.size();
        int secondLen = secondArray.size();
        for (f = 0, s = 0; f < firstLen && s < secondLen; f++) {
            X first = firstArray.get(f);
            X second = secondArray.get(s);
            int firstPos = first.getPos();
            int secondPos = second.getPos();
            if (firstPos >= secondPos) {
                if (!first.equalsData(second)) {
                    break;
                } else if (s == secondLen - 1) {
                    newTree = false;
                    break;
                }
                s++;
            }
        }
        removeTail(listener, firstArray, f, newTree, expand);
        if (expand) {
            addTail(listener, firstArray, secondArray, s, newTree);
        }
    }

    private static <X extends IPos<X>> X getNode(List<X> list, int pos)
    {
        return pos < 0 || pos >= list.size() ? null : list.get(pos);
    }

    private static <X extends IPos<X>> void removeTail(
        ITreeListener<X> listener,
        List<X> array,
        int pos,
        boolean closeTree,
        boolean remove)
    {
        X node = getNode(array, pos);
        if (node == null) {
            return;
        }
        removeTail(listener, array, pos + 1, true, true);
        listener.onEndRow(node);
        if (closeTree) {
            listener.onEndTree(node);
        }
        if (remove) {
            array.remove(pos);
        }
    }

    public List<X> fList = new ArrayList<X>();

    private ITreeListener<X> fListener;

    public TreeBuilder(ITreeListener<X> listener)
    {
        super();
        fListener = listener;
    }

    public void align(List<X> row)
    {
        doAlign(fListener, fList, row, true);
    }

    public void align(X pos)
    {
        List<X> list = new ArrayList<X>();
        if (pos != null) {
            list.add(pos);
        }
        align(list);
    }

    public X get(int pos)
    {
        return pos >= 0 && pos < fList.size() ? fList.get(pos) : null;
    }

    public X getPeek()
    {
        return get(fList.size() - 1);
    }

    public void trim(List<X> row)
    {
        doAlign(fListener, fList, row, false);
    }

    public void trim(X pos)
    {
        List<X> list = new ArrayList<X>();
        if (pos != null) {
            list.add(pos);
        }
        trim(list);
    }
}

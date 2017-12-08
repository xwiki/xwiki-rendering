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

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This is an internal utility class used as a context to keep in memory the
 * current state of parsed trees (list items).
 *
 * @version $Id$
 * @since 4.0M1
 */
public class ListBuilder
{
    static class CharPos implements TreeBuilder.IPos<CharPos>
    {
        private int fPos;

        private char fRowChar;

        private char fTreeChar;

        public CharPos(char treeChar, char rowChar, int pos)
        {
            fPos = pos;
            fTreeChar = treeChar;
            fRowChar = rowChar;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CharPos)) {
                return false;
            }
            CharPos pos = (CharPos) obj;
            return equalsData(pos) && pos.fPos == fPos;
        }
        
        @Override 
        public int hashCode()
        {
            return new HashCodeBuilder()
                .append(fTreeChar)
                .append(fPos)
                .toHashCode();
        }
        

        public boolean equalsData(CharPos pos)
        {
            return pos.fTreeChar == fTreeChar;
        }

        public int getPos()
        {
            return fPos;
        }
    }

    TreeBuilder<CharPos> fBuilder = new TreeBuilder<CharPos>(
        new TreeBuilder.ITreeListener<CharPos>()
        {
            public void onBeginRow(CharPos pos)
            {
                fListener.beginRow(pos.fTreeChar, pos.fRowChar);
            }

            public void onBeginTree(CharPos pos)
            {
                fListener.beginTree(pos.fTreeChar);
            }

            public void onEndRow(CharPos pos)
            {
                fListener.endRow(pos.fTreeChar, pos.fRowChar);
            }

            public void onEndTree(CharPos pos)
            {
                fListener.endTree(pos.fTreeChar);
            }
        });

    private IListListener fListener;

    public ListBuilder(IListListener listener)
    {
        fListener = listener;
    }

    /**
     * @param row the parameters of the row
     */
    public void alignContext(String row)
    {
        List<CharPos> list = getCharPositions(row);
        fBuilder.align(list);
    }

    private List<CharPos> getCharPositions(String s)
    {
        List<CharPos> list = new ArrayList<CharPos>();
        char[] array = s.toCharArray();
        int pos = 0;
        for (int i = 0; i < array.length; i++) {
            char ch = array[i];
            if (ch == '\r' || ch == '\n') {
                continue;
            }
            if (!Character.isSpaceChar(ch)) {
                char treeChar = getTreeType(ch);
                list.add(new CharPos(treeChar, ch, pos));
            }
            pos++;
        }
        return list;
    }

    /**
     * @param rowType the type of the row
     * @return the type of the tree corresponding to the given row type
     */
    protected char getTreeType(char rowType)
    {
        return rowType;
    }
}

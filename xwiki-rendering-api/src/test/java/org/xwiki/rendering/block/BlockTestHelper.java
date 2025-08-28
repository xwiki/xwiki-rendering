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
package org.xwiki.rendering.block;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class members for BLock tests.
 *
 * @version $Id$
 */
public class BlockTestHelper
{
    public static final WordBlock precedingBlockChild1 = new WordBlock("pc1");

    public static final WordBlock precedingBlockChild2 = new WordBlock("pc2");

    public static final ParagraphBlock precedingBlock = new ParagraphBlock(Arrays.asList(precedingBlockChild1,
        precedingBlockChild2))
    {
        @Override
        public String toString()
        {
            return "precedingBlock";
        }
    };

    public static final WordBlock contextBlockChild21 = new WordBlock("cc21");

    public static final WordBlock contextBlockChild22 = new WordBlock("cc22");

    public static final ParagraphBlock contextBlockChild2 = new ParagraphBlock(Arrays.asList(
        contextBlockChild21, contextBlockChild22))
    {
        @Override
        public String toString()
        {
            return "contextBlockChild2";
        }
    };

    public static final WordBlock contextBlockChild11 = new WordBlock("cc11");

    public static final WordBlock contextBlockChild12 = new WordBlock("cc12");

    public static final ParagraphBlock contextBlockChild1 = new ParagraphBlock(Arrays.asList(
        contextBlockChild11, contextBlockChild12))
    {
        @Override
        public String toString()
        {
            return "contextBlockChild1";
        }
    };

    public static final ParagraphBlock contextBlock = new ParagraphBlock(Arrays.asList(contextBlockChild1,
        contextBlockChild2))
    {
        @Override
        public String toString()
        {
            return "contextBlock";
        }
    };

    public static final WordBlock followingBlockChild1 = new WordBlock("fc1");

    public static final WordBlock followingBlockChild2 = new WordBlock("fc2");

    public static final ParagraphBlock followingBlock = new ParagraphBlock(Arrays.asList(followingBlockChild1,
        followingBlockChild2))
    {
        @Override
        public String toString()
        {
            return "followingBlock";
        }
    };

    public static final ParagraphBlock parentBlock = new ParagraphBlock(Arrays.asList(precedingBlock,
        contextBlock, followingBlock))
    {
        @Override
        public String toString()
        {
            return "parentBlock";
        }
    };

    public static final ParagraphBlock rootBlock = new ParagraphBlock(List.of(parentBlock))
    {
        @Override
        public String toString()
        {
            return "rootBlock";
        }
    };
}

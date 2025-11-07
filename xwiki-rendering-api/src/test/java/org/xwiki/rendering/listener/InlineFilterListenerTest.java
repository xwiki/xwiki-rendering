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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.internal.parser.XDOMGeneratorListener;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InlineFilterListener}.
 *
 * @version $Id$
 */
class InlineFilterListenerTest
{
    @Test
    void beginParagraph()
    {
        InlineFilterListener listener = new InlineFilterListener();
        XDOMGeneratorListener xdomGeneratorListener = new XDOMGeneratorListener();
        listener.setWrappedListener(xdomGeneratorListener);

        listener.beginDocument(new MetaData());
        listener.beginParagraph(Listener.EMPTY_PARAMETERS);
        listener.onWord("This");
        listener.onSpace();
        listener.onWord("is");
        listener.onSpace();
        listener.onWord("inline");
        listener.endParagraph(Listener.EMPTY_PARAMETERS);
        listener.endDocument(new MetaData());

        List<Block> children = xdomGeneratorListener.getXDOM().getChildren();
        List<Block> expectedChildren = List.of(new WordBlock("This"), new SpaceBlock(), new WordBlock("is"),
            new SpaceBlock(), new WordBlock("inline"));
        assertEquals(expectedChildren, children);
    }

    @Test
    void onMacro()
    {
        InlineFilterListener listener = new InlineFilterListener();
        XDOMGeneratorListener xdomGeneratorListener = new XDOMGeneratorListener();
        listener.setWrappedListener(xdomGeneratorListener);

        listener.beginDocument(new MetaData());
        listener.onMacro("testMacro", Listener.EMPTY_PARAMETERS, null, false);
        listener.endDocument(new MetaData());

        List<Block> children = xdomGeneratorListener.getXDOM().getChildren();
        List<Block> expectedChildren = List.of(new MacroBlock("testMacro", Listener.EMPTY_PARAMETERS, null, true));
        assertEquals(expectedChildren, children);
    }
}

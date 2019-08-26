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
package org.xwiki.rendering.listener.chaining;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LookaheadChainingListener}.
 *
 * @version $Id$
 * @since 2.0M3
 */
public class EmptyBlockChainingListenerTest
{
    private ListenerChain chain;

    private EmptyBlockChainingListener listener;

    @BeforeEach
    public void setUp()
    {
        this.chain = new ListenerChain();
        this.listener = new EmptyBlockChainingListener(this.chain);
        this.chain.addListener(this.listener);
    }

    /**
     * Verify that isCurrentContainerBlockEmpty return true if there's no children inside a paragraph container block.
     */
    @Test
    public void testEmptyParagraphContainer()
    {
        this.chain.addListener(new AbstractChainingListener()
        {
            {
                setListenerChain(EmptyBlockChainingListenerTest.this.chain);
            }

            @Override
            public void endParagraph(Map<String, String> parameters)
            {
                EmptyBlockChainingListener blockState =
                    (EmptyBlockChainingListener) getListenerChain().getListener(EmptyBlockChainingListener.class);
                assertTrue(blockState.isCurrentContainerBlockEmpty());
            }
        });

        this.listener.beginParagraph(Collections.<String, String>emptyMap());
        this.listener.endParagraph(Collections.<String, String>emptyMap());
    }

    /**
     * Verify that isCurrentContainerBlockEmpty return false if there are children inside a paragraph container block.
     */
    @Test
    public void testNonEmptyParagraphContainer()
    {
        this.chain.addListener(new AbstractChainingListener()
        {
            {
                setListenerChain(EmptyBlockChainingListenerTest.this.chain);
            }

            @Override
            public void endParagraph(Map<String, String> parameters)
            {
                EmptyBlockChainingListener blockState =
                    (EmptyBlockChainingListener) getListenerChain().getListener(EmptyBlockChainingListener.class);
                assertFalse(blockState.isCurrentContainerBlockEmpty());
            }
        });

        this.listener.beginParagraph(Collections.<String, String>emptyMap());
        this.listener.onWord("word");
        this.listener.endParagraph(Collections.<String, String>emptyMap());
    }
}

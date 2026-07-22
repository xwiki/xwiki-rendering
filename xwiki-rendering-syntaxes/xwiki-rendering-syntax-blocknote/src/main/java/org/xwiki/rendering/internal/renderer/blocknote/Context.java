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
package org.xwiki.rendering.internal.renderer.blocknote;

import java.util.Deque;
import java.util.Map;
import java.util.Objects;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Shared context between the different chaining listeners used by the {@link BlockNoteRenderer}.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public class Context
{
    private final ListenerChain listenerChain;

    /**
     * Create a new context.
     *
     * @param listenerChain the listeners chain used by the {@link BlockNoteRenderer}
     */
    public Context(ListenerChain listenerChain)
    {
        this.listenerChain = listenerChain;
    }

    /**
     * @return the path of XDOM blocks from the root to the current block being rendered
     */
    public Deque<Block> getXDOMPath()
    {
        return getRequiredListener(XDOMPathChainingListener.class).getXDOMPath();
    }

    /**
     * @return the parameters of the current block being rendered
     */
    public Map<String, String> getParameters()
    {
        return getXDOMPath().peek().getParameters();
    }

    /**
     * @return {@code true} if the current container block being rendered is empty, {@code false} otherwise
     */
    public boolean isCurrentContainerBlockEmpty()
    {
        return getEmptyBlockState().isCurrentContainerBlockEmpty();
    }

    /**
     * @return the {@link EmptyBlockChainingListener} from the listeners chain
     */
    private EmptyBlockChainingListener getEmptyBlockState()
    {
        return getRequiredListener(EmptyBlockChainingListener.class);
    }

    /**
     * @return the {@link TextChainingListener} from the listeners chain
     */
    public TextChainingListener getTextState()
    {
        return getRequiredListener(TextChainingListener.class);
    }

    /**
     * @return the {@link BlockStateChainingListener} from the listeners chain
     */
    public BlockStateChainingListener getBlockState()
    {
        return getRequiredListener(BlockStateChainingListener.class);
    }

    /**
     * @return the {@link BlockNoteChainingPrintRenderer} from the listeners chain
     */
    public BlockNoteChainingPrintRenderer getBlockNoteState()
    {
        return getRequiredListener(BlockNoteChainingPrintRenderer.class);
    }

    /**
     * @param listenerClass the listener class for which we want the instance from the chain
     * @param <T> the type of listener
     * @return the listener instance corresponding to the passed class, never {@code null}
     */
    private <T extends ChainingListener> T getRequiredListener(Class<T> listenerClass)
    {
        return Objects.requireNonNull(listenerClass.cast(this.listenerChain.getListener(listenerClass)),
            () -> String.format("The [%s] listener is missing from the chain.", listenerClass.getName()));
    }
}

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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.Collections;
import java.util.Map;

import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;

/**
 * Stack events found under the current element and if there are standalone events, then wrap them in a
 * {@link org.xwiki.rendering.block.GroupBlock}. Note that if the only top level element is a
 * {@link org.xwiki.rendering.block.GroupBlock} then we don't consider it astandalone element.
 * This is useful for example to handme rendering of {@link org.xwiki.rendering.block.TableCellBlock} elements in the
 * XWiki Syntax 2.0 since they support only inline elements (except through Group Blocks).
 *
 * @version $Id$
 * @since 10.5RC1
 */
public abstract class AbstractStackingInlineContentChainingListener extends LookaheadChainingListener
{
    private boolean isStacking;

    private int standaloneElementDepth;

    private int standaloneTopLevelElements;

    private boolean hasGroupTopLevelBlock;

    /**
     * @param listenerChain the listener chain to save
     */
    public AbstractStackingInlineContentChainingListener(ListenerChain listenerChain)
    {
        super(listenerChain, Integer.MAX_VALUE);
        setListenerChain(listenerChain);
        this.isStacking = true;
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        if (this.standaloneElementDepth == 0) {
            this.hasGroupTopLevelBlock = true;
        }
        startStandaloneElement();
        super.beginGroup(parameters);
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginHeader(level, id, parameters);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginParagraph(parameters);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginQuotation(parameters);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginTable(parameters);
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        startStandaloneElement();
        super.beginFigure(parameters);
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        super.endGroup(parameters);
        endStandaloneElement();
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        super.endHeader(level, id, parameters);
        endStandaloneElement();
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        super.endParagraph(parameters);
        endStandaloneElement();
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        super.endQuotation(parameters);
        endStandaloneElement();
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        super.endTable(parameters);
        endStandaloneElement();
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        super.endFigure(parameters);
        endStandaloneElement();
    }

    /**
     * Handle start of a standalone element and update states.
     */
    protected void startStandaloneElement()
    {
        if (this.standaloneElementDepth == 0) {
            this.standaloneTopLevelElements++;
        }
        this.standaloneElementDepth++;
    }

    /**
     * Handle end of a standalone element and update states.
     */
    protected void endStandaloneElement()
    {
        this.standaloneElementDepth--;
    }

    private boolean shouldInsertGroupBlock()
    {
        return this.standaloneTopLevelElements > 0
            && !(this.standaloneTopLevelElements == 1 && this.hasGroupTopLevelBlock);
    }

    /**
     * Stop stacking events and move them back to the {@link LookaheadChainingListener} to replay them.
     */
    protected void stopStacking()
    {
        if (this.isStacking) {
            // Stop stacking in this listener
            setLookaheadDepth(0);
            this.isStacking = false;

            // Flush all stacked events BUT flush them in the Lookahead Listener at the beginning of the stack, in order
            // to replay them and thus not break the ordering of them since there are begin/end methods in
            // XWikiSyntaxChainingRenderer that will check for the next event (e.g. onNewLine()).
            LookaheadChainingListener listener =
                (LookaheadChainingListener) getListenerChain().getListener(LookaheadChainingListener.class);
            QueueListener previousEvents = getPreviousEvents();
            if (shouldInsertGroupBlock()) {
                previousEvents.offerFirst(previousEvents.new Event(EventType.BEGIN_GROUP,
                    Collections.emptyMap()));
                // Note: we need to insert before the last element since that one is the element closing the stacking
                // (e.g. end item list for a list item) and it's already on the stack.
                previousEvents.add(previousEvents.size() - 1,
                    previousEvents.new Event(EventType.END_GROUP, Collections.emptyMap()));
            }
            listener.transferStart(previousEvents);
        }
    }
}

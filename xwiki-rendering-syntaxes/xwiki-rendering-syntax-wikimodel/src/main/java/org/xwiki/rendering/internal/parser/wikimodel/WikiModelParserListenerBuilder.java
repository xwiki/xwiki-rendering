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
package org.xwiki.rendering.internal.parser.wikimodel;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.listener.ListenerRegistry;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.ListenerProvider;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * Internal class used by {@link AbstractWikiModelParser} to dynamically initialize a list of listeners.
 *
 * @version $Id$
 * @since 15.3RC1
 * @since 14.10.8
 */
@Component(roles = WikiModelParserListenerBuilder.class)
@Singleton
public class WikiModelParserListenerBuilder
{
    @Inject
    private ListenerRegistry listenerRegistry;

    @Inject
    private SyntaxRegistry syntaxRegistry;

    @Inject
    private Logger logger;

    private static class StartChainingListener extends AbstractChainingListener
    {
    }

    class ChainingWrappingListener extends WrappingListener implements ChainingListener
    {
        @Override
        public ListenerChain getListenerChain()
        {
            return null;
        }
    }

    Listener buildListener(String roleHint, Listener listener)
    {
        // TODO: to be documented...
        ListenerChain chain = new ListenerChain();
        ChainingWrappingListener wrappedListener = new ChainingWrappingListener();
        wrappedListener.setWrappedListener(listener);
        StartChainingListener startChainingListener =
            new StartChainingListener();
        startChainingListener.setListenerChain(chain);
        chain.addListener(startChainingListener);
        Syntax syntax = null;
        try {
            syntax = this.syntaxRegistry.resolveSyntax(roleHint);
        } catch (ParseException e) {
            this.logger.warn("Failed to find syntax [{}] in the registry during parser initialization. Cause: [{}]",
                roleHint, getRootCauseMessage(e));
        }

        this.listenerRegistry.registerListeners(chain, ListenerProvider.PARSE_ACTION, syntax);
        chain.addListener(wrappedListener);
        return startChainingListener;
    }
}

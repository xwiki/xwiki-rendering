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

import java.io.Flushable;
import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.internal.listener.ListenerRegistry;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ConsecutiveNewLineStateChainingListener;
import org.xwiki.rendering.listener.chaining.GroupStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxRegistry;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.rendering.listener.ListenerProvider.RENDER_ACTION;

/**
 * XWiki Syntax Renderer implementation common to XWiki Syntax versions greater than 2.0 (X>iki Syntax 2.0, XWiki Syntax
 * 2.1, etc).
 *
 * @version $Id$
 * @since 2.5M2
 */
public abstract class AbstractXWikiSyntaxRenderer extends AbstractChainingPrintRenderer implements Initializable,
    Flushable
{
    @Inject
    private ListenerRegistry listenerRegistry;

    @Inject
    private ComponentDescriptor<PrintRenderer> descriptor;

    @Inject
    private SyntaxRegistry syntaxRegistry;

    @Inject
    private Logger logger;

    /**
     * Allows extending classes to choose which implementation to use.
     *
     * @param chain the rendering chain, see {@link org.xwiki.rendering.listener.chaining.ListenerChain}
     * @return the XWiki Syntax renderer containing the implementation to use for handling the listener's events
     */
    protected abstract ChainingListener createXWikiSyntaxChainingRenderer(ListenerChain chain);

    @Override
    public void initialize() throws InitializationException
    {
        ListenerChain chain = new XWikiSyntaxListenerChain();
        setListenerChain(chain);

        // Construct the listener chain in the right order. Listeners early in the chain are called before listeners
        // placed later in the chain. This chain allows using several listeners that make it easier
        // to write the XWiki Syntax chaining listener, for example for saving states (are we in a list, in a
        // paragraph, are we starting a new line, etc).
        chain.addListener(this);
        String roleHint = this.descriptor.getRoleHint();
        Syntax syntax = null;
        try {
            syntax = this.syntaxRegistry.resolveSyntax(roleHint);
        } catch (ParseException e) {
            this.logger.warn("Failed to find syntax [{}] in the registry during renderer initialization. Cause: [{}]",
                roleHint, getRootCauseMessage(e));
        }
        
        this.listenerRegistry.getListeners(chain, RENDER_ACTION, syntax).forEach(chain::addListener);
        chain.addListener(new LookaheadChainingListener(chain, 2));
        chain.addListener(new GroupStateChainingListener(chain));
        chain.addListener(new BlockStateChainingListener(chain));
        chain.addListener(new ConsecutiveNewLineStateChainingListener(chain));
        chain.addListener(createXWikiSyntaxChainingRenderer(chain));
    }

    @Override
    public void flush() throws IOException
    {
        endDocument(MetaData.EMPTY);
    }
}

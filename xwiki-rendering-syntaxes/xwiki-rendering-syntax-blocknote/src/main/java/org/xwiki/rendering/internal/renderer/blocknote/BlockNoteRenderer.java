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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Used to render the XDOM to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named("blocknote/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class BlockNoteRenderer extends AbstractChainingPrintRenderer implements Initializable
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    @Override
    public void initialize() throws InitializationException
    {
        ListenerChain chain = new ListenerChain();
        setListenerChain(chain);

        // Construct the listener chain in the right order. Listeners early in the chain are called before listeners
        // placed later in the chain.
        chain.addListener(this);
        chain.addListener(new BlockStateChainingListener(chain));
        chain.addListener(new EmptyBlockChainingListener(chain));
        chain.addListener(new XDOMPathChainingListener(chain));
        chain.addListener(new TextChainingListener(chain));
        chain.addListener(new TypographyChainingListener(chain));
        chain.addListener(new ListChainingListener(chain));
        chain.addListener(new TableChainingListener(chain));
        chain.addListener(new MacroChainingListener(chain));
        chain.addListener(new ImageChainingListener(chain, getWikiModel()));
        chain.addListener(new InlineContentChainingListener(chain));
        chain.addListener(new BlockNoteChainingPrintRenderer(chain));
    }

    private WikiModel getWikiModel()
    {
        ComponentManager componentManager = this.componentManagerProvider.get();
        // Try to find a WikiModel implementation and set it if it can be found. If not it means we're in non wiki
        // mode (i.e. no attachment in wiki documents and no links to documents for example).
        if (componentManager.hasComponent(WikiModel.class)) {
            try {
                return componentManager.getInstance(WikiModel.class);
            } catch (ComponentLookupException e) {
                this.logger.error("Failed to initialize the default WikiModel implementation", e);
            }
        }
        return null;
    }
}

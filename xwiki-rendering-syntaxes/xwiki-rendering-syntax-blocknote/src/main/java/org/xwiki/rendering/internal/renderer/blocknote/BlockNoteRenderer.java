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

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;

/**
 * Used to render the XDOM to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named("blocknote/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class BlockNoteRenderer extends AbstractChainingPrintRenderer implements Initializable
{
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
        chain.addListener(new ImageChainingListener(chain));
        chain.addListener(new InlineContentChainingListener(chain));
        chain.addListener(new BlockNoteChainingPrintRenderer(chain));
    }
}

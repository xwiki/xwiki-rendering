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
package org.xwiki.rendering.internal.macro.html;

import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.MetaDataStateChainingListener;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.stability.Unstable;

/**
 * Define a generic HTMLMacroRenderer with the right {@link ListenerChain}.
 *
 * @version $Id$
 * @since 11.4RC1
 */
@Unstable
public abstract class AbstractHTMLMacroRenderer extends AbstractChainingPrintRenderer implements Initializable
{
    /**
     * @return the specific syntax {@link AbstractChainingPrintRenderer} that is required for the concrete HTMLMacro
     *  renderer.
     */
    protected abstract AbstractChainingPrintRenderer getSyntaxRenderer();

    /**
     * {@inheritDoc}
     *
     * @since 2.0M3
     */
    @Override
    public void initialize() throws InitializationException
    {
        ListenerChain chain = new ListenerChain();
        setListenerChain(chain);

        // Construct the listener chain in the right order. Listeners early in the chain are called before listeners
        // placed later in the chain.
        chain.addListener(this);
        chain.addListener(new HTMLMacroBlockStateChainingListener(chain));
        chain.addListener(new EmptyBlockChainingListener(chain));
        chain.addListener(new MetaDataStateChainingListener(chain));
        chain.addListener(new HTMLMacroChainingRenderer(getSyntaxRenderer()));
    }
}

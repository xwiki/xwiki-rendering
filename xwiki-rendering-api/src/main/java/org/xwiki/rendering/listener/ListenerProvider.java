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

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.stability.Unstable;

/**
 * Allows to access a listener instance.
 *
 * @version $Id$
 * @since 15.3RC1
 * @since 14.10.8
 */
@Role
@Unstable
public interface ListenerProvider
{
    /**
     * Parse action identifier.
     */
    String PARSE_ACTION = "parse";

    /**
     * Render action identifier.
     */
    String RENDER_ACTION = "render";
    
    /**
     * @param action the action performed by the listener ("render" or "parse")
     * @param syntax the hint of the syntax using for the action
     * @return {@code true} when the listener provider can return a listener for the given action and syntaxHint
     */
    boolean accept(String action, Syntax syntax);

    /**
     * @param listenerChain the listener chain in which the listener will be included
     * @return the listener to add to the listener chain
     */
    ChainingListener getListener(ListenerChain listenerChain);
}

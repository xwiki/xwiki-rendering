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

import java.util.Map;

import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

/**
 * Custom BlockStateChainingListener to handle the fact that the {@link HTMLMacroXHTMLChainingRenderer} renderer
 * executes inside the HTML macro. Note that this is needed for example for the
 * {@link HTMLMacroXHTMLChainingRenderer#beginHeader(HeaderLevel, String, Map)} method to generate the proper CSS class
 * to signify that the  header is a generated one (and thus there should be no edit link).
 *
 * @version $Id$
 * @since 9.10RC1
 */
public class HTMLMacroBlockStateChainingListener extends BlockStateChainingListener
{
    /**
     * @param listenerChain the rendering chain
     */
    public HTMLMacroBlockStateChainingListener(ListenerChain listenerChain)
    {
        super(listenerChain);
    }

    @Override
    public int getMacroDepth()
    {
        // Simulate that we're inside a macro (we're inside the HTML Macro !)
        return super.getMacroDepth() + 1;
    }

    @Override
    public boolean isInMacro()
    {
        // Simulate that we're inside a macro (we're inside the HTML Macro !)
        return true;
    }
}

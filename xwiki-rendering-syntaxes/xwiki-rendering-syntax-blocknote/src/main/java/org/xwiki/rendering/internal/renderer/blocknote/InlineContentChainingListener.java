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

import java.util.Map;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.FREE_STANDING;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.HREF;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.LINK;

/**
 * Renders inline content to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public class InlineContentChainingListener extends AbstractChainingListener
{
    private final Context context;

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public InlineContentChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            ObjectNode link = this.context.getBlockNoteState().beginBlock(LINK, true, true, false, false);
            link.set(HREF, this.context.getBlockNoteState().toJSON(reference));
            if (freestanding) {
                ((ObjectNode) link.path(PROPS)).put(FREE_STANDING, freestanding);
            }
        }
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            // Text style has changed so we need to end the current text block.
            // Make sure the styles for the ended text block don't include the new format.
            Block formatBlock = this.context.getXDOMPath().pop();
            this.context.getBlockNoteState().endTextBlock();
            this.context.getXDOMPath().push(formatBlock);
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            this.context.getBlockNoteState().maybeAddTextBlock(true);
        }
    }
}

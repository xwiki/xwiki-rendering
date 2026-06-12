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
import java.util.Set;

import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CONTENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.TYPE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AudioBlockParser.AUDIO;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.DividerBlockParser.DIVIDER;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.FileBlockParser.FILE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.GroupBlockParser.GROUP;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.HeadingBlockParser.HEADING;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.HeadingBlockParser.LEVEL;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ImageBlockParser.IMAGE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ParagraphBlockParser.PARAGRAPH;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.VideoBlockParser.VIDEO;

/**
 * Renders paragraphs, headings and horizontal lines to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public class TypographyChainingListener extends AbstractChainingListener
{
    private static final Set<String> EMBEDS = Set.of(AUDIO, FILE, IMAGE, VIDEO);

    private final Context context;

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public TypographyChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    //
    // Events
    //

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.context.getBlockNoteState().beginBlock(PARAGRAPH, true, true, false, true);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return;
        }

        JsonNode paragraph = this.context.getBlockNoteState().endBlock();
        JsonNode content = paragraph.path(CONTENT);
        if (parameters.isEmpty() && content.size() == 1 && EMBEDS.contains(content.path(0).path(TYPE).asText())) {
            // We have a paragraph with no parameters that contains a single embed block. BlockNote supports block-level
            // embeds, so we can unwrap the embed from the paragraph.
            ArrayNode siblings = (ArrayNode) this.context.getBlockNoteState().getBlockNotePath().peek();
            siblings.set(siblings.size() - 1, content.get(0));
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        // Generate empty paragraphs.
        for (int i = 0; i < count; i++) {
            beginParagraph(EMPTY_PARAMETERS);
            endParagraph(EMPTY_PARAMETERS);
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        ObjectNode headingBlock = this.context.getBlockNoteState().beginBlock(HEADING, true, true, false, true);
        ObjectNode properties = (ObjectNode) headingBlock.get(PROPS);
        properties.put(LEVEL, level.getAsInt());
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.context.getBlockNoteState().beginBlock(DIVIDER, false, false, false, true);
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        this.context.getBlockNoteState().beginBlock(GROUP, true, false, true, true);
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        this.context.getBlockNoteState().endBlock();
    }
}

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
package org.xwiki.rendering.internal.parser.blocknote.blocks;

import java.util.Deque;
import java.util.Map;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Heading block parser.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named(HeadingBlockParser.HEADING)
@Singleton
public class HeadingBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String HEADING = "heading";

    /**
     * The property specifying the heading level (1 to 6).
     */
    public static final String LEVEL = "level";

    @Override
    public void parse(ObjectNode headingBlock, Deque<Context> contextStack) throws ParseException
    {
        HeaderLevel level = getLevel(headingBlock);
        beginSection(level, contextStack);

        Map<String, String> parameters = getBlockParameters(headingBlock);
        Context context = contextStack.peek();
        // Generate the id from the plain text content of the heading.
        String id = context.idGenerator().generateUniqueId("H", getTextContent(headingBlock));
        context.listener().beginHeader(level, id, parameters);

        visitInlineChildBlocks(headingBlock, CONTENT, contextStack);

        context.listener().endHeader(level, id, parameters);
    }

    @Override
    public void onParentBlockEnd(Deque<Context> contextStack)
    {
        endSections(contextStack, HeaderLevel.LEVEL1);
    }

    private HeaderLevel getLevel(ObjectNode headingBlock) throws ParseException
    {
        JsonNode level = headingBlock.path(PROPS).path(LEVEL);
        if (!level.isInt()) {
            throw new ParseException("The 'level' property of a 'heading' block must have an integer value.");
        }
        try {
            return HeaderLevel.parseInt(level.asInt());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid value for the 'level' property of a 'heading' block: " + level.asInt(),
                e);
        }
    }

    private void beginSection(HeaderLevel level, Deque<Context> contextStack)
    {
        endSections(contextStack, level);
        contextStack.push(contextStack.peek().withSectionLevel(level));
        contextStack.peek().listener().beginSection(Listener.EMPTY_PARAMETERS);
    }

    private void endSections(Deque<Context> contextStack, HeaderLevel upToLevel)
    {
        while (!contextStack.isEmpty() && contextStack.peek().sectionLevel() != null
            && contextStack.peek().sectionLevel().ordinal() >= upToLevel.ordinal()) {
            Context context = contextStack.pop();
            context.listener().endSection(Listener.EMPTY_PARAMETERS);
        }
    }
}

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
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.STYLES;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.TEXT;

/**
 * Paragraph block parser.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@Component
@Named(ParagraphBlockParser.PARAGRAPH)
@Singleton
public class ParagraphBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String PARAGRAPH = "paragraph";

    @Override
    public void parse(ObjectNode paragraphBlock, Deque<Context> contextStack) throws ParseException
    {
        if (isEmptyLine(paragraphBlock, contextStack)) {
            contextStack.peek().listener().onEmptyLines(getEmptyLinesCount(paragraphBlock, contextStack));
        } else {
            Map<String, String> parameters = getBlockParameters(paragraphBlock);
            contextStack.peek().listener().beginParagraph(parameters);

            visitInlineChildBlocks(paragraphBlock, CONTENT, contextStack);

            contextStack.peek().listener().endParagraph(parameters);
        }
    }

    private boolean isEmptyLine(JsonNode block, Deque<Context> contextStack)
    {
        JsonNode content = block.path(CONTENT);
        Map<String, String> parameters = getBlockParameters(block);
        // BlockNote doesn't support the concept of empty lines so we map empty lines to empty paragraphs. Empty lines
        // are used to separate blocks of content so normally they are not the first or the last child.
        return PARAGRAPH.equals(block.path(TYPE).asText()) && parameters.isEmpty()
            && !contextStack.peek().isFirstOrLastChild(block) && isEmptyContent(content);
    }

    private boolean isEmptyContent(JsonNode content)
    {
        if (content.isArray()) {
            ArrayNode contentArray = (ArrayNode) content;
            return contentArray.isEmpty() || (contentArray.size() == 1 && isEmptyContent(contentArray.get(0)));
        } else if (content.isObject()) {
            return TEXT.equals(content.path(TYPE).asText()) && content.path(STYLES).isEmpty()
                && isEmptyContent(content.path(TEXT));
        } else if (content.isTextual()) {
            return content.asText().isEmpty();
        } else {
            return true;
        }
    }

    private int getEmptyLinesCount(ObjectNode emptyLine, Deque<Context> contextStack)
    {
        int count = 1;
        ArrayNode siblings = contextStack.peek().siblings();
        int startIndex = contextStack.peek().indexOf(emptyLine);
        if (startIndex >= 0) {
            while ((startIndex + count) < siblings.size()
                && isEmptyLine(siblings.get(startIndex + count), contextStack)) {
                // Make sure this empty line is not processed again.
                ((ObjectNode) siblings.get(startIndex + count)).put(SKIP, true);
                count++;
            }
        }
        return count;
    }
}

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
import java.util.function.Consumer;

import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base class for BlockNote embed block parsers.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public abstract class AbstractEmbedBlockParser extends AbstractBlockParser
{
    /**
     * The prefix used for the macro name when parsing an embed block as a macro call.
     */
    public static final String EMBED_MACRO_PREFIX = "blocknote:";

    @Override
    public void parse(ObjectNode embedBlock, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = getEmbedParameters(embedBlock);

        // We use a macro block because XDOM doesn't have dedicated blocks for file, video and audio embeds.
        contextStack.peek().listener().onMacro(EMBED_MACRO_PREFIX + getEmbedType(), parameters, null,
            contextStack.peek().inline());
    }

    protected abstract String getEmbedType();

    @Override
    public void traverse(ObjectNode embedBlock, Consumer<ObjectNode> blockConsumer)
    {
        // Embed blocks don't have child blocks, so we don't need to traverse them.
        blockConsumer.accept(embedBlock);
    }

    private Map<String, String> getEmbedParameters(ObjectNode embedBlock)
    {
        Map<String, String> parameters = getBlockParameters(embedBlock);
        ObjectNode embedProperties = (ObjectNode) embedBlock.path(PROPS);
        embedProperties.forEachEntry((property, value) -> {
            if (!BLOCK_STYLES.containsKey(property)) {
                parameters.put(property, value.asText());
            }
        });

        return parameters;
    }
}

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.inject.Inject;

import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.internal.parser.blocknote.FragmentRenderer;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base implementation for macro block parsers.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public abstract class AbstractMacroBlockParser extends AbstractBlockParser
{
    /**
     * The macro block property used to hold the macro call information (i.e. the macro name, parameters and content).
     */
    public static final String CALL = "call";

    /**
     * The macro call property used to hold the macro name.
     */
    public static final String NAME = "name";

    /**
     * The macro call property used to hold the macro content.
     */
    public static final String PARAMETERS = "parameters";

    @Inject
    protected FragmentRenderer fragmentRenderer;

    @Override
    public void traverse(ObjectNode macroBlock, Consumer<ObjectNode> blockConsumer)
    {
        // Macro blocks are black-boxes most of the time, so we don't need to traverse them.
        blockConsumer.accept(macroBlock);
    }

    protected void visitMacro(ObjectNode macroBlock, Deque<Context> contextStack, boolean inline) throws ParseException
    {
        JsonNode macroCall = macroBlock.path(PROPS).path(CALL);
        if (!macroCall.isObject()) {
            throw new ParseException("The 'call' property of a macro block must be a JSON object.");
        }

        JsonNode macroNameNode = macroCall.path(NAME);
        if (!macroNameNode.isTextual()) {
            throw new ParseException("The macro name is missing or its value is unexpected.");
        }

        String macroName = macroNameNode.asText();
        Map<String, String> macroParameters = getMacroParameters(macroCall.path(PARAMETERS), contextStack);
        String macroContent = renderFragment((ObjectNode) macroCall, CONTENT, true, contextStack);

        contextStack.peek().listener().onMacro(macroName, macroParameters, macroContent, inline);
    }

    private Map<String, String> getMacroParameters(JsonNode parameters, Deque<Context> contextStack)
        throws ParseException
    {
        if (!parameters.isObject()) {
            throw new ParseException("The 'parameters' property of a macro call must be a JSON object.");
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : parameters.properties()) {
            params.put(entry.getKey(), renderFragment((ObjectNode) parameters, entry.getKey(), true, contextStack));
        }

        return params;
    }

    private String renderFragment(ObjectNode parent, String fragmentKey, boolean inline, Deque<Context> contextStack)
        throws ParseException
    {
        JsonNode fragment = parent.path(fragmentKey);
        if (fragment.isValueNode()) {
            return fragment.asText("");
        } else if (fragment.isObject() || fragment.isArray()) {
            this.fragmentRenderer.beginFragment(contextStack);
            contextStack.push(contextStack.peek().withInline(inline));
            if (fragment.isObject()) {
                visitBlock((ObjectNode) fragment, contextStack)
                    .ifPresent(parentBlockEndCallback -> parentBlockEndCallback.accept(contextStack));
            } else {
                visitChildBlocks(parent, fragmentKey, contextStack);
            }
            contextStack.pop();
            return this.fragmentRenderer.endFragment(contextStack);
        }
        return null;
    }
}

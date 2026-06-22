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

import java.util.List;
import java.util.Map;

import org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CHILDREN;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CONTENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.TYPE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractMacroBlockParser.CALL;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractMacroBlockParser.NAME;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractMacroBlockParser.PARAMETERS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.InlineMacroBlockParser.INLINE_MACRO;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.MacroBlockParser.MACRO;

/**
 * Renders macros to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public class MacroChainingListener extends AbstractChainingListener
{
    /**
     * The type representing a list of blocks.
     */
    private static final String BLOCK_LIST = "java.util.List<org.xwiki.rendering.block.Block>";

    private final Context context;

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public MacroChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        // Treat macros that have not been executed as macros without output.
        beginMacroMarker(id, parameters, content, inline);
        endMacroMarker(id, parameters, content, inline);
    }

    @Override
    public void beginMacroMarker(String id, Map<String, String> parameters, String content, boolean inline)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            this.context.getTextState().addText(content, inline);
        } else {
            ObjectNode macro =
                this.context.getBlockNoteState().beginBlock(inline ? INLINE_MACRO : MACRO, true, false, false, !inline);
            ObjectNode macroProperties = (ObjectNode) macro.path(PROPS);
            macroProperties.remove(AbstractBlockParser.PARAMETERS);
            macroProperties.set(CALL, getMacroCall(id, parameters, content));
            ArrayNode output = this.context.getBlockNoteState().getObjectMapper().createArrayNode();
            macroProperties.set("output", output);
            this.context.getBlockNoteState().getBlockNotePath().push(output);
        }
    }

    private ObjectNode getMacroCall(String id, Map<String, String> parameters, String content)
    {
        ObjectNode macroParams = this.context.getBlockNoteState().getObjectMapper().createObjectNode();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            macroParams.put(entry.getKey(), entry.getValue());
        }

        ObjectNode macroCall = this.context.getBlockNoteState().getObjectMapper().createObjectNode();
        macroCall.put(NAME, id);
        macroCall.set(PARAMETERS, macroParams);
        if (content != null) {
            macroCall.put(CONTENT, content);
        }

        return macroCall;
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        if (!this.context.getTextState().isPlainTextRendering() && metadata.contains(MetaData.NON_GENERATED_CONTENT)) {
            ObjectNode macroBlock = getMacroBlock();
            if (macroBlock == null) {
                throw new IllegalStateException("Unexpected non-generated content outside macro markers.");
            }
            ObjectNode editableArea =
                this.context.getBlockNoteState().beginBlock("xwiki:editable", false, false, true, true);
            if (metadata.contains(MetaData.PARAMETER_NAME)) {
                editableArea.put("name", (String) metadata.getMetaData(MetaData.PARAMETER_NAME));
            }
            if (!BLOCK_LIST.equals(metadata.getMetaData(MetaData.NON_GENERATED_CONTENT))) {
                this.context.getTextState().beginPlainTextRendering();
            }
        }
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        if (!this.context.getTextState().isPlainTextRendering() && metadata.contains(MetaData.NON_GENERATED_CONTENT)) {
            ObjectNode editableArea = (ObjectNode) this.context.getBlockNoteState().endBlock();

            Object value = editableArea.remove(CHILDREN);
            if (!BLOCK_LIST.equals(metadata.getMetaData(MetaData.NON_GENERATED_CONTENT))) {
                value = this.context.getTextState().endPlainTextRendering();
            }

            ObjectNode macroBlock = getMacroBlock();
            if (metadata.contains(MetaData.PARAMETER_NAME)) {
                // Inplace editable macro parameter.
                String parameterName = (String) metadata.getMetaData(MetaData.PARAMETER_NAME);
                setMacroParameter(macroBlock, parameterName, value);
            } else {
                // Inplace editable macro content.
                setMacroContent(macroBlock, value);
            }
        }
    }

    private ObjectNode getMacroBlock()
    {
        for (JsonNode node : this.context.getBlockNoteState().getBlockNotePath()) {
            String type = node.path(TYPE).asText();
            if (List.of(MACRO, INLINE_MACRO).contains(type)) {
                return (ObjectNode) node;
            }
        }
        return null;
    }

    private void setMacroParameter(ObjectNode macroBlock, String name, Object value)
    {
        ObjectNode macroCall = (ObjectNode) macroBlock.path(PROPS).path(CALL);
        ObjectNode parameters = (ObjectNode) macroCall.path(PARAMETERS);
        if (value instanceof String stringValue) {
            parameters.put(name, stringValue);
        } else {
            parameters.set(name, (JsonNode) value);
        }
    }

    private void setMacroContent(ObjectNode macroBlock, Object value)
    {
        ObjectNode macroCall = (ObjectNode) macroBlock.path(PROPS).path(CALL);
        if (value instanceof String stringValue) {
            macroCall.put(CONTENT, stringValue);
        } else {
            macroCall.set(CONTENT, (JsonNode) value);
        }
    }
}

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

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.syntax.Syntax;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.CONTENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PARAMETERS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CodeBlockParser.CODE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CodeBlockParser.LANGUAGE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CodeBlockParser.VERBATIM_LANGUAGE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.STYLES;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.TEXT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.TextBlockParser.VERBATIM;

/**
 * Renders text content to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public class TextChainingListener extends AbstractChainingListener
{
    private final Context context;

    private int plainTextRenderingNestingLevel;

    private StringBuilder plainText = new StringBuilder();

    /**
     * Creates a new instance that uses the given listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public TextChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    //
    // Events
    //

    @Override
    public void onNewLine()
    {
        this.plainText.append('\n');
    }

    @Override
    public void onSpace()
    {
        this.plainText.append(' ');
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.plainText.append(symbol);
    }

    @Override
    public void onWord(String word)
    {
        this.plainText.append(word);
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (isPlainTextRendering()) {
            addText(content, inline);
        } else if (inline) {
            // End the previous text block, if any.
            this.context.getBlockNoteState().endTextBlock();
            // Start a new text block with the verbatim style.
            ObjectNode textBlock = beginTextBlock(content);
            ((ObjectNode) textBlock.get(STYLES)).put(VERBATIM, true);
        } else {
            ObjectNode code = this.context.getBlockNoteState().beginBlock(CODE, true, false, false, true);
            ObjectNode codeProperties = (ObjectNode) code.path(PROPS);
            ObjectNode unknownParameters = (ObjectNode) codeProperties.path(PARAMETERS);
            unknownParameters.remove(List.of(VERBATIM_LANGUAGE));
            if (parameters.containsKey(VERBATIM_LANGUAGE)) {
                codeProperties.put(LANGUAGE, parameters.get(VERBATIM_LANGUAGE));
            }
            code.put(CONTENT, content);
        }
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        this.context.getBlockNoteState().beginBlock("xwikiRaw", true, false, false, false);
        JsonNode rawBlock = this.context.getBlockNoteState().endBlock();
        if (rawBlock != null) {
            ObjectNode properties = (ObjectNode) rawBlock.path(PROPS);
            properties.put("syntax", syntax.toIdString());
            properties.put("text", text);
            properties.remove(PARAMETERS);
        }
    }

    //
    // Utility methods to manage the pain text buffer.
    //

    private ObjectNode beginTextBlock(String content)
    {
        ObjectNode textBlock = this.context.getBlockNoteState().beginBlock(TEXT, true, false, false, false);
        ObjectNode styles = (ObjectNode) textBlock.remove(PROPS);
        textBlock.set(STYLES, styles);
        JsonNode unknownParameters = styles.path(PARAMETERS);
        if (unknownParameters.size() == 0) {
            styles.remove(PARAMETERS);
        }
        textBlock.put(TEXT, content);
        return textBlock;
    }

    /**
     * Indicates that the content being rendered should be rendered as plain text, without any formatting.
     */
    public void beginPlainTextRendering()
    {
        this.plainTextRenderingNestingLevel++;
    }

    /**
     * @return {@code true} if the content being rendered should be rendered as plain text, without any formatting, or
     *         {@code false} otherwise
     */
    public boolean isPlainTextRendering()
    {
        return this.plainTextRenderingNestingLevel > 0;
    }

    /**
     * Indicates that the rendering of content as plain text has ended.
     * 
     * @return the plain text content that was rendered, or {@code null} if the rendering of content as plain text has
     *         not ended yet
     */
    public String endPlainTextRendering()
    {
        this.plainTextRenderingNestingLevel--;
        if (this.plainTextRenderingNestingLevel == 0) {
            String result = this.plainText.toString();
            this.plainText.setLength(0);
            return result;
        }
        return null;
    }

    /**
     * @return the plain text content that was rendered so far
     */
    public StringBuilder getPlainText()
    {
        return this.plainText;
    }

    /**
     * Append text to the plain text content being rendered.
     *
     * @param text the text to append
     * @param inline indicates whether the text should be appended on the same line, or on a new line
     */
    public void addText(String text, boolean inline)
    {
        if (StringUtils.isNotEmpty(text)) {
            if (inline) {
                this.plainText.append(text);
            } else {
                this.plainText.append('\n').append(text).append('\n');
            }
        }
    }
}

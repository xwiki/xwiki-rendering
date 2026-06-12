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

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.blocknote.Context;
import org.xwiki.rendering.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Code block parser.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
@Component
@Named(CodeBlockParser.CODE)
@Singleton
public class CodeBlockParser extends AbstractBlockParser
{
    /**
     * This component's role hint. Also the type of blocks handled by this parser.
     */
    public static final String CODE = "codeBlock";

    /**
     * The code block property that indicates the code language.
     */
    public static final String LANGUAGE = "language";

    /**
     * The parameter used on verbatim blocks to store the language of the verbatim content. This is mapped to the
     * language of the code block from BlockNote.
     */
    public static final String VERBATIM_LANGUAGE = "data-xwiki-verbatim-language";

    @Override
    public void parse(ObjectNode codeBlock, Deque<Context> contextStack) throws ParseException
    {
        Map<String, String> parameters = new LinkedHashMap<>();
        JsonNode language = codeBlock.path(PROPS).path(LANGUAGE);
        if (language.isTextual()) {
            parameters.put(VERBATIM_LANGUAGE, language.asText());
        }
        String code = getTextContent(codeBlock);
        contextStack.peek().listener().onVerbatim(code, false, parameters);
    }
}

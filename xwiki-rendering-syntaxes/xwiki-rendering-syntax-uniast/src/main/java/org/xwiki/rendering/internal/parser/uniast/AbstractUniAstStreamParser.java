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
package org.xwiki.rendering.internal.parser.uniast;

import java.io.IOException;
import java.io.Reader;
import java.util.Deque;
import java.util.LinkedList;

import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.uniast.UniAst10SyntaxProvider.UNIAST_1_0;

/**
 * Base implementation for UniAst stream parsers.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
public abstract class AbstractUniAstStreamParser implements StreamParser
{
    @Override
    public Syntax getSyntax()
    {
        return UNIAST_1_0;
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        parse(source, listener, new IdGenerator());
    }

    @Override
    public void parse(Reader source, Listener listener, IdGenerator idGenerator) throws ParseException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = objectMapper.readTree(source);
        } catch (IOException e) {
            throw new ParseException("Failed to parse the UniAst JSON source.", e);
        }

        if (!root.isObject()) {
            throw new ParseException("The UniAst root element must be a JSON object.");
        }

        Deque<Context> contextStack = new LinkedList<>();
        contextStack.push(new Context(listener, idGenerator, false, null, objectMapper.createArrayNode(), null));
        visitRoot((ObjectNode) root, contextStack);
    }

    protected abstract void visitRoot(ObjectNode root, Deque<Context> contextStack) throws ParseException;
}

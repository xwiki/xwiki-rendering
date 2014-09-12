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
package org.xwiki.rendering.internal.parser.markdown;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;

/**
 * Common implementation for all Markdown Syntax versions.
 *
 * @version $Id$
 * @since 5.2RC1
 */
public abstract class AbstractMarkdownStreamParser implements StreamParser
{
    /**
     * @return the Pegdown visitor implementation to use to visit the root node
     */
    protected abstract PegdownVisitor getPegdownVisitor();

    /**
     * @param processor the Pegdown process instance on which to parse the passed content
     * @param source the content to parse
     * @param listener receive event for each element
     * @throws ParseException if the source cannot be read or an unexpected error happens during the parsing. Parsers
     *             should be written to not generate any error as much as possible.
     */
    protected void parse(PegDownProcessor processor, Reader source, Listener listener) throws ParseException
    {
        try {
            RootNode rootNode = processor.parseMarkdown(IOUtils.toString(source).toCharArray());
            getPegdownVisitor().visit(rootNode, listener);
        } catch (IOException e) {
            throw new ParseException("Failed to retrieve the source to parse", e);
        }
    }
}

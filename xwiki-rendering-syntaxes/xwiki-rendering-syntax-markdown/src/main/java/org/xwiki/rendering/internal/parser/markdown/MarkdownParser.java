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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Markdown parser based on the <a href="https://github.com/sirthias/pegdown">Pegdown Parser</a>.
 *
 * @version $Id$
 * @since 4.1M1
 */
@Component
@Named("markdown/1.0")
@Singleton
public class MarkdownParser implements Parser
{
    /**
     * PegDown AST to XDOM converter.
     */
    @Inject
    private PegdownToXDOMConverter converter;

    @Override
    public Syntax getSyntax()
    {
        return Syntax.MARKDOWN_1_0;
    }

    @Override
    public XDOM parse(Reader source) throws ParseException
    {
        // The Pegdown processor is not thread safe, thus we need one per thread at least.
        PegDownProcessor processor = new PegDownProcessor(Extensions.ALL & ~Extensions.HARDWRAPS);

        try {
            RootNode rootNode = processor.parseMarkdown(IOUtils.toString(source).toCharArray());

            return this.converter.buildBlocks(rootNode);
        } catch (IOException e) {
            throw new ParseException("Failed to retrieve ", e);
        }
    }
}

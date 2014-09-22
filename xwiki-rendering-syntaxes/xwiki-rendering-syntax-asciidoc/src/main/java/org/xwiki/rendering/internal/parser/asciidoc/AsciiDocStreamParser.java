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
package org.xwiki.rendering.internal.parser.asciidoc;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.internal.JRubyAsciidoctor;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Stream Parser for AsciiDoc syntax.
 * 
 * @version $Id$
 * @since 5.4M1
 */
@Component
@Named("asciidoc/1.0")
@Singleton
public class AsciiDocStreamParser implements StreamParser, Initializable
{
    private Asciidoctor asciidoctor;

    @Override
    public Syntax getSyntax()
    {
        return null;
    }

    @Override
    public void initialize() throws InitializationException
    {
        this.asciidoctor = JRubyAsciidoctor.create();
    }

    @Override
    public void parse(Reader source, Listener listener) throws ParseException
    {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put(Asciidoctor.STRUCTURE_MAX_LEVEL, 10);
            Document document = this.asciidoctor.load(IOUtils.toString(source), options);
            for (AbstractBlock block : document.blocks()) {
                System.out.println(":" + block.context() + " - " + block.title());
                for (AbstractBlock childBlock : block.blocks()) {
                    if (childBlock.context().equals("paragraph")) {
                        System.out.println("  :paragraph, lines: " + ((Block) childBlock).lines());
                    } else {
                        System.out.println("  :" + childBlock.context());
                    }
                }
            }
        } catch (Exception e) {
            throw new ParseException("Failed to parse AsciiDoc content", e);
        }
    }
}

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
package org.xwiki.rendering.internal.parser;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.doxia.parser.Parser;
import org.tautua.markdownpapers.doxia.MarkdownParser;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.parser.doxia.AbstractDoxiaParser;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Markdown parser based on markdown doxia module.
 * 
 * @version $Id$
 * @since 3.4M1
 */
@Component
@Named("markdown/1.0")
@Singleton
public class DoxiaMarkdownParser extends AbstractDoxiaParser
{
    
    @Override
    public Syntax getSyntax()
    {
        return Syntax.MARKDOWN_1_0;
    }

    @Override
    public Parser createDoxiaParser()
    {
        return new MarkdownParser();
    }

}

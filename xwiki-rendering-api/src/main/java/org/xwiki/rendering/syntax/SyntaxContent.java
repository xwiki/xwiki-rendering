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
package org.xwiki.rendering.syntax;

/**
 * Represents text content that has a syntax.
 * 
 * @version $Id$
 * @since 8.2RC1
 */
public class SyntaxContent
{
    /**
     * The syntax of the content.
     */
    private final Syntax syntax;

    /**
     * The text content.
     */
    private final String content;

    /**
     * Creates a new instance that wraps the given text content and its syntax.
     * 
     * @param content the text content
     * @param syntax the syntax of the content
     */
    public SyntaxContent(String content, Syntax syntax)
    {
        this.syntax = syntax;
        this.content = content;
    }

    /**
     * @return the syntax of the content
     */
    public Syntax getSyntax()
    {
        return syntax;
    }

    /**
     * @return the text content
     */
    public String getContent()
    {
        return content;
    }
}

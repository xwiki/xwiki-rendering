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
package org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel;

import org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel.MacroTagHandler.ConfluenceMacro;
import org.xwiki.rendering.wikimodel.xhtml.handler.PreserveTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler.TagStack.TagContext;

/**
 * Handles text inside a macro which might contain markup.
 * <p>
 * Example:
 * <p>
 * {@code
 * <ac:rich-text-body><p>some <em>text</em> here</p></ac:rich-text-body>
 * }
 * <p>
 * The current implementation strips all markup from the macro content, returning plain text instead.
 * This is due to a limitation of the wiki model not allowing markup inside macros.
 * 
 * @version $Id$
 * @since 5.4M1
 */
public class RichTextBodyTagHandler extends PreserveTagHandler
{

    @Override
    protected void handlePreservedContent(TagContext context, String preservedContent)
    {
        ConfluenceMacro macro = (ConfluenceMacro) context.getTagStack().getStackParameter("confluence-container");

        if (macro != null) {
            macro.content = preservedContent;
        }
    }

}
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
package org.xwiki.rendering.wikimodel.xhtml.handler;

import org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;

import static org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo.MACRO_START;
import static org.xwiki.rendering.wikimodel.xhtml.impl.MacroInfo.MACRO_STOP;

/**
 * Handle Macro definitions in comments (we store macro definitions in a comment since it wouldn't be possible at all to
 * reconstruct them from the result of their execution).
 * 
 * @version $Id$
 * @since 4.0M1
 */
public class CommentHandler
{
    public void onComment(String content, TagStack stack)
    {
        // Format of a macro definition in comment:
        // <!--startmacro:velocity|-||-|
        // Some **content**
        // --><p>Some <strong>content</strong></p><!--stopmacro-->
        if (content.startsWith(MACRO_START)) {
            if (!stack.shouldIgnoreElements()) {
                MacroInfo macroInfo = new MacroInfo(content);

                // If we're inside a block element then issue an inline macro
                // event
                // otherwise issue a block macro event
                if (stack.isInsideBlockElement()) {
                    stack.getScannerContext().onMacroInline(macroInfo.getName(), macroInfo.getParameters(), macroInfo.getContent());
                } else {
                    TagHandler.sendEmptyLines(stack);
                    stack.getScannerContext().onMacroBlock(macroInfo.getName(), macroInfo.getParameters(), macroInfo.getContent());
                }
            }

            stack.setIgnoreElements();
        } else if (content.startsWith(MACRO_STOP)) {
            stack.unsetIgnoreElements();
        }
    }
}

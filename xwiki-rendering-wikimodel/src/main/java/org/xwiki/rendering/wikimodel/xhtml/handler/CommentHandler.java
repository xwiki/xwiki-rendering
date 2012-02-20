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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.WikiScannerUtil;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler.TagStack;

/**
 * Handle Macro definitions in comments (we store macro definitions in a comment
 * since it wouldn't be possible at all to reconstruct them from the result of
 * their execution).
 *
 * @version $Id$
 * @since 4.0M1
 */
public class CommentHandler
{
    private static final String MACRO_SEPARATOR = "|-|";

    public void onComment(String content, TagStack stack)
    {
        // Format of a macro definition in comment:
        // <!--startmacro:velocity|-||-|
        // Some **content**
        // --><p>Some <strong>content</strong></p><!--stopmacro-->
        if (content.startsWith("startmacro:")) {
            if (!(Boolean) stack.getStackParameter("ignoreElements")) {
                String macroName;
                WikiParameters macroParams = WikiParameters.EMPTY;
                String macroContent = null;

                String macroString = content.substring("startmacro:".length());

                int index = macroString.indexOf(MACRO_SEPARATOR);

                if (index != -1) {
                    // Extract macro name
                    macroName = macroString.substring(0, index);

                    // Remove macro name part and continue parsing
                    macroString = macroString.substring(index
                        + MACRO_SEPARATOR.length());

                    index = macroString.indexOf(MACRO_SEPARATOR);
                    if (index != -1) {
                        // Extract macro parameters
                        List<WikiParameter> parameters = new ArrayList<WikiParameter>();
                        index = WikiScannerUtil.splitToPairs(
                            macroString,
                            parameters,
                            null,
                            MACRO_SEPARATOR);
                        macroParams = new WikiParameters(parameters);

                        // Extract macro content
                        if (macroString.length() > index) {
                            macroContent = macroString.substring(index
                                + MACRO_SEPARATOR.length());
                        }
                    } else {
                        // There is only parameters remaining in the string, the
                        // macro does not have content
                        // Extract macro parameters
                        macroParams = WikiParameters
                            .newWikiParameters(macroString);
                    }
                } else {
                    // There is only macro name, the macro does not have
                    // parameters
                    // or content
                    macroName = macroString;
                }

                // If we're inside a block element then issue an inline macro
                // event
                // otherwise issue a block macro event
                Stack<Boolean> insideBlockElementsStack = (Stack<Boolean>) stack
                    .getStackParameter("insideBlockElement");
                if (!insideBlockElementsStack.isEmpty()
                    && insideBlockElementsStack.peek())
                {
                    stack.getScannerContext().onMacroInline(
                        macroName,
                        macroParams,
                        macroContent);
                } else {
                    stack.getScannerContext().onMacroBlock(
                        macroName,
                        macroParams,
                        macroContent);
                }
            }

            stack.pushStackParameter("ignoreElements", true);
        } else if (content.startsWith("stopmacro")) {
            stack.popStackParameter("ignoreElements");
        }
    }
}

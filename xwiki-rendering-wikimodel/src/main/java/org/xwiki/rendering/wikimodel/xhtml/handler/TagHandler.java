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

import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagStack;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class TagHandler
{

    private boolean fAccumulateContent;

    /**
     * This flag is <code>true</code> if the current tag can have a text content
     */
    private final boolean fContentContainer;

    /**
     * @param contentContainer when false, text content is dropped.
     */
    public TagHandler(boolean contentContainer)
    {
        fContentContainer = contentContainer;
    }

    protected void begin(TagContext context)
    {
    }

    public void beginElement(TagContext context)
    {
        if (isBlockHandler(context)) {
            // If we're starting a block tag and we're in inline mode (ie inside
            // a block element) then start a nested document
            // and save the parent tag, see endElement().
            if (context.getTagStack().isInsideBlockElement()) {
                beginDocument(context);

                context.getTagStack().setDocumentParent();
            }

            context.getTagStack().setInsideBlockElement();
        }

        begin(context);
    }

    protected void end(TagContext context)
    {
    }

    public final void endElement(TagContext context)
    {
        // Verify if we need to close a nested document that would have been
        // opened.
        // To verify this we check the current tag being closed and verify if
        // it's the one saved when the nested document was opened.
        if (context == context.getTagStack().getDocumentParent()) {
            endDocument(context);
            context.getTagStack().setInsideBlockElement();
        }

        end(context);

        if (isBlockHandler(context)) {
            context.getTagStack().unsetInsideBlockElement();
        }
    }

    public boolean isContentContainer()
    {
        return fContentContainer;
    }

    public void setAccumulateContent(boolean accumulateContent)
    {
        fAccumulateContent = accumulateContent;
    }

    public boolean isAccumulateContent()
    {
        return fAccumulateContent;
    }

    /**
     * Check if we need to emit an onEmptyLines() event.
     */
    public static void sendEmptyLines(TagContext context)
    {
        sendEmptyLines(context.getTagStack());
    }

    public static void sendEmptyLines(TagStack stack)
    {
        int lineCount = stack.getEmptyLinesCount();
        if (lineCount > 0) {
            stack.getScannerContext().getfListener().onEmptyLines(lineCount);
            stack.resetEmptyLinesCount();
        }
    }

    public void initialize(TagStack stack)
    {
        // Nothing to do by default. Override in children classes if need be.
    }

    /**
     * @return true if the current handler handles block tags (paragraphs, lists, tables, headers, etc)
     */
    public boolean isBlockHandler(TagContext context)
    {
        return false;
    }

    protected void beginDocument(TagContext context)
    {
        beginDocument(context, null);
    }

    protected void beginDocument(TagContext context, WikiParameters params)
    {
        sendEmptyLines(context);
        if (params == null) {
            context.getScannerContext().beginDocument();
        } else {
            context.getScannerContext().beginDocument(params);
        }

        // Stack context parameters since we enter in a new document
        context.getTagStack().pushStackParameters();

        context.getTagStack().unsetInsideBlockElement();
    }

    protected void endDocument(TagContext context)
    {
        context.getTagStack().popStackParameters();

        sendEmptyLines(context);

        context.getScannerContext().endDocument();
    }
}

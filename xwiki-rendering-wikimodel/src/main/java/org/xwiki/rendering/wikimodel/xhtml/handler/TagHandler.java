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

import java.util.Stack;

import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler;

/**
 * @author kotelnikov
 * @author vmassol
 * @author thomas.mortagne
 */
public class TagHandler
{
    /**
     * @return <code>true</code> if the current tag represented by the given
     *         context requires a parent document
     */
    private static boolean requiresParentDocument(XhtmlHandler.TagStack.TagContext context)
    {
        if (context == null) {
            return true;
        }
        if (context.fHandler == null || !context.fHandler.requiresDocument()) {
            return false;
        }
        boolean inContainer = false;
        XhtmlHandler.TagStack.TagContext parent = context.getParent();
        while (parent != null) {
            if (parent.fHandler != null) {
                inContainer = parent.fHandler.isDocumentContainer();
                break;
            }
            parent = parent.getParent();
        }
        return inContainer;
    }

    private boolean fAccumulateContent;

    /**
     * This flag is <code>true</code> if the current tag can have a text content
     */
    private final boolean fContentContainer;

    /**
     * This flag shows if the current tag can be used as a container for
     * embedded documents.
     */
    private final boolean fDocumentContainer;

    /**
     * This flag shows if the current tag should be created as a direct child of
     * a document.
     */
    private final boolean fRequiresDocument;

    /**
     * @param documentContainer
     * @param requiresDocument
     * @param contentContainer
     */
    public TagHandler(
        boolean documentContainer,
        boolean requiresDocument,
        boolean contentContainer)
    {
        fDocumentContainer = documentContainer;
        fRequiresDocument = requiresDocument;
        fContentContainer = contentContainer;
    }

    protected void begin(XhtmlHandler.TagStack.TagContext context)
    {
    }

    public void beginElement(XhtmlHandler.TagStack.TagContext context)
    {

        Stack<Boolean> insideBlockElementsStack = (Stack<Boolean>) context
            .getTagStack()
            .getStackParameter("insideBlockElement");

        if (isBlockHandler(context)) {
            // If we're starting a block tag and we're in inline mode (ie inside
            // a block element) then start a nested document
            // and save the parent tag, see endElement().
            if (!insideBlockElementsStack.isEmpty()
                && insideBlockElementsStack.peek())
            {
                beginDocument(context);

                context.getTagStack().setStackParameter(
                    "documentParent",
                    context.getParent());

                // Get the new inside block element state
                insideBlockElementsStack = (Stack<Boolean>) context
                    .getTagStack()
                    .getStackParameter("insideBlockElement");
            }

            insideBlockElementsStack.push(true);
        }

        begin(context);
    }

    protected void end(XhtmlHandler.TagStack.TagContext context)
    {
    }

    public final void endElement(XhtmlHandler.TagStack.TagContext context)
    {
        // Verify if we need to close a nested document that would have been
        // opened.
        // To verify this we check the current tag being closed and verify if
        // it's the one saved when the nested document was opened.
        XhtmlHandler.TagStack.TagContext docParent = (XhtmlHandler.TagStack.TagContext) context
            .getTagStack()
            .getStackParameter("documentParent");
        if (context == docParent) {
            endDocument(context);
        }

        end(context);

        Stack<Boolean> insideBlockElementsStack = (Stack<Boolean>) context
            .getTagStack()
            .getStackParameter("insideBlockElement");

        if (isBlockHandler(context)) {
            insideBlockElementsStack.pop();
        }
    }

    public boolean isContentContainer()
    {
        return fContentContainer;
    }

    public boolean isDocumentContainer()
    {
        return fDocumentContainer;
    }

    public boolean requiresDocument()
    {
        return fRequiresDocument;
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
    public static void sendEmptyLines(XhtmlHandler.TagStack.TagContext context)
    {
        int lineCount = (Integer) context.getTagStack().getStackParameter(
            "emptyLinesCount");
        if (lineCount > 0) {
            context.getScannerContext().onEmptyLines(lineCount);
            context.getTagStack().setStackParameter("emptyLinesCount", 0);
        }
    }

    public void initialize(XhtmlHandler.TagStack stack)
    {
        // Nothing to do by default. Override in children classes if need be.
    }

    /**
     * @return true if the current handler handles block tags (paragraphs,
     *         lists, tables, headers, etc)
     */
    public boolean isBlockHandler(XhtmlHandler.TagStack.TagContext context)
    {
        return false;
    }

    protected void beginDocument(XhtmlHandler.TagStack.TagContext context)
    {
        beginDocument(context, null);
    }

    protected void beginDocument(XhtmlHandler.TagStack.TagContext context, WikiParameters params)
    {
        sendEmptyLines(context);
        if (params == null) {
            context.getScannerContext().beginDocument();
        } else {
            context.getScannerContext().beginDocument(params);
        }

        Object ignoreElements = context.getTagStack().getStackParameter(
            "ignoreElements");

        // Stack context parameters since we enter in a new document
        context.getTagStack().pushStackParameters();

        // ignoreElements apply on embedded document
        context.getTagStack().setStackParameter(
            "ignoreElements",
            ignoreElements);
    }

    protected void endDocument(XhtmlHandler.TagStack.TagContext context)
    {
        context.getTagStack().popStackParameters();

        context.getScannerContext().endDocument();
    }
}
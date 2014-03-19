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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import javax.inject.Inject;
import javax.inject.Named;

import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Default method implementation and useful methods for inheritors.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractPegdownVisitor implements PegdownVisitor
{
    /**
     * Used to extract text from nodes.
     */
    @Inject
    @Named("plain/1.0")
    protected PrintRendererFactory plainRendererFactory;

    /**
     * A plain text parser used to convert Pegdown TextNode elements to various Block elements since TextNode's values
     * contain several words and special characters which we thus need to break down into individual tokens.
     */
    @Inject
    @Named("plain/1.0")
    protected StreamParser plainTextStreamParser;

    /**
     * Listener(s) for the generated XWiki Events. Organized as a stack so that a buffering listener can hijack all
     * events for a while, for example. All generated events are sent to the top of the stack.
     */
    protected Deque<Listener> listeners = new ArrayDeque<Listener>();

    /**
     * @return the top listener on the stack
     */
    protected Listener getListener()
    {
        return this.listeners.peek();
    }

    @Override
    public void visit(SuperNode superNode, Listener listener)
    {
        SectionListener sectionListener = new SectionListener();
        sectionListener.setWrappedListener(listener);
        this.listeners.push(sectionListener);

        MetaData metaData = new MetaData(Collections.singletonMap(MetaData.SYNTAX, (Object) Syntax.MARKDOWN_1_0));
        getListener().beginDocument(metaData);
        superNode.accept(this);
        getListener().endDocument(metaData);
    }

    @Override
    public void visit(RootNode rootNode)
    {
        for (ReferenceNode ref : rootNode.getReferences()) {
            visit(ref);
        }

        for (AbbreviationNode abbr : rootNode.getAbbreviations()) {
            visit(abbr);
        }

        visitChildren(rootNode);
    }

    /**
     * Helper method to visit all children nodes.
     *
     * @param node the node to visit
     */
    protected void visitChildren(Node node)
    {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Node node)
    {
        // Not used.
    }

    @Override
    public void visit(SuperNode superNode)
    {
        visitChildren(superNode);
    }

    /**
     * Extracts the content of the passed Node as a String.
     *
     * @param node the node from which to extract the content
     * @return the textual content of the passed node and children
     */
    protected String extractText(Node node)
    {
        WikiPrinter printer = new DefaultWikiPrinter();
        this.listeners.push(this.plainRendererFactory.createRenderer(printer));
        visitChildren(node);
        this.listeners.pop();
        return printer.toString();
    }
}

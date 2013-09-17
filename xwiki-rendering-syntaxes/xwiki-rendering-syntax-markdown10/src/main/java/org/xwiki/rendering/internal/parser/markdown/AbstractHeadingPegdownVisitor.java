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

import java.util.Collections;

import org.pegdown.ast.HeaderNode;
import org.xwiki.rendering.listener.CompositeListener;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.util.IdGenerator;

/**
 * Implements Pegdown Visitor's heading events.
 *
 * @version $Id$
 * @since 4.5M1
 */
public abstract class AbstractHeadingPegdownVisitor extends AbstractLinkAndImagePegdownVisitor
{
    /**
     * Used to generate a unique id for Headings.
     */
    private IdGenerator idGenerator = new IdGenerator();

    @Override
    public void visit(HeaderNode headerNode)
    {
        // Heading needs to have an id generated from a plaintext representation of its content, so the header start
        // event will be sent at the end of the header, after reading the content inside and generating the id.
        // For this:
        // buffer all events in a queue until the header ends, and also send them to a print renderer to generate the ID
        CompositeListener composite = new CompositeListener();
        QueueListener queueListener = new QueueListener();
        composite.addListener(queueListener);
        PrintRenderer plainRenderer = this.plainRendererFactory.createRenderer(new DefaultWikiPrinter());
        composite.addListener(plainRenderer);

        // These 2 listeners will receive all events from now on until the header ends
        this.listeners.push(composite);

        visitChildren(headerNode);

        // Restore default listener
        this.listeners.pop();

        String id = this.idGenerator.generateUniqueId("H", plainRenderer.getPrinter().toString());

        HeaderLevel level = HeaderLevel.parseInt(headerNode.getLevel());
        getListener().beginHeader(level, id, Collections.EMPTY_MAP);

        // Send all buffered events to the 'default' listener
        queueListener.consumeEvents(getListener());

        getListener().endHeader(level, id, Collections.EMPTY_MAP);
    }
}

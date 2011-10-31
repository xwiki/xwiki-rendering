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
package org.xwiki.rendering.internal.renderer.xml;

import java.util.Collection;
import java.util.Collections;

import org.xml.sax.ContentHandler;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.xml.ContentHandlerBlockRenderer;
import org.xwiki.rendering.renderer.xml.ContentHandlerStreamRenderer;

public abstract class AbstractRenderer extends AbstractStreamRendererFactory implements ContentHandlerBlockRenderer,
    BlockRenderer
{
    @Override
    public void render(Collection<Block> blocks, ContentHandler contentHandler)
    {
        ContentHandlerStreamRenderer renderer = createRenderer(contentHandler);

        for (Block block : blocks) {
            block.traverse(renderer);
        }
    }

    @Override
    public void render(Block block, WikiPrinter printer)
    {
        render(Collections.singletonList(block), printer);
    }

    @Override
    public void render(Collection<Block> blocks, WikiPrinter printer)
    {
        PrintRenderer renderer = createRenderer(printer);

        for (Block block : blocks) {
            block.traverse(renderer);
        }
    }
}

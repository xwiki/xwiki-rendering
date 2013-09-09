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
package org.xwiki.rendering.internal.renderer.xwiki21;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.renderer.AbstractBlockRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Renders a {@link org.xwiki.rendering.block.Block} in XWiki Syntax 2.1.
 * 
 * @version $Id$
 * @since 2.5M2
 */
@Component
@Named("xwiki/2.1")
@Singleton
public class XWikiSyntaxBlockRenderer extends AbstractBlockRenderer
{
    /**
     * @see #getPrintRendererFactory()
     */
    @Inject
    @Named("xwiki/2.1")
    private PrintRendererFactory xwikiSyntaxRendererFactory;

    @Override
    protected PrintRendererFactory getPrintRendererFactory()
    {
        return this.xwikiSyntaxRendererFactory;
    }

    @Override
    public void render(Collection<Block> blocks, WikiPrinter printer)
    {
        Collection<Block> properBlocks = blocks;

        // TODO: add a real flush API for all syntaxes
        if (blocks.size() > 0 && !(blocks.iterator().next() instanceof XDOM)) {
            XDOM xdom = new XDOM(Collections.<Block> emptyList());
            for (Block block : blocks) {
                xdom.addChild(block);
            }
            properBlocks = Arrays.<Block> asList(xdom);
        }

        super.render(properBlocks, printer);
    }
}

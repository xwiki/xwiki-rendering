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

/**
 * @version $Id$
 */
public abstract class AbstractRenderer extends AbstractStreamRendererFactory implements ContentHandlerBlockRenderer,
    BlockRenderer
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.xml.ContentHandlerBlockRenderer#render(java.util.Collection,
     *      org.xml.sax.ContentHandler)
     */
    public void render(Collection<Block> blocks, ContentHandler contentHandler)
    {
        ContentHandlerStreamRenderer renderer = createRenderer(contentHandler);

        for (Block block : blocks) {
            block.traverse(renderer);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.BlockRenderer#render(org.xwiki.rendering.block.Block,
     *      org.xwiki.rendering.renderer.printer.WikiPrinter)
     */
    public void render(Block block, WikiPrinter printer)
    {
        render(Collections.singletonList(block), printer);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.renderer.BlockRenderer#render(java.util.Collection,
     *      org.xwiki.rendering.renderer.printer.WikiPrinter)
     */
    public void render(Collection<Block> blocks, WikiPrinter printer)
    {
        PrintRenderer renderer = createRenderer(printer);

        for (Block block : blocks) {
            block.traverse(renderer);
        }
    }
}

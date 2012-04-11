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
package org.xwiki.rendering.internal.transformation.linkchecker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.MetadataBlockMatcher;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;
import org.xwiki.rendering.transformation.linkchecker.LinkContextDataProvider;

/**
 * Looks for external URLs in links and verify their status (ok, broken, etc). In order to get good performances
 * this is done asynchronously in a Thread and same links are checked only every N minutes.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Component
@Named("linkchecker")
@Singleton
public class LinkCheckerTransformation extends AbstractTransformation implements Initializable
{
    /**
     * The component to use to locate other components.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * The link queue that the checker thread will use to check links. We use a separate checked thread and a queue
     * in order to have good performance so that this transformation doesn't slow down the rendering of content.
     */
    private Queue<LinkQueueItem> linkQueue = new ConcurrentLinkedQueue<LinkQueueItem>();

    /**
     * The thread used to check out links.
     */
    private LinkCheckerThread checkerThread;

    /**
     * Start a Thread in charge of reading links to check from the Checking queue and checking them.
     * @throws InitializationException not used
     */
    @Override
    public void initialize() throws InitializationException
    {
        this.checkerThread = new LinkCheckerThread(this.componentManager, this.linkQueue);
        this.checkerThread.setName("Link Checker Thread");
        this.checkerThread.start();
    }

    @Override
    public void transform(Block source, TransformationContext context) throws TransformationException
    {
        for (LinkBlock linkBlock : source.<LinkBlock>getBlocks(
            new ClassBlockMatcher(LinkBlock.class), Block.Axes.DESCENDANT))
        {
            if (linkBlock.getReference().getType().equals(ResourceType.URL)) {
                // This is a link pointing to an external URL, add it to the queue for processing (i.e. checking).
                String linkReference = linkBlock.getReference().getReference();
                String contentReference = extractSourceContentReference(linkBlock);
                // If there's no content reference then use a default name of "default"
                if (contentReference == null) {
                    contentReference = "default";
                }
                // Add Link Context Data
                Map<String, Object> linkContextData = createLinkContextData(linkReference, contentReference);
                this.linkQueue.add(new LinkQueueItem(linkReference, contentReference, linkContextData));
            }
        }
    }

    /**
     * Stops the checking thread. Should be called when the application is stopped for a clean shutdown.
     * @throws InterruptedException if the thread failed to be stopped
     */
    public void stopLinkCheckerThread() throws InterruptedException
    {
        this.checkerThread.stopProcessing();
        // Wait till the thread goes away
        this.checkerThread.join();
    }

    /**
     * @param linkReference the reference to the link to check (usually a URL)
     * @param contentReference the reference to the content containing the link to check
     * @return context data to provide more information about the link being checked (for example it could be useful
     *         in some situations to store the HTTP request leading to the link being checked since there could be
     *         HTTP query string parameters useful to see to understand why such a link was generated in the content)
     */
    private Map<String, Object> createLinkContextData(String linkReference, String contentReference)
    {
        // For performance reason we don't want to store an empty map in the Link state cache when there are no
        // context data.
        Map<String, Object> linkContextData = null;
        try {
            List<LinkContextDataProvider> linkContextDataProviders = 
                this.componentManager.getInstanceList(LinkContextDataProvider.class);
            for (LinkContextDataProvider linkContextDataProvider : linkContextDataProviders) {
                Map<String, Object> contextData =
                    linkContextDataProvider.getContextData(linkReference, contentReference);
                if (linkContextData == null) {
                    linkContextData = new LinkedHashMap<String, Object>(contextData.size());
                }
                linkContextData.putAll(contextData);
            }
        } catch (ComponentLookupException e) {
            throw new RuntimeException("Failed to look up [" + LinkContextDataProvider.class.getName()
                + "] components", e);
        }
        return linkContextData;
    }
    
    /**
     * @param source the blocks from where to try to extract the source content
     * @return the source content reference or null if none is found
     */
    private String extractSourceContentReference(Block source)
    {
        String contentSource = null;
        MetaDataBlock metaDataBlock =
            source.getFirstBlock(new MetadataBlockMatcher(MetaData.SOURCE), Block.Axes.ANCESTOR);
        if (metaDataBlock != null) {
            contentSource = (String) metaDataBlock.getMetaData().getMetaData(MetaData.SOURCE);
        }
        return contentSource;        
    }
}

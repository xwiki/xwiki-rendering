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
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
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
 * Looks for external URLs in links and verify their status (ok, broken, etc). In order to get good performances this is
 * done asynchronously in a Thread and same links are checked only every N minutes.
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
     * Anti-flood mechanism. We only allow adding links to check in the queue if it currently has less than
     * MAX_LINKS_IN_QUEUE already. This is a safeguard for the following:
     * - if the checker thread has some issues and thus the queue isn't getting unpiled then this ensures that it won't
     *   grow more which would slowly use up all the memory...
     * - we don't swamp the system with links to check (for ex if lots of users go to pages with links the queue can
     *   quickly grow large)
     * In any case, the links will be checked again later when the pages are rendered again.
     */
    static final int MAX_LINKS_IN_QUEUE = 10;

    @Inject
    private LinkCheckerThread checkerThread;

    @Inject
    private Provider<List<LinkContextDataProvider>> linkContextDataProvidersProvider;

    /**
     * The link queue that the checker thread will use to check links. We use a separate checker thread and a queue
     * in order to have good performance so that this transformation doesn't slow down the rendering of content.
     */
    private Queue<LinkQueueItem> linkQueue = new ConcurrentLinkedQueue<>();

    /**
     * Start a Thread in charge of reading links to check from the Checking queue and checking them.
     *
     * @throws InitializationException not used
     */
    @Override
    public void initialize() throws InitializationException
    {
        this.checkerThread.setName("Link Checker Thread");
        this.checkerThread.startProcessing(getLinkQueue());
    }

    @Override
    public void transform(Block source, TransformationContext context) throws TransformationException
    {
        // Note that we don't check for pages to excludes here because this transformation is running in the main
        // thread and is executed for each page view and thus needs to be as fast as possible. The exclusion handling
        // is thus done in the Link Checker Thread.

        // Anti-flood mechanism, only add items in the queue if there are less than MAX_LINKS_IN_QUEUE. This means that
        // if the queue has MAX_LINKS_IN_QUEUE or more elements already the links from the current page being rendered
        // will not be verified. They'll get their chance the next time the page is visited again...
        if (getLinkQueue().size() < MAX_LINKS_IN_QUEUE) {
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
    }

    /**
     * Stops the checking thread. Should be called when the application is stopped for a clean shutdown.
     *
     * @throws InterruptedException if the thread failed to be stopped
     */
    public void stopLinkCheckerThread() throws InterruptedException
    {
        this.checkerThread.stopProcessing();
        // Wait till the thread goes away
        this.checkerThread.join();
    }

    /**
     * @return the checker queue containing all pending links to check
     */
    public Queue<LinkQueueItem> getLinkQueue()
    {
        return this.linkQueue;
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
        for (LinkContextDataProvider linkContextDataProvider : this.linkContextDataProvidersProvider.get()) {
            Map<String, Object> contextData =
                linkContextDataProvider.getContextData(linkReference, contentReference);
            if (linkContextData == null) {
                linkContextData = new LinkedHashMap<>(contextData.size());
            }
            linkContextData.putAll(contextData);
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

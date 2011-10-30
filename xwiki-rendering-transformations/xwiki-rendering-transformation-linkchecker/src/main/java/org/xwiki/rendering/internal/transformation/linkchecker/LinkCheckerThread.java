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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;

/**
 * Thread that regularly check for Links to be checked on a Queue, and for each link tries to connect to it and 
 * save the result in the {@link LinkStateManager}. In order to have good performance we only recheck a link if it's
 * not been checked for a certain time.
 * 
 * @version $Id$
 * @since 3.3M1
 */
// TODO: If the LinkCheckerTransformation component is unregistered, then stop the thread.
public class LinkCheckerThread extends Thread
{
    /**
     * Wait 5 minutes before rechecking a link.
     */
    private static final long TIMEOUT = 300000L;

    /**
     * The state manager containing the state of all checked links.
     */
    private LinkStateManager linkStateManager;

    /**
     * The HTTP checker used to connect to links to verify their validity.
     */
    private HTTPChecker httpChecker;

    /**
     * The queue containing links to check.
     */
    private Queue<LinkQueueItem> linkQueue;

    /**
     * Allows to stop this thread, used in {@link #stopProcessing()}.
     */
    private volatile boolean shouldStop;

    /**
     * @param linkStateManager the state manager containing the state of all checked links.
     * @param httpChecker the HTTP checker used to connect to links to verify their validity.
     * @param linkQueue the queue containing links to check.
     */
    public LinkCheckerThread(LinkStateManager linkStateManager, HTTPChecker httpChecker,
        Queue<LinkQueueItem> linkQueue)
    {
        this.linkStateManager = linkStateManager;
        this.httpChecker = httpChecker;
        this.linkQueue = linkQueue;
    }

    @Override
    public void run()
    {
        while (!this.shouldStop) {
            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                break;
            }
            processLinkQueue();
        }
    }

    /**
     * Stop the thread.
     */
    public void stopProcessing()
    {
        this.shouldStop = true;
        // Make sure the Thread goes out of sleep if it's sleeping so that it stops immediately.
        interrupt();
    }

    /**
     * Read the queue and find links to process, removing links that have already been checked out recently.
     */
    private void processLinkQueue()
    {
        // Unqueue till we find an item that needs to be processed. We process an item if:
        // - it isn't present in the state map
        // - it is present but not enough time has elapsed since its last check time
        LinkQueueItem queueItem = null;
        boolean shouldBeChecked = false;

        while (!this.linkQueue.isEmpty() && !shouldBeChecked) {
            queueItem = this.linkQueue.poll();
            shouldBeChecked = true;
            Map<String, LinkState> contentReferences =
                this.linkStateManager.getLinkStates().get(queueItem.getLinkReference());
            if (contentReferences != null) {
                LinkState state = contentReferences.get(queueItem.getContentReference());
                if (state != null) {
                    if (System.currentTimeMillis() - state.getLastCheckedTime() <= TIMEOUT) {
                        shouldBeChecked = false;
                    }
                }
            }
        }

        if (queueItem != null) {
            checkLink(queueItem);
        }
    }

    /**
     * Perform the HTTP connection and save the result in the {@link LinkStateManager}.
     *
     * @param queueItem the link to check
     */
    private void checkLink(LinkQueueItem queueItem)
    {
        int responseCode = this.httpChecker.check(queueItem.getLinkReference());

        Map<String, LinkState> contentReferences =
            this.linkStateManager.getLinkStates().get(queueItem.getLinkReference());
        if (contentReferences == null) {
            contentReferences = new ConcurrentHashMap<String, LinkState>();
        }
        LinkState state = contentReferences.get(queueItem.getContentReference());
        if (state == null) {
            state = new LinkState(responseCode, System.currentTimeMillis());
        }
        contentReferences.put(queueItem.getContentReference(), state);
        this.linkStateManager.getLinkStates().put(queueItem.getLinkReference(), contentReferences);
    }
}

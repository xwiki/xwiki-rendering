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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.transformation.linkchecker.InvalidURLEvent;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerThreadInitializer;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;
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
     * The Component Manager to use to locate other components. For example we use it to dynamically look up an
     * Observation Manager so that this transformation works even if there isn't one available.
     */
    private ComponentManager componentManager;

    /**
     * The state manager containing the state of all checked links.
     */
    private LinkStateManager linkStateManager;

    /**
     * The time after which to recheck for link validity.
     */
    private long timeout;

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
     * @param componentManager the Component Manager to use to locate other components
     * @param linkQueue the queue containing links to check.
     * @throws InitializationException when it fails to lookup needed components
     */
    public LinkCheckerThread(ComponentManager componentManager, Queue<LinkQueueItem> linkQueue)
        throws InitializationException
    {
        try {
            this.linkStateManager = componentManager.getInstance(LinkStateManager.class);
            this.httpChecker = componentManager.getInstance(HTTPChecker.class);
            LinkCheckerTransformationConfiguration configuration =
                componentManager.getInstance(LinkCheckerTransformationConfiguration.class);
            this.timeout = configuration.getCheckTimeout();
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to initialize the Link Checker Thread. "
                + "External link states won't be checked.", e);
        }
        this.linkQueue = linkQueue;
        this.componentManager = componentManager;
    }

    @Override
    public void run()
    {
        // Allow external code to perform initialization of this thread.
        // This is useful for example if externa lcode needs to initialize the Execution Context.
        try {
            List<LinkCheckerThreadInitializer> initializers =
                this.componentManager.getInstanceList((Type) LinkCheckerThreadInitializer.class);
            for (LinkCheckerThreadInitializer initializer : initializers) {
                initializer.initialize();
            }
        } catch (ComponentLookupException e) {
            // Failed to run thread initialization. This is critical, stop the thread.
            throw new RuntimeException("Failed to initialize Link Checker Thread", e);
        }

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
                    if (System.currentTimeMillis() - state.getLastCheckedTime() <= this.timeout) {
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
        LinkState state = new LinkState(responseCode, System.currentTimeMillis(), queueItem.getContextData());
        contentReferences.put(queueItem.getContentReference(), state);
        this.linkStateManager.getLinkStates().put(queueItem.getLinkReference(), contentReferences);

        // If there's an error, then send an Observation Event so that anyone interested can listen to it.
        if (responseCode < 200 || responseCode > 299) {
            Map<String, Object> eventSource = new HashMap<String, Object>();
            eventSource.put("url", queueItem.getLinkReference());
            eventSource.put("source", queueItem.getContentReference());
            eventSource.put("state", state);
            sendEvent(queueItem.getLinkReference(), eventSource);
        }
    }

    /**
     * Send an {@link InvalidURLEvent} event.
     *
     * @param url the failing URL
     * @param source the Map containing data (link url, link source reference, state object)
     */
    private void sendEvent(String url, Map<String, Object> source)
    {
        // Dynamically look for an Observation Manager and only send the event if one can be found.
        try {
            ObservationManager observationManager = this.componentManager.getInstance(ObservationManager.class);
            observationManager.notify(new InvalidURLEvent(url), source);
        } catch (ComponentLookupException e) {
            // No observation manager found, don't send any event.
        }
    }
}

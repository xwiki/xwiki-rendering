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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
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
 * @since 5.3RC1
 */
// TODO: If the LinkCheckerTransformation component is unregistered, then stop the thread.
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultLinkCheckerThread extends java.lang.Thread implements LinkCheckerThread
{
    @Inject
    private Logger logger;

    @Inject
    private Provider<List<LinkCheckerThreadInitializer>> linkCheckerThreadInitializersProvider;

    /**
     * The state manager containing the state of all checked links.
     */
    @Inject
    private LinkStateManager linkStateManager;

    @Inject
    private Provider<ObservationManager> observationManagerProvider;

    /**
     * The HTTP checker used to connect to links to verify their validity.
     */
    @Inject
    private HTTPChecker httpChecker;

    @Inject
    private LinkCheckerTransformationConfiguration configuration;

    /**
     * The queue containing links to check.
     */
    private Queue<LinkQueueItem> linkQueue;

    /**
     * Allows to stop this thread, used in {@link #stopProcessing()}.
     */
    private volatile boolean shouldStop;

    @Override
    public void startProcessing(Queue<LinkQueueItem> linkQueue)
    {
        this.linkQueue = linkQueue;
        start();
    }

    @Override
    public void run(Queue<LinkQueueItem> linkQueue)
    {
        this.linkQueue = linkQueue;
        run();
    }

    @Override
    public void run()
    {
        // Allow external code to perform initialization of this thread.
        // This is useful for example if external code needs to initialize the Execution Context.
        for (LinkCheckerThreadInitializer initializer : this.linkCheckerThreadInitializersProvider.get()) {
            initializer.initialize();
        }

        do {
            try {
                processLinkQueue();
                DefaultLinkCheckerThread.sleep(300L);
            } catch (Exception e) {
                // There was an unexpected problem, we stop this checker thread and log the problem.
                this.logger.error("Link checker Thread was stopped due to some problem", e);
                break;
            }
        } while (!this.shouldStop);
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
    protected void processLinkQueue()
    {
        long timeout = this.configuration.getCheckTimeout();
        List<Pattern> excludedReferencePatterns = this.configuration.getExcludedReferencePatterns();

        // Unqueue till we find an item that needs to be processed. We process an item if:
        // - it isn't present in the state map
        // - it is present but not enough time has elapsed since its last check time
        LinkQueueItem queueItem = null;
        boolean shouldBeChecked = false;

        while (!this.linkQueue.isEmpty() && !shouldBeChecked) {
            queueItem = this.linkQueue.poll();

            // Don't check excluded references
            shouldBeChecked = isExcluded(queueItem.getContentReference(), excludedReferencePatterns);
            if (!shouldBeChecked) {
                break;
            }

            Map<String, LinkState> contentReferences =
                this.linkStateManager.getLinkStates().get(queueItem.getLinkReference());
            if (contentReferences != null) {
                LinkState state = contentReferences.get(queueItem.getContentReference());
                if (state != null) {
                    if (System.currentTimeMillis() - state.getLastCheckedTime() <= timeout) {
                        shouldBeChecked = false;
                    }
                }
            }
        }

        if (shouldBeChecked && queueItem != null) {
            checkLink(queueItem);
        }
    }

    private boolean isExcluded(String contentReference, List<Pattern> excludedReferencePatterns)
    {
        for (Pattern pattern : excludedReferencePatterns) {
            Matcher matcher = pattern.matcher(contentReference);
            if (matcher.matches()) {
                return false;
            }
        }

        return true;
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
            eventSource.put("contextData", queueItem.getContextData());
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
        ObservationManager observationManager = this.observationManagerProvider.get();
        if (observationManager != null) {
            observationManager.notify(new InvalidURLEvent(url), source);
        }
    }
}

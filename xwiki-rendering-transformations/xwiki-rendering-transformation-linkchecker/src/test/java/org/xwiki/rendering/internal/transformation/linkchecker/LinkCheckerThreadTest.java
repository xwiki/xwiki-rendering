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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerThreadInitializer;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;
import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

/**
 * Unit tests for {@link DefaultLinkCheckerThread}. Note that the Link Checker Thread is also tested indirectly by
 * {@link LinkCheckerTransformationTest} which is where most tests are located.
 *
 * @version $Id$
 * @since 4.0M2
 */
@ComponentTest
class LinkCheckerThreadTest
{
    @RegisterExtension
    private static final LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private DefaultLinkCheckerThread thread;

    @MockComponent
    private ObservationManager observationManager;

    @MockComponent
    private Provider<ObservationManager> observationManagerProvider;

    @MockComponent
    private LinkCheckerTransformationConfiguration configuration;

    @MockComponent
    private HTTPChecker httpChecker;

    @MockComponent
    private LinkStateManager linkStateManager;

    @MockComponent
    private LinkCheckerThreadInitializer initializer;

    @BeforeComponent
    public void setUpComponents()
    {
        when(this.observationManagerProvider.get()).thenReturn(this.observationManager);
    }

    /**
     * Just verify that we can register a LinkCheckerThreadInitializer and it'll be called.
     */
    @Test
    void runWithInitializer()
    {
        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<>();

        // Make sure the thread is stopped quickly
        ReflectionUtils.setFieldValue(this.thread, "shouldStop", true);

        this.thread.run(queue);

        // This is the test, we verify that the registered Link Checker Initializer is called.
        verify(this.initializer).initialize();
    }

    @Test
    void runWithExclusion()
    {
        when(this.configuration.getCheckTimeout()).thenReturn(3600000L);
        when(this.configuration.getExcludedReferencePatterns()).thenReturn(
            Collections.singletonList(Pattern.compile(".*:excludedspace\\.excludedpage")));

        when(this.httpChecker.check("linkreference1")).thenReturn(200);
        when(this.httpChecker.check("linkreference2")).thenReturn(200);

        Map<String, Map<String, LinkState>> states = new HashMap<>();
        when(this.linkStateManager.getLinkStates()).thenReturn(states);

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<>();
        queue.add(new LinkQueueItem("linkreference1", "excludedwiki:excludedspace.excludedpage",
            Collections.emptyMap()));
        queue.add(new LinkQueueItem("linkreference2", "someotherpage", Collections.emptyMap()));

        ReflectionUtils.setFieldValue(this.thread, "linkQueue", queue);

        // Process first element in queue
        this.thread.processLinkQueue();

        // Process second element in queue
        this.thread.processLinkQueue();

        assertEquals(1, states.size());
        assertNull(states.get("linkreference1"));
        assertNotNull(states.get("linkreference2"));
    }

    @Test
    void sendEventWhenNoObservationManager()
    {
        when(this.httpChecker.check("linkreference")).thenReturn(404);

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<>();
        queue.add(new LinkQueueItem("linkreference", "someref", Collections.emptyMap()));

        ReflectionUtils.setFieldValue(this.thread, "linkQueue", queue);

        when(this.observationManagerProvider.get()).thenThrow(new RuntimeException("error"));

        this.thread.processLinkQueue();

        assertEquals("The Invalid URL Event for URL [linkreference] (source [someref]) wasn't sent as no Observation "
            + "Manager Component was found", logCapture.getMessage(0));
    }
}
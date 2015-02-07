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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import javax.inject.Provider;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerThreadInitializer;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;
import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * Unit tests for {@link DefaultLinkCheckerThread}. Note that the Link Checker Thread is also tested indirectly by
 * {@link LinkCheckerTransformationTest} which is where most tests are located.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class LinkCheckerThreadTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultLinkCheckerThread> componentManager =
        new MockitoComponentMockingRule<>(DefaultLinkCheckerThread.class);

    private Provider<ObservationManager> observationManagerProvider;

    @BeforeComponent
    public void setUpComponents() throws Exception
    {
        ObservationManager observationManager = mock(ObservationManager.class);
        this.observationManagerProvider = this.componentManager.registerMockComponent(
             new DefaultParameterizedType(null, Provider.class, ObservationManager.class));
        when(this.observationManagerProvider.get()).thenReturn(observationManager);
    }

    /**
     * Just verify that we can register a LinkCheckerThreadInitializer and it'll be called.
     */
    @Test
    public void runWithInitializer() throws Exception
    {
        LinkCheckerThreadInitializer initializer =
            this.componentManager.registerMockComponent(LinkCheckerThreadInitializer.class);

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<>();

        DefaultLinkCheckerThread thread = this.componentManager.getComponentUnderTest();

        // Make sure the thread is stopped quickly
        ReflectionUtils.setFieldValue(thread, "shouldStop", true);

        thread.run(queue);

        // This is the test, we verify that the registered Link Checker Initializer is called.
        verify(initializer).initialize();
    }

    @Test
    public void runWithExclusion() throws Exception
    {
        LinkCheckerTransformationConfiguration configuration =
            this.componentManager.getInstance(LinkCheckerTransformationConfiguration.class);
        when(configuration.getCheckTimeout()).thenReturn(3600000L);
        when(configuration.getExcludedReferencePatterns()).thenReturn(
            Collections.singletonList(Pattern.compile(".*:excludedspace\\.excludedpage")));

        HTTPChecker httpChecker = this.componentManager.getInstance(HTTPChecker.class);
        when(httpChecker.check("linkreference1")).thenReturn(200);
        when(httpChecker.check("linkreference2")).thenReturn(200);

        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        Map<String, Map<String, LinkState>> states = new HashMap<>();
        when(linkStateManager.getLinkStates()).thenReturn(states);

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<>();
        queue.add(new LinkQueueItem("linkreference1", "excludedwiki:excludedspace.excludedpage",
            Collections.<String, Object>emptyMap()));
        queue.add(new LinkQueueItem("linkreference2", "someotherpage", Collections.<String, Object>emptyMap()));

        DefaultLinkCheckerThread thread = this.componentManager.getComponentUnderTest();
        ReflectionUtils.setFieldValue(thread, "linkQueue", queue);

        // Process first element in queue
        thread.processLinkQueue();

        // Process second element in queue
        thread.processLinkQueue();

        assertEquals(1, states.size());
        assertNull(states.get("linkreference1"));
        assertNotNull(states.get("linkreference2"));
    }

    @Test
    public void sendEventWhenNoObservationManager() throws Exception
    {
        HTTPChecker httpChecker = this.componentManager.getInstance(HTTPChecker.class);
        when(httpChecker.check("linkreference")).thenReturn(404);

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<>();
        queue.add(new LinkQueueItem("linkreference", "someref", Collections.<String, Object>emptyMap()));

        DefaultLinkCheckerThread thread = this.componentManager.getComponentUnderTest();
        ReflectionUtils.setFieldValue(thread, "linkQueue", queue);

        when(this.observationManagerProvider.get()).thenThrow(new RuntimeException("error"));

        thread.processLinkQueue();

        verify(this.componentManager.getMockedLogger()).warn("The Invalid URL Event for URL [{}] (source [{}]) wasn't "
            + "sent as no Observation Manager Component was found", "linkreference", "someref");
    }
}
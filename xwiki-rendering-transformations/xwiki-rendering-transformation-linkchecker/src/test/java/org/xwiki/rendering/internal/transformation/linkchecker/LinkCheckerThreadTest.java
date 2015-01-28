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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import javax.inject.Provider;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerThreadInitializer;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;
import org.xwiki.rendering.transformation.linkchecker.LinkState;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        new MockitoComponentMockingRule<DefaultLinkCheckerThread>(DefaultLinkCheckerThread.class);

    /**
     * Just verify that we can register a LinkCheckerThreadInitializer and it'll be called.
     */
    @Test
    public void runWithInitializer() throws Exception
    {
        LinkCheckerThreadInitializer initializer = mock(LinkCheckerThreadInitializer.class);
        Provider<List<LinkCheckerThreadInitializer>> initializersProvider =
            this.componentManager.registerMockComponent(new DefaultParameterizedType(null, Provider.class,
                new DefaultParameterizedType(null, List.class, LinkCheckerThreadInitializer.class)));

        when(initializersProvider.get()).thenReturn(Arrays.asList(initializer));

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<LinkQueueItem>();

        DefaultLinkCheckerThread thread = this.componentManager.getComponentUnderTest();
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
            Arrays.asList(Pattern.compile(".*:excludedspace\\.excludedpage")));

        HTTPChecker httpChecker = this.componentManager.getInstance(HTTPChecker.class);
        when(httpChecker.check("linkreference1")).thenReturn(200);
        when(httpChecker.check("linkreference2")).thenReturn(200);

        LinkStateManager linkStateManager = this.componentManager.getInstance(LinkStateManager.class);
        Map<String, Map<String, LinkState>> states = new HashMap<String, Map<String, LinkState>>();
        when(linkStateManager.getLinkStates()).thenReturn(states);

        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<LinkQueueItem>();
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
}

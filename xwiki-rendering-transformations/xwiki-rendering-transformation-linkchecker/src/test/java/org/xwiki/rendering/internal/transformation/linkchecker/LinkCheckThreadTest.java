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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerThreadInitializer;
import org.xwiki.rendering.transformation.linkchecker.LinkCheckerTransformationConfiguration;
import org.xwiki.rendering.transformation.linkchecker.LinkStateManager;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Unit tests for {@link LinkCheckerThread}. Note that the LinkChecker Thread is also tested by
 * {@link LinkCheckerTransformationTest}.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class LinkCheckThreadTest extends AbstractComponentTestCase
{
    @Test
    public void runWithInitializer() throws Exception
    {
        final ComponentManager componentManager = getMockery().mock(ComponentManager.class);
        Queue<LinkQueueItem> queue = new ConcurrentLinkedQueue<LinkQueueItem>();
        final LinkCheckerTransformationConfiguration configuration =
            getMockery().mock(LinkCheckerTransformationConfiguration.class);

        // Register a LinkChecker Thread Initializer
        final LinkCheckerThreadInitializer initializer = getMockery().mock(LinkCheckerThreadInitializer.class);

        getMockery().checking(new Expectations()
        {
            {
                oneOf(componentManager).getInstance(LinkStateManager.class);
                will(returnValue(getMockery().mock(LinkStateManager.class)));
                oneOf(componentManager).getInstance(HTTPChecker.class);
                will(returnValue(getMockery().mock(HTTPChecker.class)));
                oneOf(componentManager).getInstance(LinkCheckerTransformationConfiguration.class);
                will(returnValue(configuration));
                oneOf(configuration).getCheckTimeout();
                will(returnValue(3600000L));

                // This is the test:
                oneOf(componentManager).getInstanceList(LinkCheckerThreadInitializer.class);
                will(returnValue(Arrays.asList(initializer)));
                oneOf(initializer).initialize();
            }
        });

        LinkCheckerThread thread = new LinkCheckerThread(componentManager, queue);
        ReflectionUtils.setFieldValue(thread, "shouldStop", true);
        thread.run();
    }
}

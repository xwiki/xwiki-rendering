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
package org.xwiki.rendering.internal.listener;

import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.ListenerProvider;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import ch.qos.logback.classic.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.xwiki.rendering.syntax.Syntax.XWIKI_2_1;

/**
 * Test of {@link ListenerRegistry}.
 *
 * @version $Id$
 * @since x.y.z
 */
@ComponentTest
class ListenerRegistryTest
{
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @InjectMockComponents
    private ListenerRegistry listenerRegistry;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Mock
    private ComponentManager componentManager;

    @Mock
    private ListenerChain listenerChain;

    @Mock
    private ListenerProvider listenerProvider0;

    @Mock
    private ListenerProvider listenerProvider1;

    @Mock
    private ChainingListener chainingListener;

    @BeforeEach
    void setUp()
    {
        when(this.componentManagerProvider.get()).thenReturn(this.componentManager);
    }

    @Test
    void getListenersNoListenerProvider() throws Exception
    {
        when(this.componentManager.getInstanceList(ListenerProvider.class)).thenReturn(List.of());
        List<ChainingListener> listeners =
            this.listenerRegistry.getListeners(this.listenerChain, "action_id", XWIKI_2_1);
        assertEquals(List.of(), listeners);
    }

    @Test
    void getListenersComponentLookupException() throws Exception
    {
        when(this.componentManager.getInstanceList(ListenerProvider.class)).thenThrow(ComponentLookupException.class);
        List<ChainingListener> listeners =
            this.listenerRegistry.getListeners(this.listenerChain, "action_id", XWIKI_2_1);
        assertEquals(List.of(), listeners);
        assertEquals("Failed to load the list of [interface org.xwiki.rendering.listener.ListenerProvider] "
                + "for action [action_id] and syntax [XWiki 2.1]. Cause [ComponentLookupException: ].",
            this.logCapture.getMessage(0));
        assertEquals(Level.WARN, this.logCapture.getLogEvent(0).getLevel());
    }

    @Test
    void getListeners() throws Exception
    {
        when(this.componentManager.getInstanceList(ListenerProvider.class)).thenReturn(List.of(
            this.listenerProvider0,
            this.listenerProvider1
        ));
        when(this.listenerProvider0.accept(anyString(), any(Syntax.class))).thenReturn(false);
        when(this.listenerProvider1.accept(anyString(), any(Syntax.class))).thenReturn(true);
        when(this.listenerProvider1.getListener(this.listenerChain)).thenReturn(this.chainingListener);

        String actionId = "action_id";
        List<ChainingListener> listeners = this.listenerRegistry.getListeners(this.listenerChain, actionId, XWIKI_2_1);

        assertEquals(List.of(this.chainingListener), listeners);
        verify(this.listenerProvider0).accept(actionId, XWIKI_2_1);
        verify(this.listenerProvider1).accept(actionId, XWIKI_2_1);
        verify(this.listenerProvider0, never()).getListener(any());
        verify(this.listenerProvider1).getListener(this.listenerChain);
    }
}

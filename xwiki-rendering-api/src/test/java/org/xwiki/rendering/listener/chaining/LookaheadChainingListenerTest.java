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
package org.xwiki.rendering.listener.chaining;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.text.CaseUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.QueueListener;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link LookaheadChainingListener}.
 *
 * @version $Id$
 * @since 1.8RC1
 */
class LookaheadChainingListenerTest
{
    public static class TestChainingListener extends AbstractChainingListener
    {
        public int calls = 0;

        public TestChainingListener(ListenerChain listenerChain)
        {
            setListenerChain(listenerChain);
        }

        @Override
        public void beginDocument(MetaData metadata)
        {
            this.calls++;
        }

        @Override
        public void beginParagraph(Map<String, String> parameters)
        {
            this.calls++;
        }

        @Override
        public void endDocument(MetaData metadata)
        {
            this.calls++;
        }

        @Override
        public void endParagraph(Map<String, String> parameters)
        {
            this.calls++;
        }
    }

    @Test
    void testLookahead()
    {
        ListenerChain chain = new ListenerChain();
        LookaheadChainingListener listener = new LookaheadChainingListener(chain, 2);
        chain.addListener(listener);
        TestChainingListener testListener = new TestChainingListener(chain);
        chain.addListener(testListener);

        // The begin document flushes
        listener.beginDocument(MetaData.EMPTY);
        assertEquals(1, testListener.calls);

        // 1st lookahead, nothing is sent to the test listener
        listener.beginParagraph(Listener.EMPTY_PARAMETERS);
        assertEquals(1, testListener.calls);
        assertEquals(EventType.BEGIN_PARAGRAPH, listener.getNextEvent().eventType);
        assertNull(listener.getNextEvent(2));

        // 2nd lookahead, nothing is sent to the test listener
        listener.beginParagraph(Listener.EMPTY_PARAMETERS);
        assertEquals(1, testListener.calls);
        assertEquals(EventType.BEGIN_PARAGRAPH, listener.getNextEvent().eventType);
        assertEquals(EventType.BEGIN_PARAGRAPH, listener.getNextEvent(2).eventType);
        assertNull(listener.getNextEvent(3));

        // 3rd events, the first begin paragraph is sent
        listener.endParagraph(Listener.EMPTY_PARAMETERS);
        assertEquals(2, testListener.calls);
        assertEquals(EventType.BEGIN_PARAGRAPH, listener.getNextEvent().eventType);
        assertEquals(EventType.END_PARAGRAPH, listener.getNextEvent(2).eventType);
        assertNull(listener.getNextEvent(3));

        // The end document flushes
        listener.endDocument(MetaData.EMPTY);
        assertEquals(5, testListener.calls);
        assertNull(listener.getNextEvent());
    }

    /**
     * Test all methods of the {@link Listener} interface.
     * <p>
     * Tests for all methods if they are properly forwarded.
     *
     * @param method The method to test.
     * @param parameters Suitable parameters for the method.
     */
    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#allMethodsProvider")
    void testAllMethods(Method method, Object[] parameters) throws InvocationTargetException, IllegalAccessException
    {
        ListenerChain chain = new ListenerChain();
        LookaheadChainingListener listener = new LookaheadChainingListener(chain, 1);
        chain.addListener(listener);

        ChainingListener mockListener = mock(ChainingListener.class);
        chain.addListener(mockListener);

        listener.onId("Before");

        verifyNoInteractions(mockListener);

        // Check that the event is available when the next listener in the chain is called.
        doAnswer(invocation -> {
            QueueListener.Event event = listener.getNextEvent();
            assertNotNull(event);

            // MetaData events unfortunately do not follow the naming scheme...
            if (method.getName().equals("beginMetaData")) {
                assertEquals("BEGIN_METADATA", event.eventType.name());
            } else if (method.getName().equals("endMetaData")) {
                assertEquals("END_METADATA", event.eventType.name());
            } else {
                assertEquals(method.getName(), CaseUtils.toCamelCase(event.eventType.name(), false, '_'));
            }
            assertArrayEquals(parameters, event.eventParameters);

            // Check that fireEvent calls the correct listener method with the correct parameter.
            Listener nextEventListener = mock(Listener.class);
            event.eventType.fireEvent(nextEventListener, event.eventParameters);
            method.invoke(verify(nextEventListener), parameters);
            verifyNoMoreInteractions(nextEventListener);
            return null;
        }).when(mockListener).onId("Before");

        method.invoke(listener, parameters);

        verify(mockListener).onId("Before");

        // Call another method to trigger forwarding.
        listener.onId("After");

        method.invoke(verify(mockListener), parameters);
        verifyNoMoreInteractions(mockListener);
    }
}

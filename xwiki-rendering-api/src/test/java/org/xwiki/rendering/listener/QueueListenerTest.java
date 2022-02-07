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
package org.xwiki.rendering.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.text.CaseUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit test for {@link QueueListener}.
 *
 * @version $Id$
 * @since 14.1RC1
 * @since 13.10.3
 */
class QueueListenerTest
{
    /**
     * Test all methods of the {@link Listener} interface.
     * <p>
     * Tests for all methods if they are properly stored.
     *
     * @param method The method to test.
     * @param parameters Suitable parameters for the method.
     */
    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#allMethodsProvider")
    void testAllMethods(Method method, Object[] parameters) throws InvocationTargetException, IllegalAccessException
    {
        QueueListener queueListener = new QueueListener();

        method.invoke(queueListener, parameters);

        QueueListener.Event event = queueListener.getEvent(1);
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
        Listener mockListener = mock(Listener.class);
        event.eventType.fireEvent(mockListener, event.eventParameters);
        method.invoke(verify(mockListener), parameters);
        verifyNoMoreInteractions(mockListener);

        // Check that consumeEvents also calls the correct method.
        mockListener = mock(Listener.class);
        queueListener.consumeEvents(mockListener);
        method.invoke(verify(mockListener), parameters);
        verifyNoMoreInteractions(mockListener);
    }
}

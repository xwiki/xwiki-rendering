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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit test for {@link CompositeListener}.
 *
 * @version $Id$
 * @since 14.1RC1
 */
class CompositeListenerTest
{
    /**
     * Test all methods of the {@link Listener} interface.
     * <p>
     * Tests for all methods if they properly call the nested listener.
     *
     * @param method The method to test.
     * @param parameters Suitable parameters for the method.
     */
    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#allMethodsProvider")
    void testAllMethods(Method method, Object[] parameters) throws InvocationTargetException, IllegalAccessException
    {
        CompositeListener compositeListener = new CompositeListener();
        Listener mockListener = mock(Listener.class);
        compositeListener.addListener(mockListener);

        method.invoke(compositeListener, parameters);

        method.invoke(verify(mockListener), parameters);
        verifyNoMoreInteractions(mockListener);
    }
}

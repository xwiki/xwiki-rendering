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
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Validate {@link AbstractChainingListener}.
 * 
 * @version $Id$
 */
class AbstractChainingListenerTest
{
    private static class AbstractChild extends AbstractChainingPrintRenderer
    {
        boolean called;

        @Override
        public void beginListItem()
        {
            this.called = true;
        }
    }

    private static class Child extends AbstractChainingListener
    {
        boolean called;

        @Override
        public void beginListItem()
        {
            this.called = true;
        }
    }

    private static class Child2 extends AbstractChild
    {

    }

    private static class ChildWithBothBeginMethods extends AbstractChild
    {
        boolean calledWithoutParameter;
        boolean calledWithParameter;

        @Override
        public void beginListItem()
        {
            super.beginListItem();

            this.calledWithoutParameter = true;
        }

        @Override
        public void beginListItem(Map<String, String> parameters)
        {
            super.beginListItem(parameters);

            this.calledWithParameter = true;
        }
    }

    private static class Child3 extends AbstractChainingListener
    {
        boolean called;

        @Override
        public void beginListItem(Map<String, String> parameters)
        {
            this.called = true;
        }
    }

    private static class DummyListener extends AbstractChainingListener
    {
    }

    private static class EndListItemChild extends AbstractChainingListener
    {
        boolean called;

        @Override
        public void endListItem()
        {
            this.called = true;
        }
    }

    // Tests

    @Test
    void beginListItemRetroCompatibility()
    {
        // Old, First level

        Child child = new Child();

        assertFalse(child.called);

        child.beginListItem(Collections.emptyMap());

        assertTrue(child.called);

        // Old, Second level

        Child2 child2 = new Child2();

        assertFalse(child2.called);

        child2.beginListItem(Collections.emptyMap());

        assertTrue(child2.called);

        // New

        Child3 child3 = new Child3();

        assertFalse(child3.called);

        child3.beginListItem(Collections.emptyMap());

        assertTrue(child3.called);
    }

    @Test
    void beginListItemRetroCompatibilityWithBothImplemented()
    {
        ListenerChain chain = new ListenerChain();
        ChildWithBothBeginMethods child = new ChildWithBothBeginMethods();
        child.setListenerChain(chain);
        chain.addListener(child);


        assertFalse(child.calledWithoutParameter);
        assertFalse(child.calledWithParameter);
        assertFalse(child.called);

        child.beginListItem(Listener.EMPTY_PARAMETERS);

        assertAll(
            () -> assertTrue(child.calledWithParameter, "Child hasn't been called with parameters."),
            () -> assertFalse(child.calledWithoutParameter, "Child has been called without parameters but shouldn't "
                + "have been called."),
            () -> assertFalse(child.called, "Parent has been called without parameters but shouldn't have been called.")
        );
    }

    /**
     * Test all methods of the {@link Listener} interface.
     * <p>
     * Tests for all methods if they properly forward the call to the next listener.
     *
     * @param method The method to test.
     * @param parameters Suitable parameters for the method.
     */
    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#allMethodsProvider")
    void allMethodsForward(Method method, Object[] parameters) throws InvocationTargetException,
        IllegalAccessException
    {
        ListenerChain chain = new ListenerChain();

        DummyListener listener = new DummyListener();
        listener.setListenerChain(chain);
        chain.addListener(listener);

        ChainingListener mockListener = mock(ChainingListener.class);
        chain.addListener(mockListener);

        method.invoke(listener, parameters);
        method.invoke(verify(mockListener), parameters);
        verifyNoMoreInteractions(mockListener);
    }

    @Test
    void endListItemRetroCompatibility()
    {
        EndListItemChild listener = new EndListItemChild();

        assertFalse(listener.called);
        listener.endListItem(Listener.EMPTY_PARAMETERS);
        assertTrue(listener.called);
    }
}

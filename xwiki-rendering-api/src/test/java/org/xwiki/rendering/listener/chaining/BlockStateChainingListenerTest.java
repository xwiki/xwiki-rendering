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
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.CaseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.stubbing.Stubber;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BlockStateChainingListener}.
 *
 * @version $Id$
 * @since 14.0RC1
 */
public class BlockStateChainingListenerTest
{
    private BlockStateChainingListener listener;

    private ChainingListener mockListener;

    @BeforeEach
    void setUpChain()
    {
        ListenerChain chain = new ListenerChain();
        this.listener = new BlockStateChainingListener(chain);
        chain.addListener(this.listener);
        this.mockListener = mock(ChainingListener.class);
        chain.addListener(this.mockListener);
    }

    /**
     * Tests for all "begin/end"-methods if they do not modify the parent event (for the next in the chain) in the
     * begin-method, correctly set it afterwards and if the previous event is correctly set, but only after the end
     * event has been forwarded in the chain.
     */
    @TestFactory
    Stream<DynamicTest> beginEndMethods()
    {
        return Arrays.stream(Listener.class.getMethods())
            .filter(m -> m.getName().startsWith("begin"))
            .map(beginMethod ->
                DynamicTest.dynamicTest(getTestName(beginMethod),
                    () -> testBeginEndMethod(beginMethod)));
    }

    /**
     * Tests for all "on..." methods if they do not modify the parent event (for the next in the chain) and if the
     * previous event is correctly set, but only after the event has been forwarded in the chain.
     */
    @TestFactory
    Stream<DynamicTest> onMethods()
    {
        return Arrays.stream(Listener.class.getMethods())
            .filter(m -> m.getName().startsWith("on"))
            .map(beginMethod ->
                DynamicTest.dynamicTest(getTestName(beginMethod),
                    () -> testOnMethod(beginMethod)));
    }

    private String getTestName(Method method)
    {
        return method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getName)
            .collect(Collectors.joining(", ")) + ")";
    }

    private void testBeginEndMethod(Method beginMethod)
    {
        String endMethodName = beginMethod.getName().replace("begin", "end");
        Class<?>[] parameterClasses = beginMethod.getParameterTypes();

        try {
            Method endMethod = Listener.class.getMethod(endMethodName, parameterClasses);

            boolean isListItem = beginMethod.getName().equals("beginListItem");
            boolean isDefinitionItem =
                beginMethod.getName().equals("beginDefinitionTerm") || beginMethod.getName().equals(
                    "beginDefinitionDescription");

            BlockStateChainingListener.Event expectedParentEvent;

            if (isListItem) {
                this.listener.beginList(ListType.NUMBERED, Listener.EMPTY_PARAMETERS);
                expectedParentEvent = BlockStateChainingListener.Event.LIST;
            } else if (isDefinitionItem) {
                this.listener.beginDefinitionList(Listener.EMPTY_PARAMETERS);
                expectedParentEvent = BlockStateChainingListener.Event.DEFINITION_LIST;
            } else {
                expectedParentEvent = null;
            }

            Object[] parameters = Arrays.stream(parameterClasses).map(this::mockParameter).toArray();

            this.listener.onId("MockID");

            Stubber verifyPreviousAndParentEventStubber = doAnswer(invocation -> {
                assertEquals(BlockStateChainingListener.Event.ID, this.listener.getPreviousEvent());
                assertEquals(expectedParentEvent, this.listener.getParentEvent());
                return null;
            }).doNothing();

            // Assert that in the begin method, the parent and the previous event are unchanged.
            beginMethod.invoke(verifyPreviousAndParentEventStubber.when(this.mockListener), parameters);

            // Actually call the begin method.
            beginMethod.invoke(this.listener, parameters);

            // Verify the mock listener in the chain has been called.
            beginMethod.invoke(verify(this.mockListener), parameters);

            BlockStateChainingListener.Event parentEvent = this.listener.getParentEvent();
            assertNotNull(parentEvent, "No parent set after calling " + beginMethod.getName());
            String parentEventName = parentEvent.name();
            String eventNameCamelCase = CaseUtils.toCamelCase(parentEventName, true, '_');
            assertEquals(beginMethod.getName(), "begin" + eventNameCamelCase,
                "Wrong event " + parentEventName + " generated for " + beginMethod.getName());

            // Assert that in the end method, the parent has been restored and the previous event is unchanged.
            endMethod.invoke(verifyPreviousAndParentEventStubber.when(this.mockListener), parameters);

            // Actually call the end method.
            endMethod.invoke(this.listener, parameters);

            // Verify the mock listener in the chain has been called.
            endMethod.invoke(verify(this.mockListener), parameters);

            // Verify that the previous event has been set to the event corresponding to the current methods.
            assertEquals(parentEvent, this.listener.getPreviousEvent());

            if (isDefinitionItem) {
                this.listener.endDefinitionList(Listener.EMPTY_PARAMETERS);
            } else if (isListItem) {
                this.listener.endList(ListType.NUMBERED, Listener.EMPTY_PARAMETERS);
            }

            assertNull(this.listener.getParentEvent());
        } catch (NoSuchMethodException e) {
            fail("Expected end method " + endMethodName + " for " + beginMethod.getName() + " not found: "
                + e.getMessage());
        } catch (InvocationTargetException e) {
            fail("Listener method has thrown exception: " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Listener method not callable: " + e.getMessage());
        }
    }

    private void testOnMethod(Method method)
    {
        this.listener.beginDocument(MetaData.EMPTY);

        // Make sure the previous event is one that we never trigger.
        this.listener.beginParagraph(Listener.EMPTY_PARAMETERS);
        this.listener.endParagraph(Listener.EMPTY_PARAMETERS);

        Object[] parameters = Arrays.stream(method.getParameterTypes()).map(this::mockParameter).toArray();

        try {
            // Verify that the next in the chain still gets the old previous event and that the parent is not
            // changed.
            method.invoke(
                doAnswer(invocationOnMock -> {
                    assertEquals(BlockStateChainingListener.Event.DOCUMENT, this.listener.getParentEvent());
                    assertEquals(BlockStateChainingListener.Event.PARAGRAPH, this.listener.getPreviousEvent());
                    return null;
                })
                    .doThrow(new AssertionError("Listener must only be called once"))
                    .when(this.mockListener),
                parameters);

            // Actually call the listener method.
            method.invoke(this.listener, parameters);

            // Verify that the call has been correctly forwarded.
            method.invoke(verify(this.mockListener), parameters);
        } catch (InvocationTargetException e) {
            fail("Listener method has thrown exception: " + e.getMessage());
        } catch (IllegalAccessException e) {
            fail("Listener method not callable: " + e.getMessage());
        }

        // Check if the previous event is the expected event.
        String previousEventName = this.listener.getPreviousEvent().name();

        // Verbatim has two events, as our mock boolean is true we always get the inline event.
        if (this.listener.getPreviousEvent().equals(BlockStateChainingListener.Event.VERBATIM_INLINE)) {
            previousEventName = "VERBATIM";
        }

        String eventCamelCaseName = CaseUtils.toCamelCase(previousEventName, true, '_');
        assertEquals(method.getName(), "on" + eventCamelCaseName, "Previous event " + previousEventName + " "
            + "does not match method name " + method.getName());

        this.listener.endDocument(MetaData.EMPTY);
    }

    /**
     * @param classToMock The class to return a mock object for.
     * @return Either a mock object or in the case of an enum or primitive type a concrete value.
     */
    private Object mockParameter(Class<?> classToMock)
    {
        if (classToMock.equals(Format.class)) {
            return Format.BOLD;
        }

        if (classToMock.equals(ListType.class)) {
            return ListType.BULLETED;
        }

        if (classToMock.equals(HeaderLevel.class)) {
            return HeaderLevel.LEVEL1;
        }

        if (classToMock.equals(String.class)) {
            return "Mock";
        }

        if (classToMock.equals(boolean.class)) {
            return true;
        }

        if (classToMock.equals(char.class)) {
            return '{';
        }

        if (classToMock.equals(int.class)) {
            return 42;
        }

        return mock(classToMock);
    }
}

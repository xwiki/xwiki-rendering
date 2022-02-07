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

import org.apache.commons.text.CaseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.Stubber;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BlockStateChainingListener}.
 *
 * @version $Id$
 * @since 14.0RC1
 */
class BlockStateChainingListenerTest
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
     * Test all begin/end-methods.
     *
     * Tests for all "begin/end"-methods if they do not modify the parent event (for the next in the chain) in the
     * begin-method, correctly set it afterwards and if the previous event is correctly set, but only after the end
     * event has been forwarded in the chain.
     *
     * @param beginMethod The method to begin the container.
     * @param endMethod The corresponding end method.
     * @param parameters Suitable parameters for both methods.
     */
    @ParameterizedTest(name = "{0} and {1} with {2}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#beginEndMethodsProvider")
    void testBeginEndMethod(Method beginMethod, Method endMethod, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        boolean isListItem = beginMethod.getName().equals("beginListItem");
        boolean isDefinitionItem = beginMethod.getName().equals("beginDefinitionTerm") || beginMethod.getName()
            .equals("beginDefinitionDescription");

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
    }

    /**
     * Test the "on"-methods of the {@link Listener} interface.
     *
     * Tests for all "on..." methods if they do not modify the parent event (for the next in the chain) and if the
     * previous event is correctly set, but only after the event has been forwarded in the chain.
     *
     * @param method The "on"-method.
     * @param parameters Suitable parameters for the method.
     */
    @ParameterizedTest(name = "{0} with {1}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#onMethodsProvider")
    void testOnMethod(Method method, Object[] parameters) throws InvocationTargetException, IllegalAccessException
    {
        this.listener.beginDocument(MetaData.EMPTY);

        // Make sure the previous event is one that we never trigger.
        this.listener.beginParagraph(Listener.EMPTY_PARAMETERS);
        this.listener.endParagraph(Listener.EMPTY_PARAMETERS);

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
}

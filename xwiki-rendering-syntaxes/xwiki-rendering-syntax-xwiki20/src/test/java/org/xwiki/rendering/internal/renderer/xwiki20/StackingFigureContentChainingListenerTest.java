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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit test for {@link StackingFigureContentChainingListener}.
 *
 * @version $Id$
 * @since 14.1RC1
 */
class StackingFigureContentChainingListenerTest
{
    private static final ResourceReference IMAGE_REFERENCE = new ResourceReference("test.png", ResourceType.ATTACHMENT);

    private static final Map<String, String> IMAGE_PARAMETERS = Map.of("param", "value");

    private static final List<String> ALLOWED_METHODS_IN_FIGURE = List.of("beginParagraph", "beginGroup",
        "beginFormat", "beginSection", "onSpace", "beginFigureCaption", "onEmptyLines", "onNewLine");

    private static final List<String> FORBIDDEN_METHODS_IN_FIGURE_CAPTION = List.of("beginFigure",
        "beginFigureCaption", "beginDocument");

    private static final Map<String, String> IMAGE_FIGURE_PARAMETER = Map.of("class", "image");

    private LookaheadChainingListener firstListener;
    private StackingFigureContentChainingListener listener;
    private ChainingListener mockListener;

    @BeforeEach
    void setupListenerChain()
    {
        ListenerChain listenerChain = new ListenerChain();

        // Add a dummy listener as first listener as StackingFigureContentChainingListener expects that setup.
        this.firstListener = new LookaheadChainingListener(listenerChain, 1);
        listenerChain.addListener(this.firstListener);

        this.listener = new StackingFigureContentChainingListener(listenerChain);
        listenerChain.addListener(this.listener);

        this.mockListener = mock(ChainingListener.class);
        listenerChain.addListener(this.mockListener);

    }

    /**
     * Test all begin/end-methods in the figure content.
     *
     * Tests for all "begin/end"-methods if they are correctly forwarded and the image is reported as clean only for
     * allowed methods in the figure content.
     *
     * @param beginMethod The method to begin the container.
     * @param endMethod The corresponding end method.
     * @param parameters Suitable parameters for both methods.
     */
    @ParameterizedTest(name = "{0} and {1} with {2}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#beginEndMethodsProvider")
    void testBeginEndMethodInFigure(Method beginMethod, Method endMethod, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        testMethodsInFigure(List.of(beginMethod, endMethod), parameters);
    }

    /**
     * Test all on-methods in the figure content.
     *
     * Tests for all "on"-methods if they are correctly forwarded and the image is reported as clean only for
     * allowed methods in the figure content.
     *
     * @param method The method to test.
     * @param parameters Suitable parameters for both methods.
     */
    @ParameterizedTest(name = "{0} and {1} with {2}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#onMethodsProvider")
    void testOnMethodInFigure(Method method, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        testMethodsInFigure(List.of(method), parameters);
    }

    private void testMethodsInFigure(List<Method> methods, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        // Assert that already in the begin method of the figure the correct results are provided.
        doAnswer(invocation -> {
            assertEquals(ALLOWED_METHODS_IN_FIGURE.contains(methods.get(0).getName()), this.listener.isCleanImageFigure());
            if (this.listener.isCleanImageFigure()) {
                assertSame(IMAGE_REFERENCE, this.listener.getImageReference());
                assertSame(IMAGE_PARAMETERS, this.listener.getImageParameters());
            }
            return null;
        }).when(this.mockListener).beginFigure(IMAGE_FIGURE_PARAMETER);

        this.firstListener.beginFigure(IMAGE_FIGURE_PARAMETER);
        for (Method method : methods) {
            method.invoke(this.firstListener, parameters);
        }
        this.firstListener.onImage(IMAGE_REFERENCE, false, IMAGE_PARAMETERS);
        this.firstListener.endFigure(IMAGE_FIGURE_PARAMETER);
        this.firstListener.endDocument(MetaData.EMPTY);

        InOrder inOrder = Mockito.inOrder(this.mockListener);
        inOrder.verify(this.mockListener).beginFigure(IMAGE_FIGURE_PARAMETER);
        for (Method method : methods) {
            method.invoke(inOrder.verify(this.mockListener), parameters);
        }
        inOrder.verify(this.mockListener).onImage(IMAGE_REFERENCE, false, IMAGE_PARAMETERS);
        inOrder.verify(this.mockListener).endFigure(IMAGE_FIGURE_PARAMETER);
        inOrder.verify(this.mockListener).endDocument(MetaData.EMPTY);

        verifyNoMoreInteractions(this.mockListener);
    }

    /**
     * Test all begin/end-methods in the figure content.
     *
     * Tests for all "begin/end"-methods if they are correctly forwarded and the image is reported as clean only for
     * allowed methods in the figure content.
     *
     * @param beginMethod The method to begin the container.
     * @param endMethod The corresponding end method.
     * @param parameters Suitable parameters for both methods.
     */
    @ParameterizedTest(name = "{0} and {1} with {2}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#beginEndMethodsProvider")
    void testBeginEndMethodInFigureCaption(Method beginMethod, Method endMethod, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        testMethodsInFigureCaption(List.of(beginMethod, endMethod), parameters);
    }

    /**
     * Test all on-methods in the figure content.
     *
     * Tests for all "on"-methods if they are correctly forwarded and the image is reported as clean only for
     * allowed methods in the figure content.
     *
     * @param method The method to test.
     * @param parameters Suitable parameters for both methods.
     */
    @ParameterizedTest(name = "{0} and {1} with {2}")
    @MethodSource("org.xwiki.rendering.test.ListenerMethodProvider#onMethodsProvider")
    void testOnMethodInFigureCaption(Method method, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        testMethodsInFigureCaption(List.of(method), parameters);
    }


    private void testMethodsInFigureCaption(List<Method> methods, Object[] parameters)
        throws InvocationTargetException, IllegalAccessException
    {
        // Assert that already in the begin method of the figure the correct results are provided.
        doAnswer(invocation -> {
            assertEquals(!FORBIDDEN_METHODS_IN_FIGURE_CAPTION.contains(methods.get(0).getName()),
                this.listener.isCleanImageFigure());
            if (this.listener.isCleanImageFigure()) {
                assertSame(IMAGE_REFERENCE, this.listener.getImageReference());
                assertSame(IMAGE_PARAMETERS, this.listener.getImageParameters());
            }
            return null;
        }).when(this.mockListener).beginFigure(IMAGE_FIGURE_PARAMETER);

        this.firstListener.beginFigure(IMAGE_FIGURE_PARAMETER);
        this.firstListener.onImage(IMAGE_REFERENCE, false, IMAGE_PARAMETERS);
        this.firstListener.beginFigureCaption(Listener.EMPTY_PARAMETERS);
        for (Method method : methods) {
            method.invoke(this.firstListener, parameters);
        }
        this.firstListener.endFigureCaption(Listener.EMPTY_PARAMETERS);
        this.firstListener.endFigure(IMAGE_FIGURE_PARAMETER);
        this.firstListener.endDocument(MetaData.EMPTY);

        InOrder inOrder = Mockito.inOrder(this.mockListener);
        inOrder.verify(this.mockListener).beginFigure(IMAGE_FIGURE_PARAMETER);
        inOrder.verify(this.mockListener).onImage(IMAGE_REFERENCE, false, IMAGE_PARAMETERS);
        inOrder.verify(this.mockListener).beginFigureCaption(Listener.EMPTY_PARAMETERS);
        for (Method method : methods) {
            method.invoke(inOrder.verify(this.mockListener), parameters);
        }
        inOrder.verify(this.mockListener).endFigureCaption(Listener.EMPTY_PARAMETERS);
        inOrder.verify(this.mockListener).endFigure(IMAGE_FIGURE_PARAMETER);
        inOrder.verify(this.mockListener).endDocument(MetaData.EMPTY);

        verifyNoMoreInteractions(this.mockListener);
    }
}

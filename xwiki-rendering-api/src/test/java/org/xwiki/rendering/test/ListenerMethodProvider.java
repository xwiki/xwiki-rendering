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
package org.xwiki.rendering.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

/**
 * Provider for methods of the listener interface.
 *
 * @version $Id$
 * @since 14.1RC1
 * @since 13.10.3
 */
public class ListenerMethodProvider
{
    /**
     * @return A stream of {@link Arguments} consisting each all begin-methods of the {@link Listener} interface,
     * each with the matching end method and suitable parameters.
     */
    public static Stream<Arguments> beginEndMethodsProvider()
    {
        return Arrays.stream(Listener.class.getMethods()).filter(m -> m.getName().startsWith("begin")).map(m -> {
            String endMethodName = m.getName().replace("begin", "end");
            Method endMethod = null;
            try {
                endMethod = Listener.class.getMethod(endMethodName, m.getParameterTypes());
            } catch (NoSuchMethodException e) {
                fail("Expected end method " + endMethodName + " for " + m.getName() + " not found: " + e.getMessage());
            }
            return arguments(
                Named.of(getTestName(m), m),
                Named.of(getTestName(endMethod), endMethod),
                getMockParameters(m)
            );
        });
    }

    /**
     * @return A stream of {@link Arguments} consisting each of all "on"-method of the {@link Listener} interface and
     * suitable parameters.
     */
    public static Stream<Arguments> onMethodsProvider()
    {
        return Arrays.stream(Listener.class.getMethods())
            .filter(m -> m.getName().startsWith("on")).map(m -> arguments(
                Named.of(getTestName(m), m),
                getMockParameters(m)
                ));
    }

    /**
     * @return A stream of {@link Arguments} consisting each of a method of the {@link Listener} interface and
     * suitable parameters.
     */
    public static Stream<Arguments> allMethodsProvider()
    {
        return Arrays.stream(Listener.class.getMethods())
            .map(m -> arguments(Named.of(getTestName(m), m), getMockParameters(m)));
    }

    /**
     * @param method The method to get a name for.
     * @return The name of the method without class but with all parameter types.
     */
    static private String getTestName(Method method)
    {
        return method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getName)
            .collect(Collectors.joining(", ")) + ")";
    }

    /**
     * @param method The method to get parameters for.
     * @return A mock object or value for each expected parameter.
     */
    static private Object getMockParameters(Method method)
    {
        return Arrays.stream(method.getParameterTypes()).map(ListenerMethodProvider::mockParameter).toArray();
    }

    /**
     * @param classToMock The class to return a mock object for.
     * @return Either a mock object or in the case of an enum or primitive type a concrete value.
     */
    static private Object mockParameter(Class<?> classToMock)
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

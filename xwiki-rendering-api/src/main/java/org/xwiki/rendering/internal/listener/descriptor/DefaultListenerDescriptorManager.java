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
package org.xwiki.rendering.internal.listener.descriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptor;
import org.xwiki.rendering.listener.descriptor.ListenerDescriptorManager;
import org.xwiki.rendering.listener.descriptor.ListenerElement;

/**
 * Default implementation of {@link ListenerDescriptorManager}.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Component
@Singleton
public class DefaultListenerDescriptorManager implements ListenerDescriptorManager
{
    /**
     * The prefix of the begin events.
     */
    private static final String PREFIX_BEGIN = "begin";

    /**
     * The prefix of the end events.
     */
    private static final String PREFIX_END = "end";

    /**
     * The prefix of the on events.
     */
    private static final String PREFIX_ON = "on";

    /**
     * The descriptors.
     */
    private Map<Class<?>, ListenerDescriptor> descriptors = new ConcurrentHashMap<Class<?>, ListenerDescriptor>();

    @Override
    public ListenerDescriptor getListenerDescriptor(Class<?> type)
    {
        ListenerDescriptor descriptor = this.descriptors.get(type);
        if (descriptor == null) {
            descriptor = createDescriptor(type);
            this.descriptors.put(type, descriptor);
        }

        return descriptor;
    }

    /**
     * @param type the class of the listener
     * @return the descriptor of the listener
     */
    public ListenerDescriptor createDescriptor(Class<?> type)
    {
        ListenerDescriptor descriptor = new ListenerDescriptor();

        for (Method method : ReflectionUtils.getTypeClass(type).getMethods()) {
            String methodName = method.getName();

            String elementName;
            if (methodName.startsWith(PREFIX_BEGIN)) {
                elementName = methodName.substring(PREFIX_BEGIN.length(), methodName.length());
            } else if (methodName.startsWith(PREFIX_END)) {
                elementName = methodName.substring(PREFIX_END.length(), methodName.length());
            } else if (methodName.startsWith(PREFIX_ON)) {
                elementName = methodName.substring(PREFIX_ON.length(), methodName.length());
            } else {
                elementName = null;
            }

            if (elementName != null) {
                elementName =
                    Character.toLowerCase(elementName.charAt(0)) + elementName.substring(1, elementName.length());

                addElement(elementName, descriptor, method);
            }

        }

        return descriptor;
    }

    /**
     * @param elementName the name of the element
     * @param descriptor the descriptor in which to add the element
     * @param method the method associated to the element
     */
    private void addElement(String elementName, ListenerDescriptor descriptor, Method method)
    {
        String lowerElementName = elementName.toLowerCase(Locale.ROOT);

        ListenerElement element = descriptor.getElements().get(lowerElementName);

        Type[] methodTypes = method.getGenericParameterTypes();
        // TODO: add support for multiple methods
        if (element == null || methodTypes.length > element.getParameters().size()) {
            element = new ListenerElement(elementName);

            element.getParameters().clear();
            for (Type parameterType : method.getGenericParameterTypes()) {
                element.getParameters().add(parameterType);
            }

            descriptor.getElements().put(lowerElementName, element);
        }

        addMethod(element, method);
    }

    /**
     * @param element the element
     * @param method the method to add to the element
     */
    private void addMethod(ListenerElement element, Method method)
    {
        String methodName = method.getName();
        Type[] methodTypes = method.getGenericParameterTypes();

        if (methodName.startsWith(PREFIX_BEGIN)) {
            if (element.getBeginMethod() == null
                || element.getBeginMethod().getGenericParameterTypes().length < methodTypes.length) {
                element.setBeginMethod(method);
            }
        } else if (methodName.startsWith(PREFIX_END)) {
            if (element.getEndMethod() == null
                || element.getEndMethod().getGenericParameterTypes().length < methodTypes.length) {
                element.setEndMethod(method);
            }
        } else {
            if (element.getOnMethod() == null
                || element.getOnMethod().getGenericParameterTypes().length < methodTypes.length) {
                element.setOnMethod(method);
            }
        }
    }
}

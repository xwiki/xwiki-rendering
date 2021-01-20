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
package org.xwiki.rendering.test.integration.junit5;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.opentest4j.IncompleteExecutionException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.test.integration.TestDataGenerator;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.jmock.XWikiComponentInitializer;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * Wraps a {@link RenderingTest} by setting up a Component Manager, executing
 * methods annotated with {@link org.xwiki.rendering.test.integration.junit5.RenderingTests.Initialized} and tearing
 * down the Component Manager.
 *
 * @version $Id$
 * @since 13.0
 */
public class RenderingTestExecutor
{
    private final XWikiComponentInitializer componentInitializer = new XWikiComponentInitializer();

    private final MockitoComponentManager mockitoComponentManager = new MockitoComponentManager();

    /**
     * @param objects the test data objects returned by {@link TestDataGenerator#generateData(String, String)}
     * @param testInstance the test instance being executed
     * @throws Exception in case of error
     */
    public void execute(Object[] objects, Object testInstance) throws Exception
    {
        initializeComponentManager(testInstance);

        // Check all methods for a ComponentManager annotation and call the found ones.
        try {
            for (Method klassMethod : testInstance.getClass().getMethods()) {
                RenderingTests.Initialized
                    componentManagerAnnotation = klassMethod.getAnnotation(RenderingTests.Initialized.class);
                if (componentManagerAnnotation != null) {
                    // Call it!
                    klassMethod.invoke(testInstance, getComponentManager(testInstance));
                }
            }
        } catch (Exception e) {
            throw new IncompleteExecutionException("Failed to call Component Manager initialization method", e);
        }

        try {
            Object[] newObjects = computeParams(objects, testInstance);
            RenderingTest renderingTest = new RenderingTest((String) newObjects[0], (String) newObjects[1],
                (String) newObjects[2], (String) newObjects[3], (boolean) newObjects[4], (List<String>) newObjects[5],
                (Map<String, ?>) newObjects[6], (ComponentManager) newObjects[7]);
            renderingTest.execute();
        } finally {
            shutdownComponentManager(testInstance);
        }
    }

    private Object[] computeParams(Object[] originalObjects, Object testInstance) throws Exception
    {
        // Add the Component Manager as the last parameter in order to pass it to the Test constructor
        // Remove the first parameter which is the test name and that is not needed in RenderingTest
        Object[] newObjects = new Object[originalObjects.length];
        System.arraycopy(originalObjects, 1, newObjects, 0, originalObjects.length - 1);
        newObjects[originalObjects.length - 1] = getComponentManager(testInstance);
        return newObjects;
    }

    private void initializeComponentManager(Object testInstance)
    {
        try {
            if (isLegacyMode(testInstance)) {
                this.componentInitializer.initializeConfigurationSource();
                this.componentInitializer.initializeExecution();
            } else {
                this.mockitoComponentManager.initializeTest(testInstance);
                this.mockitoComponentManager.registerMemoryConfigurationSource();
            }
        } catch (Exception e) {
            throw new IncompleteExecutionException("Failed to initialize Component Manager", e);
        }
    }

    private void shutdownComponentManager(Object testInstance)
    {
        try {
            if (isLegacyMode(testInstance)) {
                this.componentInitializer.shutdown();
            } else {
                this.mockitoComponentManager.shutdownTest();
            }
        } catch (Exception e) {
            throw new IncompleteExecutionException("Failed to shutdown Component Manager", e);
        }
    }

    private ComponentManager getComponentManager(Object testInstance) throws Exception
    {
        if (isLegacyMode(testInstance)) {
            return this.componentInitializer.getComponentManager();
        } else {
            return this.mockitoComponentManager;
        }
    }

    private boolean isLegacyMode(Object testInstance)
    {
        boolean isLegacyMode = true;
        for (Method klassMethod : testInstance.getClass().getMethods()) {
            RenderingTests.Initialized
                componentManagerAnnotation = klassMethod.getAnnotation(RenderingTests.Initialized.class);
            if (componentManagerAnnotation != null) {
                if (MockitoComponentManager.class.isAssignableFrom(klassMethod.getParameterTypes()[0])) {
                    isLegacyMode = false;
                }
                break;
            }
        }
        // If the class is using either @AllComponents or @ComponentList then consider we're not in legacy.
        if (isLegacyMode
            && (testInstance.getClass().getAnnotation(AllComponents.class) != null
            || testInstance.getClass().getAnnotation(ComponentList.class) != null))
        {
            isLegacyMode = false;
        }
        return isLegacyMode;
    }
}

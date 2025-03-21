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
package org.xwiki.rendering.test.integration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.IncompleteExecutionException;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class AbstractRenderingTest
{
    private MockitoComponentManager componentManager;

    @BeforeEach
    void initializeComponentManager()
    {
        // Initialize a component manager used to locate parsers and renderers to decide what tests to execute in
        // TestDataGenerator and also used in RenderingTestExecutor to execute the tests.
        if (this.componentManager == null) {
            MockitoComponentManager mockitoComponentManager = new MockitoComponentManager();
            try {
                mockitoComponentManager.initializeTest(this);
                mockitoComponentManager.registerMemoryConfigurationSource();
            } catch (Exception e) {
                throw new IncompleteExecutionException("Failed to initialize Component Manager", e);
            }
            this.componentManager = mockitoComponentManager;
        }
    }

    @BeforeEach
    void callInitializers()
    {
        callAnnotatedMethods(Initialized.class);
    }

    @AfterEach
    void shutdownComponentManager()
    {
        if (this.componentManager != null) {
            try {
                this.componentManager.shutdownTest();
            } catch (Exception e) {
                throw new IncompleteExecutionException("Failed to shutdown Component Manager", e);
            }
        }
    }

    protected MockitoComponentManager getComponentManager()
    {
        return this.componentManager;
    }

    private void callAnnotatedMethods(Class<? extends Annotation> annotationClass)
    {
        try {
            for (Method klassMethod : getClass().getDeclaredMethods()) {
                Annotation componentManagerAnnotation = klassMethod.getAnnotation(annotationClass);
                if (componentManagerAnnotation != null) {
                    // Call it!
                    klassMethod.invoke(this, this.componentManager);
                }
            }
        } catch (Exception e) {
            throw new IncompleteExecutionException(String.format("Failed to call test methods annotated with [%s]",
                annotationClass.getCanonicalName()), e);
        }
    }
}

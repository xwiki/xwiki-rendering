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
package org.xwiki.rendering.test.cts;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.xwiki.test.XWikiComponentInitializer;

/**
 * Represents a Test Runner for a single Rendering Test to execute.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class RenderingTestClassRunner  extends BlockJUnit4ClassRunner
{
    /**
     * Used to pass the Component Manager to the Rendering Test instance executing.
     */
    private XWikiComponentInitializer componentInitializer = new XWikiComponentInitializer();

    /**
     * @see #RenderingTestClassRunner(Object, Class, TestData, String)
     */
    private Object testInstance;

    /**
     * @see #RenderingTestClassRunner(Object, Class, TestData, String)
     */
    private TestData testData;

    /**
     * @see #RenderingTestClassRunner(Object, Class, TestData, String)
     */
    private String metadataSyntaxId;

    /**
     * @param testInstance the Test instance (The Test instance is the class on which this Compatibility Test Suite is
     *        used)
     * @param testClass the {@link RenderingTest} class
     * @param testData the Test Data, passed to the Rendering Test instance executing
     * @param metadataSyntaxId the Syntax id of the syntax used as Metadata in the generated XDOM for parsers
     * @throws InitializationError if the {@link RenderingTest} isn't a valid JUnit Test class
     */
    RenderingTestClassRunner(Object testInstance, Class<?> testClass, TestData testData, String metadataSyntaxId)
        throws InitializationError
    {
        super(testClass);
        this.testInstance = testInstance;
        this.testData = testData;
        this.metadataSyntaxId = metadataSyntaxId;
    }

    @Override
    public Object createTest() throws Exception
    {
        return getTestClass().getOnlyConstructor().newInstance(
            this.testData, this.metadataSyntaxId, this.componentInitializer.getComponentManager());
    }

    @Override
    protected String getName()
    {
        return this.testData.computeTestName();
    }

    @Override
    protected String testName(final FrameworkMethod method)
    {
        return getName();
    }

    @Override
    protected void validateConstructor(List<Throwable> errors)
    {
        validateOnlyOneConstructor(errors);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier)
    {
        return childrenInvoker(notifier);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Initialize the Component Manager and call all methods annotated with {@link Initialized} in the suite,
     * before each test is executed, to ensure test isolation.
     * </p>
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier)
    {
        try {
            this.componentInitializer.initializeConfigurationSource();
            this.componentInitializer.initializeExecution();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Component Manager", e);
        }

        // Check all methods for a ComponentManager annotation and call the found ones.
        try {
            for (Method klassMethod : testInstance.getClass().getMethods()) {
                Initialized componentManagerAnnotation = klassMethod.getAnnotation(Initialized.class);
                if (componentManagerAnnotation != null) {
                    // Call it!
                    klassMethod.invoke(testInstance, this.componentInitializer.getComponentManager());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to call Component Manager initialization method", e);
        }

        try {
            super.runChild(method, notifier);
        } finally {
            try {
                this.componentInitializer.shutdown();
            } catch (Exception e) {
                throw new RuntimeException("Failed to shutdown Component Manager", e);
            }
        }
    }
}

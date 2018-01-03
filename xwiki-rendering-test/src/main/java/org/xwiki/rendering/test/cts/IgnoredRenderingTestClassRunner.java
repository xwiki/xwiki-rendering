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

import org.junit.internal.builders.IgnoredClassRunner;
import org.junit.runner.Description;

/**
 * Used to ignore tests for which there is CTS data but no Syntax test data.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class IgnoredRenderingTestClassRunner extends IgnoredClassRunner
{
    /**
     * @see #IgnoredRenderingTestClassRunner(Class, TestData)
     */
    private final TestData testData;

    /**
     * @see #IgnoredRenderingTestClassRunner(Class, TestData)
     */
    private Class<?> testClass;

    /**
     * @param testClass the {@link RenderingTest} class
     * @param testData the Test Data, passed to the Rendering Test instance executing
     */
    public IgnoredRenderingTestClassRunner(Class<?> testClass, TestData testData)
    {
        super(testClass);
        this.testClass = testClass;
        this.testData = testData;
    }

    @Override
    public Description getDescription()
    {
        // Add the cause of the ignore at the end of the test description
        StringBuilder testName = new StringBuilder(this.testData.computeTestName());
        if (this.testData.syntaxData == null) {
            testName.append(" - Missing");
        } else if (this.testData.isFailingTest()) {
            testName.append(" - Failing");
        }

        return Description.createTestDescription(this.testClass, testName.toString());
    }
}

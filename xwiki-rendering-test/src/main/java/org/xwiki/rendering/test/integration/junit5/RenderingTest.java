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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.xwiki.rendering.test.integration.AbstractRenderingTest;
import org.xwiki.rendering.test.integration.Scope;
import org.xwiki.rendering.test.integration.TestDataGenerator;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 * <p>Usage Example</p>
 * <pre><code>
 * &#064;AllComponents
 * class MyIntegrationTests extends RenderingTest
 * {
 * }
 * </code></pre>
 * <p>It's also possible to get access to the underlying Component Manager used, for example in order to register
 * Mock implementations of components. For example:</p>
 * <pre><code>
 * &#064;AllComponents
 * class MyIntegrationTests extends RenderingTest
 * {
 *     &#064;Initialized
 *     public void initialize(MockitoComponentManager componentManager)
 *     {
 *         // Init mocks here for example
 *     }
 * }
 * </code></pre>
 *
 * @version $Id$
 * @since 13.0
 */
public class RenderingTest extends AbstractRenderingTest
{
    /**
     * @return the dynamic list of tests to execute
     */
    @TestFactory
    Stream<DynamicTest> renderingTests()
    {
        // Step 1: Generate inputs

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = getClass().getAnnotation(Scope.class);
        String packagePrefix = "";
        String pattern = Scope.DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }
        TestDataGenerator generator = new TestDataGenerator();
        List<Object[]> parametersList = (List<Object[]>) generator.generateData(packagePrefix, pattern);

        // Step 2: Generate test names
        Function<Object[], String> displayNameGenerator = (input) -> (String) input[0];

        // Step 3: Generate tests to execute
        ThrowingConsumer<Object[]> testExecutor = (input) -> {
            new InternalRenderingTest((String) input[1], (String) input[2],
                (String) input[3], (String) input[4], (boolean) input[5], (List<String>) input[6],
                (Map<String, ?>) input[7], getComponentManager()).execute();
        };

        // Return the dynamically created tests
        return DynamicTest.stream(parametersList.iterator(), displayNameGenerator, testExecutor);
    }
}

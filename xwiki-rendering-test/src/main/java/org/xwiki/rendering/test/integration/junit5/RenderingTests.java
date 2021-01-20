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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.xwiki.rendering.test.integration.TestDataGenerator;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 * <p>Usage Example</p>
 * <pre>
 * <code>
 * public class IntegrationTests implements RenderingTests
 * {
 * }
 * </code>
 * </pre>
 * <p>It's also possible to get access to the underlying Component Manager used, for example in order to register
 * Mock implementations of components. For example:</p>
 * <pre>
 * <code>
 * {@literal @}AllComponents
 * public class IntegrationTests implements RenderingTests
 * {
 *     {@literal @}RenderingTests.Initialized
 *     public void initialize(MockitoComponentManager componentManager)
 *     {
 *         // Init mocks here for example
 *     }
 * }
 * </code>
 * </pre>
 *
 * @version $Id$
 * @since 13.0
 */
public interface RenderingTests
{
    /**
     * The default pattern matching the tests to execute.
     */
    String DEFAULT_PATTERN = ".*\\.test";

    /**
     * Annotation to use to indicate a method to execute before the test executes.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Initialized
    {
    }

    /**
     * Annotation to use to indicate the resources directory containing the tests to execute.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Scope
    {
        /**
         * @return the classpath prefix to search in
         */
        String value() default "";

        /**
         * @return the regex pattern to filter *.test files to execute
         */
        String pattern() default DEFAULT_PATTERN;
    }

    /**
     * @return the dynamic list of tests to execute
     */
    @TestFactory
    default Stream<DynamicTest> renderingTests()
    {
        // Step 1: Generate inputs

        // If a Scope Annotation is present then use it to define the scope
        RenderingTests.Scope scopeAnnotation = getClass().getAnnotation(RenderingTests.Scope.class);
        String packagePrefix = "";
        String pattern = DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }
        TestDataGenerator generator = new TestDataGenerator();
        List<Object[]> parametersList = (List<Object[]>) generator.generateData(packagePrefix, pattern);

        // Step 2: Generate test names
        Function<Object[], String> displayNameGenerator = (input) -> (String) input[0];

        // Step 3: Generate tests to execute
        RenderingTestExecutor executor = new RenderingTestExecutor();
        ThrowingConsumer<Object[]> testExecutor = (input) -> {
            executor.execute(input, this);
        };

        // Return the dynamically created tests
        return DynamicTest.stream(parametersList.iterator(), displayNameGenerator, testExecutor);
    }
}

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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify which test data to run in the test suite. By default runs all tests ending with {@code .xml} found
 * in the classpath.
 * <p>
 * For example:
 * </p>
 * <pre><code>
 * &#064;RunWith(CompatibilityTestSuite.class)
 * &#064;Syntax("xwiki/2.0")
 * &#064;Scope("simple")
 * public class MySyntaxTest
 * {
 * ...
 * }
 * </code></pre>
 *
 * @version $Id$
 * @since 4.1M1
 * @see CompatibilityTestSuite
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Scope
{
    /**
     * The default regex to locate test data files, see {@link CompatibilityTestSuite}.
     */
    String DEFAULT_PATTERN = ".*\\.xml";

    /**
     * @return the classpath prefix to search in.
     */
    String value() default "";

    /**
     * @return the regex pattern to filter files to execute, see {@link CompatibilityTestSuite}.
     */
    String pattern() default DEFAULT_PATTERN;
}

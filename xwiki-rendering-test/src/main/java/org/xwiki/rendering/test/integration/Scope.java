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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.xwiki.rendering.test.integration.junit5.RenderingTest;

/**
 * Annotation to use to indicate the resources directory containing the tests to execute.
 *
 * @version $Id$
 * @since 17.0.0RC1
 * @see RenderingTest
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Scope
{
    /**
     * The default pattern matching the tests to execute.
     */
    String DEFAULT_PATTERN = ".*\\.test";

    /**
     * @return the classpath prefix to search in
     */
    String value() default "";

    /**
     * @return the regex pattern to filter *.test files to execute
     */
    String pattern() default DEFAULT_PATTERN;
}

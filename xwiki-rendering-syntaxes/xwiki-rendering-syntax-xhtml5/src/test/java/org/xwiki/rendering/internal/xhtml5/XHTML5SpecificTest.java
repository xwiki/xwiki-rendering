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
package org.xwiki.rendering.internal.xhtml5;

import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;

/**
 * Run all specific tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow
 * the conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 */
@RenderingTests.Scope(value = "xhtml5.specific")
public class XHTML5SpecificTest implements RenderingTests
{
}

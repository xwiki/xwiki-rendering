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

/**
 * Contains Test Data for a single test. Specifically contains:
 * <ul>
 *   <li>The CTS input content</li>
 *   <li>The CTS output content</li>
 *   <li>The syntax-specific input content (or null if not defined)</li>
 *   <li>The syntax-specific output content (or null if not defined)</li>
 * </ul>
 *
 * @version $Id$
 * @since 4.1M1
 */
public class TestData
{
    public String ctsInput;

    public String ctsOutput;

    public String syntaxInput;

    public String syntaxOutput;
}

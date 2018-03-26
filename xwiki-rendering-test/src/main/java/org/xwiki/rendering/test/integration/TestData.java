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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 * @since 3.0RC1
 */
public class TestData
{
    public Map<String, String> inputs = new LinkedHashMap<String, String>();

    public Map<String, String> expectations = new LinkedHashMap<String, String>();

    /**
     * @since 3.0M3
     */
    public boolean streaming;

    /**
     * If null it means don't execute transformations. If empty then it means execute all transformations. Otherwise
     * execute the transformations corresponding to the tx hints in the list.
     *
     * @since 9.11.4
     * @since 10.2
     */
    public List<String> transformations;

    /**
     * @since 2.5RC1
     */
    public Map<String, String> configuration = new LinkedHashMap<String, String>();
}

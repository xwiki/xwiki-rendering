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
package org.xwiki.rendering.test.cts.junit5;

import org.opentest4j.AssertionFailedError;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.test.cts.AbstractRenderingTest;
import org.xwiki.rendering.test.cts.CompatibilityTestSuite;
import org.xwiki.rendering.test.cts.TestData;

/**
 * A generic JUnit Test used by {@link CompatibilityTestSuite} to run a single CTS test.
 *
 * @version $Id$
 * @since 13.0
 */
public class RenderingTest extends AbstractRenderingTest
{
    /**
     * @param testData the data for a single test
     * @param metadataSyntaxId the Syntax id of the syntax used as Metadata in the generated XDOM for parsers
     * @param componentManager the component manager used to find Parser and Renderers
     */
    public RenderingTest(TestData testData, String metadataSyntaxId, ComponentManager componentManager)
    {
        super(testData, metadataSyntaxId, componentManager);
    }

    @Override
    protected void throwAssertionException(String message, String expected, String result)
    {
        throw new AssertionFailedError(message, expected, result);
    }
}

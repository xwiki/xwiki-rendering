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
package org.xwiki.rendering.listener.chaining;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link AbstractChainingListener}.
 * 
 * @version $Id$
 */
public class AbstractChainingListenerTest
{
    private class AsbtractChild extends AbstractChainingPrintRenderer
    {
        boolean called;

        @Override
        public void beginListItem()
        {
            this.called = true;
        }
    }

    private class Child extends AbstractChainingListener
    {
        boolean called;

        @Override
        public void beginListItem()
        {
            this.called = true;
        }
    }

    private class Child2 extends AsbtractChild
    {

    }

    private class Child3 extends AbstractChainingListener
    {
        boolean called;

        @Override
        public void beginListItem(Map<String, String> parameters)
        {
            this.called = true;
        }
    }

    // Tests

    @Test
    public void beginListItemRetroCompatibility()
    {
        // Old, First level

        Child child = new Child();

        assertFalse(child.called);

        child.beginListItem(Collections.emptyMap());

        assertTrue(child.called);

        // Old, Second level

        Child2 child2 = new Child2();

        assertFalse(child2.called);

        child2.beginListItem(Collections.emptyMap());

        assertTrue(child2.called);

        // New

        Child3 child3 = new Child3();

        assertFalse(child3.called);

        child3.beginListItem(Collections.emptyMap());

        assertTrue(child3.called);
    }
}

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

import org.junit.jupiter.api.Test;
import org.xwiki.rendering.listener.MetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link MetaDataStateChainingListener}.
 *
 * @version $Id$
 * @since 3.0M2
 */
public class MetaDataStateChainingListenerTest
{
    @Test
    public void getMetaData()
    {
        MetaDataStateChainingListener listener = new MetaDataStateChainingListener(new ListenerChain());

        MetaData metaData1 = new MetaData();
        metaData1.addMetaData("key1", "value1");
        metaData1.addMetaData("key2", "value2");
        listener.beginMetaData(metaData1);

        assertEquals("value1", listener.getMetaData("key1"));
        assertEquals("value2", listener.getMetaData("key2"));
        assertNull(listener.getMetaData("unknown"));

        MetaData metaData2 = new MetaData();
        metaData2.addMetaData("key3", "value3");
        listener.beginMetaData(metaData2);

        assertEquals("value1", listener.getMetaData("key1"));
        assertEquals("value2", listener.getMetaData("key2"));
        assertEquals("value3", listener.getMetaData("key3"));
        assertNull(listener.getMetaData("unknown"));

        MetaData metaData3 = new MetaData();
        metaData3.addMetaData("key1", "value4");
        listener.beginMetaData(metaData3);

        assertEquals("value4", listener.getMetaData("key1"));
        assertEquals("value2", listener.getMetaData("key2"));
        assertEquals("value3", listener.getMetaData("key3"));
        assertNull(listener.getMetaData("unknown"));

        listener.endMetaData(metaData3);

        assertEquals("value1", listener.getMetaData("key1"));
        assertEquals("value2", listener.getMetaData("key2"));
        assertEquals("value3", listener.getMetaData("key3"));
        assertNull(listener.getMetaData("unknown"));

        listener.endMetaData(metaData2);

        assertEquals("value1", listener.getMetaData("key1"));
        assertEquals("value2", listener.getMetaData("key2"));
        assertNull(listener.getMetaData("unknown"));

        listener.endMetaData(metaData1);
        assertNull(listener.getMetaData("key1"));
    }
}

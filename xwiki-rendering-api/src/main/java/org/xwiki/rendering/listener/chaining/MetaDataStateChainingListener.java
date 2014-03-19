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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.xwiki.rendering.listener.MetaData;

/**
 * Provides the accumulated MetaData for all the previous blocks.
 *
 * @version $Id$
 * @since 3.0M2
 */
public class MetaDataStateChainingListener extends AbstractChainingListener
{
    /**
     * @see #getMetaData(String)
     */
    private Deque<MetaData> metaDataStack = new ArrayDeque<MetaData>();

    /**
     * @param listenerChain see {@link #getListenerChain()}
     */
    public MetaDataStateChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    /**
     * @param <T> the type of the value for the passed key
     * @param key the key for which to find the value
     * @return the accumulated MetaData during all the previous begin/endMetaData events
     */
    public <T> List<T> getAllMetaData(String key)
    {
        List<T> result = new ArrayList<T>();
        if (!this.metaDataStack.isEmpty()) {
            Iterator<MetaData> it = this.metaDataStack.descendingIterator();
            while (it.hasNext()) {
                MetaData metaData = it.next();
                Object value = metaData.getMetaData(key);
                if (value != null) {
                    result.add((T) metaData.getMetaData(key));
                }
            }
        }
        return result;
    }

    /**
     * @param <T> the type of the value for the passed key
     * @param key the key for which to find the value
     * @return the accumulated MetaData during all the previous begin/endMetaData events, for the passed key
     */
    public <T> T getMetaData(String key)
    {
        T result = null;
        if (!this.metaDataStack.isEmpty()) {
            for (MetaData metaData : this.metaDataStack) {
                result = (T) metaData.getMetaData(key);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void beginDocument(MetaData metaData)
    {
        this.metaDataStack.push(metaData);
        super.beginDocument(metaData);
    }

    @Override
    public void endDocument(MetaData metaData)
    {
        super.endDocument(metaData);
        this.metaDataStack.pop();
    }

    @Override
    public void beginMetaData(MetaData metaData)
    {
        this.metaDataStack.push(metaData);
        super.beginMetaData(metaData);
    }

    @Override
    public void endMetaData(MetaData metaData)
    {
        super.endMetaData(metaData);
        this.metaDataStack.pop();
    }
}

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
package org.xwiki.rendering.internal.parser.uniast;

import java.util.Map;

import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.WrappingListener;

/**
 * Used to parse plain text without generating any document or paragraph events.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
public class PlainTextWrappingListener extends WrappingListener
{
    /**
     * Creates a new instance that wraps the given listener.
     *
     * @param wrappedListener the listener to wrap
     */
    public PlainTextWrappingListener(Listener wrappedListener)
    {
        setWrappedListener(wrappedListener);
    }

    @Override
    public void beginDocument(MetaData metadata)
    {
        // ignore.
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        // ignore.
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        // ignore.
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        // ignore.
    }
}

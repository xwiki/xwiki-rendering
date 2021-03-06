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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import java.util.Map;

import org.xwiki.rendering.listener.WrappingListener;

/**
 * Wrap a listener and skip begin/end section & paragraph.
 *
 * @version $Id$
 * @since 10.10RC1
 */
public class InlineFilterListener extends WrappingListener
{
    @Override
    public void beginSection(Map<String, String> parameters)
    {
        // Disable this event
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        // Disable this event
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        // Disable this event
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        // Disable this event
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        // Disable this event
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        // Disable this event
    }
}

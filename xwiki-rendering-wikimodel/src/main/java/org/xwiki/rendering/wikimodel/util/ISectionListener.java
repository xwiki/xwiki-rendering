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
package org.xwiki.rendering.wikimodel.util;

/**
 * @param <T> the type of data managed by this listener
 * @version $Id$
 * @since 4.0M1
 */
public interface ISectionListener<T>
{
    public static interface IPos<T>
    {
        T getData();

        int getDocumentLevel();

        int getHeaderLevel();
    }

    void beginDocument(IPos<T> pos);

    void beginSection(IPos<T> pos);

    void beginSectionContent(IPos<T> pos);

    void beginSectionHeader(IPos<T> pos);

    void endDocument(IPos<T> pos);

    void endSection(IPos<T> pos);

    void endSectionContent(IPos<T> pos);

    void endSectionHeader(IPos<T> pos);
}
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
package org.xwiki.rendering.wikimodel;

/**
 * This interface is a marker for classes implementing all WEM listeners. In the
 * future it should be removed and replaced by an Object Adapter providing
 * individual listeners. Such a provider should be used something like that:
 *
 * <pre>
 * IWemListenerProvider provider = new MyWemListenerProvider();
 * provider.registerListener(
 *      IWemListenerDocument.class,
 *      new MyDocumentListener());
 * ...
 * IWemListenerDocument docListener =
 *      provider.getListener(IWemListenerProvider.class);
 * if (docListener != null) {
 *      docListener.beginDocument();
 * }
 * </pre>
 *
 * Adapter-based approach is much more flexible and it can be used to
 * transparently extend parsers to handle new types of structural elements.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemListener
    extends
    IWemListenerDocument,
    IWemListenerSimpleBlocks,
    IWemListenerInline,
    IWemListenerTable,
    IWemListenerList,
    IWemListenerSemantic,
    IWemListenerProgramming,
    IWemListenerFigure
{
    //
}

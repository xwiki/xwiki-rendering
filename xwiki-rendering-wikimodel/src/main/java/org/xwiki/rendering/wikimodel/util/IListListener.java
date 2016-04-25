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
 * @version $Id$
 * @since 4.0M1
 */
public interface IListListener
{
    /**
     * Begins of a new row.
     *
     * @param treeType TODO
     * @param rowType the type of the row
     */
    void beginRow(char treeType, char rowType);

    /**
     * Notifies about a new tree of the given type
     */
    void beginTree(char type);

    /**
     * Ends of the row.
     *
     * @param treeType TODO
     * @param rowType the type of the row
     */
    void endRow(char treeType, char rowType);

    /**
     * Ends of the tree of the given type.
     */
    void endTree(char type);
}

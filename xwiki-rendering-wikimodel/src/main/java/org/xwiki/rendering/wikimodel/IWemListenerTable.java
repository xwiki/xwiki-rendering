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
 * This interface re-groups all methods used to notify about tables and their
 * structural elements.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemListenerTable
{
    /**
     * This method notifies about the beginning of a new table in the document.
     *
     * @param params table parameters
     * @see #endTable(WikiParameters)
     */
    void beginTable(WikiParameters params);

    /**
     * This method is used to notify about the beginning of a new table cell.
     *
     * @param tableHead if this flag is <code>true</code> then the reported cell
     * corresponds to the table head ("th" element); otherwise it should
     * be considered as a normal table cell ("td" element).
     * @param params parameters of this cell
     * @see #endTableCell(boolean, WikiParameters)
     */
    void beginTableCell(boolean tableHead, WikiParameters params);

    /**
     * This method is used to notify about the beginning of a new table row.
     *
     * @param params parameters of the row.
     * @see #endTableRow(WikiParameters)
     */
    void beginTableRow(WikiParameters params);

    /**
     * This method notifies about the end of a table in the document.
     *
     * @param params table parameters
     * @see #beginTable(WikiParameters)
     */
    void endTable(WikiParameters params);

    /**
     * This method is used to notify about the end of a table cell.
     *
     * @param tableHead if this flag is <code>true</code> then the reported cell
     * corresponds to the table head ("th" element); otherwise it should
     * be considered as a normal table cell ("td" element).
     * @param params parameters of this cell
     * @see #beginTableCell(boolean, WikiParameters)
     */
    void endTableCell(boolean tableHead, WikiParameters params);

    /**
     * This method is used to notify about the end of a table row.
     *
     * @param params parameters of the row.
     * @see #beginTableRow(WikiParameters)
     */
    void endTableRow(WikiParameters params);

    /**
     * Notifies the table caption.
     *
     * @param str the content of the table caption
     */
    void onTableCaption(String str);
}

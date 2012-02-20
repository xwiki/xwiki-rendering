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
 * This listener re-groups methods used to notify about simple block elements.
 * Each simple block element (like paragraphs) can not have other block elements
 * or documents. Some of them can contain only formatted in-line elements.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemListenerSimpleBlocks
{
    /**
     * This method is used to notify about the beginning of a "typed" paragraph.
     * Some wiki syntaxes have special markups to define simple block elements
     * corresponding to "warnings", "info blocks" and so on. Blocks of this type
     * can have formatted inline elements.
     *
     * @param infoType the "type" of the special block
     * @param params parameters of this block
     * @see #endInfoBlock(String, WikiParameters)
     */
    void beginInfoBlock(String infoType, WikiParameters params);

    /**
     * Begin of a simple paragraph. Paragraphs can contain only formatted inline
     * elements.
     *
     * @param params paragraph parameters
     * @see #endParagraph(WikiParameters)
     */
    void beginParagraph(WikiParameters params);

    /**
     * This method is used to notify about the end of a "typed" paragraph.
     *
     * @param infoType the "type" of the special block
     * @param params parameters of this block
     * @see #beginInfoBlock(String, WikiParameters)
     */
    void endInfoBlock(String infoType, WikiParameters params);

    /**
     * End of a simple paragraph.
     *
     * @param params paragraph parameters
     * @see #beginParagraph(WikiParameters)
     */
    void endParagraph(WikiParameters params);

    /**
     * This method is used to notify about a sequence of empty lines. This event
     * can be interpreted as an "empty paragraph".
     *
     * @param count the number of empty lines found in the text
     */
    void onEmptyLines(int count);

    /**
     * This method notifies horizontal lines defined in the text.
     *
     * @param params parameters of the horizontal line
     */
    void onHorizontalLine(WikiParameters params);

    /**
     * This method notifies about a verbatim (pre-formatted) block defined in
     * the text
     *
     * @param str the content of the verbatim (pre-formatted) block
     * @param params parameters of the verbatim block
     */
    void onVerbatimBlock(String str, WikiParameters params);
}

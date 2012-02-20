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
 * This listener is used to notify about semantic elements defined in the
 * document. Possible semantic elements are inline and block properties. Each
 * block property can have a document or a simple paragraph as its value. Each
 * inline element contains only formatted inline elements.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface IWemListenerSemantic
{
    /**
     * This method is called to notify about the beginning of a new property
     * found in the parsed document.
     *
     * @param propertyUri the URI of the semantic block property found in the
     * document
     * @param doc this flag is <code>true</code> if the found property contains
     * a whole document; if this flag is <code>false</code> then expected
     * property value contains a paragraph
     */
    void beginPropertyBlock(String propertyUri, boolean doc);

    /**
     * This method is called to notify about the beginning of a new inline
     * property found in the text of the parsed document.
     *
     * @param propertyUri the URI of the semantic inline property found in the
     * document
     */
    void beginPropertyInline(String propertyUri);

    /**
     * This method is called to notify about the end of a block property found
     * in the parsed document.
     *
     * @param propertyUri the URI of the semantic block property found in the
     * document
     * @param doc this flag is <code>true</code> if the found property contains
     * a whole document; otherwise (if this flag is <code>false</code>)
     * the value of the property is a simple paragraph
     */
    void endPropertyBlock(String propertyUri, boolean doc);

    /**
     * This method is called to notify about the end of an inline property found
     * in the text of the parsed document.
     *
     * @param propertyUri the URI of the semantic inline property found in the
     * document
     */
    void endPropertyInline(String propertyUri);
}

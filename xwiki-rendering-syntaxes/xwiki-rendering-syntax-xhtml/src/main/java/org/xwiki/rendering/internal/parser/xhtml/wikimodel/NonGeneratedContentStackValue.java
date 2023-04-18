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

/**
 * Stores a {@link XWikiWikiModelHandler#NON_GENERATED_CONTENT_STACK} value, stores if the current element is
 * non-generated as well as if it is a meta-data element.
 *
 * @version $Id$
 * @since 14.10.9
 * @since 15.3
 */
public class NonGeneratedContentStackValue
{
    /**
     * {@code true} when the element is non-generator, {@code false} otherwise.
     */
    public final boolean nonGeneratedContent;

    /**
     * {@code true} when the element is a meta-data element, {@code false} otherwise.
     */
    public final boolean isMetaDataElement;

    /**
     * Default and only constructor.
     *
     * @param nonGeneratedContent {@code true} when the element is non-generator, {@code false} otherwise
     * @param isMetaDataElement {@code true} when the element is a meta-data element, {@code false} otherwise
     */
    public NonGeneratedContentStackValue(boolean nonGeneratedContent, boolean isMetaDataElement)
    {
        this.nonGeneratedContent = nonGeneratedContent;
        this.isMetaDataElement = isMetaDataElement;
    }
}

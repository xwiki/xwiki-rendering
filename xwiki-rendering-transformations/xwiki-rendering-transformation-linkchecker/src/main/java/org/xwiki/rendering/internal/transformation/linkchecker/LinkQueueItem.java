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
package org.xwiki.rendering.internal.transformation.linkchecker;

/**
 * Represents a Link placed on the queue for checking.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class LinkQueueItem
{
    /**
     * @see #getLinkReference() 
     */
    private String linkReference;

    /**
     * @see #getContentReference() 
     */
    private String contentReference;

    /**
     * @param linkReference see {@link #getLinkReference()}
     * @param contentReference see {@link #getContentReference()}
     */
    public LinkQueueItem(String linkReference, String contentReference)
    {
        this.linkReference = linkReference;
        this.contentReference = contentReference;
    }

    /**
     * @return the reference to the link that needs to be checked
     */
    public String getLinkReference()
    {
        return this.linkReference;
    }

    /**
     * @return the reference to the content, ie where the content was taken from (eg a page reference)
     */
    public String getContentReference()
    {
        return this.contentReference;
    }
}

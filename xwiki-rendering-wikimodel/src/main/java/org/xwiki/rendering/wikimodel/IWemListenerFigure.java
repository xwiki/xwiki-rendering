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
 * This listener is used to notify about figures and their captions.
 *
 * @version $Id$
 * @since 14.1RC1
 */
public interface IWemListenerFigure
{
    /**
     * Denotes the beginning of a figure, a container for a table or an image that optionally has a caption.
     *
     * @param params Parameters for the figure
     */
    default void beginFigure(WikiParameters params)
    {
        // Ignore by default.
    }

    /**
     * Denotes the end of a figure, a container for a table or an image that optionally has a caption.
     *
     * @param params Parameters for the figure
     */
    default void endFigure(WikiParameters params)
    {
        // Ignore by default.
    }

    /**
     * Denotes the beginning of a figure caption. Must be inside a figure.
     *
     * @param params Parameters for the figure caption
     */
    default void beginFigureCaption(WikiParameters params)
    {
        // Ignore by default.
    }

    /**
     * Denotes the beginning of a figure caption. Must be inside a figure.
     *
     * @param params Parameters for the figure caption
     */
    default void endFigureCaption(WikiParameters params)
    {
        // Ignore by default.
    }
}

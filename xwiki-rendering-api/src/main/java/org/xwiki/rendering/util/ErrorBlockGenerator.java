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
package org.xwiki.rendering.util;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;

/**
 * Generates error blocks to render an error in a wiki page.
 *
 * @version $Id$
 * @since 8.1M1
 */
@Role
public interface ErrorBlockGenerator
{
    /**
     * The CSS class attribute name to use for the generated top level error block.
     */
    String CLASS_ATTRIBUTE_NAME = "class";

    /**
     * The CSS class attribute value to use for the block displaying the message.
     */
    String CLASS_ATTRIBUTE_MESSAGE_VALUE = "xwikirenderingerror";

    /**
     * The CSS class attribute value to use for the block displaying the description.
     */
    String CLASS_ATTRIBUTE_DESCRIPTION_VALUE = "xwikirenderingerrordescription hidden";

    /**
     * Generates error blocks to render an error in a wiki page.
     *
     * @param message the short message that will be displayed to the user
     * @param description the description that will be displayed to the user when he clicks on the message
     * @param isInline whether the generated blocks should be inline or not
     * @return the generated blocks
     */
    List<Block> generateErrorBlocks(String message, String description, boolean isInline);

    /**
     * Generates error blocks to render an error in a wiki page.
     *
     * @param messagePrefix the prefix of the short message that will be displayed to the user. This message will be
     *                      augmented with the root cause of the error extracted from the passed throwable and an
     *                      additional text inviting the user to click the message will be added to the message prefix
     * @param throwable the exception from which the description will be extracted from
     * @param isInline whether the generated blocks should be inline or not
     * @return the generated blocks
     */
    List<Block> generateErrorBlocks(String messagePrefix, Throwable throwable, boolean isInline);
}

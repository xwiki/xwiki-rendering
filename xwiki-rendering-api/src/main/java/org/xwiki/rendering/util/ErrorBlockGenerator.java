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

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.xwiki.component.annotation.Role;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogUtils;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.stability.Unstable;

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
     * @param block the block in which to search for rendering errors
     * @return true if the passed block contains a rendering error, false otherwise
     * @since 14.0RC1
     */
    @Unstable
    default boolean containsError(Block block)
    {
        return false;
    }

    /**
     * Generates error blocks to render an error in a wiki page.
     *
     * @param message the short message that will be displayed to the user
     * @param description the description that will be displayed to the user when he clicks on the message
     * @param isInline whether the generated blocks should be inline or not
     * @return the generated blocks
     * @deprecated since 14.0RC1, use {@link #generateErrorBlocks(boolean, String, String, String, Object...)} instead
     */
    @Deprecated
    List<Block> generateErrorBlocks(String message, String description, boolean isInline);

    /**
     * Generates error blocks to render an error in a wiki page.
     * <p>
     * It's generally recommended to also provide a translation key to let the {@link ErrorBlockGenerator}
     * implementation search for a localized version of the error message depending on the context locale.
     *
     * @param messagePrefix the prefix of the short message that will be displayed to the user. This message will be
     *            augmented with the root cause of the error extracted from the passed throwable and an additional text
     *            inviting the user to click the message will be added to the message prefix
     * @param throwable the exception from which the description will be extracted from
     * @param isInline whether the generated blocks should be inline or not
     * @return the generated blocks
     * @deprecated since 14.0RC1, use {@link #generateErrorBlocks(boolean, String, String, String, Object...)} instead
     */
    @Deprecated
    List<Block> generateErrorBlocks(String messagePrefix, Throwable throwable, boolean isInline);

    /**
     * Generates error blocks to render an error in a wiki page.
     * <p>
     * If a {@link TranslationMarker} is provided, this message and description can be translated based on the current
     * context depending on the implementation of this component.
     * 
     * @param inline whether the generated blocks should be inline or not
     * @param messageId an identifier associated to the message. It's generally used, among other things, to find a
     *            translation for the message and the description in implementation which supports it.
     * @param defaultMessage the default message following SLF4J's {@link Logger} syntax
     * @param defaultDescription the default description following SLF4J's {@link Logger} syntax
     * @param arguments a list arguments to insert in the message and the description and/or a {@link Throwable}
     * @return the generated blocks. Return a {@link XDOM} if {@code inline} is false.
     * @since 14.0RC1
     */
    @Unstable
    default List<Block> generateErrorBlocks(boolean inline, String messageId, String defaultMessage,
        String defaultDescription, Object... arguments)
    {
        Marker marker = new TranslationMarker(messageId);
        LogEvent message = LogUtils.newLogEvent(marker, LogLevel.ERROR, defaultMessage, arguments);

        if (message.getThrowable() != null) {
            return generateErrorBlocks(message.getFormattedMessage(), message.getThrowable(), inline);
        } else {
            LogEvent description = LogUtils.newLogEvent(marker, LogLevel.ERROR, defaultDescription, arguments);

            return generateErrorBlocks(message.getFormattedMessage(), description.getFormattedMessage(), inline);
        }
    }
}

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
package org.xwiki.rendering.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LogUtils;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.OrBlockMatcher;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.util.ErrorBlockGenerator;

/**
 * Default implementation to generate error blocks to render an error in a wiki page.
 *
 * @version $Id$
 * @since 8.1M1
 */
@Component
@Singleton
public class DefaultErrorBlockGenerator implements ErrorBlockGenerator
{
    @Inject
    protected Logger logger;

    @Override
    public List<Block> generateErrorBlocks(boolean inline, String messageId, String defaultMessage,
        String defaultDescription, Object... arguments)
    {
        LogEvent message = LogUtils.newLogEvent(messageId != null ? new TranslationMarker(messageId) : null,
            LogLevel.ERROR,
            defaultMessage != null && !defaultMessage.endsWith(".") ? defaultMessage + '.' : defaultMessage, arguments);
        LogEvent description = defaultDescription != null
            ? LogUtils.newLogEvent(messageId != null ? new TranslationMarker(messageId + ".description") : null,
                LogLevel.ERROR, defaultDescription, arguments)
            : null;

        return generateErrorBlocks(inline, message, description);
    }

    protected List<Block> generateErrorBlocks(boolean inline, LogEvent message, LogEvent description)
    {
        List<Block> errorBlocks = new ArrayList<>();

        Map<String, String> errorBlockParams =
            Collections.singletonMap(CLASS_ATTRIBUTE_NAME, CLASS_ATTRIBUTE_MESSAGE_VALUE);
        Map<String, String> errorDescriptionBlockParams =
            Collections.singletonMap(CLASS_ATTRIBUTE_NAME, CLASS_ATTRIBUTE_DESCRIPTION_VALUE);

        StringBuilder messageBuilder = new StringBuilder();

        if (StringUtils.isNotEmpty(message.getMessage())) {
            messageBuilder.append(message.getFormattedMessage());
        }

        List<Block> descriptionChildren = new ArrayList<>();

        // Description
        addDescriptionBlock(inline, description, descriptionChildren);

        // Stack trace
        addStackTraceBlock(inline, message, messageBuilder, descriptionChildren);

        if (!descriptionChildren.isEmpty()) {
            messageBuilder.append(" Click on this message for details.");
        }

        if (inline) {
            errorBlocks.add(new FormatBlock(Arrays.asList(new WordBlock(messageBuilder.toString())), Format.NONE,
                errorBlockParams));
            if (!descriptionChildren.isEmpty()) {
                errorBlocks.add(new FormatBlock(descriptionChildren, Format.NONE, errorDescriptionBlockParams));
            }
        } else {
            errorBlocks.add(new GroupBlock(Arrays.asList(new WordBlock(messageBuilder.toString())), errorBlockParams));
            if (!descriptionChildren.isEmpty()) {
                errorBlocks.add(new GroupBlock(descriptionChildren, errorDescriptionBlockParams));
            }
        }

        return errorBlocks;
    }

    private void addDescriptionBlock(boolean inline, LogEvent description, List<Block> descriptionChildren)
    {
        if (description != null) {
            descriptionChildren.add(new VerbatimBlock(description.getFormattedMessage(), inline));
        }
    }

    private void addStackTraceBlock(boolean inline, LogEvent message, StringBuilder messageBuilder,
        List<Block> descriptionChildren)
    {
        if (message.getThrowable() != null) {
            // Note: We're using ExceptionUtils.getRootCause(e).getMessage() instead of getRootCauseMessage()
            // below because getRootCauseMessage() adds a technical prefix (the name of the exception), that
            // we don't want to display to our users.
            Throwable rootCause = ExceptionUtils.getRootCause(message.getThrowable());
            if (rootCause == null) {
                // If there's no nested exception, fall back to the throwable itself for getting the cause
                rootCause = message.getThrowable();
            }

            descriptionChildren.add(new VerbatimBlock(ExceptionUtils.getStackTrace(message.getThrowable()), inline));

            // Also add more details to the message
            messageBuilder.append(" Cause: [");
            messageBuilder.append(rootCause.getMessage());
            messageBuilder.append("].");
        }
    }

    @Override
    public List<Block> generateErrorBlocks(String message, String description, boolean isInline)
    {
        return generateErrorBlocks(isInline, null, message, description, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public List<Block> generateErrorBlocks(String messagePrefix, Throwable throwable, boolean isInline)
    {
        return generateErrorBlocks(isInline, null, messagePrefix, null, throwable);
    }

    @Override
    public boolean containsError(Block parent)
    {
        boolean foundError = false;
        List<Block> groupAndFormatBlocks = parent.getBlocks(
            new OrBlockMatcher(new ClassBlockMatcher(GroupBlock.class), new ClassBlockMatcher(FormatBlock.class)),
            Block.Axes.DESCENDANT);
        for (Block block : groupAndFormatBlocks) {
            String classParameter = block.getParameters().get(ErrorBlockGenerator.CLASS_ATTRIBUTE_NAME);
            if (classParameter != null && classParameter.contains(ErrorBlockGenerator.CLASS_ATTRIBUTE_MESSAGE_VALUE)) {
                foundError = true;
                break;
            }
        }
        return foundError;
    }
}

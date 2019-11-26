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
package org.xwiki.rendering.internal.block;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Construct a BlockMatcher from a String to a Syntax object and the other way around.
 *
 * @version $Id$
 * @since 6.1RC1
 */
@Component
@Singleton
public class BlockMatcherConverter extends AbstractConverter<BlockMatcher>
{

    @Override
    protected BlockMatcher convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }
        String matcherName = value.toString().trim();

        BlockMatcher matcher = null;
        if (matcherName.startsWith("class:")) {
            String blockClassName = matcherName.substring(6);
            if (blockClassName.indexOf('.') == -1) {
                blockClassName = "org.xwiki.rendering.block." + blockClassName;
            }
            try {
                Class<?> blockClass = Class.forName(blockClassName);
                if (Block.class.isAssignableFrom(blockClass)) {
                    matcher = new ClassBlockMatcher((Class<Block>) blockClass);
                }
            } catch (ClassNotFoundException c) {
                // keep matcher as null and throw new exception later on
            }
        }

        // still having null here means the matcher is not found, return an error
        if (matcher == null) {
            throw new ConversionException(String.format("Unknown BlockMatcher [%s]", matcherName));
        }
        return matcher;
    }

    @Override
    protected String convertToString(BlockMatcher value)
    {
        throw new ConversionException("not implemented yet");
    }
}

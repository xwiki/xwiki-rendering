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
package org.xwiki.rendering.block.match;

import java.util.Optional;
import java.util.function.Function;

import org.xwiki.rendering.block.Block;

/**
 * Search for a specific value in a {@link Block}.
 *
 * @param <T> the type of searched value
 * @version $Id$
 * @since 15.9RC1
 */
public class FunctionBlockMatcher<T> implements BlockMatcher
{
    private final Function<Block, Optional<T>> function;

    private Optional<T> value = Optional.empty();

    /**
     * @param function the function in charger of searching of the value in the {@link Block}
     */
    public FunctionBlockMatcher(Function<Block, Optional<T>> function)
    {
        this.function = function;
    }

    @Override
    public boolean match(Block block)
    {
        Optional<T> result = this.function.apply(block);

        if (result.isEmpty()) {
            return false;
        }

        // Remember the found value
        this.value = result;

        return true;
    }

    /**
     * @return the found value
     */
    public Optional<T> getValue()
    {
        return value;
    }
}

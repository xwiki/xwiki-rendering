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
package org.xwiki.rendering.internal.parser.uniast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.xwiki.rendering.listener.Format;

/**
 * Used to convert the flat UniAst text formatting to the nexted XDOM text formatting.
 *
 * @param sharedFormats the formats that are shared with the parent format context
 * @param sharedParameters the parameters that are shared with the parent format context
 * @param ownFormats the formats that are specific to this format context
 * @param ownParameters the parameters that are specific to this format context
 * @version $Id$
 * @since 18.1.0RC1
 */
public record FormatContext(List<Format> sharedFormats, Map<String, String> sharedParameters, List<Format> ownFormats,
    Map<String, String> ownParameters)
{
    /**
     * Attempts to create a nested format context if the given formats and parameters extend the current format context.
     *
     * @param newFormats new formats
     * @param newParameters new parameters
     * @return the current format context if there is no change, a nested format context if the given formats and
     *         parameters extend the current format context, or {@code null} if the given formats and parameters are
     *         incompatible with the current format context
     */
    public FormatContext maybeNest(List<Format> newFormats, Map<String, String> newParameters)
    {
        if (newFormats.containsAll(this.sharedFormats) && contains(newParameters, this.sharedParameters)) {
            if (newFormats.equals(this.sharedFormats) && newParameters.equals(this.sharedParameters)) {
                // Continue in the same format context.
                return this;
            } else {
                // Open a nested format context.
                return nest(newFormats, newParameters);
            }
        }
        // Can't continue in the same format context.
        return null;
    }

    private static boolean contains(Map<String, String> alice, Map<String, String> bob)
    {
        return bob.entrySet().stream().allMatch(
            entry -> alice.containsKey(entry.getKey()) && Objects.equals(alice.get(entry.getKey()), entry.getValue()));
    }

    private FormatContext nest(List<Format> newFormats, Map<String, String> newParameters)
    {
        List<Format> myOwnFormats = newFormats.stream().filter(format -> !this.sharedFormats.contains(format)).toList();
        Map<String, String> myOwnParameters = newParameters.entrySet().stream()
            .filter(entry -> !Objects.equals(this.sharedParameters.get(entry.getKey()), entry.getValue())).collect(
                LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);
        return new FormatContext(newFormats, newParameters, myOwnFormats, myOwnParameters);
    }

    /**
     * @return the actual list of formats for which we need to send begin/end events
     */
    public List<Format> actualOwnFormats()
    {
        if (this.ownFormats.isEmpty() && !this.ownParameters.isEmpty()) {
            return List.of(Format.NONE);
        } else {
            return this.ownFormats;
        }
    }
}

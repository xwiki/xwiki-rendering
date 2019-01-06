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
package org.xwiki.rendering.xdomxml10.internal.parser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.xdomxml10.internal.renderer.parameter.HeaderLevelConverter;

@Component
@Named("header")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class HeaderBlockParser extends DefaultBlockParser
{
    private static final HeaderLevelConverter HEADERLEVELCONVERTER = new HeaderLevelConverter();

    private static final Set<String> NAMES = Stream.of("level", "id").collect(Collectors.toSet());

    public HeaderBlockParser()
    {
        super(NAMES);
    }

    @Override
    protected void beginBlock()
    {
        getListener().beginHeader(HEADERLEVELCONVERTER.toFormat(getParameterAsString("level", null)),
            getParameterAsString("id", null), getCustomParameters());
    }

    @Override
    protected void endBlock()
    {
        getListener().endHeader(HEADERLEVELCONVERTER.toFormat(getParameterAsString("level", null)),
            getParameterAsString("id", null), getCustomParameters());
    }
}

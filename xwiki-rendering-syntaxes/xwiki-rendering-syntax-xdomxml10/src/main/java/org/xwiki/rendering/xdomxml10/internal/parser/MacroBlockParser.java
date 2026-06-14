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

/**
 * Parses a macro block.
 *
 * @version $Id$
 */
@Component
@Named("macro")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MacroBlockParser extends DefaultBlockParser
{
    private static final String ID = "id";

    private static final String CONTENT = "content";

    private static final String INLINE = "inline";

    private static final Set<String> NAMES = Stream.of(ID, CONTENT, INLINE).collect(Collectors.toSet());

    /**
     * Default constructor.
     */
    public MacroBlockParser()
    {
        super(NAMES);
    }

    @Override
    protected void endBlock()
    {
        getListener().onMacro(getParameterAsString(ID, "macro"), getCustomParameters(),
            getParameterAsString(CONTENT, null), getParameterAsBoolean(INLINE, false));
    }
}

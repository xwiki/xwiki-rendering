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
package org.xwiki.rendering.internal.macro.toc;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link DefaultTocEntriesResolver}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultTocEntriesResolverTest
{
    @InjectMockComponents
    private DefaultTocEntriesResolver resolver;

    public static Stream<Arguments> getBlocksSource()
    {
        return Stream.of(
            Arguments.of(
                new GroupBlock(List.of()),
                List.of()
            ),
            Arguments.of(
                new GroupBlock(List.of(new ParagraphBlock(List.of(new HeaderBlock(List.of(), HeaderLevel.LEVEL1))))),
                List.of(new HeaderBlock(List.of(), HeaderLevel.LEVEL1))
            )
        );
    }
    
    @ParameterizedTest
    @MethodSource("getBlocksSource")
    void getBlocks(GroupBlock rootBlock, List<HeaderBlock> expected)
    {
        assertEquals(expected, this.resolver.getBlocks(rootBlock));
    }
}

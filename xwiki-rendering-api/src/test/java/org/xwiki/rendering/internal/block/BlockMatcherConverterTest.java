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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Unit tests for {@link org.xwiki.rendering.internal.block.BlockMatcherConverter}.
 *
 * @version $Id$
 * @since 6.1RC1
 */
public class BlockMatcherConverterTest
{
    private BlockMatcherConverter converter  = new BlockMatcherConverter();
    
    @Test
    public void testConvertFromString()
    {
        BlockMatcher wordMatcher = converter.convert(BlockMatcher.class, "class:org.xwiki.rendering.block.WordBlock");
        Assert.assertTrue(wordMatcher instanceof ClassBlockMatcher);
        // any better check that this matcher 
        Assert.assertTrue(wordMatcher.match(new WordBlock("test")));
        Assert.assertFalse(wordMatcher.match(new SpaceBlock()));
        
        BlockMatcher wordMatcher2 = converter.convert(BlockMatcher.class, "class:WordBlock");
        Assert.assertTrue(wordMatcher2 instanceof ClassBlockMatcher);
    }

    @Test
    public void testConvertFromStringFailures()
    {
        assertConversionException("class:does.not.exist.FantasyBlock");
        assertConversionException("class:java.lang.String");
        assertConversionException("class:FantasyBlock");
        assertConversionException("macro:box"); // at least yet
    }
    
    private void assertConversionException(String input)
    {
        try {
            converter.convert(BlockMatcher.class, input);
            Assert.fail("should not parse " + input);
        } catch(ConversionException e) {
            // expected
        }
    }
}

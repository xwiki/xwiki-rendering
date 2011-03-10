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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Validate {@link IdGenerator}.
 * 
 * @version $Id$
 */
public class IdGeneratorTest
{
    private IdGenerator idGenerator;
    
    @Before
    public void setUp() throws Exception
    {
        this.idGenerator = new IdGenerator();
    }

    @Test
    public void testGenerateUniqueId()
    {
        Assert.assertEquals("Itext", this.idGenerator.generateUniqueId("text"));
        Assert.assertEquals("Itext-1", this.idGenerator.generateUniqueId("te xt"));
    }

    @Test
    public void testGenerateUniqueIdWithPrefix()
    {
        Assert.assertEquals("prefixtext", this.idGenerator.generateUniqueId("prefix", "text"));
        Assert.assertEquals("prefixtext-1", this.idGenerator.generateUniqueId("prefix", "te xt"));
    }

    @Test
    public void testGenerateUniqueIdFromNonAlphaNum()
    {
        Assert.assertEquals("I:_.-", this.idGenerator.generateUniqueId(":_.-"));
        Assert.assertEquals("Iwithspace", this.idGenerator.generateUniqueId("with space"));
        Assert.assertEquals("Iwithtab", this.idGenerator.generateUniqueId("with\ttab"));
        Assert.assertEquals("I5BC67801", this.idGenerator.generateUniqueId("\u5BC6\u7801"));
        Assert.assertEquals("I3D", this.idGenerator.generateUniqueId("="));
    }

    @Test
    public void testGenerateUniqueIdWhenInvalidEmptyPrefix()
    {
        try {
            this.idGenerator.generateUniqueId("", "whatever");
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("The prefix [] should only contain alphanumerical characters and not be empty.",
                expected.getMessage());
        }
    }

    @Test
    public void testGenerateUniqueIdWhenInvalidNonAlphaPrefix()
    {
        try {
            this.idGenerator.generateUniqueId("a-b", "whatever");
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("The prefix [a-b] should only contain alphanumerical characters and not be empty.",
                expected.getMessage());
        }
    }
}

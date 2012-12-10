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
package org.xwiki.rendering.internal.syntax;

import org.junit.Before;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.test.jmock.AbstractComponentTestCase;
import org.junit.Test;
import org.junit.Assert;

/**
 * Unit tests for {@link DefaultSyntaxFactory}.
 *
 * @version $Id$
 */
public class DefaultSyntaxFactoryTest extends AbstractComponentTestCase
{
    private SyntaxFactory syntaxFactory;

    @Before
    public void configure() throws Exception
    {
        this.syntaxFactory = getComponentManager().getInstance(SyntaxFactory.class);
    }

    @Test
    public void testCreateSyntaxFromSyntaxIdString() throws Exception
    {
        Syntax syntax = this.syntaxFactory.createSyntaxFromIdString("type/version");
        Assert.assertEquals("type", syntax.getType().getId());
        Assert.assertEquals("type", syntax.getType().getName());
        Assert.assertEquals("version", syntax.getVersion());
    }

    @Test
    public void testCreateSyntaxFromSyntaxIdStringWhenInvalid() throws Exception
    {
        try {
            this.syntaxFactory.createSyntaxFromIdString("invalid");
            Assert.fail("Should have thrown an exception");
        } catch (ParseException expected) {
            Assert.assertEquals("Invalid Syntax format [invalid]", expected.getMessage());
        }
    }

    @Test
    public void testCreateSyntaxFromSyntaxIdStringWhenNull() throws Exception
    {
        try {
            this.syntaxFactory.createSyntaxFromIdString(null);
            Assert.fail("Should have thrown an exception");
        } catch (ParseException expected) {
            Assert.assertEquals("The passed Syntax cannot be NULL", expected.getMessage());
        }
    }
}

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
package org.xwiki.rendering.test.cts;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.reflections.util.ClasspathHelper;

import junit.framework.Assert;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * Unit tests for {@link TestDataParser}.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class TestDataParserTest
{
    @Test
    public void findTestPrefixes()
    {
        TestDataParser parser = new TestDataParser();
        Set<String> prefixes =
            parser.findTestPrefixes("ctstest.type", ".*\\.xml", ClasspathHelper.forPackage("ctstest"));
        Assert.assertEquals(1, prefixes.size());
        Assert.assertEquals("ctstest/type/test/test1", prefixes.iterator().next());
    }

    @Test
    public void readTestData() throws Exception
    {
        TestDataParser parser = new TestDataParser();
        List<TestData> data =
            parser.parseTestData("syntax/1.0", "ctstest.type", ".*\\.xml", ClasspathHelper.forPackage("ctstest"));
        Assert.assertEquals(2, data.size());

        TestData dataIn = new TestData();
        dataIn.isSyntaxInputTest = true;
        dataIn.prefix = "ctstest/type/test/test1";
        dataIn.syntaxData = "in";
        dataIn.syntaxId = "syntax/1.0";
        dataIn.ctsData = "<cts/>";
        dataIn.configuration = new TestDataConfiguration();

        TestData dataOut = new TestData();
        dataOut.isSyntaxInputTest = false;
        dataOut.prefix = dataIn.prefix;
        dataOut.syntaxData = "out";
        dataOut.syntaxId = dataIn.syntaxId;
        dataOut.ctsData = dataIn.ctsData;
        dataOut.configuration = new TestDataConfiguration();

        assertThat(data, containsInAnyOrder(dataIn, dataOut));
    }
}

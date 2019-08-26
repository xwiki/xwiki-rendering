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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TestDataParser}.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class TestDataParserTest
{
    @Test
    public void findRelativeTestDirectoryNames()
    {
        TestDataParser parser = new TestDataParser();
        Set<String> prefixes = parser.findRelativeTestDirectoryNames("ctstest", "type", ".*\\.xml");
        assertEquals(1, prefixes.size());
        assertEquals("type/test/test1", prefixes.iterator().next());
    }

    @Test
    public void readTestData() throws Exception
    {
        TestDataParser parser = new TestDataParser();
        List<TestData> data = parser.parseTestData("syntax/1.0", "ctstest", "type", ".*\\.xml");
        assertEquals(4, data.size());

        TestDataConfiguration configuration = new TestDataConfiguration();
        Properties properties = new Properties();
        properties.setProperty("type/test/test1", "Description1");
        configuration.testDescriptions = properties;
        configuration.failingTests = Arrays.asList("failingregex");
        configuration.notApplicableTests = Arrays.asList("notapplicableregex");

        TestData test1 = new TestData();
        test1.isSyntaxInputTest = true;
        test1.prefix = "type/test/test1";
        test1.syntaxData = "in";
        test1.syntaxExtension = ".in.txt";
        test1.syntaxId = "syntax/1.0";
        test1.ctsData = "<cts/>";
        test1.ctsExtension = ".inout.xml";
        test1.configuration = configuration;

        TestData test2 = new TestData();
        test2.isSyntaxInputTest = true;
        test2.prefix = "type/test/test1";
        test2.syntaxData = "in1";
        test2.syntaxExtension = ".in.1.txt";
        test2.syntaxId = "syntax/1.0";
        test2.ctsData = "<cts/>";
        test2.ctsExtension = ".inout.xml";
        test2.configuration = configuration;

        TestData test3 = new TestData();
        test3.isSyntaxInputTest = true;
        test3.prefix = "type/test/test1";
        test3.syntaxData = "in2";
        test3.syntaxExtension = ".in.2.txt";
        test3.syntaxId = "syntax/1.0";
        test3.ctsData = "<cts/>";
        test3.ctsExtension = ".inout.xml";
        test3.configuration = configuration;

        TestData test4 = new TestData();
        test4.isSyntaxInputTest = false;
        test4.prefix = test1.prefix;
        test4.syntaxData = "out";
        test4.syntaxExtension = ".out.txt";
        test4.syntaxId = test1.syntaxId;
        test4.ctsData = test1.ctsData;
        test4.ctsExtension = test1.ctsExtension;
        test4.configuration = configuration;

        assertThat(data, containsInAnyOrder(test1, test2, test3, test4));
    }
}

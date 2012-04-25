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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Finds all test files in the current classloader, read them and return test data to represent them. See
 * {@link CompatibilityTestSuite} for a description of the algorithm.
 *
 * @version $Id$
 * @since 4.1M1
 * @see CompatibilityTestSuite
 */
public class TestDataGenerator
{
    /**
     * Read all test data.
     *
     * @param syntaxId the id of the syntax for which to generate data for
     * @param testPackage the name of a resource directory to look into for {@code *.txt} resources
     * @param pattern a regex to decide which {@code *.txt} resources should be found. The default should be to
     *        find them all
     * @return the list of test data, keyed by test prefix (eg {@code cts/simple/bold/bold1})
     * @throws IOException in case of error while reading test data
     */
    public Map<String, TestData> generateTestData(String syntaxId, String testPackage, String pattern)
        throws IOException
    {
        ClassLoader classLoader = getClass().getClassLoader();

        Map<String, TestData> data = new HashMap<String, TestData>();
        String syntaxDirectory = computeSyntaxDirectory(syntaxId);

        for (String testPrefix : findTestPrefixes(testPackage, pattern)) {
            data.put(testPrefix, generateSingleTestData(syntaxDirectory, testPrefix, classLoader));
        }
        return data;
    }

    public TestData generateSingleTestData(String syntaxDirectory, String testPrefix, ClassLoader classLoader)
        throws IOException
    {
        TestData testData = new TestData();

        // Look for CTS input/output file and read their content
        String ctsInOut = readData(testPrefix, ".inout.txt", classLoader);
        if (ctsInOut == null) {
            testData.ctsInput = readData(testPrefix, ".in.txt", classLoader);
            testData.ctsOutput = readData(testPrefix, ".out.txt", classLoader);
        } else {
            testData.ctsInput = ctsInOut;
            testData.ctsOutput = ctsInOut;
        }

        // Replace the "cts" prefix by the syntax id
        String testDirectory = testPrefix.replaceFirst("cts", syntaxDirectory);

        // Look for syntax-specific input/output file and read their content
        String syntaxInOut = readData(testDirectory, ".inout.txt", classLoader);
        if (syntaxInOut == null) {
            testData.syntaxInput = readData(testDirectory, ".in.txt", classLoader);
            testData.syntaxOutput = readData(testDirectory, ".out.txt", classLoader);
        } else {
            testData.syntaxInput = syntaxInOut;
            testData.syntaxOutput = syntaxInOut;
        }

        return testData;
    }

    private String readData(String testPrefix, String suffix, ClassLoader classLoader) throws IOException
    {
        String input = null;

        URL inputURL = classLoader.getResource(testPrefix + suffix);
        if (inputURL != null) {
            input = IOUtils.toString(inputURL);
        }
        return input;
    }

    /**
     * Find {@code *.txt} files in the classpath and return the list of all resources found, without their filename
     * extensions. For example if {@code syntax/simple/bold/bold1.*.txt} is found, return
     * {@code syntax/simple/bold/bold1}.
     *
     * @param testPackage the name of a resource directory to look into for {@code *.txt} resources
     * @param pattern a regex to decide which {@code *.txt} resources should be found. The default should be to find
     *        them all
     * @return the list of resources found
     */
    public Set<String> findTestPrefixes(String testPackage, String pattern)
    {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setScanners(new ResourcesScanner())
            .setUrls(ClasspathHelper.forPackage(""))
            .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(testPackage))));

        Set<String> prefixes = new TreeSet<String>();
        for (String testResourceName : reflections.getResources(Pattern.compile(pattern))) {
            // Remove the trailing extension
            prefixes.add(StringUtils.substringBeforeLast(testResourceName, ".inout.txt"));
        }

        return prefixes;
    }

    private String computeSyntaxDirectory(String syntaxId)
    {
        // Remove "/" and "."
        return syntaxId.replace("/", "").replace(".", "");
    }
}

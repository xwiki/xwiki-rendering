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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
public class TestDataParser
{
    /**
     * Represents the Java resource separator.
     */
    private static final String SLASH = "/";

    /**
     * Read all test data. See {@link CompatibilityTestSuite} for a detailed explanation of the algorithm.
     *
     * @param syntaxId the id of the syntax for which to parse data for
     * @param testPackage the name of a resource directory to look into for {@code *.xml} resources
     * @param pattern a regex to decide which {@code *.xml} resources should be found. The default should be to
     *        find them all
     * @return the list of test data
     * @throws Exception in case of error while reading test data
     */
    public List<TestData> parseTestData(String syntaxId, String testPackage, String pattern) throws Exception
    {
        ClassLoader classLoader = getClass().getClassLoader();

        List<TestData> data = new ArrayList<TestData>();
        String syntaxDirectory = computeSyntaxDirectory(syntaxId);

        // Read the suite-level Configuration data.
        TestDataConfiguration configuration = parseTestConfiguration(syntaxDirectory, classLoader);

        for (String testPrefix : findTestPrefixes(testPackage, pattern)) {
            for (TestData testData : parseSingleTestData(syntaxDirectory, testPrefix, classLoader)) {
                testData.syntaxId = syntaxId;
                testData.prefix = testPrefix;
                testData.configuration = configuration;
                data.add(testData);
            }
        }
        return data;
    }

    /**
     * Parse data for single test.
     *
     * @param syntaxDirectory the syntax directory from where to read syntax test data (eg "xwiki20" for "xwiki/2.0"
     *        syntax)
     * @param testPrefix the CTS test prefix (eg "cts/simple/bold/bold1")
     * @param classLoader the class loader from which the test data is read from
     * @return the 2 TestData instances for both input and output tests
     * @throws IOException in case of error while reading test data
     */
    public List<TestData> parseSingleTestData(String syntaxDirectory, String testPrefix, ClassLoader classLoader)
        throws IOException
    {
        // Look for CTS input/output file and read their contents
        Pair<String, String> ctsData = readDataForPrefix(testPrefix, "xml", classLoader);

        // Replace the first prefix by the syntax id
        String testDirectory = String.format("%s/%s", syntaxDirectory, StringUtils.substringAfter(testPrefix, SLASH));

        // Look for syntax-specific input/output file and read their content
        TestData testDataIN = new TestData();
        TestData testDataOUT = new TestData();

        Pair<String, String> syntaxData = readDataForPrefix(testDirectory, "txt", classLoader);

        testDataIN.isSyntaxInputTest = true;
        testDataIN.syntaxData = syntaxData.getLeft();
        testDataIN.ctsData = ctsData.getRight();
        testDataOUT.syntaxData = syntaxData.getRight();
        testDataOUT.ctsData = ctsData.getLeft();

        return Arrays.asList(testDataIN, testDataOUT);
    }

    /**
     * Parse Test configuration by looking for a {@code config.properties} file in the Syntax directory.
     *
     * @param syntaxDirectory the syntax directory under which to look for the configuration file
     * @param classLoader the class loader from which the test configuration is read from
     * @return the configuration
     * @throws Exception in case of error while reading test configuration
     */
    public TestDataConfiguration parseTestConfiguration(String syntaxDirectory, ClassLoader classLoader)
        throws Exception
    {
        TestDataConfiguration configuration = new TestDataConfiguration();

        URL configurationURL = classLoader.getResource(syntaxDirectory + "/config.properties");
        if (configurationURL != null) {
            PropertiesConfiguration properties = new PropertiesConfiguration(configurationURL);
            // TODO: Remove this unsafe cast, need to find out how to do that nicely...
            configuration.ignoredTests =
                (List<String>) (List<?>) properties.getList("ignoredTests", Collections.emptyList());
        }

        return configuration;
    }

    /**
     * Read both input and output test data.
     *
     * @param prefix the prefix where to look for to read the test data
     * @param fileExtension the test data file extension to look for
     * @param classLoader the class loader from which the test data is read from
     * @return the input and output test content
     * @throws IOException in case of error while reading test data
     */
    private Pair<String, String> readDataForPrefix(String prefix, String fileExtension, ClassLoader classLoader)
        throws IOException
    {
        String inOut = readData(prefix, ".inout." + fileExtension, classLoader);
        String in;
        String out;
        if (inOut == null) {
            in = readData(prefix, ".in." + fileExtension, classLoader);
            out = readData(prefix, ".out." + fileExtension, classLoader);
        } else {
            in = inOut;
            out = inOut;
        }

        return new ImmutablePair<String, String>(in, out);
    }

    /**
     *
     * @param prefix the prefix where to look for to read the test data
     * @param suffix the suffix including the test type to read (".in.", ".out." or ".inout.") + the file extension
     * @param classLoader the class loader from which the test data is read from
     * @return the test content or null if not found
     * @throws IOException in case of error while reading test data
     */
    private String readData(String prefix, String suffix, ClassLoader classLoader) throws IOException
    {
        String input = null;

        URL inputURL = classLoader.getResource(prefix + suffix);
        if (inputURL != null) {
            input = IOUtils.toString(inputURL);
        }
        return input;
    }

    /**
     * Find {@code *.xml} files in the classpath and return the list of all resources found, without their filename
     * extensions. For example if {@code syntax/simple/bold/bold1.*.xml} is found, return
     * {@code syntax/simple/bold/bold1}.
     *
     * @param testPackage the name of a resource directory to look into for {@code *.xml} resources
     * @param pattern a regex to decide which {@code *.xml} resources should be found. The default should be to find
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
            prefixes.add(StringUtils.substringBeforeLast(testResourceName, ".inout.xml"));
        }

        return prefixes;
    }

    /**
     * Normalize a syntax directory by replacing removing "/" and "." characters. For example "xwiki/2.0" becomes
     * "xwiki20".
     *
     * @param syntaxId the syntax id from which to compute a syntax directory
     * @return the computed syntax directory
     */
    private String computeSyntaxDirectory(String syntaxId)
    {
        // Remove "/" and "."
        return syntaxId.replace(SLASH, "").replace(".", "");
    }
}

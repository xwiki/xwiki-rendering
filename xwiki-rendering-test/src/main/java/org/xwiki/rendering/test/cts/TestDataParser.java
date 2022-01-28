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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
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
     * Represents the dot package separator.
     */
    private static final String DOT = ".";

    /**
     * Read all test data. See {@link CompatibilityTestSuite} for a detailed explanation of the algorithm.
     *
     * @param syntaxId the id of the syntax for which to parse data for
     * @param ctsRootPackageName the root of the CTS resources
     * @param packageFilter the regex to filter packages
     * @param pattern a regex to decide which {@code *.xml} resources should be found. The default should be to find
     *            them all
     * @return the list of test data
     * @throws Exception in case of error while reading test data
     */
    public List<TestData> parseTestData(String syntaxId, String ctsRootPackageName, String packageFilter,
        String pattern) throws Exception
    {
        ClassLoader classLoader = getClass().getClassLoader();

        List<TestData> data = new ArrayList<>();
        String syntaxDirectory = computeSyntaxDirectory(syntaxId);

        // Read the suite-level Configuration data.
        TestDataConfiguration configuration = parseTestConfiguration(syntaxDirectory, ctsRootPackageName, classLoader);

        Set<String> relativeDirectoryNames = findRelativeTestDirectoryNames(ctsRootPackageName, packageFilter, pattern);
        for (String relativeDirectoryName : relativeDirectoryNames) {
            List<TestData> testDatas = parseSingleTestData(syntaxDirectory, ctsRootPackageName, relativeDirectoryName,
                configuration, classLoader);
            for (TestData testData : testDatas) {
                testData.syntaxId = syntaxId;
                testData.prefix = relativeDirectoryName;
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
     *            syntax)
     * @param ctsRootPackageName the root of the CTS resources
     * @param relativeDirectoryName the name of the relative directory for a CTS test (eg "/simple/bold/bold1")
     * @param configuration the test configuration
     * @param classLoader the class loader from which the test data is read from
     * @return the TestData instances for both input and output tests, including possible input alias tests
     * @throws Exception in case of error while reading test data or inherited configurations
     */
    public List<TestData> parseSingleTestData(String syntaxDirectory, String ctsRootPackageName,
        String relativeDirectoryName, TestDataConfiguration configuration, ClassLoader classLoader) throws Exception
    {
        // Look for syntax-specific input/output file and read their content
        TestData testDataIN = new TestData();
        testDataIN.isSyntaxInputTest = true;
        TestData testDataOUT = new TestData();

        // Look for CTS input/output file and read their contents
        Pair<Pair<String, String>, Pair<String, String>> ctsData =
            readDataForPrefix(ctsRootPackageName + SLASH + relativeDirectoryName, "xml", classLoader);

        Pair<Pair<String, String>, Pair<String, String>> syntaxData = readDataForPrefix(
            syntaxDirectory + SLASH + relativeDirectoryName, configuration.fileExtension, classLoader);

        testDataIN.syntaxData = syntaxData.getLeft().getLeft();
        testDataIN.syntaxExtension = syntaxData.getLeft().getRight();
        testDataIN.ctsData = ctsData.getRight().getLeft();
        testDataIN.ctsExtension = ctsData.getRight().getRight();
        testDataOUT.syntaxData = syntaxData.getRight().getLeft();
        testDataOUT.syntaxExtension = syntaxData.getRight().getRight();
        testDataOUT.ctsData = ctsData.getLeft().getLeft();
        testDataOUT.ctsExtension = ctsData.getLeft().getRight();

        // Read possible "in" aliases
        List<TestData> testDataINAliases =
            parseAliasesTestData(syntaxDirectory, relativeDirectoryName, ctsData, configuration, classLoader);

        // If the inherit configuration property is set and if the returned syntax is empty load from the inherit
        // syntax.
        TestDataConfiguration currentConfiguration = configuration;
        while ((testDataIN.syntaxData == null || testDataOUT.syntaxData == null)
            && currentConfiguration.inheritSyntax != null) {
            final String inheritedSyntaxDirectory = computeSyntaxDirectory(currentConfiguration.inheritSyntax);

            Pair<Pair<String, String>, Pair<String, String>> inheritedSyntaxData =
                readDataForPrefix(inheritedSyntaxDirectory + SLASH + relativeDirectoryName,
                    configuration.fileExtension, classLoader);
            if (testDataIN.syntaxData == null) {
                testDataIN.syntaxData = inheritedSyntaxData.getLeft().getLeft();
                testDataIN.syntaxExtension = inheritedSyntaxData.getLeft().getRight();
            }
            if (testDataOUT.syntaxData == null) {
                testDataOUT.syntaxData = inheritedSyntaxData.getRight().getLeft();
                testDataOUT.syntaxExtension = inheritedSyntaxData.getRight().getRight();
            }

            currentConfiguration = parseTestConfiguration(inheritedSyntaxDirectory, ctsRootPackageName, classLoader);
        }

        List<TestData> result = new ArrayList<>();
        result.add(testDataIN);
        result.addAll(testDataINAliases);
        result.add(testDataOUT);
        return result;
    }

    /**
     * Parse Alias test data for inputs.
     *
     * @param syntaxDirectory the syntax directory from where to read syntax test data (eg "xwiki20" for "xwiki/2.0"
     *            syntax)
     * @param relativeDirectoryName the name of the relative directory for a CTS test (eg "/simple/bold/bold1")
     * @param ctsData the CTS data to use to construct the alias test data
     * @param configuration the test configuration
     * @param classLoader the class loader from which the test data is read from
     * @return the TestData instances for both input and output tests, including possible input alias tests
     * @throws IOException in case of error while reading test data
     */
    private List<TestData> parseAliasesTestData(String syntaxDirectory, String relativeDirectoryName,
        Pair<Pair<String, String>, Pair<String, String>> ctsData, TestDataConfiguration configuration,
        ClassLoader classLoader) throws IOException
    {
        List<TestData> testDataINAliases = new ArrayList<>();
        Pair<Pair<String, String>, Pair<String, String>> syntaxDataAlias;
        int i = 1;
        do {
            syntaxDataAlias = readDataForPrefix(syntaxDirectory + SLASH + relativeDirectoryName,
                i + DOT + configuration.fileExtension, classLoader);
            if (syntaxDataAlias.getLeft().getLeft() != null) {
                TestData testDataINAlias = new TestData();
                testDataINAlias.isSyntaxInputTest = true;
                testDataINAlias.syntaxData = syntaxDataAlias.getLeft().getLeft();
                testDataINAlias.syntaxExtension = syntaxDataAlias.getLeft().getRight();
                testDataINAlias.ctsData = ctsData.getRight().getLeft();
                testDataINAlias.ctsExtension = ctsData.getRight().getRight();
                testDataINAliases.add(testDataINAlias);
            }
            i++;
        } while (syntaxDataAlias.getLeft().getLeft() != null);
        return testDataINAliases;
    }

    /**
     * Parse Test configuration by looking for a {@code config.properties} file in the Syntax directory.
     *
     * @param syntaxDirectory the syntax directory under which to look for the configuration file
     * @param ctsRootPackageName the root of the CTS resources
     * @param classLoader the class loader from which the test configuration is read from
     * @return the configuration
     * @throws Exception in case of error while reading test configuration
     */
    public TestDataConfiguration parseTestConfiguration(String syntaxDirectory, String ctsRootPackageName,
        ClassLoader classLoader) throws Exception
    {
        TestDataConfiguration configuration = new TestDataConfiguration();

        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        addConfigurationData(compositeConfiguration, ctsRootPackageName, classLoader);
        addConfigurationData(compositeConfiguration, syntaxDirectory, classLoader);

        // TODO: Remove these unsafe casts, need to find out how to do that nicely...
        configuration.notApplicableTests =
            (List<String>) (List<?>) compositeConfiguration.getList("notApplicableTests", Collections.emptyList());
        configuration.failingTests =
            (List<String>) (List<?>) compositeConfiguration.getList("failingTests", Collections.emptyList());
        configuration.testDescriptions = compositeConfiguration.getProperties("testDescriptions", new Properties());
        configuration.inheritSyntax = compositeConfiguration.getString("inheritSyntax");
        configuration.fileExtension = compositeConfiguration.getString("fileExtension", "txt");

        return configuration;
    }

    /**
     * Add Configuration Data loaded from "config.properties" resources.
     *
     * @param configuration the composite configuration to add to
     * @param rootPackageName the package where the configuration properties file is located
     * @param classLoader the class loader from which the configuration is read from
     * @throws Exception in case of error while reading test configuration
     */
    private void addConfigurationData(CompositeConfiguration configuration, String rootPackageName,
        ClassLoader classLoader) throws Exception
    {
        URL configurationURL = classLoader.getResource(rootPackageName + "/config.properties");
        if (configurationURL != null) {
            configuration.addConfiguration(new Configurations().properties(configurationURL));
        }
    }

    /**
     * Read both input and output test data.
     *
     * @param prefix the prefix where to look for to read the test data
     * @param fileExtension the test data file extension to look for
     * @param classLoader the class loader from which the test data is read from
     * @return the input and output test content along with their extensions
     * @throws IOException in case of error while reading test data
     */
    private Pair<Pair<String, String>, Pair<String, String>> readDataForPrefix(String prefix, String fileExtension,
        ClassLoader classLoader) throws IOException
    {
        String in;
        String out;
        String inExtension = ".inout." + fileExtension;
        String outExtension = inExtension;
        String inOut = readData(prefix + inExtension, classLoader);
        if (inOut == null) {
            inExtension = ".in." + fileExtension;
            outExtension = ".out." + fileExtension;
            in = readData(prefix + inExtension, classLoader);
            out = readData(prefix + outExtension, classLoader);
        } else {
            in = inOut;
            out = inOut;
        }

        return new ImmutablePair<>(new ImmutablePair<>(in, inExtension), new ImmutablePair<>(out, outExtension));
    }

    /**
     * @param resourceName the resource to load
     * @param classLoader the class loader from which the test data is read from
     * @return the test content or null if not found
     * @throws IOException in case of error while reading test data
     */
    private String readData(String resourceName, ClassLoader classLoader) throws IOException
    {
        String input = null;

        URL inputURL = classLoader.getResource(resourceName);
        if (inputURL != null) {
            input = IOUtils.toString(inputURL);
        }
        return input;
    }

    /**
     * Find {@code *.xml} files in the classpath and return the list of all resources found, without their filename
     * extensions. For example if <code>{ctsDirectoryName}/simple/bold/bold1.*.xml</code> is found, return
     * {@code simple/bold/bold1}.
     *
     * @param ctsRootPackageName the root of the CTS resources
     * @param packageFilter the regex to filter packages
     * @param pattern a regex to decide which {@code *.xml} resources should be found. The default should be to find
     *            them all
     * @return the list of relative test directories found
     */
    public Set<String> findRelativeTestDirectoryNames(String ctsRootPackageName, String packageFilter, String pattern)
    {
        Reflections reflections = new Reflections(new ConfigurationBuilder().setScanners(Scanners.Resources)
            .setUrls(ClasspathHelper.forPackage(ctsRootPackageName))
            .filterInputsBy(new FilterBuilder().includePackage(ctsRootPackageName + DOT + packageFilter)));

        Set<String> prefixes = new TreeSet<>();
        for (String fullTestDirectoryName : reflections.getResources(Pattern.compile(pattern))) {
            // Remove the prefix and trailing extension
            String testDirectoryName = StringUtils.substringAfter(fullTestDirectoryName, ctsRootPackageName + SLASH);
            testDirectoryName = StringUtils.substringBeforeLast(testDirectoryName, ".inout.xml");
            prefixes.add(testDirectoryName);
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
        return syntaxId.replace(SLASH, "").replace(DOT, "");
    }
}

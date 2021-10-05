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
package org.xwiki.rendering.test.integration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Finds all test files in the current classloader, read them and return test data to represent them.
 *
 * @version $Id$
 * @since 3.0RC1
 */
public class TestDataGenerator
{
    private TestDataParser parser = new TestDataParser();

    public Collection<Object[]> generateData(String testPackage, String pattern)
    {
        Reflections reflections =
            new Reflections(new ConfigurationBuilder().setScanners(Scanners.Resources)
                .setUrls(ClasspathHelper.forPackage(""))
                .filterInputsBy(new FilterBuilder().includePattern(Pattern.quote(testPackage) + ".*")));

        Collection<Object[]> data = new ArrayList<>();
        for (String testResourceName : reflections.getResources(Pattern.compile(pattern))) {
            data.addAll(parseSingleResource(testResourceName));
        }

        return data;
    }

    /**
     * Parse a single test data file and return test data objects that represent the file data.
     *
     * @param testResourceName the name of the resource file containing the test data in the current classloader
     * @return the in-memory Objects representing the test data
     */
    private Collection<Object[]> parseSingleResource(String testResourceName)
    {
        String resourceName = "/" + testResourceName;
        TestData data;
        try {
            InputStream source = getClass().getResourceAsStream(resourceName);
            if (source == null) {
                throw new RuntimeException("Failed to find test file [" + resourceName + "]");
            }
            data = this.parser.parse(source, resourceName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read test data from [" + resourceName + "]", e);
        }

        boolean hasEventOutput = false;
        int inputCounter = 0;

        Collection<Object[]> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.inputs.entrySet()) {
            inputCounter++;
            for (String targetSyntaxId : data.expectations.keySet()) {

                String parserId = entry.getKey();
                String input = entry.getValue();

                if ("xhtml/1.0".equals(parserId) && !input.startsWith("<?xml") && !input.startsWith("<!DOCTYPE")) {
                    input = normalizeHTML(input);
                } else if ("docbook/4.4".equals(parserId) && !input.startsWith("<?xml")
                    && !input.startsWith("<!DOCTYPE")) {
                    input = normalizeDocBook(input);
                }

                // In order to improve test performance we exclude unneeded tests. A test is not required when the
                // following conditions are met:
                // - the target syntax is not event/1.0
                // - there's already another parser with an output of event/1.0
                // The reason these tests are not needed is because rendering is done from the XDOM and the event/1.0
                // syntax is an exact representation of the XDOM object and thus we only need to check once the
                // expected output (except for event/1.0).
                if (inputCounter < 2 || !hasEventOutput || "event/1.0".equals(targetSyntaxId)) {

                    Object[] singleResult = new Object[8];
                    singleResult[0] = computeTestName(testResourceName, parserId, targetSyntaxId);
                    singleResult[1] = input;

                    String expected = data.expectations.get(targetSyntaxId);
                    if ("docbook/4.4".equals(targetSyntaxId) && !expected.startsWith("<?xml")
                        && !expected.startsWith("<!DOCTYPE")) {
                        expected = normalizeDocBook(expected);
                    }

                    singleResult[2] = expected;
                    singleResult[3] = parserId;
                    singleResult[4] = targetSyntaxId;
                    singleResult[5] = data.streaming;
                    singleResult[6] = data.transformations;
                    singleResult[7] = data.configuration;

                    result.add(singleResult);

                    if ("event/1.0".equals(targetSyntaxId)) {
                        hasEventOutput = true;
                    }
                }
            }
        }

        return result;
    }

    private String normalizeHTML(String content)
    {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">" + "<html>" + content + "</html>";
    }

    private String normalizeDocBook(String content)
    {
        return "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE article PUBLIC \"-//OASIS//DTD Simplified DocBook XML V1.1//EN\" "
            + "\"http://www.oasis-open.org/docbook/xml/simple/1.1/sdocbook.dtd\">" + content;
    }

    private String computeTestName(String prefix, String parserId, String targetSyntaxId)
    {
        // Note: For some reason the Eclipse JUnit test runner strips the information found in parenthesis. Thus we use
        // square brackets instead.
        return prefix + " [" + parserId + ", " + targetSyntaxId + "]";
    }
}

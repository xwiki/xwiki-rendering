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
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * @version $Id$
 * @since 3.0RC1
 */
public class TestDataGenerator
{
    private TestDataParser parser = new TestDataParser();

    public Collection<Object[]> generateData()
    {
        return generateData("");
    }

    public Collection<Object[]> generateData(String testPackage)
    {
        return generateData(testPackage, ".*\\.test");
    }

    public Collection<Object[]> generateData(String testPackage, String pattern)
    {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setScanners(new ResourcesScanner())
            .setUrls(ClasspathHelper.getUrlsForPackagePrefix(""))
            .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(testPackage))));

        Collection<Object[]> data = new ArrayList<Object[]>();
        for (String testResourceName : reflections.getResources(Pattern.compile(pattern))) {
            data.addAll(parseSingleResource(testResourceName));
        }

        return data;
    }

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

        Collection<Object[]> result = new ArrayList<Object[]>();
        for (Map.Entry<String, String> entry : data.inputs.entrySet()) {
            for (String targetSyntaxId : data.expectations.keySet()) {
                String parserId = entry.getKey();
                String input = entry.getValue();

                if ("xhtml/1.0".equals(parserId) && !input.startsWith("<?xml") && !input.startsWith("<!DOCTYPE")) {
                    input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                        + "<html>" + input + "</html>";
                }

                Object[] singleResult = new Object[8];
                singleResult[0] = computeTestName(testResourceName, parserId, targetSyntaxId);
                singleResult[1] = input;
                singleResult[2] = data.expectations.get(targetSyntaxId);
                singleResult[3] = parserId;
                singleResult[4] = targetSyntaxId;
                singleResult[5] = data.streaming;
                singleResult[6] = data.runTransformations;
                singleResult[7] = data.configuration;
                result.add(singleResult);
            }
        }

        return result;
    }

    private String computeTestName(String prefix, String parserId, String targetSyntaxId)
    {
        // Note: For some reason the Eclipse JUnit test runner strips the information found in parenthesis. Thus we use
        // square brackets instead.
        return prefix + " [" + parserId + ", " + targetSyntaxId + "]";
    }
}

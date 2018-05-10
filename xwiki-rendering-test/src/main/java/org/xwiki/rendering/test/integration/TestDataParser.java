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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Parses test data defined using the following syntax, shown with this example:
 * {@code
 * .streaming
 * .runTransformations or .runTransformations:tx1,tx2,...txN
 * .configuration <key=value>
 * .input|<syntax parser id>
 * <input content here>
 * .expect|<renderer id>
 * <expected content here>
 * }
 * <p>
 * Note that there can be several {@code .input} and {@code .expect} entries. For each {@code .input} definition, all
 * the found {@code .expect} will be executed and checked.
 *
 * @version $Id$
 * @since 3.0RC1
 */
public class TestDataParser
{
    private final static String TRANSFORMATION_PREFIX = ".runTransformations";

    public TestData parse(InputStream source, String resourceName) throws IOException
    {
        TestData data = new TestData();

        // Resources should always be encoded as UTF-8, to reduce the dependency on the system encoding
        BufferedReader reader = new BufferedReader(new InputStreamReader(source, "UTF-8"));

        // Read each line and look for lines starting with ".". When this happens it means we've found a separate
        // test case.
        try {
            String action = null;
            String keyName = null;
            boolean skip = false;
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".")) {
                    if (line.startsWith(".#")) {
                        // Ignore comments
                    } else if (line.startsWith(".streaming")) {
                        data.streaming = true;
                    } else if (line.startsWith(TRANSFORMATION_PREFIX)) {
                        data.transformations = parseTransformationDirective(line);
                    } else if (line.startsWith(".configuration")) {
                        StringTokenizer st = new StringTokenizer(line.substring(".configuration".length() + 1), "=");
                        data.configuration.put(st.nextToken(), st.nextToken());
                    } else {
                        if (!skip) {
                            saveData(action, buffer, data, keyName);
                        }
                        buffer.setLength(0);
                        // Parse the directive line starting with "." and with "|" separators.
                        // For example ".input|xwiki/2.0|skip" or ".expect|xhtml"
                        StringTokenizer st = new StringTokenizer(line.substring(1), "|");
                        // First token is "input", "expect" or "inputexpect".
                        action = st.nextToken();
                        // Second token is either the input syntax id or the expectation renderer short name
                        keyName = st.nextToken();
                        // Third (optional) token is whether the test should be skipped (useful while waiting for
                        // a fix to wikimodel for example).
                        skip = false;
                        if (st.hasMoreTokens()) {
                            skip = true;
                            System.out.println("[WARNING] Skipping test for [" + keyName + "] in resource ["
                                + resourceName + "] since it has been marked as skipped in the test. This needs to be "
                                + "reviewed and fixed.");
                        }
                    }
                } else {
                    buffer.append(line).append('\n');
                }
            }

            if (!skip) {
                saveData(action, buffer, data, keyName);
            }

        } finally {
            reader.close();
        }

        return data;
    }

    private List<String> parseTransformationDirective(String line)
    {
        if (line.length() > TRANSFORMATION_PREFIX.length() + 1 && line.charAt(TRANSFORMATION_PREFIX.length()) == ':') {
            return Arrays.asList(StringUtils.split(line.substring(TRANSFORMATION_PREFIX.length() + 1), ","));
        } else {
            return Collections.emptyList();
        }
    }

    private void saveData(String action, StringBuffer buffer, TestData data, String keyName)
    {
        if (action != null) {
            if ("input".equalsIgnoreCase(action)) {
                saveBuffer(buffer, data.inputs, keyName);
            } else if ("expect".equalsIgnoreCase(action)) {
                saveBuffer(buffer, data.expectations, keyName);
            } else if ("inputexpect".equalsIgnoreCase(action)) {
                saveBuffer(buffer, data.inputs, keyName);
                saveBuffer(buffer, data.expectations, keyName);
            }
        }
    }

    private void saveBuffer(StringBuffer buffer, Map<String, String> map, String keyName)
    {
        // Remove the last newline since our test format forces an additional new lines
        // at the end of input texts.
        if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) == '\n') {
            buffer.setLength(buffer.length() - 1);
        }
        map.put(keyName, buffer.toString());
    }
}

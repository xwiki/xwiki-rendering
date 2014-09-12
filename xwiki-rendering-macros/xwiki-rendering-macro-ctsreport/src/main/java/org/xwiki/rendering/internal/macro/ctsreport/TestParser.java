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
package org.xwiki.rendering.internal.macro.ctsreport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses JUnit test results of the form {@code testname [syntax, IN|OUT:extension, CTS:extension] cause}, where
 * {@code cause} is either "Failing", "Missing" or "Passed".
 *
 * @version $Id$
 * @since 4.1M2
 */
public class TestParser
{
    /**
     * The regex to parse test result.
     */
    private static final Pattern PATTERN = Pattern.compile("(.*) \\[(.*), (.*):(.*), CTS:(.*)\\](.*)");

    /**
     * @param syntaxTest the string to parse
     * @return the parsed Result
     */
    public Result parse(String syntaxTest)
    {
        Result data = new Result();
        Matcher matcher = PATTERN.matcher(syntaxTest);
        if (matcher.matches()) {
            data.syntaxId = matcher.group(2);
            data.isSyntaxInputTest = "IN".equals(matcher.group(3));

            Test test = new Test();
            data.test = test;
            test.prefix = matcher.group(1);
            test.syntaxExtension = matcher.group(4);
            test.ctsExtension = matcher.group(5);

            String cause = matcher.group(6);
            if (cause.contains("Failing")) {
                test.state = State.FAILING;
            } else if (cause.contains("Missing")) {
                test.state = State.MISSING;
            } else if (cause.contains("Passed")) {
                test.state = State.PASSED;
            } else {
                test.state = State.UNKNOWN;
            }
        } else {
            throw new RuntimeException(String.format("Invalid Syntax Test format for [%s]", syntaxTest));
        }
        return data;
    }
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Extracts Data structures from a List of {@link Result} objects (which corresponds to parsed JUnit test result).
 *
 * @version $Id$
 * @since 4.1M2
 */
public class ResultExtractor
{
    /**
     * @param results the list of {@link Result} objects from which to extract the test names
     * @return the test names (eg "simple/bold/bold1")
     */
    public Set<String> extractByTestName(List<Result> results)
    {
        Set<String> testNames = new TreeSet<String>();
        for (Result result : results) {
            testNames.add(result.test.prefix);
        }
        return testNames;
    }

    /**
     * @param results the list of {@link Result} objects from which to extract the test names
     * @return a data structure providing test results by syntax. The returned Map's keys are syntax ids, the values
     *         are Pairs of input (left items of the pair) and output tests (right items of the pair)
     */
    public Map<String, Pair<Set<Test>, Set<Test>>> extractBySyntax(List<Result> results)
    {
        Map<String, Pair<Set<Test>, Set<Test>>> tests = new HashMap<String, Pair<Set<Test>, Set<Test>>>();

        for (Result result : results) {

            // Is there already an entry for the syntax?
            Pair<Set<Test>, Set<Test>> inOutTests = tests.get(result.syntaxId);
            if (inOutTests == null) {
                inOutTests = new ImmutablePair<Set<Test>, Set<Test>>(
                    new TreeSet<Test>(), new TreeSet<Test>());
                tests.put(result.syntaxId, inOutTests);
            }

            // Get the list of Test for the result type (In or Out)
            Set<Test> typeTests;
            if (result.isSyntaxInputTest) {
                typeTests = inOutTests.getLeft();
            } else {
                typeTests = inOutTests.getRight();
            }

            // Add the result as a Test object
            typeTests.add(result.test);
        }

        // Sort the Map to order by prefix
        return tests;
    }

    /**
     * Add not applicable tests for all syntaxes.
     *
     * @param testNames the test names (eg "simple/bold/bold1")
     * @param tests the tests by syntaxes
     */
    public void normalize(Set<String> testNames, Map<String, Pair<Set<Test>, Set<Test>>> tests)
    {
        for (Pair<Set<Test>, Set<Test>> inOutTests : tests.values()) {
            addNotApplicableTests(testNames, inOutTests.getLeft());
            addNotApplicableTests(testNames, inOutTests.getRight());
        }
    }

    /**
     * Add not applicable tests for all syntaxes, for input or output tests.
     *
     * @param testNames the test names (eg "simple/bold/bold1")
     * @param tests the tests by syntaxes
     */
    private void addNotApplicableTests(Set<String> testNames, Set<Test> tests)
    {
        List<String> inTestNames = extractTestNames(tests);

        // We only add "not applicable" tests if there's at least one test since otherwise we consider that
        // there's no Parser or Renderer for the syntax.
        if (!inTestNames.isEmpty()) {
            for (String testName : testNames) {
                if (!inTestNames.contains(testName)) {
                    // Add it with a Not Applicable State!
                    Test test = new Test();
                    test.prefix = testName;
                    test.state = State.NOT_APPLICABLE;
                    tests.add(test);
                }
            }
        }
    }

    /**
     * Extracts tests names from the passed set of tests.
     *
     * @param tests the set of tests from which to extract test names
     * @return the test names (eg "simple/bold/bold1")
     */
    private List<String> extractTestNames(Set<Test> tests)
    {
        List<String> inTestNames = new ArrayList<String>();
        Iterator<Test> it = tests.iterator();
        while (it.hasNext()) {
            inTestNames.add(it.next().prefix);
        }
        return inTestNames;
    }
}

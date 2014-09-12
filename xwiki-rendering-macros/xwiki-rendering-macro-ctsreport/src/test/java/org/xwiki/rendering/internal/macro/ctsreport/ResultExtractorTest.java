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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;

/**
 * Unit tests for {@link ResultExtractor}.
 *
 * @version $Id$
 * @since 4.1M2
 */
public class ResultExtractorTest
{
    private List<Result> results;

    @Before
    public void setUpResults()
    {
        TestParser parser = new TestParser();
        this.results = Arrays.asList(
            parser.parse("simple/italic/italic1 [xwiki/2.0, IN:.inout.txt, CTS:.inout.xml] - Passed"),
            parser.parse("simple/italic/italic1 [xwiki/2.1, IN:.inout.txt, CTS:.inout.xml] - Passed"),
            parser.parse("simple/bold/bold1 [xwiki/2.0, IN:.inout.txt, CTS:.inout.xml] - Passed"),
            parser.parse("simple/bold/bold1 [xwiki/2.0, OUT:.inout.txt, CTS:.inout.xml] - Passed")
        );
    }

    @org.junit.Test
    public void extractByTestName()
    {
        ResultExtractor extractor = new ResultExtractor();
        Set<String> testNames = extractor.extractByTestName(this.results);

        Assert.assertEquals(2, testNames.size());
        Iterator<String> it = testNames.iterator();
        Assert.assertEquals("simple/bold/bold1", it.next());
        Assert.assertEquals("simple/italic/italic1", it.next());
    }

    @org.junit.Test
    public void extractBySyntaxes()
    {
        ResultExtractor extractor = new ResultExtractor();
        Map<String, Pair<Set<Test>, Set<Test>>> tests = extractor.extractBySyntax(this.results);

        Assert.assertEquals(2, tests.size());

        Set<Test> inTestsForXWiki20 = tests.get("xwiki/2.0").getLeft();
        Assert.assertEquals(2, inTestsForXWiki20.size());
        Iterator<Test> it = inTestsForXWiki20.iterator();
        Assert.assertEquals("simple/bold/bold1", it.next().prefix);
        Assert.assertEquals("simple/italic/italic1", it.next().prefix);

        Set<Test> outTestsForXWiki20 = tests.get("xwiki/2.0").getRight();
        Assert.assertEquals(1, outTestsForXWiki20.size());
        Assert.assertEquals("simple/bold/bold1", outTestsForXWiki20.iterator().next().prefix);

        Set<Test> inTestsForXWiki21 = tests.get("xwiki/2.1").getLeft();
        Assert.assertEquals(1, inTestsForXWiki21.size());
        Assert.assertEquals("simple/italic/italic1", inTestsForXWiki21.iterator().next().prefix);

        Set<Test> outTestsForXWiki21 = tests.get("xwiki/2.1").getRight();
        Assert.assertEquals(0, outTestsForXWiki21.size());
    }

    @org.junit.Test
    public void normalize()
    {
        ResultExtractor extractor = new ResultExtractor();
        Set<String> testNames = extractor.extractByTestName(this.results);
        Map<String, Pair<Set<Test>, Set<Test>>> tests = extractor.extractBySyntax(this.results);
        extractor.normalize(testNames, tests);

        Assert.assertEquals(2, tests.size());

        Set<Test> inTestsForXWiki20 = tests.get("xwiki/2.0").getLeft();
        Assert.assertEquals(2, inTestsForXWiki20.size());
        Iterator<Test> it = inTestsForXWiki20.iterator();
        Assert.assertEquals("simple/bold/bold1", it.next().prefix);
        Assert.assertEquals("simple/italic/italic1", it.next().prefix);

        Set<Test> outTestsForXWiki20 = tests.get("xwiki/2.0").getRight();
        it = outTestsForXWiki20.iterator();
        Assert.assertEquals(2, outTestsForXWiki20.size());
        Assert.assertEquals("simple/bold/bold1", it.next().prefix);
        Assert.assertEquals("simple/italic/italic1", it.next().prefix);

        Set<Test> inTestsForXWiki21 = tests.get("xwiki/2.1").getLeft();
        it = inTestsForXWiki21.iterator();
        Assert.assertEquals(2, inTestsForXWiki21.size());
        Assert.assertEquals("simple/bold/bold1", it.next().prefix);
        Assert.assertEquals("simple/italic/italic1", it.next().prefix);

        Set<Test> outTestsForXWiki21 = tests.get("xwiki/2.1").getRight();
        Assert.assertEquals(0, outTestsForXWiki21.size());
    }

}

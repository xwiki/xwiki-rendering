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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Contains Test Data for a single test.
 *
 * @version $Id$
 * @since 4.1M1
 */
public class TestData
{
    /**
     * The syntax being tested and in which the syntax data is written in (eg "xwiki/2.0").
     */
    public String syntaxId;

    /**
     * The location where this tests is found in the CTS classpath (eg "cts/simple/bold/bold1").
     */
    public String prefix;

    /**
     * Test Syntax file extension. To compute the full Syntax test name, use: prefix + syntaxExtension.
     */
    public String syntaxExtension;

    /**
     * CTS file extension. To compute the full CTS test name, use: prefix + ctsExtension.
     */
    public String ctsExtension;

    /**
     * True if this test is an input test, ie the syntax data represents an input, false otherwise.
     */
    public boolean isSyntaxInputTest;

    /**
     * The CTS test data (either input or output, depending on whether the syntax data is input or outut; it's the
     * opposite of the syntax data).
     */
    public String ctsData;

    /**
     * The syntax data (either input or output).
     */
    public String syntaxData;

    /**
     * Configuration data for the test.
     */
    public TestDataConfiguration configuration;

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("syntaxId", this.syntaxId)
            .append("prefix", this.prefix)
            .append("syntaxExtension", this.syntaxExtension)
            .append("ctsExtension", this.ctsExtension)
            .append("isSyntaxInputTest", this.isSyntaxInputTest)
            .append("ctsData", this.ctsData)
            .append("syntaxData", this.syntaxData)
            .append("configuration", this.configuration)
            .toString();
    }

    /**
     * @return if this test matches a not-applicable-test regex
     */
    public boolean isNotApplicable()
    {
        return matches(this.configuration.notApplicableTests);
    }

    /**
     * @return if this test matches a failing-test regex
     */
    public boolean isFailingTest()
    {
        return matches(this.configuration.failingTests);
    }

    /**
     * @param regexes the list of regexes to match against
     * @return true if the current test matches at least one of the passed regexes or false otherwise
     */
    private boolean matches(List<String> regexes)
    {
        boolean matches = false;
        for (String regex : regexes) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(computeTestName());
            if (matcher.matches()) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    /**
     * Compute the test name, used to display in the JUnit Test Runner and for ignoring tests.
     *
     * @return the computed test name (eg "simple/bold/bold1 [xwiki/2.0, IN:bold1.in.txt, CTS:bold1.inout.xml]")
     */
    public String computeTestName()
    {
        return String.format("%s [%s, %s:%s, CTS:%s]", this.prefix, this.syntaxId,
            this.isSyntaxInputTest ? "IN" : "OUT", this.syntaxExtension, this.ctsExtension);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this) {
            return true;
        }
        if (!(object instanceof TestData)) {
            return false;
        }
        TestData rhs = (TestData) object;
        return new EqualsBuilder()
            .append(this.syntaxId, rhs.syntaxId)
            .append(this.prefix, rhs.prefix)
            .append(this.syntaxExtension, rhs.syntaxExtension)
            .append(this.ctsExtension, rhs.ctsExtension)
            .append(this.isSyntaxInputTest, rhs.isSyntaxInputTest)
            .append(this.ctsData, rhs.ctsData)
            .append(this.syntaxData, rhs.syntaxData)
            .append(this.configuration, rhs.configuration)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 17)
            .append(this.syntaxId)
            .append(this.prefix)
            .append(this.syntaxExtension)
            .append(this.ctsExtension)
            .append(this.isSyntaxInputTest)
            .append(this.ctsData)
            .append(this.syntaxData)
            .append(this.configuration)
            .toHashCode();
    }
}

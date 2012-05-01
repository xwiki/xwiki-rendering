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
            .append("isSyntaxInputTest", this.isSyntaxInputTest)
            .append("ctsData", this.ctsData)
            .append("syntaxData", this.syntaxData)
            .append("configuration", this.configuration)
            .toString();
    }

    /**
     * @return if this tests matches the tests to ignore
     */
    public boolean isIgnored()
    {
        boolean isIgnored = false;
        for (String ignoredTest : this.configuration.ignoredTests) {
            Pattern pattern = Pattern.compile(ignoredTest);
            Matcher matcher = pattern.matcher(computeTestName());
            if (matcher.matches()) {
                isIgnored = true;
                break;
            }
        }
        return isIgnored;
    }

    /**
     * Compute the test name, used to display in the JUnit Test Runner and for ignoring tests.
     *
     * @return the computed test name (eg "cts/simple/bold/bold1(IN) [xwiki/2.0]")
     */
    public String computeTestName()
    {
        return String.format("%s(%s) [%s]", this.prefix, this.isSyntaxInputTest ? "IN" : "OUT",
            this.syntaxId);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != getClass()) {
            return false;
        }
        TestData rhs = (TestData) object;
        return new EqualsBuilder()
            .append(this.syntaxId, rhs.syntaxId)
            .append(this.prefix, rhs.prefix)
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
            .append(this.isSyntaxInputTest)
            .append(this.ctsData)
            .append(this.syntaxData)
            .append(this.configuration)
            .toHashCode();
    }
}

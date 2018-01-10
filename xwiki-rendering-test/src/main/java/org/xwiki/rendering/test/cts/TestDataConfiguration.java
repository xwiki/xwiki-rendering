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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Contains Test configuration data (whether to run transformations or not, list of tests to ignore, etc).
 *
 * @version $Id$
 * @since 4.1M1
 */
public class TestDataConfiguration
{
    /**
     * List of tests that are not applicable, using regexes (eg {@code .*&#47;bold1\\(IN\\).*}).
     */
    public List<String> notApplicableTests = Collections.emptyList();

    /**
     * List of tests that are excluded because they're currently failing (they need to be fixed ASAP), using regexes
     * (eg {@code .*&#47;bold1\\(IN\\).*}).
     */
    public List<String> failingTests = Collections.emptyList();

    /**
     * List of test descriptions.
     */
    public Properties testDescriptions = new Properties();

    /**
     * The Syntax to inherit from if any. If an inherited syntax is specified then if a test doesn't exist for the
     * current Syntax the test runner will look for that test in the inherited syntax.
     */
    public String inheritSyntax;

    /**
     * The extension of the test files.
     */
    public String fileExtension = "txt";

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("notApplicableTests", this.notApplicableTests)
            .append("failingTests", this.failingTests)
            .append("testDescriptions", this.testDescriptions)
            .append("inheritSyntax", this.testDescriptions)
            .toString();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this) {
            return true;
        }
        if (!(object instanceof TestDataConfiguration)) {
            return false;
        }
        TestDataConfiguration rhs = (TestDataConfiguration) object;
        return new EqualsBuilder()
            .append(this.notApplicableTests, rhs.notApplicableTests)
            .append(this.failingTests, rhs.failingTests)
            .append(this.testDescriptions, rhs.testDescriptions)
            .append(this.inheritSyntax, rhs.inheritSyntax)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(1, 15)
            .append(this.notApplicableTests)
            .append(this.failingTests)
            .append(this.testDescriptions)
            .append(this.inheritSyntax)
            .toHashCode();
    }
}

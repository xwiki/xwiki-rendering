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
     * List of tests to ignore using regexes (eg {@code .*&#47;bold1\\(IN\\).*}).
     */
    public List<String> ignoredTests = Collections.emptyList();

    /**
     * List of test descriptions.
     */
    public Properties testDescriptions = new Properties();

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("ignoredTests", this.ignoredTests)
            .append("testDescriptions", this.testDescriptions)
            .toString();
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
        TestDataConfiguration rhs = (TestDataConfiguration) object;
        return new EqualsBuilder()
            .append(this.ignoredTests, rhs.ignoredTests)
            .append(this.testDescriptions, rhs.testDescriptions)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(1, 15)
            .append(this.ignoredTests)
            .append(this.testDescriptions)
            .toHashCode();
    }

}

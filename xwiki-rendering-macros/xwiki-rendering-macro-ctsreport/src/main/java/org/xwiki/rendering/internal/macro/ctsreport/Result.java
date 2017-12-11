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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Represents a parsed JUnit test result (see {@link TestParser} for more).
 *
 * @version $Id$
 * @since 4.1M2
 */
public class Result
{
    /**
     * The syntax being tested and in which the syntax data is written in (eg "xwiki/2.0").
     */
    public String syntaxId;

    /**
     * True if this test is an input test, ie the syntax data represents an input, false otherwise.
     */
    public boolean isSyntaxInputTest;

    /**
     * Test Data (test name, syntax extension, cts extension, state).
     */
    public Test test;

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("syntaxId", this.syntaxId)
            .append("test", this.test)
            .append("isSyntaxInputTest", this.isSyntaxInputTest)
            .toString();
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof Result)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Result rhs = (Result) object;
        return new EqualsBuilder()
            .append(this.syntaxId, rhs.syntaxId)
            .append(this.test, rhs.test)
            .append(this.isSyntaxInputTest, rhs.isSyntaxInputTest)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 17)
            .append(this.syntaxId)
            .append(this.test)
            .append(this.isSyntaxInputTest)
            .toHashCode();
    }
}

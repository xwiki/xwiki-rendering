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
 * Represents a Test (name, syntax extension, cts extension, state).
 *
 * @version $Id$
 * @since 4.1M2
 */
public class Test implements Comparable<Test>
{
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
     * The state of the tests (passing, missing, etc).
     */
    public State state;

    @Override
    public String toString()
    {
        return new XWikiToStringBuilder(this)
            .append("syntaxExtension", this.syntaxExtension)
            .append("ctsExtension", this.ctsExtension)
            .append("status", this.state)
            .append("prefix", this.prefix)
            .toString();
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof Test)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Test rhs = (Test) object;
        return new EqualsBuilder()
            .append(this.syntaxExtension, rhs.syntaxExtension)
            .append(this.ctsExtension, rhs.ctsExtension)
            .append(this.state, rhs.state)
            .append(this.prefix, rhs.prefix)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(3, 17)
            .append(this.syntaxExtension)
            .append(this.ctsExtension)
            .append(this.state)
            .append(this.prefix)
            .toHashCode();
    }

    @Override
    public int compareTo(Test test)
    {
        return this.prefix.compareTo(test.prefix);
    }
}

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
package org.xwiki.rendering.transformation.linkchecker;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a Link State, ie the HTTP response code when the link was checked, the time when the link was last
 * checked and context data.
 *
 * @version $Id$
 * @since 3.3M1
 */
public class LinkState
{
    /**
     * @see #getResponseCode()
     */
    private int responseCode;

    /**
     * @see #getLastCheckedTime()
     */
    private long lastCheckedTime;

    /**
     * @see #getContextData()
     */
    private Map<String, Object> contextData;

    /**
     * @param responseCode see {@link #getResponseCode()}
     * @param lastCheckedTime see {@link #getLastCheckedTime()}
     * @param contextData see {@link #getContextData()}
     */
    public LinkState(int responseCode, long lastCheckedTime, Map<String, Object> contextData)
    {
        this(responseCode, lastCheckedTime);
        this.contextData = contextData;
    }

    /**
     * @param responseCode see {@link #getResponseCode()}
     * @param lastCheckedTime see {@link #getLastCheckedTime()}
     */
    public LinkState(int responseCode, long lastCheckedTime)
    {
        this.responseCode = responseCode;
        this.lastCheckedTime = lastCheckedTime;
    }

    /**
     * @return the time when the link was last checked
     */
    public long getLastCheckedTime()
    {
        return this.lastCheckedTime;
    }

    /**
     * @return the HTTP response code when the link was checked
     */
    public int getResponseCode()
    {
        return this.responseCode;
    }

    /**
     * @return the context data associated with the content reference. What gets put in the Context Data Map depends
     *         purely on implementations of
     *         {@link org.xwiki.rendering.transformation.linkchecker.LinkContextDataProvider}
     */
    public Map<String, Object> getContextData()
    {
        return this.contextData;
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
        LinkState rhs = (LinkState) object;
        return new EqualsBuilder()
            .append(getResponseCode(), rhs.getResponseCode())
            .append(getLastCheckedTime(), rhs.getLastCheckedTime())
            .append(getContextData(), rhs.getContextData())
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(9, 15)
            .append(getResponseCode())
            .append(getLastCheckedTime())
            .append(getContextData())
            .toHashCode();
    }
}

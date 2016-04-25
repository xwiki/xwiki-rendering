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
package org.xwiki.rendering.wikimodel;

/**
 * A wiki parameter object.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiParameter
{
    private String fKey;

    private String fStr;

    private Boolean fValid;

    private String fValue;

    public WikiParameter(String key, String value)
    {
        fKey = key;
        fValue = value;
    }

    public WikiParameter(WikiParameter pair)
    {
        fKey = pair.getKey();
        fValue = pair.getValue();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WikiParameter)) {
            return false;
        }
        WikiParameter pair = (WikiParameter) obj;
        return fKey.equals(pair.fKey)
            && (fValue == pair.fValue || (fValue != null && fValue
            .equals(pair.fValue)));
    }

    /**
     * @return the key
     */
    public String getKey()
    {
        return fKey;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return fValue;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return fKey.hashCode() ^ (fValue != null ? fValue.hashCode() : 0);
    }

    /**
     * @return <code>true</code> if this key/value pair is valid
     */
    public boolean isValid()
    {
        if (fValid == null) {
            int len = (fKey != null) ? fKey.length() : 0;
            boolean result = len > 0;
            boolean delimiter = false;
            for (int i = 0; result && i < len; i++) {
                char ch = fKey.charAt(i);
                if (ch == ':') {
                    result = !delimiter && i > 0 && i < len - 1;
                    delimiter = true;
                } else if (ch == '.' || ch == '-') {
                    result = i > 0 && i < len - 1;
                } else {
                    result &= (i == 0 && Character.isLetter(ch))
                        || Character.isLetterOrDigit(ch);
                }
            }
            fValid = result ? Boolean.TRUE : Boolean.FALSE;
        }
        return fValid == Boolean.TRUE;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (fStr == null) {
            fStr = fKey + "='" + WikiPageUtil.escapeXmlAttribute(fValue) + "'";
        }
        return fStr;
    }
}
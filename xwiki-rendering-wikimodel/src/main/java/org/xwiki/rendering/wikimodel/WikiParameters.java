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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.wikimodel.impl.WikiScannerUtil;

/**
 * Allow iterating over {@link WikiParameter}s.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiParameters implements Iterable<WikiParameter>
{
    /**
     * The default character to use has escaping char.
     */
    public static final char DEFAULT_ESCAPECHAR = '\\';

    /**
     * An empty parameter list
     */
    public final static WikiParameters EMPTY = new WikiParameters();

    private static final long serialVersionUID = 1253393289284318413L;

    public static WikiParameters newWikiParameters(String str)
    {
        return newWikiParameters(str, DEFAULT_ESCAPECHAR);
    }

    public static WikiParameters newWikiParameters(String str, char escapeChar)
    {
        if (str == null) {
            return EMPTY;
        }
        str = str.trim();
        if ("".equals(str)) {
            return EMPTY;
        }
        return new WikiParameters(str, escapeChar);
    }

    protected final List<WikiParameter> fList = new ArrayList<WikiParameter>();

    private Map<String, WikiParameter[]> fMap;

    private String fStr;

    /**
     */
    public WikiParameters()
    {
        
    }

    /**
     * @param list
     */
    public WikiParameters(Collection<WikiParameter> list)
    {
        fList.addAll(list);
    }

    /**
     * @param str
     */
    public WikiParameters(String str)
    {
        WikiScannerUtil.splitToPairs(str, fList);
    }

    public WikiParameters(String str, char escapeChar)
    {
        WikiScannerUtil.splitToPairs(str, fList, escapeChar);
    }

    /**
     * @param str
     * @param delimiter
     */
    public WikiParameters(String str, String delimiter)
    {
        WikiScannerUtil.splitToPairs(str, fList, delimiter);
    }

    public WikiParameters(WikiParameters parameters)
    {
        fList.addAll(parameters.fList);
    }

    /**
     * Creates a new copy of this parameter object with new specified key/value
     * pair.
     *
     * @param key the parameter name
     * @param value the value of the parameter
     * @return a new copy of parameters object with the given key/value pair
     */
    public WikiParameters addParameter(String key, String value)
    {
        WikiParameters result = new WikiParameters();
        result.fList.addAll(fList);
        result.fList.add(new WikiParameter(key, value));
        return result;
    }

    public WikiParameters addParameters(WikiParameters parameters)
    {
        WikiParameters result = new WikiParameters();
        result.fList.addAll(fList);
        result.fList.addAll(parameters.fList);
        return result;
    }

    public WikiParameters setParameter(String key, String value)
    {
        return remove(key).addParameter(key, value);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof WikiParameters)) {
            return false;
        }
        WikiParameters params = (WikiParameters) obj;
        return fList.equals(params.fList);
    }

    /**
     * @param pos the position of the parameter
     * @return the parameter from the specified position
     */
    public WikiParameter getParameter(int pos)
    {
        return pos < 0 || pos >= fList.size() ? null : fList.get(pos);
    }

    /**
     * @param key the key of the parameter
     * @return the wiki parameter by key
     */
    public WikiParameter getParameter(String key)
    {
        WikiParameter[] list = getParameters(key);
        return (list != null) ? list[0] : null;
    }

    private Map<String, WikiParameter[]> getParameters()
    {
        if (fMap == null) {
            fMap = new HashMap<String, WikiParameter[]>();
            for (WikiParameter param : fList) {
                String key = param.getKey();
                WikiParameter[] list = fMap.get(key);
                int len = list != null ? list.length : 0;
                WikiParameter[] newList = new WikiParameter[len + 1];
                if (len > 0) {
                    System.arraycopy(list, 0, newList, 0, len);
                }
                newList[len] = param;
                fMap.put(key, newList);
            }
        }
        return fMap;
    }

    /**
     * Returns all parameters with this key
     *
     * @param key the key of the parameter
     * @return the wiki parameter by key
     */
    public WikiParameter[] getParameters(String key)
    {
        Map<String, WikiParameter[]> map = getParameters();
        WikiParameter[] list = map.get(key);
        return list;
    }

    /**
     * Returns the number of parameters in the internal list.
     *
     * @return the number of parameters in the internal list
     */
    public int getSize()
    {
        return fList.size();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fList.hashCode();
    }

    public Iterator<WikiParameter> iterator()
    {
        return fList.iterator();
    }

    /**
     * @param key the key of the parameter to remove
     * @return a new copy of parameter list without the specified parameter; if
     *         this parameter list does not contain such a key then this method
     *         returns a reference to this object
     */
    public WikiParameters remove(String key)
    {
        int pos = 0;
        for (WikiParameter param : fList) {
            if (key.equals(param.getKey())) {
                break;
            }
            pos++;
        }
        WikiParameters result = this;
        if (pos < fList.size()) {
            result = new WikiParameters(fList);
            result.fList.remove(pos);
        }
        return result;
    }

    /**
     * Returns a new list containing all parameters defined in this object.
     *
     * @return a list of all parameters
     */
    public List<WikiParameter> toList()
    {
        List<WikiParameter> result = new ArrayList<WikiParameter>(fList);
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (fStr == null) {
            StringBuffer buf = new StringBuffer();
            int len = fList.size();
            int counter = 0;
            for (int i = 0; i < len; i++) {
                WikiParameter pair = fList.get(i);
                if (pair.isValid()) {
                    buf.append(' ');
                    buf.append(pair);
                    counter++;
                }
            }
            fStr = buf.toString();
        }
        return fStr;
    }
}

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
package org.xwiki.rendering.wikimodel.xml;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;
import org.xwiki.rendering.wikimodel.WikiStyle;

/**
 * @author kotelnikov
 */
public class AbstractTagNotifier implements ISaxConst
{
    protected static final Map<String, String> EMPTY_MAP = Collections
        .emptyMap();

    protected ITagListener fListener;

    /**
     *
     */
    public AbstractTagNotifier(ITagListener listener)
    {
        fListener = listener;
    }

    /**
     * @param map
     * @param params
     * @return
     */
    private Map<String, String> addParams(
        Map<String, String> map,
        String... params)
    {
        for (int i = 0; i < params.length; i++) {
            String key = params[i];
            i++;
            String value = i < params.length ? params[i] : null;
            map.put(key, value);
        }
        return map;
    }

    /**
     * @param params
     * @return
     */
    private Map<String, String> getParamsMap(Iterable<WikiParameter> params)
    {
        Map<String, String> map = newParamMap();
        for (WikiParameter param : params) {
            String key = param.getKey();
            String value = param.getValue();
            map.put(key, value);
        }
        return map;
    }

    /**
     * @return
     */
    protected Map<String, String> newParamMap()
    {
        return new LinkedHashMap<String, String>();
    }

    protected Map<String, String> tagParams(
        Map<String, String> tagParams,
        String... params)
    {
        if (tagParams == EMPTY_MAP) {
            return tagParams(params);
        }
        return addParams(tagParams, params);
    }

    protected Map<String, String> tagParams(String... params)
    {
        if (params.length == 0) {
            return EMPTY_MAP;
        }
        Map<String, String> map = newParamMap();
        return addParams(map, params);
    }

    protected Map<String, String> tagParams(WikiFormat format)
    {
        if (format == null) {
            return EMPTY_MAP;
        }
        List<WikiStyle> styles = format.getStyles();
        if (styles.isEmpty()) {
            return EMPTY_MAP;
        }
        Map<String, String> map = newParamMap();
        StringBuffer buf = new StringBuffer();
        for (WikiStyle style : styles) {
            String name = style.toString();
            if (buf.length() > 0) {
                buf.append("; ");
            }
            buf.append(name);
        }
        map.put(STYLES, buf.toString());
        return map;
    }

    protected Map<String, String> tagParams(WikiReference ref)
    {
        if (ref == null) {
            return EMPTY_MAP;
        }
        return tagParams("label", ref.getLabel(), "href", ref.getLink());
    }

    protected Map<String, String> userParams(WikiFormat format)
    {
        if (format == null) {
            return EMPTY_MAP;
        }
        List<WikiParameter> params = format.getParams();
        return getParamsMap(params);
    }

    protected Map<String, String> userParams(WikiParameters params)
    {
        if (params == null || params.getSize() == 0) {
            return EMPTY_MAP;
        }
        return getParamsMap(params);
    }

    protected Map<String, String> userParams(WikiReference ref)
    {
        if (ref == null) {
            return EMPTY_MAP;
        }
        return userParams(ref.getParameters());
    }
}

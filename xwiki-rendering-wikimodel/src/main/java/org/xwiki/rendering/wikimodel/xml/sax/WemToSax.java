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
package org.xwiki.rendering.wikimodel.xml.sax;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xwiki.rendering.wikimodel.WikiPageUtil;
import org.xwiki.rendering.wikimodel.xml.ITagListener;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class WemToSax implements ITagListener
{
    private static Logger log = Logger.getLogger(WemToSax.class.getName());

    public final static String USER_NS = "http://www.wikimodel.org/ns/user-defined-params#";

    public final static String USER_PREFIX = "u";

    public final static String WEM_NS = "http://www.wikimodel.org/ns/wem#";

    public final static String WEM_PREFIX = "w";

    private int fDepth;

    private ContentHandler fHandler;

    public WemToSax(ContentHandler handler)
    {
        fHandler = handler;
    }

    /**
     * @see org.xwiki.rendering.wikimodel.xml.ITagListener#beginTag(java.lang.String,
     *      java.util.Map, java.util.Map)
     */
    public void beginTag(
        String tagName,
        Map<String, String> tagParams,
        Map<String, String> userParams)
    {
        String qualifiedTagName = null;
        try {
            qualifiedTagName = getQualifiedName(WEM_PREFIX, tagName);
            if (fDepth == 0) {
                fHandler.startDocument();
                fHandler.startPrefixMapping(WEM_PREFIX, WEM_NS);
                fHandler.startPrefixMapping(USER_PREFIX, USER_NS);
            }
            Attributes atts = getAttributes(tagParams, userParams);
            fHandler.startElement(WEM_NS, tagName, qualifiedTagName, atts);
            fDepth++;
        } catch (Throwable t) {
            handleError("[BEGIN_TAG]"
                + " Tag: '"
                + tagName
                + "'. QualifiedTagName: '"
                + qualifiedTagName
                + "'. "
                + "TagParams: ["
                + tagParams
                + "]. UserParams: ["
                + userParams
                + "]", t);
        }
    }

    /**
     * @see org.xwiki.rendering.wikimodel.xml.ITagListener#endTag(java.lang.String,
     *      java.util.Map, java.util.Map)
     */
    public void endTag(
        String tagName,
        Map<String, String> tagParams,
        Map<String, String> userParams)
    {
        String qualifiedTagName = null;
        try {
            qualifiedTagName = getQualifiedName(WEM_PREFIX, tagName);
            fHandler.endElement(WEM_NS, tagName, qualifiedTagName);
            fDepth--;
            if (fDepth == 0) {
                fHandler.endDocument();
            }
        } catch (Throwable t) {
            handleError("[END_TAG]"
                + " Tag: '"
                + tagName
                + "'. QualifiedTagName: '"
                + qualifiedTagName
                + "'. "
                + "TagParams: ["
                + tagParams
                + "]. UserParams: ["
                + userParams
                + "]", t);
        }
    }

    private Attributes getAttributes(
        Map<String, String> tagParams,
        Map<String, String> userParams)
    {
        final Map<String, String> attrs = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : tagParams.entrySet()) {
            String qName = getQualifiedName(WEM_PREFIX, entry.getKey());
            if (qName == null) {
                continue;
            }
            String value = entry.getValue();
            attrs.put(qName, value);
        }
        for (Map.Entry<String, String> entry : userParams.entrySet()) {
            String qName = getQualifiedName(USER_PREFIX, entry.getKey());
            if (qName == null) {
                continue;
            }
            String value = entry.getValue();
            attrs.put(qName, value);
        }
        @SuppressWarnings("unchecked")
        final Map.Entry<String, String>[] array = attrs.entrySet().toArray(
            new Map.Entry[attrs.size()]);

        return new Attributes()
        {
            private Map.Entry<String, String> getEntry(int index)
            {
                return array[index];
            }

            public int getIndex(String qName)
            {
                int result = -1;
                for (int i = 0; i < array.length; i++) {
                    if (qName.equals(array[i].getKey())) {
                        result = i;
                        break;
                    }
                }
                return result;
            }

            public int getIndex(String uri, String localName)
            {
                String prefix = null;
                if (WEM_NS.equals(uri)) {
                    prefix = WEM_PREFIX;
                } else if (USER_NS.equals(uri)) {
                    prefix = USER_PREFIX;
                }
                if (prefix == null) {
                    return -1;
                }
                return getIndex(prefix + ":" + localName);
            }

            public int getLength()
            {
                return array.length;
            }

            public String getLocalName(int index)
            {
                Map.Entry<String, String> entry = getEntry(index);
                if (entry == null) {
                    return null;
                }
                return entry.getKey();
            }

            public String getQName(int index)
            {
                if (index < 0 || index >= array.length) {
                    return null;
                }
                Entry<String, String> entry = array[index];
                return entry.getKey();
            }

            public String getType(int index)
            {
                return "CDATA";
            }

            public String getType(String qName)
            {
                return "CDATA";
            }

            public String getType(String uri, String localName)
            {
                return "CDATA";
            }

            public String getURI(int index)
            {
                String qName = getQName(index);
                if (qName == null) {
                    return null;
                }
                return qName.startsWith(WEM_PREFIX) ? WEM_NS : qName
                    .startsWith(USER_PREFIX) ? USER_NS : null;
            }

            private String getValue(Entry<String, String> entry)
            {
                String value = entry != null ? entry.getValue() : null;
                return value != null ? value : "";
            }

            public String getValue(int index)
            {
                Entry<String, String> entry = getEntry(index);
                return getValue(entry);
            }

            public String getValue(String qName)
            {
                int idx = getIndex(qName);
                Entry<String, String> entry = idx >= 0 ? array[idx] : null;
                return getValue(entry);
            }

            public String getValue(String uri, String localName)
            {
                int idx = getIndex(uri, localName);
                Entry<String, String> entry = idx >= 0 ? array[idx] : null;
                return entry != null ? entry.getValue() : null;
            }
        };
    }

    /**
     * @param tagName
     * @return
     */
    private String getQualifiedName(String prefix, String tagName)
    {
        if (tagName == null || tagName.length() == 0) {
            return null;
        }
        tagName = WikiPageUtil.escapeXmlAttribute(tagName);
        tagName = tagName.replaceAll(" ", "-");
        boolean valid = WikiPageUtil.isValidXmlName(tagName, false);
        if (!valid) {
            return null;
        }
        return prefix != null && !"".equals(prefix)
            ? prefix + ":" + tagName
            : tagName;
    }

    private void handleError(String s, Throwable e)
    {
        log.log(Level.SEVERE, s, e);
        if (e instanceof Error) {
            throw ((Error) e);
        }
        throw new RuntimeException(e);
    }

    /**
     * @see org.xwiki.rendering.wikimodel.xml.ITagListener#onCDATA(java.lang.String)
     */
    public void onCDATA(String content)
    {
        // FIXME: restore CDATA blocks notification
        // onText(content);
    }

    /**
     * @see org.xwiki.rendering.wikimodel.xml.ITagListener#onTag(java.lang.String,
     *      java.util.Map, java.util.Map)
     */
    public void onTag(
        String tagName,
        Map<String, String> tagParams,
        Map<String, String> userParams)
    {
        beginTag(tagName, tagParams, userParams);
        endTag(tagName, tagParams, userParams);
    }

    /**
     * @see org.xwiki.rendering.wikimodel.xml.ITagListener#onText(java.lang.String)
     */
    public void onText(String content)
    {
        try {
            if (content != null) {
                char[] array = content.toCharArray();
                int start = 0;
                int i;
                for (i = 0; i < array.length; i++) {
                    char c = array[i];
                    int ch = c;
                    if (WikiPageUtil.isValidXmlChar(ch)) {
                        boolean toEscape = (ch == '\''
                            || ch == '\"'
                            || ch == '['
                            || ch == ']'
                            || ch == '&' || ch == '|');
                        if (!toEscape) {
                            continue;
                        }
                    }
                    if (i != start) {
                        fHandler.characters(array, start, i - start);
                        start = i;
                    }
                    // FIXME: add notifications about the found entity
                    // buf.append("&#x" + Integer.toHexString(array[i]) +
                    // ";");
                }
                if (i != start) {
                    fHandler.characters(array, start, i - start);
                }
            }
        } catch (Throwable e) {
            handleError("onText error " + content, e);
        }
    }
}

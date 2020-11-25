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
package org.xwiki.rendering.listener;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a set of MetaData.
 *
 * @version $Id$
 * @since 3.0M2
 */
public class MetaData
{
    /**
     * Represents no MetaData.
     */
    public static final MetaData EMPTY = new MetaData();

    /**
     * Represents a source metaData, which corresponds to the reference to the source entity containing the content to
     * render. The reference is a free form text in a format that is understood by the Listeners supporting it.
     */
    public static final String SOURCE = "source";

    /**
     * Represents the syntax of the content found in macro containing wiki content (like a box macro for example). The
     * value has to be a {@link org.xwiki.rendering.syntax.Syntax} object.
     *
     * @since 3.0M3
     */
    public static final String SYNTAX = "syntax";

    /**
     * Represent the base reference to resolve all the references in the blocks (links, images, macro parameters, etc.).
     *
     * @since 3.4M1
     */
    public static final String BASE = "base";

    /**
     * Represents a non generated content: a content that has not been transformed in any way.
     *
     * @since 10.10
     */
    public static final String NON_GENERATED_CONTENT = "non-generated-content";

    /**
     * Represents a metadata attached to a specific parameter identified by the given name.
     *
     * @since 11.1RC1
     */
    public static final String PARAMETER_NAME = "parameter-name";

    /**
     * Contains all MetaData for this Block and its children. Note: we preserve the order of metadata elements as they
     * are added as a service for the user so he can count on it.
     */
    private final Map<String, Object> metadata;

    /**
     * Empty metaData.
     */
    public MetaData()
    {
        this.metadata = new LinkedHashMap<String, Object>();
    }

    /**
     * @param metaData the metadata to set
     */
    public MetaData(Map<String, Object> metaData)
    {
        this.metadata = new LinkedHashMap<String, Object>(metaData);
    }

    /**
     * @param key the key to the metadata element to add (e.g. "syntax")
     * @param value the value of the metadata element to add (e.g. a Syntax object)
     */
    public void addMetaData(String key, Object value)
    {
        this.metadata.put(key, value);
    }

    /**
     * @param metaData the metadata to add
     */
    public void addMetaData(MetaData metaData)
    {
        this.metadata.putAll(metaData.getMetaData());
    }

    /**
     * @param key the key to the metadata element to retrieve (e.g. "syntax")
     * @return the metadata corresponding to the passed key of null if no such metadata exist.
     */
    public Object getMetaData(String key)
    {
        return this.metadata.get(key);
    }

    /**
     * @param key the key to the metadata element to check for
     * @return true if there's a metadata with the passed key, false otherwise
     * @since 3.0M3
     */
    public boolean contains(String key)
    {
        return this.metadata.containsKey(key);
    }

    /**
     * @return all the metadata
     */
    public Map<String, Object> getMetaData()
    {
        return Collections.unmodifiableMap(this.metadata);
    }

    // Object

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        return obj instanceof MetaData && this.metadata.equals(((MetaData) obj).metadata);
    }

    @Override
    public int hashCode()
    {
        return this.metadata.hashCode();
    }

    @Override
    public String toString()
    {
        return "MetaData{metadata=" + metadata + '}';
    }
}

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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An immutable set of styles.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiFormat
{
    public static WikiFormat EMPTY = new WikiFormat();

    private String fClosingTags;

    private String fOpeningTags;

    private LinkedHashSet<WikiStyle> fStyles = new LinkedHashSet<WikiStyle>();

    private WikiParameters fParams = WikiParameters.EMPTY;

    public WikiFormat()
    {
    }

    public WikiFormat(Set<WikiStyle> styles)
    {
        this(styles, Collections.<WikiParameter>emptyList());
    }

    public WikiFormat(Set<WikiStyle> styles, Collection<WikiParameter> params)
    {
        if (!styles.isEmpty()) {
            fStyles.addAll(styles);
        }
        if (!params.isEmpty()) {
            fParams = new WikiParameters(params);
        }
    }

    public WikiFormat(Collection<WikiParameter> params)
    {
        this(Collections.<WikiStyle>emptySet(), params);
    }

    public WikiFormat(WikiStyle style)
    {
        this(Collections.<WikiStyle>singleton(style));
    }

    public WikiFormat(WikiStyle style, Collection<WikiParameter> params)
    {
        this(Collections.<WikiStyle>singleton(style), params);
    }

    public WikiFormat(WikiStyle[] styles)
    {
        super();
        for (WikiStyle style : styles) {
            fStyles.add(style);
        }
    }

    public WikiFormat setParameters(Collection<WikiParameter> params)
    {
        return new WikiFormat(fStyles, params);
    }

    /**
     * Creates a new style set and adds the given style to it.
     *
     * @param style the style to add
     * @return a new copy of the style set containing the given style
     */
    public WikiFormat addStyle(WikiStyle style)
    {
        if (fStyles.contains(style)) {
            return this;
        }
        WikiFormat clone = getClone();
        clone.fStyles.add(style);
        return clone;
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
        if (!(obj instanceof WikiFormat)) {
            return false;
        }
        WikiFormat set = (WikiFormat) obj;
        return fStyles.equals(set.fStyles) && fParams.equals(set.fParams);
    }

    /**
     * @return a new clone of this format object
     */
    protected WikiFormat getClone()
    {
        return new WikiFormat(fStyles, fParams.toList());
    }

    /**
     * Returns opening or closing tags corresponding to the given format(it
     * depends on the given flag).
     *
     * @param open if this flag is <code>true</code> then this method returns
     * opening tags for this format
     * @return opening or closing tags corresponding to the given format(it
     *         depends on the given flag)
     */
    public String getTags(boolean open)
    {
        if (fOpeningTags == null) {
            StringBuffer o = new StringBuffer();
            StringBuffer c = new StringBuffer();
            for (WikiStyle style : fStyles) {
                o.append('<').append(style).append('>');
                c.insert(0, ">").insert(0, style).insert(0, "</");
            }
            fOpeningTags = o.toString().intern();
            fClosingTags = c.toString().intern();
        }
        return open ? fOpeningTags : fClosingTags;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        // Random number. See
        // http://www.geocities.com/technofundo/tech/java/equalhash.html
        // for the detail of this algorithm.
        int hash = 8;
        hash = 31 * hash + (null == fStyles ? 0 : fStyles.hashCode());
        hash = 31 * hash + (null == fParams ? 0 : fParams.hashCode());
        return hash;
    }

    /**
     * @param style the style to check
     * @return <code>true</code> if this format has the specified style
     */
    public boolean hasStyle(WikiStyle style)
    {
        return fStyles.contains(style);
    }

    /**
     * Creates a new style set which does not contain the specified style.
     *
     * @param style the style to add
     * @return a new copy of the style set containing the given style
     */
    public WikiFormat removeStyle(WikiStyle style)
    {
        if (!fStyles.contains(style)) {
            return this;
        }
        WikiFormat clone = getClone();
        clone.fStyles.remove(style);
        return clone;
    }

    /**
     * Creates a new format object where the specified style is switched: if
     * this format contains the given style then the resulting format does not
     * and vice versa.
     *
     * @param wikiStyle the style to switch
     * @return a format object where the given style is inverted relatively to
     *         this format
     */
    public WikiFormat switchStyle(WikiStyle wikiStyle)
    {
        WikiFormat clone = getClone();
        if (clone.fStyles.contains(wikiStyle)) {
            clone.fStyles.remove(wikiStyle);
        } else {
            clone.fStyles.add(wikiStyle);
        }
        return clone;
    }

    /**
     * @return the list of styles in the order in which they were created
     */
    public List<WikiStyle> getStyles()
    {
        return new ArrayList<WikiStyle>(fStyles);
    }

    public List<WikiParameter> getParams()
    {
        return fParams.toList();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return fStyles.toString();
    }
}

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
 * This object represents an individual reference in the wiki document. A
 * reference contains the following parts:
 * <ul>
 * <li>link (mandatory) - it can b e a hyperlink, URI or a wiki page name</li>
 * <li>label (optional) - a human readable label associated with this reference</li>
 * <li>parameters (optional) - additional wiki parameters associated with the
 * reference. It can be a style name, a target of the link or something else.</li>
 * </ul>
 * Instances of this type are immutable so they can be shared between various
 * contexts (like threads, parser instances and so on).
 *
 * @version $Id$
 * @since 4.0M1
 */
public class WikiReference
{
    /**
     * A human-readable label associated with this reference. This is an
     * optional part of the reference.
     */
    private final String fLabel;

    /**
     * The link corresponding to this reference. It can be a hyperlink, URI or a
     * wiki page name. This is a mandatory part of the reference.
     */
    private final String fLink;

    /**
     * Additional parameters associated with this reference. This is an optional
     * part of the reference.
     */
    private final WikiParameters fParameters;

    /**
     * This field is used only by {@link #toString()} method to cache the
     * resulting string representation of this reference.
     */
    private String fString;

    /**
     * This constructor is used to initialize only the link part of the
     * reference
     *
     * @param link the link corresponding to the reference; it can be a
     * hyperlink, URI or a wiki name
     */
    public WikiReference(String link)
    {
        this(link, null, null);
    }

    /**
     * This constructor is used to initialize the link and label of this
     * reference
     *
     * @param link the link corresponding to the reference; it can be a
     * hyperlink, URI or a wiki name
     * @param label the label corresponding to this reference
     */
    public WikiReference(String link, String label)
    {
        this(link, label, null);
    }

    /**
     * This constructor is used to initialize all internal fields of this class.
     *
     * @param link the link corresponding to the reference; it can be a
     * hyperlink, URI or a wiki name
     * @param label the label corresponding to this reference
     * @param params a list of parameters of this reference
     */
    public WikiReference(String link, String label, WikiParameters params)
    {
        assert link != null : "Link can not be null";
        fLink = link;
        fLabel = label;
        if (params == null) {
            params = WikiParameters.EMPTY;
        }
        fParameters = params;
    }

    /**
     * This constructor is used to initialize the link and params fields
     *
     * @param link the link corresponding to the reference; it can be a
     * hyperlink, URI or a wiki name
     * @param params a list of parameters of this reference
     */
    public WikiReference(String link, WikiParameters params)
    {
        this(link, null, params);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WikiReference)) {
            return false;
        }
        WikiReference r = (WikiReference) obj;
        return fLink.equals(r.fLink)
            && ((fLabel == r.fLabel) || (fLabel != null && fLabel
            .equals(r.fLabel)))
            && fParameters.equals(r.fParameters);
    }

    /**
     * Returns a human-readable label associated with this reference. This is an
     * optional part of the reference so this method can return
     * <code>null</code>.
     *
     * @return a human-readable label associated with this reference
     */
    public String getLabel()
    {
        return fLabel;
    }

    /**
     * Returns a link of this reference. It can be an a hyperlink, an URI or a
     * wiki name. This part of the reference is mandatory so the returned value
     * is not empty.
     *
     * @return a link associated with this reference
     */
    public String getLink()
    {
        return fLink;
    }

    /**
     * Returns parameters associated with this reference. This method never
     * returns <code>null</code>. If there is no specific parameters for the
     * reference then this method returns the {@link WikiParameters#EMPTY}
     * instance.
     *
     * @return a non-<code>null</code> object representing parameters of this
     *         link
     */
    public WikiParameters getParameters()
    {
        return fParameters;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (fString == null) {
            StringBuffer buf = new StringBuffer();
            buf.append(fLink);
            if (fLabel != null) {
                buf.append('(');
                buf.append(fLabel);
                buf.append(')');
            }
            buf.append(fParameters);
            fString = buf.toString();
        }
        return fString;
    }
}

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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * WikiModel extension in order to add additional XWiki Link information so that the XWiki Generator Listener for the
 * XHTML syntax doesn't have to do any link parsing.
 *
 * @version $Id$
 * @since 2.5RC1
 */
public class XWikiWikiReference extends WikiReference
{
    private ResourceReference reference;

    private boolean freeStanding;

    public XWikiWikiReference(ResourceReference reference, String label, WikiParameters linkParameters,
        boolean freeStanding)
    {
        super(reference.getReference(), label, linkParameters);
        this.reference = reference;
        this.freeStanding = freeStanding;
    }

    public boolean isFreeStanding()
    {
        return this.freeStanding;
    }

    public ResourceReference getReference()
    {
        return this.reference;
    }

    // TODO: Ensure there's no sync issue between reference object and getLink() from WikiReference
    // TODO: implement totring

    @Override
    public boolean equals(Object obj)
    {
        EqualsBuilder builder = new EqualsBuilder();

        builder.appendSuper(super.equals(obj));
        builder.append(reference);
        builder.append(freeStanding);

        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(reference);
        builder.append(freeStanding);

        return builder.toHashCode();
    }
}

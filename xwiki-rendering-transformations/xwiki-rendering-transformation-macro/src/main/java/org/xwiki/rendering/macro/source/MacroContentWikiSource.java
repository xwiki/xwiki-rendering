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
package org.xwiki.rendering.macro.source;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.stability.Unstable;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * A wiki content to use as macro content.
 * 
 * @version $Id$
 * @since 15.1RC1
 * @since 14.10.5
 */
@Unstable
public class MacroContentWikiSource
{
    private final MacroContentSourceReference reference;

    private final String content;

    private final Syntax syntax;

    /**
     * @param reference the reference of the content
     * @param content the wiki content
     * @param syntax the syntax of the content if known, null otherwise
     */
    public MacroContentWikiSource(MacroContentSourceReference reference, String content, Syntax syntax)
    {
        this.reference = reference;
        this.content = content;
        this.syntax = syntax;
    }

    /**
     * @return the reference of the content
     */
    public MacroContentSourceReference getReference()
    {
        return this.reference;
    }

    /**
     * @return the wiki content
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * @return the syntax of the content if known, null otherwise
     */
    public Syntax getSyntax()
    {
        return this.syntax;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);

        builder.append("reference", getReference());
        builder.append("syntax", getSyntax());
        builder.append("content", getContent());

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj != null) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof MacroContentWikiSource) {
                MacroContentWikiSource otherSource = (MacroContentWikiSource) obj;

                EqualsBuilder builder = new EqualsBuilder();

                builder.append(getReference(), otherSource.getReference());
                builder.append(getContent(), otherSource.getContent());
                builder.append(getSyntax(), otherSource.getSyntax());

                return builder.isEquals();
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getReference());
        builder.append(getContent());
        builder.append(getSyntax());

        return builder.build();
    }
}

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
package org.xwiki.rendering.syntax;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a wiki syntax that the user can use to enter wiki content. A syntax is made of two parts: a type
 * (eg XWiki, Confluence, MediaWiki, etc) and a version (1.0, 2.0, etc). 
 * For example the XWiki 1.0 syntax, the XWiki 2.0 syntax, the Confluence 1.0 syntax, etc.
 * 
 * @version $Id$
 * @since 2.0RC1
 */
public class Syntax
{
    public static final Syntax XHTML_1_0 = new Syntax(SyntaxType.XHTML, "1.0");
    public static final Syntax HTML_4_01 = new Syntax(SyntaxType.HTML, "4.01");
    public static final Syntax XWIKI_1_0 = new Syntax(SyntaxType.XWIKI, "1.0");
    public static final Syntax XWIKI_2_0 = new Syntax(SyntaxType.XWIKI, "2.0");
    public static final Syntax XWIKI_2_1 = new Syntax(SyntaxType.XWIKI, "2.1");
    public static final Syntax XWIKI_2_2 = new Syntax(SyntaxType.XWIKI, "2.2", "experimental");
    public static final Syntax PLAIN_1_0 = new Syntax(SyntaxType.PLAIN, "1.0");
    public static final Syntax EVENT_1_0 = new Syntax(SyntaxType.EVENT, "1.0");
    public static final Syntax TEX_1_0 = new Syntax(SyntaxType.TEX, "1.0");
    public static final Syntax CREOLE_1_0 = new Syntax(SyntaxType.CREOLE, "1.0");
    public static final Syntax JSPWIKI_1_0 = new Syntax(SyntaxType.JSPWIKI, "1.0");
    public static final Syntax MEDIAWIKI_1_0 = new Syntax(SyntaxType.MEDIAWIKI, "1.0");
    public static final Syntax CONFLUENCE_1_0 = new Syntax(SyntaxType.CONFLUENCE, "1.0");
    public static final Syntax TWIKI_1_0 = new Syntax(SyntaxType.TWIKI, "1.0");
    public static final Syntax DOCBOOK_4_4 = new Syntax(SyntaxType.DOCBOOK, "4.4");

    /**
     * @since 3.3M1
     */
    public static final Syntax XDOMXML_CURRENT = new Syntax(SyntaxType.XDOMXML, "current");

    /**
     * @since 3.3M1
     */
    public static final Syntax XDOMXML_1_0 = new Syntax(SyntaxType.XDOMXML, "1.0");
    
    /**
     * @since 3.4M1
     */
    public static final Syntax MARKDOWN_1_0 = new Syntax(SyntaxType.MARKDOWN, "1.0");

    /**
     * @since 4.3M1
     */
    public static final Syntax APT_1_0 = new Syntax(SyntaxType.APT, "1.0");

    /**
     * This is HTML with annotations (comments) in order to allow round tripping between for example the WYSIWYG editor
     * and wiki syntax.
     */
    public static final Syntax ANNOTATED_XHTML_1_0 = new Syntax(SyntaxType.ANNOTATED_XHTML, "1.0");

    private SyntaxType type;

    private String version;

    /**
     * Optional free form text that qualifies the version, eg "experimental". 
     */
    private String qualifier;

    public Syntax(SyntaxType type, String version)
    {
        this.type = type;
        this.version = version;
    }

    public Syntax(SyntaxType type, String version, String qualifier)
    {
        this(type, version);
        this.qualifier = qualifier;
    }

    public SyntaxType getType()
    {
        return this.type;
    }

    public String getVersion()
    {
        return this.version;
    }

    public String getQualifier()
    {
        return this.qualifier;
    }

    public String toIdString()
    {
        return getType().getId() + "/" + getVersion().toLowerCase();
    }

    @Override
    public String toString()
    {
        return getType().toString() + " " + getVersion() + (getQualifier() != null ? " (" + getQualifier() + ")" : "");
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 7)
            .append(getType())
            .append(getVersion())
            .append(getQualifier())
            .toHashCode();
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
        Syntax rhs = (Syntax) object;
        return new EqualsBuilder()
            .append(getType(), rhs.getType())
            .append(getVersion(), rhs.getVersion())
            .append(getQualifier(), rhs.getQualifier())
            .isEquals();
    }
}

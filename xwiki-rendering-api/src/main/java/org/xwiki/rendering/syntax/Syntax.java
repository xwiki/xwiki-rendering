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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.parser.ParseException;

/**
 * Represents a wiki syntax that the user can use to enter wiki content. A syntax is made of two parts: a type (eg
 * XWiki, Confluence, MediaWiki, etc) and a version (1.0, 2.0, etc). For example the XWiki 1.0 syntax, the XWiki 2.0
 * syntax, the Confluence 1.0 syntax, etc.
 *
 * @version $Id$
 * @since 2.0RC1
 */
public class Syntax implements Comparable<Syntax>
{
    /**
     * HTML5 syntax.
     *
     * @since 6.4M3
     */
    public static final Syntax HTML_5_0 = new Syntax(SyntaxType.HTML, "5.0");

    /**
     * XHTML 1.0 syntax.
     */
    public static final Syntax XHTML_1_0 = new Syntax(SyntaxType.XHTML, "1.0");

    /**
     * HTML 4.01 syntax.
     */
    public static final Syntax HTML_4_01 = new Syntax(SyntaxType.HTML, "4.01");

    /**
     * XWiki 1.0 syntax.
     * 
     * @deprecated since 5.0, use {@link #XWIKI_2_1} instead
     */
    @Deprecated
    public static final Syntax XWIKI_1_0 = new Syntax(SyntaxType.XWIKI, "1.0");

    /**
     * XWiki 2.0 syntax.
     */
    public static final Syntax XWIKI_2_0 = new Syntax(SyntaxType.XWIKI, "2.0");

    /**
     * XWiki 2.1 syntax.
     */
    public static final Syntax XWIKI_2_1 = new Syntax(SyntaxType.XWIKI, "2.1");

    /**
     * Plain text syntax.
     */
    public static final Syntax PLAIN_1_0 = new Syntax(SyntaxType.PLAIN, "1.0");

    /**
     * Events syntax.
     */
    public static final Syntax EVENT_1_0 = new Syntax(SyntaxType.EVENT, "1.0");

    /**
     * TEX syntax.
     */
    public static final Syntax TEX_1_0 = new Syntax(SyntaxType.TEX, "1.0");

    /**
     * Creole syntax.
     */
    public static final Syntax CREOLE_1_0 = new Syntax(SyntaxType.CREOLE, "1.0");

    /**
     * JSPWiki syntax.
     */
    public static final Syntax JSPWIKI_1_0 = new Syntax(SyntaxType.JSPWIKI, "1.0");

    /**
     * Old MediaWiki syntax.
     * 
     * @deprecated since 8.2RC1, use {@link #MEDIAWIKI_1_6} instead
     */
    public static final Syntax MEDIAWIKI_1_0 = new Syntax(SyntaxType.MEDIAWIKI, "1.0");

    /**
     * New MediaWiki syntax.
     */
    public static final Syntax MEDIAWIKI_1_6 = new Syntax(SyntaxType.MEDIAWIKI, "1.6");

    /**
     * DokuWiki syntax.
     * 
     * @since 9.8RC1
     */
    public static final Syntax DOKUWIKI_1_0 = new Syntax(SyntaxType.DOKUWIKI, "1.0");

    /**
     * TWiki syntax.
     */
    public static final Syntax TWIKI_1_0 = new Syntax(SyntaxType.TWIKI, "1.0");

    /**
     * Docbook 4.4 syntax.
     */
    public static final Syntax DOCBOOK_4_4 = new Syntax(SyntaxType.DOCBOOK, "4.4");

    /**
     * Confluence wiki syntax.
     */
    public static final Syntax CONFLUENCE_1_0 = new Syntax(SyntaxType.CONFLUENCE, "1.0");

    /**
     * Confluence XHTML based syntax.
     *
     * @since 5.3M1
     */
    public static final Syntax CONFLUENCEXHTML_1_0 = new Syntax(SyntaxType.CONFLUENCEXHTML, "1.0");

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
     * @since 5.2M1
     */
    public static final Syntax MARKDOWN_1_1 = new Syntax(SyntaxType.MARKDOWN, "1.1");

    /**
     * @since 4.3M1
     */
    public static final Syntax APT_1_0 = new Syntax(SyntaxType.APT, "1.0");

    /**
     * This is HTML with annotations (comments) in order to allow round tripping between for example the WYSIWYG editor
     * and wiki syntax.
     */
    public static final Syntax ANNOTATED_XHTML_1_0 = new Syntax(SyntaxType.ANNOTATED_XHTML, XHTML_1_0.getVersion());

    /**
     * This is HTML5 with annotations (comments) in order to allow round tripping between for example the WYSIWYG editor
     * and wiki syntax.
     */
    public static final Syntax ANNOTATED_HTML_5_0 = new Syntax(SyntaxType.ANNOTATED_HTML, HTML_5_0.getVersion());

    private static final long serialVersionUID = 1L;

    /**
     * Used to cut the syntax identifier into syntax name and syntax version.
     */
    private static final Pattern SYNTAX_PATTERN = Pattern.compile("(.*)\\/(.*)");

    private SyntaxType type;

    private String version;

    /**
     * Optional free form text that qualifies the version, eg "experimental".
     */
    private String qualifier;

    private transient volatile String idStringCache;

    /**
     * @param type the type of the syntax
     * @param version the specific version of the syntax
     */
    public Syntax(SyntaxType type, String version)
    {
        this.type = type;
        this.version = version;
    }

    /**
     * @param type the type of the syntax
     * @param version the specific version of the syntax
     * @param qualifier a qualifier
     */
    public Syntax(SyntaxType type, String version, String qualifier)
    {
        this(type, version);
        this.qualifier = qualifier;
    }

    /**
     * @param syntaxIdAsString the syntax as a string (eg "xwiki/2.0", "plain/1.0")
     * @return the parsed syntax as a Syntax object
     * @throws ParseException in case the string doesn't represent a valid syntax
     * @since 9.8RC1
     */
    public static Syntax valueOf(String syntaxIdAsString) throws ParseException
    {
        if (syntaxIdAsString == null) {
            throw new ParseException("The passed Syntax cannot be NULL");
        }

        Matcher matcher = SYNTAX_PATTERN.matcher(syntaxIdAsString);
        if (!matcher.matches()) {
            throw new ParseException("Invalid Syntax format [" + syntaxIdAsString + "]");
        }

        String syntaxId = matcher.group(1);
        String version = matcher.group(2);

        // For well-known syntaxes, get the Syntax Name from the registered SyntaxType, otherwise use the id as both
        // the human readable name and the technical id (since the syntax string doesn't contain any information about
        // the pretty name of a syntax type).
        SyntaxType syntaxType = SyntaxType.getSyntaxTypes().get(syntaxId);
        if (syntaxType == null) {
            syntaxType = new SyntaxType(syntaxId, syntaxId);
        }

        return new Syntax(syntaxType, version);
    }

    /**
     * @return the type of the syntax
     */
    public SyntaxType getType()
    {
        return this.type;
    }

    /**
     * @return the specific version of the syntax
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * @return a qualifier
     */
    public String getQualifier()
    {
        return this.qualifier;
    }

    /**
     * @return a unique String identifier, does not contain display name and qualifier. Usable when searching for
     *         parsers and renderers components for example.
     */
    public String toIdString()
    {
        if (this.idStringCache == null) {
            this.idStringCache = getType().getId() + "/" + getVersion().toLowerCase();
        }

        return this.idStringCache;
    }

    @Override
    public String toString()
    {
        return getType().toString() + " " + getVersion() + (getQualifier() != null ? " (" + getQualifier() + ")" : "");
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(5, 7).append(getType()).append(getVersion()).append(getQualifier()).toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null && object.getClass() != getClass()) {
            return false;
        }
        if (object == this) {
            return true;
        }
        Syntax rhs = (Syntax) object;
        return new EqualsBuilder().append(getType(), rhs.getType()).append(getVersion(), rhs.getVersion())
            .append(getQualifier(), rhs.getQualifier()).isEquals();
    }

    @Override
    public int compareTo(Syntax syntax)
    {
        return new CompareToBuilder().append(getType(), syntax.getType())
            // TODO: Add a real version parser to compare the versions
            .append(getVersion(), syntax.getVersion()).toComparison();
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.text.StringUtils;

/**
 * A syntax type is made of three parts:
 * <ul>
 *   <li>a base syntax type (e.g. {@code xwiki}, {@code confluence}, {@code mediawiki}, etc).</li>
 *   <li>zero or more variants, which represent Syntax type variations. For example the {@code markdown} syntax has
 *       the {@code commonmark} variant and the {@code github} variant.</li>
 *   <li>a human-readable name (e.g. {@code XWiki}, {@code Confluence}, {@code MediaWiki}).</li>
 * </ul>
 * The syntax type string format is: {@code <base type>[+<variant>]*}.
 * Examples:
 * <ul>
 *   <li>{@code xwiki}</li>
 *   <li>{@code markdown+commonmark}</li>
 *   <li>{@code sometype+variant1+...+variantN}</li>
 * </ul>
 *
 * @version $Id$
 * @since 2.0RC1
 */
public class SyntaxType implements Comparable<SyntaxType>
{
    /**
     * Well-known Syntax types.
     */
    private static final Map<String, SyntaxType> KNOWN_SYNTAX_TYPES = new HashMap<>();

    /**
     * XWiki wiki syntax.
     */
    public static final SyntaxType XWIKI = register("xwiki", "XWiki");

    /**
     * Confluence wiki syntax.
     */
    public static final SyntaxType CONFLUENCE = register("confluence", "Confluence");

    /**
     * Confluence XHTML based syntax.
     *
     * @since 5.3M1
     */
    public static final SyntaxType CONFLUENCEXHTML = register("confluence", Arrays.asList("xhtml"),
        "Confluence (XHTML)");

    /**
     * MediaWiki wiki syntax.
     */
    public static final SyntaxType MEDIAWIKI = register("mediawiki", "MediaWiki");

    /**
     * DokuWiki wiki syntax.
     * 
     * @since 9.8RC1
     */
    public static final SyntaxType DOKUWIKI = new SyntaxType("dokuwiki", "DokuWiki");

    /**
     * Creole wiki syntax.
     */
    public static final SyntaxType CREOLE = register("creole", "Creole");

    /**
     * JSPWiki wiki syntax.
     */
    public static final SyntaxType JSPWIKI = register("jspwiki", "JSPWiki");

    /**
     * TWiki wiki syntax.
     */
    public static final SyntaxType TWIKI = register("twiki", "TWiki");

    /**
     * XHTML syntax.
     */
    public static final SyntaxType XHTML = register("xhtml", "XHTML");

    /**
     * Annotated XHTML syntax.
     */
    public static final SyntaxType ANNOTATED_XHTML = register("annotatedxhtml", "Annotated XHTML");

    /**
     * Annotated HTML syntax.
     */
    public static final SyntaxType ANNOTATED_HTML = register("annotatedhtml", "Annotated HTML");

    /**
     * HTML syntaxes.
     */
    public static final SyntaxType HTML = register("html", "HTML");

    /**
     * Plain text syntax.
     */
    public static final SyntaxType PLAIN = register("plain", "Plain");

    /**
     * Events syntax.
     */
    public static final SyntaxType EVENT = register("event", "Event");

    /**
     * TEX syntax.
     */
    public static final SyntaxType TEX = register("tex", "TeX");

    /**
     * DoxBook syntax.
     */
    public static final SyntaxType DOCBOOK = register("docbook", "DocBook");

    /**
     * XML based XWiki DOM syntax.
     * 
     * @since 3.3M1
     */
    public static final SyntaxType XDOMXML = register("xdom", Arrays.asList("xml"), "XDOM (XML)");

    /**
     * MarkDown wiki syntax.
     * 
     * @since 3.4M1
     */
    public static final SyntaxType MARKDOWN = register("markdown", "Markdown");

    /**
     * APT syntax.
     * 
     * @since 4.3M1
     */
    public static final SyntaxType APT = register("apt", "APT");

    /**
     * @see #getName()
     */
    private String name;

    /**
     * @see #getId()
     */
    private String id;

    /**
     * @see #getVariants()
     */
    private List<String> variants;

    private transient volatile String idStringCache;

    /**
     * @param id the technical id of the Syntax type (ex "annotatedxhtml")
     * @param name the human readable name of the Syntax type (ex "Annotated XHTML")
     * @since 2.0M3
     */
    public SyntaxType(String id, String name)
    {
        this(id, Collections.emptyList(), name);
    }

    /**
     * @param id the technical id of the Syntax type (ex "annotatedxhtml")
     * @param name the human readable name of the Syntax type (ex "Annotated XHTML")
     * @param variants the variants (can be empty or null)
     * @since 13.0
     * @since 12.10.1
     */
    public SyntaxType(String id, List<String> variants, String name)
    {
        this.id = id;
        // Copy the variants to avoid someone messing with them from the outside.
        if (variants == null || variants.isEmpty()) {
            this.variants = Collections.emptyList();
        } else {
            this.variants = new ArrayList<>(variants);
        }
        this.name = name;
    }

    /**
     * Register a Syntax Type.
     *
     * @param id see {@link SyntaxType#getId()}
     * @param name see {@link SyntaxType#getName()}
     * @return the created Syntax Type object
     */
    private static SyntaxType register(String id, String name)
    {
        return register(id, Collections.emptyList(), name);
    }

    /**
     * Register a Syntax Type.
     *
     * @param id see {@link SyntaxType#getId()}
     * @param variants the variants (can be empty)
     * @param name see {@link SyntaxType#getName()}
     * @return the created Syntax Type object
     */
    private static SyntaxType register(String id, List<String> variants, String name)
    {
        SyntaxType syntaxType = new SyntaxType(id, variants, name);
        KNOWN_SYNTAX_TYPES.put(computeIdString(id, variants), syntaxType);
        return syntaxType;
    }

    /**
     * @return the well-known Syntax types
     * @deprecated since 13.3RC1, use {@link SyntaxRegistry#getSyntaxes()}
     */
    @Deprecated
    public static Map<String, SyntaxType> getSyntaxTypes()
    {
        return KNOWN_SYNTAX_TYPES;
    }

    /**
     * @return the technical id of the Syntax type (ex "annotatedxhtml")
     * @since 2.0M3
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the variants (can never be null but can be empty)
     * @since 13.0
     * @since 12.10.1
     */
    public List<String> getVariants()
    {
        // Handle backward-compatibility for serialization (i.e. it's possible that the variants field is null when
        // unserialized).
        return this.variants == null ? Collections.emptyList() : this.variants;
    }

    /**
     * @return the human readable name of the Syntax type (ex "Annotated XHTML")
     * @since 2.0M3
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return a unique String identifier, does not contain the display name. Usable when searching for parsers and
     *         renderers components for example.
     * @since 13.0
     * @since 12.10.1
     */
    public String toIdString()
    {
        if (this.idStringCache == null) {
            this.idStringCache = computeIdString(getId(), getVariants());
        }
        return this.idStringCache;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Display a human readable name of the Syntax type.
     * </p>
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(7, 7)
            .append(getId())
            .append(getVariants())
            .toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == this) {
            return true;
        }
        if (!(object instanceof SyntaxType)) {
            return false;
        }
        SyntaxType rhs = (SyntaxType) object;
        return new EqualsBuilder()
            .append(getId(), rhs.getId())
            .append(getVariants(), rhs.getVariants())
            .isEquals();
    }

    @Override
    public int compareTo(SyntaxType syntaxType)
    {
        return new CompareToBuilder()
            .append(getName(), syntaxType.getName())
            .toComparison();
    }

    /**
     * @param syntaxTypesString the syntax type as a string (eg {@code xwiki}, {@code confluence+xhtml})
     * @return the parsed syntax type  as a SyntaxType object
     * @throws ParseException in case the string doesn't represent a valid syntax type
     * @since 13.0
     * @since 12.10.1
     */
    public static SyntaxType valueOf(String syntaxTypesString) throws ParseException
    {
        if (syntaxTypesString == null) {
            throw new ParseException("The passed Syntax type cannot be NULL");
        }

        String[] tokens = StringUtils.split(syntaxTypesString, '+');
        String id = tokens[0];
        List<String> variants = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            variants.add(tokens[i]);
        }

        // For well-known syntax types, get the Syntax name from the registered Syntax types, otherwise use the id as
        // both the human readable name and the technical id (since the syntax type string doesn't contain any
        // information about the pretty name of a syntax type).
        String idString = computeIdString(id, variants);
        SyntaxType syntaxType = SyntaxType.getSyntaxTypes().get(idString);
        if (syntaxType == null) {
            // Use the full id string (id + variant) for the name. We cannot do better since the information is not in
            // the String representation of a syntax.
            syntaxType = new SyntaxType(id, variants, idString);
        }

        return syntaxType;
    }

    private static String computeIdString(String id, List<String> variants)
    {
        StringBuilder idString = new StringBuilder();
        idString.append(id);
        for (String variant : variants) {
            idString.append('+').append(variant);
        }
        return idString.toString();
    }
}

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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;

/**
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
    public static final SyntaxType CONFLUENCEXHTML = register("confluence+xhtml", CONFLUENCE.getName());

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
    public static final SyntaxType XDOMXML = register("xdom+xml", "XML based XDOM");

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
     * @param id the technical id of the Syntax type (ex "annotatedxhtml")
     * @param name the human readable name of the Syntax type (ex "Annotated XHTML")
     * @since 2.0M3
     */
    public SyntaxType(String id, String name)
    {
        this.name = name;
        this.id = id;
    }

    /**
     * Register a Syntax Type.
     *
     * @param id see {@link SyntaxType#SyntaxType(String, String)}
     * @param name see {@link SyntaxType#SyntaxType(String, String)}
     * @return the created Syntax Type object
     */
    private static SyntaxType register(String id, String name)
    {
        SyntaxType syntaxType = new SyntaxType(id, name);
        KNOWN_SYNTAX_TYPES.put(id, syntaxType);
        return syntaxType;
    }

    /**
     * @return the well-known Syntax types
     */
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
     * @return the human readable name of the Syntax type (ex "Annotated XHTML")
     * @since 2.0M3
     */
    public String getName()
    {
        return this.name;
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
        return this.name;
    }

    @Override
    public int hashCode()
    {
        // Random number. See http://www.technofundo.com/tech/java/equalhash.html for the detail of this
        // algorithm.
        // Note that the name isn't part of the hashCode computation since it's not part of the Syntax type's identity
        int hash = 7;
        hash = 31 * hash + (null == getId() ? 0 : getId().hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean result;

        // See http://www.technofundo.com/tech/java/equalhash.html for the detail of this algorithm.
        if (this == object) {
            result = true;
        } else {
            if ((object == null) || (object.getClass() != this.getClass())) {
                result = false;
            } else {
                // Object must be Syntax at this point.
                SyntaxType syntaxType = (SyntaxType) object;
                // Note that the name isn't part of the hashCode computation since it's not part of the Syntax type's
                // identity.
                result = (getId() == syntaxType.getId() || getId() != null && getId().equals(syntaxType.getId()));
            }
        }

        return result;
    }

    @Override
    public int compareTo(SyntaxType syntaxType)
    {
        return new CompareToBuilder().append(getName(), syntaxType.getName()).toComparison();
    }
}

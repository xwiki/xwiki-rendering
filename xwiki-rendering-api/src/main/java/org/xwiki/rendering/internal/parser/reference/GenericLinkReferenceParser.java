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
package org.xwiki.rendering.internal.parser.reference;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.InterWikiResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.ResourceReferenceTypeParser;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Each syntax should have its own resource reference parser. However while we wait for syntax specific parser to be
 * implemented this generic parser should provide a good approximation.
 * 
 * @version $Id$
 * @since 2.5RC1
 */
@Component
@Named("default/link")
@Singleton
public class GenericLinkReferenceParser implements ResourceReferenceParser
{
    /**
     * Interwiki separator.
     */
    public static final String SEPARATOR_INTERWIKI = "@";

    /**
     * Query String separator.
     */
    public static final String SEPARATOR_QUERYSTRING = "?";

    /**
     * Anchor separator.
     */
    public static final String SEPARATOR_ANCHOR = "#";

    /**
     * Escape character to allow "#", "@" and "?" characters in a reference's name.
     */
    public static final char ESCAPE_CHAR = '\\';

    /**
     * Escapes to remove from the document reference part when parsing the raw reference (i.e. excluding query string,
     * anchor and interwiki parts). Note that we don't remove the escaped escape char since this is how an escape char
     * is represented in an Entity Reference.
     */
    private static final String[] ESCAPES_REFERENCE = new String[] {ESCAPE_CHAR + SEPARATOR_QUERYSTRING,
        ESCAPE_CHAR + SEPARATOR_INTERWIKI, ESCAPE_CHAR + SEPARATOR_ANCHOR};

    /**
     * Escapes to remove from the query string, anchor and interwiki parts when parsing the raw reference.
     */
    private static final String[] ESCAPES_EXTRA = new String[] {ESCAPE_CHAR + SEPARATOR_QUERYSTRING,
        ESCAPE_CHAR + SEPARATOR_INTERWIKI, ESCAPE_CHAR + SEPARATOR_ANCHOR, "" + ESCAPE_CHAR + ESCAPE_CHAR};

    /**
     * Escapes to remove the interwiki content.
     */
    private static final String[] ESCAPE_INTERWIKI = new String[] {"" + ESCAPE_CHAR + ESCAPE_CHAR, "" + ESCAPE_CHAR};

    /**
     * Replacement chars for the escapes to be removed from the reference part.
     */
    private static final String[] ESCAPE_REPLACEMENTS_REFERENCE = new String[] {SEPARATOR_QUERYSTRING,
        SEPARATOR_INTERWIKI, SEPARATOR_ANCHOR};

    /**
     * Replacement chars for the escapes to be removed from the query string, anchor and interwiki parts.
     */
    private static final String[] ESCAPE_REPLACEMENTS_EXTRA = new String[] {SEPARATOR_QUERYSTRING, SEPARATOR_INTERWIKI,
        SEPARATOR_ANCHOR, "" + ESCAPE_CHAR};

    /**
     * Replacements chars for the escapes to be removed from the interwiki content.
     */
    private static final String[] ESCAPE_REPLACEMENTS_INTERWIKI = new String[] {"" + ESCAPE_CHAR, ""};

    /**
     * The list of recognized URL prefixes.
     */
    private static final List<String> URI_PREFIXES = Arrays.asList("mailto");

    /**
     * Parser to parse link references pointing to URLs.
     */
    @Inject
    @Named("url")
    private ResourceReferenceTypeParser urlResourceReferenceTypeParser;

    /**
     * Used to verify if we're in wiki mode or not by looking up an implementation of
     * {@link org.xwiki.rendering.wiki.WikiModel}.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * @return the list of URI prefixes the link parser recognizes
     */
    protected List<String> getAllowedURIPrefixes()
    {
        return URI_PREFIXES;
    }

    @Override
    public ResourceReference parse(String rawReference)
    {
        // Step 1: If we're not in wiki mode then all links are URL links.
        if (!isInWikiMode()) {
            ResourceReference resourceReference = new ResourceReference(rawReference, ResourceType.URL);
            resourceReference.setTyped(false);
            return resourceReference;
        }

        // Step 2: Check if it's a known URI by looking for one of the known URI schemes. If not, check if it's a URL.
        ResourceReference resourceReference = parseURILinks(rawReference);
        if (resourceReference != null) {
            return resourceReference;
        }

        // Step 3: Look for an InterWiki link
        StringBuffer content = new StringBuffer(rawReference);
        resourceReference = parseInterWikiLinks(content);
        if (resourceReference != null) {
            return resourceReference;
        }

        // Step 4: Consider that we have a reference to a document.
        return parseDocumentLink(content);
    }

    /**
     * Construct a Document Link reference out of the passed content.
     * 
     * @param content the string containing the Document link reference
     * @return the parsed Link Object corresponding to the Document link reference
     */
    private ResourceReference parseDocumentLink(StringBuffer content)
    {
        String queryString = null;
        String text = parseElementAfterString(content, SEPARATOR_QUERYSTRING);
        if (text != null) {
            queryString = removeEscapesFromExtraParts(text);
        }

        String anchor = null;
        text = parseElementAfterString(content, SEPARATOR_ANCHOR);
        if (text != null) {
            anchor = removeEscapesFromExtraParts(text);
        }

        DocumentResourceReference reference =
            new DocumentResourceReference(removeEscapesFromReferencePart(content.toString()));
        reference.setTyped(false);
        reference.setQueryString(queryString);
        reference.setAnchor(anchor);

        return reference;
    }

    /**
     * Check if the passed link references is an URI link reference.
     * 
     * @param rawLink the original reference to parse
     * @return the parsed Link object or null if the passed reference is not an URI link reference or if no URI type
     *         parser was found for the passed URI scheme
     */
    private ResourceReference parseURILinks(String rawLink)
    {
        ResourceReference result = null;
        int uriSchemeDelimiterPos = rawLink.indexOf(":");
        if (uriSchemeDelimiterPos > -1) {
            String scheme = rawLink.substring(0, uriSchemeDelimiterPos);
            String reference = rawLink.substring(uriSchemeDelimiterPos + 1);
            if (getAllowedURIPrefixes().contains(scheme)) {
                try {
                    ResourceReferenceTypeParser parser =
                        this.componentManagerProvider.get().getInstance(ResourceReferenceTypeParser.class, scheme);
                    ResourceReference resourceReference = parser.parse(reference);
                    if (resourceReference != null) {
                        result = resourceReference;
                    }
                } catch (ComponentLookupException e) {
                    // Failed to lookup component, this shouldn't happen but ignore it.
                }
            } else {
                // Check if it's a URL
                ResourceReference resourceReference = this.urlResourceReferenceTypeParser.parse(rawLink);
                if (resourceReference != null) {
                    resourceReference.setTyped(false);
                    result = resourceReference;
                }
            }
        }
        return result;
    }

    /**
     * Check if the passed link references is an interwiki link reference.
     * 
     * @param content the original content to parse
     * @return the parsed Link object or null if the passed reference is not an interwiki link reference
     */
    private ResourceReference parseInterWikiLinks(StringBuffer content)
    {
        ResourceReference result = null;
        String interWikiAlias = parseElementAfterString(content, SEPARATOR_INTERWIKI);
        if (interWikiAlias != null) {
            InterWikiResourceReference link = new InterWikiResourceReference(removeEscapes(content.toString()));
            link.setInterWikiAlias(removeEscapes(interWikiAlias));
            result = link;
        }
        return result;
    }

    /**
     * Find out the element located to the right of the passed separator.
     * 
     * @param content the string to parse. This parameter will be modified by the method to remove the parsed content.
     * @param separator the separator string to locate the element
     * @return the parsed element or null if the separator string wasn't found
     */
    protected String parseElementAfterString(StringBuffer content, String separator)
    {
        String element = null;

        // Find the first non escaped separator (starting from the end of the content buffer).
        int index = content.lastIndexOf(separator);
        while (index != -1) {
            // Check if the element is found and it's not escaped.
            if (!shouldEscape(content, index)) {
                element = content.substring(index + separator.length()).trim();
                content.delete(index, content.length());
                break;
            }

            if (index > 0) {
                index = content.lastIndexOf(separator, index - 1);
            } else {
                break;
            }
        }

        return element;
    }

    /**
     * @return true if we're in wiki mode (ie there's no implementing class for
     *         {@link org.xwiki.rendering.wiki.WikiModel})
     */
    private boolean isInWikiMode()
    {
        boolean result = true;
        try {
            this.componentManagerProvider.get().getInstance(WikiModel.class);
        } catch (ComponentLookupException e) {
            result = false;
        }
        return result;
    }

    /**
     * Count the number of escape chars before a given character and if that number is odd then that character should be
     * escaped.
     * 
     * @param content the content in which to check for escapes
     * @param charPosition the position of the char for which to decide if it should be escaped or not
     * @return true if the character should be escaped
     */
    private boolean shouldEscape(StringBuffer content, int charPosition)
    {
        int counter = 0;
        int pos = charPosition - 1;
        while (pos > -1 && content.charAt(pos) == ESCAPE_CHAR) {
            counter++;
            pos--;
        }
        return (counter % 2 != 0);
    }

    /**
     * @param text the reference from which to remove unneeded escapes
     * @return the cleaned text
     */
    private String removeEscapesFromReferencePart(String text)
    {
        return StringUtils.replaceEach(text, ESCAPES_REFERENCE, ESCAPE_REPLACEMENTS_REFERENCE);
    }

    /**
     * @param text the reference from which to remove unneeded escapes
     * @return the cleaned text
     */
    private String removeEscapesFromExtraParts(String text)
    {
        return StringUtils.replaceEach(text, ESCAPES_EXTRA, ESCAPE_REPLACEMENTS_EXTRA);
    }

    /**
     * @param text the reference from which to remove unneeded escapes
     * @return the cleaned text
     */
    private String removeEscapes(String text)
    {
        return StringUtils.replaceEach(text, ESCAPE_INTERWIKI, ESCAPE_REPLACEMENTS_INTERWIKI);
    }
}

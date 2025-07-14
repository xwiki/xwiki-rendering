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
package org.xwiki.rendering.renderer.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xwiki.xml.html.HTMLElementSanitizer;

/**
 * Base toolkit class for all XHTML-based renderers. This printer handles whitespaces so that it prints "&nbsp;" when
 * needed (i.e. when the spaces are at the beginning or at the end of an element's content or when there are more than 1
 * contiguous spaces, except for CDATA sections and inside PRE elements. It also knows how to handle XHTML comments).
 *
 * @version $Id$
 * @since 1.7M1
 */
public class XHTMLWikiPrinter extends XMLWikiPrinter
{
    /**
     * Prefix that is used for invalid/disallowed attributes.
     *
     * @since 14.6RC1
     */
    public static final String TRANSLATED_ATTRIBUTE_PREFIX = "data-xwiki-translated-attribute-";

    /**
     * Pattern for matching characters not allowed in data attributes.
     * <p>
     * This is the inverse of the definition of a name being
     * <a href="https://html.spec.whatwg.org/multipage/infrastructure.html#xml-compatible>XML-compatible</a>,
     * i.e., matching the <a href="https://www.w3.org/TR/xml/#NT-Name">Name production</a> without ":".
     */
    private static final Pattern DATA_REPLACEMENT_PATTERN = Pattern.compile("[^A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6"
        + "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
        + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\x{10000}-\\x{EFFFF}\\-.0-9\\u00b7\\u0300-\\u036f\\u203f-\\u2040]");

    // Precomputed forbidden suffixes/prefixes to prevent the injection of opening or closing HTML macro syntaxes at
    // the end of raw content.
    private static final List<String> FORBIDDEN_RAW_SUFFIX_PREFIXES = computeForbiddenRawSuffixPrefixes();

    // Forbidden substrings to prevent the injection of opening and closing HTML macro syntaxes in raw content to
    // ensure that rendering output can be used safely in HTML macros.
    private static final String[] FORBIDDEN_RAW_STRINGS = new String[] { "{{html", "{{/html" };

    private static final String[] FORBIDDEN_RAW_REPLACEMENTS = new String[] { "&#123;&#123;html", "&#123;&#123;/html" };

    /**
     * The sanitizer used to restrict allowed elements and attributes, can be null (no restrictions).
     *
     * @since 14.6RC1
     */
    protected final HTMLElementSanitizer htmlElementSanitizer;

    private int spaceCount;

    private boolean isInCData;

    private boolean isInPreserveElement;

    private boolean elementEnded;

    private boolean hasTextBeenPrinted;

    private boolean isStandalone;

    /**
     * @param printer the object to which to write the XHTML output to
     */
    public XHTMLWikiPrinter(WikiPrinter printer)
    {
        this(printer, null);
    }

    /**
     * @param printer the object to which to write the XHTML output to
     * @param htmlElementSanitizer the sanitizer to use for sanitizing elements and attributes
     */
    public XHTMLWikiPrinter(WikiPrinter printer, HTMLElementSanitizer htmlElementSanitizer)
    {
        super(printer);
        this.htmlElementSanitizer = htmlElementSanitizer;
    }

    /**
     * Use it to specify that the current element to print is standalone. This value might be used to know if the first
     * space should be printed with a simple space or a {@code &nbsp;} entity. Note that the standalone value is
     * automatically reset after first printing of a space, or when a text is printed.
     *
     * @since 12.2
     */
    public void setStandalone()
    {
        this.isStandalone = true;
    }

    @Override
    public void printXML(String str)
    {
        handleSpaceWhenInText();
        super.printXML(str);
        this.hasTextBeenPrinted = true;
        this.isStandalone = false;
    }

    @Override
    public void printXMLElement(String name)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLElement(name);
        }
    }

    @Override
    public void printXMLElement(String name, String[][] attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLElement(String name, Map<String, String> attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLStartElement(String name)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name);
        }
    }

    @Override
    public void printXMLStartElement(String name, String[][] attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLStartElement(String name, Map<String, String> attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLStartElement(String name, Attributes attributes)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenStartElement();
            super.printXMLStartElement(name, cleanAttributes(name, attributes));
        }
    }

    @Override
    public void printXMLEndElement(String name)
    {
        if (this.htmlElementSanitizer == null || this.htmlElementSanitizer.isElementAllowed(name)) {
            handleSpaceWhenEndlement();
            super.printXMLEndElement(name);
            this.elementEnded = true;
        }
    }

    @Override
    public void printXMLComment(String content)
    {
        printXMLComment(content, false);
    }

    @Override
    public void printXMLComment(String content, boolean escape)
    {
        handleSpaceWhenStartElement();
        super.printXMLComment(content, escape);
        this.elementEnded = true;
    }

    @Override
    public void printXMLStartCData()
    {
        handleSpaceWhenStartElement();
        super.printXMLStartCData();
    }

    @Override
    public void printXMLEndCData()
    {
        handleSpaceWhenEndlement();
        super.printXMLEndCData();
    }

    /**
     * This method should be used to print a space rather than calling <code>printXML(" ")</code>.
     */
    public void printSpace()
    {
        this.spaceCount++;
    }

    @Override
    public void printRaw(String raw)
    {
        handleSpaceWhenStartElement();
        // Prevent injecting {{/html}}. As there can be an arbitrary number of spaces before the }}, we actually
        // escape {{/html. We escape {{/html as well as prefixes of {/html and {html at the end of the raw content to
        // avoid that raw content and plain texts can be combined to construct the full {{/html}} or {{html}}. This may
        // cause errors as we might not be using the right escaping for the context (e.g., JSON or HTML comments), but
        // for this reason we also escape { in JSON output and HTML comments.
        String escapedRaw = StringUtils.replaceEach(raw, FORBIDDEN_RAW_STRINGS, FORBIDDEN_RAW_REPLACEMENTS);

        // Check all prefixes, they are pre-computed to ensure that this code is as efficient as possible in
        // particular in the very likely case that no such suffix actually exists.
        for (String prefix : FORBIDDEN_RAW_SUFFIX_PREFIXES) {
            if (escapedRaw.endsWith(prefix)) {
                escapedRaw =
                    escapedRaw.substring(0, escapedRaw.length() - prefix.length()) + "&#123;" + prefix.substring(1);
                break;
            }
        }
        super.printRaw(escapedRaw);
        this.elementEnded = true;
    }

    private static List<String> computeForbiddenRawSuffixPrefixes()
    {
        List<String> forbidden = new ArrayList<>(12);
        // Add the common { prefix separately such that we won't add it twice in the loop below.
        forbidden.add("{");
        for (String suffix : List.of("{/html", "{html")) {
            for (int i = 2; i <= suffix.length(); i++) {
                forbidden.add(suffix.substring(0, i));
            }
        }
        return forbidden;
    }

    private void handleSpaceWhenInText()
    {
        if (this.elementEnded || this.hasTextBeenPrinted) {
            handleSpaceWhenStartElement();
        } else {
            handleSpaceWhenEndlement();
        }
    }

    private Map<String, String> cleanAttributes(String elementName, Map<String, String> attributes)
    {
        Map<String, String> cleanAttributes;

        if (this.htmlElementSanitizer == null || attributes == null) {
            cleanAttributes = attributes;
        } else {
            cleanAttributes = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                if (this.htmlElementSanitizer.isAttributeAllowed(elementName, e.getKey(), e.getValue())) {
                    cleanAttributes.put(e.getKey(), e.getValue());
                } else {
                    // Keep but clean invalid attributes with a prefix (removed during parsing) to avoid loosing them
                    // through WYSIWYG editing.
                    String translatedName =
                        TRANSLATED_ATTRIBUTE_PREFIX + removeInvalidDataAttributeCharacters(e.getKey());
                    if (this.htmlElementSanitizer.isAttributeAllowed(elementName, translatedName, e.getValue())) {
                        cleanAttributes.put(translatedName, e.getValue());
                    }
                }
            }
        }

        return cleanAttributes;
    }

    private String[][] cleanAttributes(String elementName, String[][] attributes)
    {
        String[][] allowedAttributes;
        if (this.htmlElementSanitizer == null || attributes == null) {
            allowedAttributes = attributes;
        } else {
            allowedAttributes = Arrays.stream(attributes)
                .map(entry -> {
                    if (this.htmlElementSanitizer.isAttributeAllowed(elementName, entry[0], entry[1])) {
                        return entry;
                    } else {
                        // Keep but clean invalid attributes with a prefix (removed during parsing) to avoid loosing
                        // them through WYSIWYG editing.
                        String translatedName =
                            TRANSLATED_ATTRIBUTE_PREFIX + removeInvalidDataAttributeCharacters(entry[0]);
                        if (this.htmlElementSanitizer.isAttributeAllowed(elementName, translatedName, entry[1])) {
                            return new String[] { translatedName, entry[1] };
                        } else {
                            return null;
                        }
                    }
                })
                .filter(Objects::nonNull)
                .toArray(String[][]::new);
        }

        return allowedAttributes;
    }

    private Attributes cleanAttributes(String elementName, Attributes attributes)
    {
        Attributes allowedAttribute;

        if (this.htmlElementSanitizer == null || attributes == null) {
            allowedAttribute = attributes;
        } else {
            allowedAttribute = new AttributesImpl();

            for (int i = 0; i < attributes.getLength(); ++i) {
                if (this.htmlElementSanitizer.isAttributeAllowed(elementName, attributes.getQName(i),
                    attributes.getValue(i)))
                {
                    ((AttributesImpl) allowedAttribute).addAttribute(null, null, attributes.getQName(i),
                        null, attributes.getValue(i));
                } else {
                    // Keep but clean invalid attributes with a prefix (removed during parsing) to avoid loosing them
                    // through WYSIWYG editing.
                    String translatedName =
                        TRANSLATED_ATTRIBUTE_PREFIX + removeInvalidDataAttributeCharacters(attributes.getQName(i));
                    if (this.htmlElementSanitizer.isAttributeAllowed(elementName, translatedName,
                        attributes.getValue(i)))
                    {
                        ((AttributesImpl) allowedAttribute).addAttribute(null, null,
                            translatedName, null, attributes.getValue(i));
                    }
                }
            }
        }

        return allowedAttribute;
    }

    /**
     * Strips out invalid characters from names used for data attributes.
     *
     * @param name the data attribute name to clean
     * @return valid name, to be prefixed with data-
     */
    public static String removeInvalidDataAttributeCharacters(String name)
    {
        return DATA_REPLACEMENT_PATTERN.matcher(name).replaceAll("");
    }

    private void handleSpaceWhenStartElement()
    {
        // Use case: <tag1>something <tag2>...
        // Use case: <tag1>something <!--...
        if (this.spaceCount > 0) {
            if (!this.isInCData && !this.isInPreserveElement) {
                // We print a single space as a normal space, except if we are at the beginning of a standalone element
                // in that case we want a non-breaking space so it won't be stripped.
                // Any supplementary space will be printed as non-breaking spaces so we keep them too.
                if (this.isStandalone && !this.hasTextBeenPrinted) {
                    printEntity("&nbsp;");
                } else {
                    super.printXML(" ");
                }
                for (int i = 0; i < this.spaceCount - 1; i++) {
                    printEntity("&nbsp;");
                }
            } else {
                super.printXML(StringUtils.repeat(' ', this.spaceCount));
            }
            this.isStandalone = false;
        }
        this.spaceCount = 0;
        this.elementEnded = false;
        this.hasTextBeenPrinted = false;
    }

    private void handleSpaceWhenEndlement()
    {
        // Use case: <tag1>something </tag1>...
        // All spaces are &nbsp; spaces since otherwise they'll be all stripped by browsers
        if (!this.isInCData && !this.isInPreserveElement) {
            for (int i = 0; i < this.spaceCount; i++) {
                printEntity("&nbsp;");
            }
        } else {
            super.printXML(StringUtils.repeat(' ', this.spaceCount));
        }
        this.spaceCount = 0;
        this.elementEnded = false;
        this.hasTextBeenPrinted = false;
    }
}

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
package org.xwiki.rendering.internal.renderer.xwiki20.reference;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;
import org.xwiki.rendering.internal.parser.plain.PlainTextStreamParser;
import org.xwiki.rendering.internal.renderer.ParametersPrinter;
import org.xwiki.rendering.internal.renderer.xwiki20.XWikiSyntaxEscapeHandler;
import org.xwiki.rendering.internal.renderer.xwiki20.XWikiSyntaxEscapeWikiPrinter;
import org.xwiki.rendering.internal.renderer.xwiki20.XWikiSyntaxListenerChain;
import org.xwiki.rendering.listener.QueueListener.Event;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;

/**
 * Logic to render a Resource Reference into XWiki Syntax 2.0.
 *
 * @version $Id$
 * @since 2.0M3
 */
public class XWikiSyntaxResourceRenderer
{
    /**
     * Separator to use between the link reference and link parameters.
     */
    protected static final String PARAMETER_SEPARATOR = "||";

    protected static final ParametersPrinter PARAMETERS_PRINTER = new ParametersPrinter('~', "||", "]]", ">>");

    protected static final CharSequenceTranslator ESCAPE_LABEL = new LookupTranslator(Map.of(
        "~", "~~",
        "|", "~|",
        "[", "~[",
        "]", "~]",
        ">", "~>"
    ));

    private Deque<Boolean> forceFullSyntaxDeque = new ArrayDeque<Boolean>();

    private XWikiSyntaxListenerChain listenerChain;

    private ResourceReferenceSerializer referenceSerializer;

    /**
     * @since 2.5RC1
     */
    public XWikiSyntaxResourceRenderer(XWikiSyntaxListenerChain listenerChain,
        ResourceReferenceSerializer referenceSerializer)
    {
        this.listenerChain = listenerChain;
        this.referenceSerializer = referenceSerializer;
        this.forceFullSyntaxDeque.push(false);
    }

    /**
     * Serializes a resource reference into the string that represents it in XWiki Syntax.
     * <p>
     * A reference printed inside the full syntax is escaped so that it can neither end the reference early ("~", ">>",
     * "||" and "]]") nor close the macro it might be serialized in ("{{"). A free-standing reference cannot be escaped
     * at all, so it is returned as-is, which is why a reference that would need escaping isn't printed free-standing
     * in the first place.
     *
     * @param reference the reference to serialize
     * @param isFreeStanding {@code true} if the reference is printed without the surrounding {@code [[]]}, in which
     *     case no escaping is applied
     * @return the string representation of the reference
     */
    public String serialize(ResourceReference reference, boolean isFreeStanding)
    {
        String result = this.referenceSerializer.serialize(reference);

        if (!isFreeStanding) {
            result = XWikiSyntaxEscapeHandler.escapeCurlyBrackets(result.replace("~", "~~")
                .replace(">>", "~>~>")
                .replace(PARAMETER_SEPARATOR, "~|~|")
                .replace("]]", "~]~]"));
        }

        return result;
    }

    /**
     * Checks whether printing the reference as a free-standing reference could produce a "{{" in the output.
     * <p>
     * A "{{" is not rejected here because the reference wouldn't survive it. Quite the contrary: the image and the
     * attachment tokens of the parser accept every character but white space, so a free-standing "image:a{{b.png"
     * parses back to the very same reference. A free-standing URL is indeed truncated at the "{", as the URI token
     * accepts no "{", but that is the fate it shares with the many other characters the URI token doesn't accept
     * either - a "}" or a "," for instance - and that general limitation of free-standing URLs is not what this
     * method is about.
     * <p>
     * What makes a "{{" special is that the content of a macro is scanned for the macro's closing marker without any
     * regard for the construct that marker appears in. A "{{" printed inside a free-standing reference can thus close
     * the macro this reference is serialized in and break the structure of the whole document. Every other value that
     * the renderer prints as-is is escaped to prevent that, but a free-standing reference cannot be escaped, so the
     * full syntax is the only way out. As references containing a "{{" are rare, that is a small price to pay.
     *
     * @param reference the reference to check
     * @return {@code true} if printing the reference as a free-standing reference could produce a "{{" in the output
     */
    private boolean mayProduceMacroSyntax(ResourceReference reference)
    {
        if (reference == null) {
            return false;
        }

        String serializedReference = serialize(reference, true);

        // A leading "{" forms a "{{" with a "{" that has been printed just before the reference. Rather than
        // inspecting the output printed so far, always use the full syntax then - a free-standing reference starting
        // with a "{" is broken anyway.
        return serializedReference.contains("{{") || serializedReference.startsWith("{");
    }

    /**
     * Starts printing a link or an image by printing the {@code [[} that opens the full syntax, unless the reference
     * can be printed free-standing. Whether the full syntax is used is remembered until the matching
     * {@link #endRenderLink(XWikiSyntaxEscapeWikiPrinter, ResourceReference, boolean, Map)} call.
     *
     * @param printer the printer to print to
     * @param reference the reference that is going to be printed
     * @param freestanding {@code true} if the reference was written without the {@code [[]]} syntax in the first
     *     place, which is only a request as the full syntax can still be forced
     * @param parameters the parameters of the link or image
     * @since 16.10.19
     * @since 17.10.14
     * @since 18.4.6
     * @since 18.8.0RC1
     */
    public void beginRenderLink(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference reference,
        boolean freestanding, Map<String, String> parameters)
    {
        // find if the last printed char is part of a syntax (i.e. consumed by the parser before starting to parse the
        // link)
        boolean isLastSyntax = printer.getBuffer().isEmpty();

        printer.flush();

        if (forceFullSyntax(printer, reference, isLastSyntax, freestanding, parameters)) {
            this.forceFullSyntaxDeque.push(true);

            printer.print("[[");
        } else {
            this.forceFullSyntaxDeque.push(false);
        }
    }

    /**
     * Same as {@link #forceFullSyntax(XWikiSyntaxEscapeWikiPrinter, ResourceReference, boolean, boolean, Map)} with
     * {@code isLastSyntax} set to {@code true}.
     *
     * @param printer the printer the reference is going to be printed to
     * @param reference the reference that is going to be printed
     * @param freestanding {@code true} if the reference was written without the {@code [[]]} syntax
     * @param parameters the parameters of the link or image
     * @return {@code true} if the full {@code [[]]} syntax needs to be used
     * @since 16.10.19
     * @since 17.10.14
     * @since 18.4.6
     * @since 18.8.0RC1
     */
    public boolean forceFullSyntax(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference reference,
        boolean freestanding, Map<String, String> parameters)
    {
        return forceFullSyntax(printer, reference, true, freestanding, parameters);
    }

    /**
     * Checks whether the reference has to be printed with the full {@code [[]]} syntax, either because the reference
     * wasn't free-standing in the first place, or because printing it free-standing wouldn't produce a document that
     * parses back to the same content.
     *
     * @param printer the printer the reference is going to be printed to
     * @param reference the reference that is going to be printed
     * @param isLastSyntax {@code true} if the last character printed is part of a syntax construct, that is one
     *     consumed by the parser before it starts parsing the reference
     * @param freestanding {@code true} if the reference was written without the {@code [[]]} syntax
     * @param parameters the parameters of the link or image
     * @return {@code true} if the full {@code [[]]} syntax needs to be used
     * @since 16.10.19
     * @since 17.10.14
     * @since 18.4.6
     * @since 18.8.0RC1
     */
    public boolean forceFullSyntax(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference reference,
        boolean isLastSyntax, boolean freestanding, Map<String, String> parameters)
    {
        Event nextEvent = this.listenerChain.getLookaheadChainingListener().getNextEvent();

        // force full syntax if
        // 1: it's not a free standing URI
        // 2: there is parameters
        // 3: it follows a character which is not a white space (newline/space) and is not consumed by the parser (like
        // a another link)
        // 4: it's followed by a character which is not a white space (TODO: find a better way than this endless list of
        // EventType test but it probably need some big refactoring of the printer and XWikiSyntaxResourceRenderer)
        // 5: printing the reference free-standing could produce a "{{" in the output, which could close the macro
        // this reference is serialized in and which cannot be escaped in a free-standing reference
        return !freestanding
            || !parameters.isEmpty()
            || isNonWhiteSpaceAndConsumed(isLastSyntax, printer)
            || isNotAWhiteSpace(nextEvent)
            || mayProduceMacroSyntax(reference);
    }

    private boolean isNonWhiteSpaceAndConsumed(boolean isLastSyntax, XWikiSyntaxEscapeWikiPrinter printer)
    {
        return !isLastSyntax && !printer.isAfterWhiteSpace() && (!PlainTextStreamParser.SPECIAL_SYMBOLS.get(
            printer.getLastPrinted().charAt(printer.getLastPrinted().length() - 1)));
    }

    private boolean isNotAWhiteSpace(Event nextEvent)
    {
        return nextEvent != null && nextEvent.eventType != EventType.ON_SPACE
            && nextEvent.eventType != EventType.ON_NEW_LINE && nextEvent.eventType != EventType.END_PARAGRAPH
            && nextEvent.eventType != EventType.END_LINK && nextEvent.eventType != EventType.END_LIST_ITEM
            && nextEvent.eventType != EventType.END_DEFINITION_DESCRIPTION
            && nextEvent.eventType != EventType.END_DEFINITION_TERM
            && nextEvent.eventType != EventType.END_QUOTATION_LINE && nextEvent.eventType != EventType.END_SECTION;
    }

    public void renderLinkContent(XWikiSyntaxEscapeWikiPrinter printer, String label)
    {
        // If there was some link content specified then output the character separator ">>".
        if (!StringUtils.isEmpty(label)) {
            printer.print(ESCAPE_LABEL.translate(label));
            printer.print(">>");
        }
    }

    /**
     * Finishes printing a link or an image by printing its reference, its parameters and, when the full syntax is used,
     * the closing {@code ]]}.
     *
     * @param printer the printer to print to
     * @param reference the reference to print
     * @param freestanding {@code true} if the reference was written without the {@code [[]]} syntax in the first
     *     place; whether the full syntax is actually used has been decided by
     *     {@link #beginRenderLink(XWikiSyntaxEscapeWikiPrinter, ResourceReference, boolean, Map)}
     * @param parameters the parameters of the link or image
     */
    public void endRenderLink(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference reference,
        boolean freestanding, Map<String, String> parameters)
    {
        // The reference is only left unescaped when it is actually printed as a free-standing reference, as escaping
        // is not possible there. beginRenderLink() already took the "!freestanding" case into account when it pushed
        // this flag, so both decisions come from the very same place.
        boolean fullSyntax = this.forceFullSyntaxDeque.peek();

        printer.print(serialize(reference, !fullSyntax));

        // If there were parameters specified, print them
        printParameters(printer, reference, parameters);

        if (fullSyntax) {
            printer.print("]]");
        }

        this.forceFullSyntaxDeque.pop();
    }

    /**
     * Prints the parameters of a link or an image, separated from the reference by the parameter separator.
     *
     * @param printer the printer to print to
     * @param resourceReference the reference the parameters belong to
     * @param parameters the parameters to print
     */
    protected void printParameters(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference resourceReference,
        Map<String, String> parameters)
    {
        // If there were parameters specified, output them separated by the "||" characters
        if (!parameters.isEmpty()) {
            printer.print(PARAMETER_SEPARATOR);
            printer.print(XWikiSyntaxEscapeHandler.escapeCurlyBrackets(PARAMETERS_PRINTER.print(parameters)));
        }
    }
}

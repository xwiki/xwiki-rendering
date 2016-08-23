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
import org.xwiki.rendering.internal.parser.plain.PlainTextStreamParser;
import org.xwiki.rendering.internal.renderer.ParametersPrinter;
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

    private Deque<Boolean> forceFullSyntax = new ArrayDeque<Boolean>();

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
        this.forceFullSyntax.push(false);
    }

    public String serialize(ResourceReference reference, boolean isFreeStanding)
    {
        String result = this.referenceSerializer.serialize(reference);

        if (!isFreeStanding) {
            result = result.replace("~", "~~").replace(">>", "~>~>").replace(PARAMETER_SEPARATOR, "~|~|");
        }

        return result;
    }

    public void beginRenderLink(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference reference,
        boolean freestanding, Map<String, String> parameters)
    {
        // find if the last printed char is part of a syntax (i.e. consumed by the parser before starting to parse the
        // link)
        boolean isLastSyntax = printer.getBuffer().length() == 0;

        printer.flush();

        if (forceFullSyntax(printer, isLastSyntax, freestanding, parameters)) {
            this.forceFullSyntax.push(true);

            printer.print("[[");
        } else {
            this.forceFullSyntax.push(false);
        }
    }

    public boolean forceFullSyntax(XWikiSyntaxEscapeWikiPrinter printer, boolean freestanding,
        Map<String, String> parameters)
    {
        return forceFullSyntax(printer, true, freestanding, parameters);
    }

    public boolean forceFullSyntax(XWikiSyntaxEscapeWikiPrinter printer, boolean isLastSyntax,
        boolean freestanding, Map<String, String> parameters)
    {
        Event nextEvent = this.listenerChain.getLookaheadChainingListener().getNextEvent();

        // force full syntax if
        // 1: it's not a free standing URI
        // 2: there is parameters
        // 3: it follows a character which is not a white space (newline/space) and is not consumed by the parser (like
        // a another link)
        // 4: it's followed by a character which is not a white space (TODO: find a better way than this endless list of
        // EventType test but it probably need some big refactoring of the printer and XWikiSyntaxResourceRenderer)
        return !freestanding
            || !parameters.isEmpty()
            || (!isLastSyntax && !printer.isAfterWhiteSpace() && (!PlainTextStreamParser.SPECIALSYMBOL_PATTERN.matcher(
                String.valueOf(printer.getLastPrinted().charAt(printer.getLastPrinted().length() - 1))).matches()))
            || (nextEvent != null && nextEvent.eventType != EventType.ON_SPACE
                && nextEvent.eventType != EventType.ON_NEW_LINE && nextEvent.eventType != EventType.END_PARAGRAPH
                && nextEvent.eventType != EventType.END_LINK && nextEvent.eventType != EventType.END_LIST_ITEM
                && nextEvent.eventType != EventType.END_DEFINITION_DESCRIPTION
                && nextEvent.eventType != EventType.END_DEFINITION_TERM
                && nextEvent.eventType != EventType.END_QUOTATION_LINE && nextEvent.eventType != EventType.END_SECTION);
    }

    public void renderLinkContent(XWikiSyntaxEscapeWikiPrinter printer, String label)
    {
        // If there was some link content specified then output the character separator ">>".
        if (!StringUtils.isEmpty(label)) {
            printer.print(label);
            printer.print(">>");
        }
    }

    public void endRenderLink(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference reference,
        boolean freestanding, Map<String, String> parameters)
    {
        printer.print(serialize(reference, freestanding));

        // If there were parameters specified, print them
        printParameters(printer, reference, parameters);

        if (this.forceFullSyntax.peek() || !freestanding) {
            printer.print("]]");
        }

        this.forceFullSyntax.pop();
    }

    protected void printParameters(XWikiSyntaxEscapeWikiPrinter printer, ResourceReference resourceReference,
        Map<String, String> parameters)
    {
        // If there were parameters specified, output them separated by the "||" characters
        if (!parameters.isEmpty()) {
            printer.print(PARAMETER_SEPARATOR);
            printer.print(PARAMETERS_PRINTER.print(parameters));
        }
    }
}

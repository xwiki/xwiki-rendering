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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.internal.renderer.xwiki20.reference.XWikiSyntaxResourceRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.StackableChainingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.VoidWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;

/**
 * Convert listener events to XWiki Syntax 2.0 output.
 *
 * @version $Id$
 * @since 1.8RC1
 */
public class XWikiSyntaxChainingRenderer extends AbstractChainingPrintRenderer implements StackableChainingListener
{
    private XWikiSyntaxResourceRenderer linkResourceRenderer;

    private XWikiSyntaxResourceRenderer imageResourceRenderer;

    private XWikiSyntaxMacroRenderer macroPrinter;

    protected ResourceReferenceSerializer linkReferenceSerializer;

    protected ResourceReferenceSerializer imageReferenceSerializer;

    // Custom States

    private boolean isFirstElementRendered;

    private StringBuilder listStyle = new StringBuilder();

    private Map<String, String> previousFormatParameters;

    /**
     * @since 2.5RC1
     */
    public XWikiSyntaxChainingRenderer(ListenerChain listenerChain,
        ResourceReferenceSerializer linkReferenceSerializer, ResourceReferenceSerializer imageReferenceSerializer)
    {
        setListenerChain(listenerChain);

        this.linkReferenceSerializer = linkReferenceSerializer;
        this.imageReferenceSerializer = imageReferenceSerializer;
        this.linkResourceRenderer = createXWikiSyntaxLinkRenderer(getListenerChain(), linkReferenceSerializer);
        this.imageResourceRenderer = createXWikiSyntaxImageRenderer(getListenerChain(), imageReferenceSerializer);
        this.macroPrinter = new XWikiSyntaxMacroRenderer();
    }

    /**
     * @since 2.5RC1
     */
    protected XWikiSyntaxResourceRenderer createXWikiSyntaxLinkRenderer(ListenerChain listenerChain,
        ResourceReferenceSerializer linkReferenceSerializer)
    {
        return new XWikiSyntaxResourceRenderer((XWikiSyntaxListenerChain) listenerChain, linkReferenceSerializer);
    }

    /**
     * @since 2.5RC1
     */
    protected XWikiSyntaxResourceRenderer createXWikiSyntaxImageRenderer(ListenerChain listenerChain,
        ResourceReferenceSerializer imageReferenceSerializer)
    {
        return new XWikiSyntaxResourceRenderer((XWikiSyntaxListenerChain) listenerChain, imageReferenceSerializer);
    }

    // State

    private BlockStateChainingListener getBlockState()
    {
        return getXWikiSyntaxListenerChain().getBlockStateChainingListener();
    }

    @Override
    public StackableChainingListener createChainingListenerInstance()
    {
        XWikiSyntaxChainingRenderer renderer = new XWikiSyntaxChainingRenderer(getListenerChain(),
            this.linkReferenceSerializer, this.imageReferenceSerializer);
        renderer.setPrinter(getPrinter());
        return renderer;
    }

    private XWikiSyntaxListenerChain getXWikiSyntaxListenerChain()
    {
        return (XWikiSyntaxListenerChain) getListenerChain();
    }

    private XWikiSyntaxResourceRenderer getLinkRenderer()
    {
        return this.linkResourceRenderer;
    }

    private XWikiSyntaxResourceRenderer getImageRenderer()
    {
        return this.imageResourceRenderer;
    }

    private XWikiSyntaxMacroRenderer getMacroPrinter()
    {
        return this.macroPrinter;
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        if (!getBlockState().isInLine()) {
            printEmptyLine();
        }

        if (parameters.size() > 0) {
            printParameters(parameters, true);
        }

        print("(((");
        print("\n");

        // Create a new listener stack in order to preserve current states, to handle the group.
        getListenerChain().pushAllStackableListeners();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        // Ensure that all data in the escape printer have been flushed
        getXWikiPrinter().flush();
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        print("\n");
        print(")))");

        // Restore previous listeners that were stacked
        getListenerChain().popAllStackableListeners();
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // Flush test content before the link.
        // TODO: improve the block state renderer to be able to make the difference between what is bufferized
        // before the link and what in the label link
        getXWikiPrinter().setBeforeLink(true);
        // escape open link syntax when before a link
        if (getLinkRenderer().forceFullSyntax(getXWikiPrinter(), freestanding, parameters)
            && getXWikiPrinter().getBuffer().length() > 0
            && getXWikiPrinter().getBuffer().charAt(getXWikiPrinter().getBuffer().length() - 1) == '[') {
            getXWikiPrinter().setEscapeLastChar(true);
        }
        getXWikiPrinter().flush();
        getXWikiPrinter().setBeforeLink(false);

        int linkDepth = getBlockState().getLinkDepth();

        // If we are at a depth of 2 or greater it means we're in a link inside a link and in this case we
        // shouldn't output the nested link as a link unless it's a free standing link.
        if (linkDepth < 2) {
            getLinkRenderer().beginRenderLink(getXWikiPrinter(), freestanding, parameters);

            XWikiSyntaxEscapeWikiPrinter linkLabelPrinter =
                new XWikiSyntaxEscapeWikiPrinter(new DefaultWikiPrinter(), getXWikiSyntaxListenerChain());

            // Make sure the escape handler knows there is already characters before
            linkLabelPrinter.setOnNewLine(getXWikiPrinter().isOnNewLine());

            // Defer printing the link content since we need to gather all nested elements
            pushPrinter(linkLabelPrinter);
        } else if (freestanding) {
            print(getLinkRenderer().serialize(reference, freestanding));
        }
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // The links in a top level link label are not rendered as link (only the label is printed)
        if (getBlockState().getLinkDepth() == 1) {
            XWikiSyntaxEscapeWikiPrinter linkBlocksPrinter = getXWikiPrinter();
            linkBlocksPrinter.flush();
            String content = linkBlocksPrinter.toString();
            popPrinter();

            getLinkRenderer().renderLinkContent(getXWikiPrinter(), content);
            getLinkRenderer().endRenderLink(getXWikiPrinter(), reference, freestanding, parameters);
        }
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        // If the previous format had parameters and the parameters are different from the current ones then close them
        if (this.previousFormatParameters != null) {
            if (parameters.isEmpty()) {
                // this.previousFormatParameters = null;
            } else if (!this.previousFormatParameters.equals(parameters)) {
                this.previousFormatParameters = null;
                printParameters(parameters, false);
            } else {
                this.previousFormatParameters = null;
            }
        } else if (this.previousFormatParameters == null) {
            printParameters(parameters, false);
        }

        switch (format) {
            case BOLD:
                // Handle empty formatting parameters.
                if (this.previousFormatParameters != null) {
                    getPrinter().print("(%%)");
                    this.previousFormatParameters = null;
                }

                getXWikiPrinter().printBeginBold();
                break;
            case ITALIC:
                // Handle empty formatting parameters.
                if (this.previousFormatParameters != null) {
                    getPrinter().print("(%%)");
                    this.previousFormatParameters = null;
                }

                getXWikiPrinter().printBeginItalic();
                break;
            case STRIKEDOUT:
                print("--");
                break;
            case UNDERLINED:
                print("__");
                break;
            case SUPERSCRIPT:
                print("^^");
                break;
            case SUBSCRIPT:
                print(",,");
                break;
            case MONOSPACE:
                print("##");
                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        switch (format) {
            case BOLD:
                print("**");
                break;
            case ITALIC:
                getXWikiPrinter().printEndItalic();
                break;
            case STRIKEDOUT:
                print("--");
                break;
            case UNDERLINED:
                print("__");
                break;
            case SUPERSCRIPT:
                print("^^");
                break;
            case SUBSCRIPT:
                print(",,");
                break;
            case MONOSPACE:
                print("##");
                break;
            case NONE:
                break;
            default : // Unsupported format
                break;
        }
        if (!parameters.isEmpty()) {
            this.previousFormatParameters = parameters;
        }
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        printEmptyLine();
        printParameters(parameters);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        this.previousFormatParameters = null;

        // Ensure that any not printed characters are flushed.
        // TODO: Fix this better by introducing a state listener to handle escapes
        getXWikiPrinter().flush();
    }

    @Override
    public void onNewLine()
    {
        // - If we're inside a table cell, a paragraph, a list or a section header then if we have already outputted
        // a new line before then this new line should be a line break in order not to break the table cell,
        // paragraph, list or section header.

        // - If the new line is the last element of the paragraph, list or section header then it should be a line break
        // as otherwise it'll be considered as an empty line event next time the generated syntax is read by the XWiki
        // parser.

        if (getBlockState().isInLine()) {
            if (getXWikiSyntaxListenerChain().getConsecutiveNewLineStateChainingListener().getNewLineCount() > 1) {
                print("\\\\");
            } else if (getXWikiSyntaxListenerChain().getLookaheadChainingListener().getNextEvent().eventType
                .isInlineEnd()) {
                print("\\\\");
            } else {
                print("\n");
            }
        } else {
            print("\n");
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        if (!inline) {
            printEmptyLine();
            print(getMacroPrinter().renderMacro(id, parameters, content, inline));
        } else {
            getXWikiPrinter().printInlineMacro(getMacroPrinter().renderMacro(id, parameters, content, inline));
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        printEmptyLine();
        printParameters(parameters);
        print(StringUtils.repeat("=", level.getAsInt()) + " ");
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        print(" " + StringUtils.repeat("=", level.getAsInt()));
    }

    @Override
    public void onWord(String word)
    {
        printDelayed(word);
    }

    @Override
    public void onSpace()
    {
        printDelayed(" ");
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        printDelayed("" + symbol);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        // This need to be done in case of a subitem before endListItem() is not called
        this.previousFormatParameters = null;

        if (getBlockState().getListDepth() == 1) {
            printEmptyLine();
        } else {
            getPrinter().print("\n");
        }

        if (type == ListType.BULLETED) {
            this.listStyle.append("*");
        } else {
            this.listStyle.append("1");
        }
        printParameters(parameters);
    }

    @Override
    public void beginListItem()
    {
        if (getBlockState().getListItemIndex() > 0) {
            getPrinter().print("\n");
        }

        print(this.listStyle.toString());
        if (StringUtils.contains(this.listStyle.toString(), '1')) {
            print(".");
        }
        print(" ");
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        this.listStyle.setLength(this.listStyle.length() - 1);

        // Ensure that any not printed characters are flushed.
        // TODO: Fix this better by introducing a state listener to handle escapes
        getXWikiPrinter().flush();
    }

    @Override
    public void endListItem()
    {
        this.previousFormatParameters = null;
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        if (!isInline) {
            printEmptyLine();
        }

        // When we encounter a macro marker we ignore all other blocks inside since we're going to use the macro
        // definition wrapped by the macro marker to construct the xwiki syntax.
        pushPrinter(new XWikiSyntaxEscapeWikiPrinter(VoidWikiPrinter.VOIDWIKIPRINTER, getXWikiSyntaxListenerChain()));
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        this.previousFormatParameters = null;

        popPrinter();

        print(getMacroPrinter().renderMacro(name, parameters, content, isInline));
    }

    @Override
    public void onId(String name)
    {
        print("{{id name=\"" + name + "\"/}}");
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        printEmptyLine();
        printParameters(parameters);
        print("----");
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        if (!inline) {
            printEmptyLine();
        }
        printParameters(parameters);

        print("{{{");
        getXWikiPrinter().printVerbatimContent(content);
        print("}}}");
    }

    @Override
    public void onEmptyLines(int count)
    {
        print(StringUtils.repeat('\n', count));
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0RC1
     */
    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        if (getBlockState().getDefinitionListDepth() == 1 && !getBlockState().isInList()) {
            printEmptyLine();
        } else {
            print("\n");
        }
        printParameters(parameters);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6M2
     */
    @Override
    public void beginDefinitionTerm()
    {
        if (getBlockState().getDefinitionListItemIndex() > 0) {
            getPrinter().print("\n");
        }

        if (this.listStyle.length() > 0) {
            print(this.listStyle.toString());
            if (this.listStyle.charAt(0) == '1') {
                print(".");
            }
        }
        print(StringUtils.repeat(':', getBlockState().getDefinitionListDepth() - 1));
        print("; ");
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6M2
     */
    @Override
    public void beginDefinitionDescription()
    {
        if (getBlockState().getDefinitionListItemIndex() > 0) {
            getPrinter().print("\n");
        }

        if (this.listStyle.length() > 0) {
            print(this.listStyle.toString());
            if (this.listStyle.charAt(0) == '1') {
                print(".");
            }
        }
        print(StringUtils.repeat(':', getBlockState().getDefinitionListDepth() - 1));
        print(": ");
    }

    @Override
    public void endDefinitionDescription()
    {
        this.previousFormatParameters = null;

        getXWikiPrinter().flush();
    }

    @Override
    public void endDefinitionTerm()
    {
        this.previousFormatParameters = null;

        getXWikiPrinter().flush();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6M2
     */
    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        if (!getBlockState().isInQuotationLine()) {
            printEmptyLine();
        }

        if (!parameters.isEmpty()) {
            printParameters(parameters);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.6M2
     */
    @Override
    public void beginQuotationLine()
    {
        if (getBlockState().getQuotationLineIndex() > 0) {
            getPrinter().print("\n");
        }

        print(StringUtils.repeat('>', getBlockState().getQuotationDepth()));
    }

    @Override
    public void endQuotationLine()
    {
        this.previousFormatParameters = null;

        getXWikiPrinter().flush();
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        printEmptyLine();
        if (!parameters.isEmpty()) {
            printParameters(parameters);
        }
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        print("|");
        printParameters(parameters, false);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        print("|=");
        printParameters(parameters, false);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        if (getBlockState().getCellRow() > 0) {
            print("\n");
        }

        printParameters(parameters, false);
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        this.previousFormatParameters = null;

        // Ensure that any not printed characters are flushed.
        // TODO: Fix this better by introducing a state listener to handle escapes
        getXWikiPrinter().flush();
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        this.previousFormatParameters = null;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        getImageRenderer().beginRenderLink(getXWikiPrinter(), freestanding, parameters);
        getImageRenderer().endRenderLink(getXWikiPrinter(), reference, freestanding, parameters);
    }

    protected void printParameters(Map<String, String> parameters)
    {
        printParameters(parameters, true);
    }

    protected void printParameters(Map<String, String> parameters, boolean newLine)
    {
        StringBuffer parametersStr = new StringBuffer();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = entry.getValue();
            String key = entry.getKey();

            if (key != null && value != null) {
                // Escape quotes in value to not break parameter value syntax
                value = value.replaceAll("[~\"]", "~$0");
                // Escape ending custom parameters syntax
                value = value.replace("%)", "~%)");
                parametersStr.append(' ').append(key).append('=').append('\"').append(value).append('\"');
            }
        }

        if (parametersStr.length() > 0) {
            StringBuffer buffer = new StringBuffer("(%");
            buffer.append(parametersStr);
            buffer.append(" %)");

            if (newLine) {
                buffer.append("\n");
            }

            print(buffer.toString());
        }
    }

    private void printDelayed(String text)
    {
        print(text, true);
    }

    private void print(String text)
    {
        print(text, false);
    }

    private void print(String text, boolean isDelayed)
    {
        // Handle empty formatting parameters.
        if (this.previousFormatParameters != null) {
            getPrinter().print("(%%)");
            this.previousFormatParameters = null;
        }

        if (isDelayed) {
            getXWikiPrinter().printDelayed(text);
        } else {
            getPrinter().print(text);
        }
    }

    private void printEmptyLine()
    {
        if (this.isFirstElementRendered) {
            print("\n\n");
        } else {
            this.isFirstElementRendered = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0M3
     */
    @Override
    public void setPrinter(WikiPrinter printer)
    {
        // If the printer is already a XWiki Syntax Escape printer don't wrap it again. This case happens when
        // the createChainingListenerInstance() method is called, ie when this renderer's state is stacked
        // (for example when a Group event is being handled).
        if (printer instanceof XWikiSyntaxEscapeWikiPrinter) {
            super.setPrinter(printer);
        } else {
            super.setPrinter(new XWikiSyntaxEscapeWikiPrinter(printer, (XWikiSyntaxListenerChain) getListenerChain()));
        }
    }

    /**
     * Allows exposing the additional methods of {@link XWikiSyntaxEscapeWikiPrinter}, namely the ability to delay
     * printing some text and the ability to escape characters that would otherwise have a meaning in XWiki syntax.
     */
    public XWikiSyntaxEscapeWikiPrinter getXWikiPrinter()
    {
        return (XWikiSyntaxEscapeWikiPrinter) super.getPrinter();
    }

    @Override
    protected void popPrinter()
    {
        // Ensure that any not printed characters are flushed
        getXWikiPrinter().flush();

        super.popPrinter();
    }
}

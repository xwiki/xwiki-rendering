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
package org.xwiki.rendering.internal.parser.markdown;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.EmphNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrongNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.WikiLinkNode;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.CompositeListener;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.reference.link.URILabelGenerator;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;

/**
 * <a href="https://github.com/sirthias/pegdown">Pegdown</a> visitor to transform the Pegdown AST into XWiki Rendering
 * Events.
 *
 * @version $Id$
 * @since 4.4M1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XWikiPegdownVisitor implements PegdownVisitor
{
    /**
     * Used to generate unique header ids.
     */
    @Inject
    @Named("plain/1.0")
    protected PrintRendererFactory plainRendererFactory;

    /**
     * A plain text parser used to convert Pegdown TextNode elements to various Block elements since TextNode's values
     * contain several words and special characters which we thus need to break down into individual tokens.
     */
    @Inject
    @Named("plain/1.0")
    private StreamParser plainTextStreamParser;

    /**
     * We parse link references with the default reference parser (i.e. the same one used by XWiki Syntax 2.1).
     */
    @Inject
    @Named("link")
    private ResourceReferenceParser linkResourceReferenceParser;

    /**
     * We parse image references with the default reference parser (i.e. the same one used by XWiki Syntax 2.1).
     */
    @Inject
    @Named("image")
    private ResourceReferenceParser imageResourceReferenceParser;

    @Inject
    private ComponentManager componentManager;

    /**
     * Used to generate a unique id for Headings.
     */
    private IdGenerator idGenerator = new IdGenerator();

    /**
     * Listener(s) for the generated XWiki Events. Organized as a stack so that a buffering listener can hijack all
     * events for a while, for example. All generated events are sent to the top of the stack.
     */
    private Stack<Listener> listeners = new Stack<Listener>();

    private Stack<Boolean> isInTableHeaderStack = new Stack<Boolean>();

    /**
     * Link References.
     * @see #visit(org.pegdown.ast.ReferenceNode)
     */
    private Map<String, ReferenceNode> references = new HashMap<String, ReferenceNode>();

    /**
     * @return the top listener on the stack
     */
    private Listener getListener()
    {
        return this.listeners.peek();
    }

    @Override
    public void visit(SuperNode superNode, Listener listener)
    {
        SectionListener sectionListener = new SectionListener();
        sectionListener.setWrappedListener(listener);
        this.listeners.push(sectionListener);

        MetaData metaData = new MetaData(Collections.singletonMap(MetaData.SYNTAX, (Object) Syntax.MARKDOWN_1_0));
        getListener().beginDocument(metaData);
        superNode.accept(this);
        getListener().endDocument(metaData);
    }

    @Override
    public void visit(RootNode rootNode)
    {
        for (ReferenceNode ref : rootNode.getReferences()) {
            visit(ref);
        }

        // TODO: Add handling for AbbreviationNode
        //        for (AbbreviationNode abbr : rootNode.getAbbreviations()) {
        //            visitChildren(abbr);
        //        }
        visitChildren(rootNode);
    }

    /**
     * Helper method to visit all children nodes.
     *
     * @param node the node to visit
     */
    protected void visitChildren(Node node)
    {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(Node node)
    {
        // Not used.
    }

    @Override
    public void visit(ParaNode paraNode)
    {
        getListener().beginParagraph(Collections.EMPTY_MAP);
        visitChildren(paraNode);
        getListener().endParagraph(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(TextNode textNode)
    {
        visit(textNode.getText());
    }

    /**
     * @param text the text to parse and for which to return XWiki events
     */
    private void visit(String text)
    {
        try {
            WrappingListener inlineListener = new InlineFilterListener();
            inlineListener.setWrappedListener(getListener());
            this.plainTextStreamParser.parse(new StringReader(text), inlineListener);
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Error parsing content [%s]", text), e);
        }
    }

    @Override
    public void visit(SpecialTextNode specialTextNode)
    {
        visit(specialTextNode.getText());
    }

    @Override
    public void visit(StrongNode strongNode)
    {
        getListener().beginFormat(Format.BOLD, Collections.EMPTY_MAP);
        visitChildren(strongNode);
        getListener().endFormat(Format.BOLD, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(EmphNode emphNode)
    {
        getListener().beginFormat(Format.ITALIC, Collections.EMPTY_MAP);
        visitChildren(emphNode);
        getListener().endFormat(Format.ITALIC, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(BulletListNode bulletListNode)
    {
        getListener().beginList(ListType.BULLETED, Collections.EMPTY_MAP);
        visitChildren(bulletListNode);
        getListener().endList(ListType.BULLETED, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(ListItemNode listItemNode)
    {
        getListener().beginListItem();
        visitChildren(listItemNode);
        getListener().endListItem();
    }

    @Override
    public void visit(CodeNode codeNode)
    {
        // Since XWiki doesn't have a Code Block we generate a Code Macro Block
        getListener().onMacro("code", Collections.EMPTY_MAP, codeNode.getText(), true);
    }

    @Override
    public void visit(VerbatimNode verbatimNode)
    {
        String text = StringUtils.removeEnd(verbatimNode.getText(), "\n");

        Map<String, String> parameters;
        if (verbatimNode.getType().length() > 0) {
            parameters = Collections.singletonMap("language", verbatimNode.getType());
        } else {
            parameters = Collections.EMPTY_MAP;
        }

        getListener().onMacro("code", parameters, text, false);
    }

    @Override
    public void visit(HeaderNode headerNode)
    {
        // Heading needs to have an id generated from a plaintext representation of its content, so the header start
        // event will be sent at the end of the header, after reading the content inside and generating the id.
        // For this:
        // buffer all events in a queue until the header ends, and also send them to a print renderer to generate the ID
        CompositeListener composite = new CompositeListener();
        QueueListener queueListener = new QueueListener();
        composite.addListener(queueListener);
        PrintRenderer plainRenderer = this.plainRendererFactory.createRenderer(new DefaultWikiPrinter());
        composite.addListener(plainRenderer);

        // These 2 listeners will receive all events from now on until the header ends
        this.listeners.push(composite);

        visitChildren(headerNode);

        // Restore default listener
        this.listeners.pop();

        String id = this.idGenerator.generateUniqueId("H", plainRenderer.getPrinter().toString());

        HeaderLevel level = HeaderLevel.parseInt(headerNode.getLevel());
        getListener().beginHeader(level, id, Collections.EMPTY_MAP);

        // Send all buffered events to the 'default' listener
        queueListener.consumeEvents(getListener());

        getListener().endHeader(level, id, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(AbbreviationNode abbreviationNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(AutoLinkNode autoLinkNode)
    {
        ResourceReference reference = new ResourceReference(autoLinkNode.getText(), ResourceType.URL);
        reference.setTyped(false);
        getListener().beginLink(reference, true, Collections.EMPTY_MAP);
        getListener().endLink(reference, true, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(BlockQuoteNode blockQuoteNode)
    {
        getListener().beginQuotation(Collections.EMPTY_MAP);

        // XWiki only supports paragraph in quotations, see http://jira.xwiki.org/browse/XRENDERING-259.
        // We replace Paragraph events with QuotationLine events.
        QuoteListener quoteListener = new QuoteListener();
        quoteListener.setWrappedListener(getListener());
        this.listeners.push(quoteListener);
        visitChildren(blockQuoteNode);
        this.listeners.pop();
        quoteListener.closeOpenedQuotationLines();

        getListener().endQuotation(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(DefinitionListNode definitionListNode)
    {
        getListener().beginDefinitionList(Collections.EMPTY_MAP);
        visitChildren(definitionListNode);
        getListener().endDefinitionList(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(DefinitionNode definitionNode)
    {
        getListener().beginDefinitionDescription();

        DefinitionListListener listener = new DefinitionListListener();
        listener.setWrappedListener(getListener());
        this.listeners.push(listener);
        visitChildren(definitionNode);
        this.listeners.pop();

        getListener().endDefinitionDescription();
    }

    @Override
    public void visit(DefinitionTermNode definitionTermNode)
    {
        getListener().beginDefinitionTerm();
        visitChildren(definitionTermNode);
        getListener().endDefinitionTerm();
    }

    @Override
    public void visit(ExpImageNode expImageNode)
    {
        ResourceReference reference = this.imageResourceReferenceParser.parse(expImageNode.url);
        Map<String, String> parameters = new HashMap<String, String>();

        // Handle alt text. Note that in order to have the same behavior as the XWiki Syntax 2.0+ we don't add the alt
        // parameter if its content is the same as the one that would be automatically generated by the XHTML Renderer.
        String computedAltValue = computeAltAttributeValue(reference);
        String extractedAltValue = extractText(expImageNode);
        if (StringUtils.isNotEmpty(extractedAltValue) && !extractedAltValue.equals(computedAltValue)) {
            parameters.put("alt", extractedAltValue);
        }

        // Handle optional title
        addTitle(parameters, expImageNode.title);

        getListener().onImage(reference, false, parameters);
    }

    private String computeAltAttributeValue(ResourceReference reference)
    {
        String label;
        try {
            URILabelGenerator uriLabelGenerator = this.componentManager.getInstance(URILabelGenerator.class,
                    reference.getType().getScheme());
            label = uriLabelGenerator.generateLabel(reference);
        } catch (ComponentLookupException e) {
            label = reference.getReference();
        }
        return label;
    }

    @Override
    public void visit(ExpLinkNode expLinkNode)
    {
        ResourceReference reference = this.linkResourceReferenceParser.parse(expLinkNode.url);
        Map<String, String> parameters = new HashMap<String, String>();

        // Handle optional title
        addTitle(parameters, expLinkNode.title);

        getListener().beginLink(reference, false, parameters);
        visitChildren(expLinkNode);
        getListener().endLink(reference, false, parameters);
    }

    private void addTitle(Map<String, String> parameters, String title)
    {
        addParameter(parameters, "title", title);
    }

    private void addParameter(Map<String, String> parameters, String key, String value)
    {
        if (StringUtils.isNotEmpty(value)) {
            parameters.put(key, value);
        }
    }

    @Override
    public void visit(HtmlBlockNode htmlBlockNode)
    {
        getListener().onRawText(htmlBlockNode.getText(), Syntax.HTML_4_01);
    }

    @Override
    public void visit(InlineHtmlNode inlineHtmlNode)
    {
        getListener().onRawText(inlineHtmlNode.getText(), Syntax.HTML_4_01);
    }

    @Override
    public void visit(MailLinkNode mailLinkNode)
    {
        ResourceReference reference = new ResourceReference("mailto:" + mailLinkNode.getText(), ResourceType.MAILTO);
        reference.setTyped(false);
        getListener().beginLink(reference, true, Collections.EMPTY_MAP);
        getListener().endLink(reference, false, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(OrderedListNode orderedListNode)
    {
        getListener().beginList(ListType.NUMBERED, Collections.EMPTY_MAP);
        visitChildren(orderedListNode);
        getListener().endList(ListType.NUMBERED, Collections.EMPTY_MAP);
    }

    @Override
    public void visit(QuotedNode quotedNode)
    {
        // Don't do anything, this is just some quoted content but XWiki doesn't handle quoted content in any specific
        // manner.
        if (QuotedNode.Type.Double.equals(quotedNode.getType())) {
            visit("\"");
        } else if (QuotedNode.Type.Single.equals(quotedNode.getType())) {
            visit("'");
        } else {
            visit("\u00AB");
        }

        visitChildren(quotedNode);

        if (QuotedNode.Type.Double.equals(quotedNode.getType())) {
            visit("\"");
        } else if (QuotedNode.Type.Single.equals(quotedNode.getType())) {
            visit("'");
        } else {
            visit("\u00BB");
        }
    }

    @Override
    public void visit(ReferenceNode referenceNode)
    {
        // Since XWiki doesn't support reference links, we store reference definitions and memory and when a reference
        // is used we generate a standard link.
        this.references.put(extractText(referenceNode), referenceNode);
    }

    private String extractText(SuperNode superNode)
    {
        WikiPrinter printer = new DefaultWikiPrinter();
        this.listeners.push(this.plainRendererFactory.createRenderer(printer));
        visitChildren(superNode);
        this.listeners.pop();
        return printer.toString();
    }

    @Override
    public void visit(RefImageNode refImageNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(RefLinkNode refLinkNode)
    {
        // Since XWiki doesn't support reference links, we generate a standard link instead
        String label = extractText(refLinkNode.referenceKey);

        ReferenceNode referenceNode = this.references.get(label);
        if (referenceNode != null) {
            ResourceReference reference = this.linkResourceReferenceParser.parse(referenceNode.getUrl());

            // Handle an optional link title
            Map<String, String> parameters = Collections.EMPTY_MAP;
            if (StringUtils.isNotEmpty(referenceNode.getTitle())) {
                parameters = Collections.singletonMap("title", referenceNode.getTitle());
            }

            getListener().beginLink(reference, false, parameters);
            visitChildren(refLinkNode);
            getListener().endLink(reference, false, parameters);
        }
    }

    @Override
    public void visit(SimpleNode simpleNode)
    {
        if (SimpleNode.Type.Linebreak.equals(simpleNode.getType())) {
            getListener().onNewLine();
        } else if (SimpleNode.Type.Apostrophe.equals(simpleNode.getType())) {
            visit("'");
        } else if (SimpleNode.Type.HRule.equals(simpleNode.getType())) {
            getListener().onHorizontalLine(Collections.EMPTY_MAP);
        } else if (SimpleNode.Type.Endash.equals(simpleNode.getType())) {
            visit("\u2013");
        } else if (SimpleNode.Type.Emdash.equals(simpleNode.getType())) {
            visit("\u2014");
        } else if (SimpleNode.Type.Nbsp.equals(simpleNode.getType())) {
            visit(" ");
        } else if (SimpleNode.Type.Ellipsis.equals(simpleNode.getType())) {
            visit("...");
        }
    }

    @Override
    public void visit(TableBodyNode tableBodyNode)
    {
        this.isInTableHeaderStack.push(false);
        visitChildren(tableBodyNode);
        this.isInTableHeaderStack.pop();
    }

    @Override
    public void visit(TableCellNode tableCellNode)
    {
        boolean isInHeader = this.isInTableHeaderStack.peek();
        if (isInHeader) {
            getListener().beginTableHeadCell(Collections.EMPTY_MAP);
        } else {
            getListener().beginTableCell(Collections.EMPTY_MAP);
        }
        visitChildren(tableCellNode);
        if (isInHeader) {
            getListener().endTableHeadCell(Collections.EMPTY_MAP);
        } else {
            getListener().endTableCell(Collections.EMPTY_MAP);
        }
    }

    @Override
    public void visit(TableColumnNode tableColumnNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableHeaderNode tableHeaderNode)
    {
        this.isInTableHeaderStack.push(true);
        visitChildren(tableHeaderNode);
        this.isInTableHeaderStack.pop();
    }

    @Override
    public void visit(TableNode tableNode)
    {
        getListener().beginTable(Collections.EMPTY_MAP);
        visitChildren(tableNode);
        getListener().endTable(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(TableRowNode tableRowNode)
    {
        getListener().beginTableRow(Collections.EMPTY_MAP);
        visitChildren(tableRowNode);
        getListener().endTableRow(Collections.EMPTY_MAP);
    }

    @Override
    public void visit(TableCaptionNode tableCaptionNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(WikiLinkNode wikiLinkNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(SuperNode superNode)
    {
        visitChildren(superNode);
    }
}

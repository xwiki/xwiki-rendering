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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

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
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;

/**
 * <a href="https://github.com/sirthias/pegdown">Pegdown</a> visitor to transform the Pegdown AST into an XDOM
 * representation.
 *
 * @version $Id$
 * @since 4.4M1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultPegdownToXDOMConverter implements Visitor, PegdownToXDOMConverter
{
    /**
     * A plain text parser used to convert Pegdown TextNode elements to various Block elements since TextNode's values
     * contain several words and special characters which we thus need to break down into individual tokens.
     */
    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    /**
     * Used to generate a unique id for Headings (specifically used to convert the Heading content into a String).
     */
    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainTextBlockRenderer;

    /**
     * Used to convert the Pegdown AST subtree for Headings into XWiki Block elements which can then be fed to the
     * {@link #plainTextBlockRenderer} to convert them to a String, which is then fed to the {@link #idGenerator} to
     * generate a unique id.
     */
    @Inject
    private Provider<PegdownToXDOMConverter> converterProvider;

    /**
     * Used to generate a unique id for Headings.
     */
    private IdGenerator idGenerator = new IdGenerator();

    /**
     * List of the blocks we'll be building when visiting the Pegdown tree.
     */
    private List<Block> blocks = new ArrayList<Block>();

    /**
     * The top level XWiki Block element to return.
     */
    private XDOM rootBlock = new XDOM(blocks, new MetaData(Collections.<String, Object>singletonMap(MetaData.SYNTAX,
        Syntax.MARKDOWN_1_0)));

    /**
     * The current Block we're on.
     */
    private Block currentBlock = rootBlock;

    @Override
    public XDOM buildBlocks(SuperNode superNode)
    {
        superNode.accept(this);
        return rootBlock;
    }

    @Override
    public void visit(RootNode rootNode)
    {
        // TODO: Add handling for ReferenceNode and AbbreviationNode
        //        for (ReferenceNode ref : rootNode.getReferences()) {
        //            visitChildren(ref);
        //        }
        //
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

    /**
     * Helper method to visit all children nodes.
     *
     * @param node the node to visit
     * @param blockToAdd the Block to add and set as the new current block prior to visiting the children
     */
    protected void visitChildren(Node node, Block blockToAdd)
    {
        currentBlock.addChild(blockToAdd);
        currentBlock = blockToAdd;
        visitChildren(node);
        currentBlock = currentBlock.getParent();
    }

    @Override
    public void visit(Node node)
    {
        // Not used.
    }

    @Override
    public void visit(ParaNode paraNode)
    {
        List<Block> paraChildren = new ArrayList<Block>();
        ParagraphBlock paragraphBlock = new ParagraphBlock(paraChildren);

        visitChildren(paraNode, paragraphBlock);
    }

    @Override
    public void visit(TextNode textNode)
    {
        XDOM xdom;
        try {
            xdom = plainTextParser.parse(new StringReader(textNode.getText()));
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Error parsing content [%s]", textNode.getText()), e);
        }

        currentBlock.addChildren(
            xdom.getFirstBlock(new ClassBlockMatcher(ParagraphBlock.class), Block.Axes.CHILD).getChildren());
    }

    @Override
    public void visit(SpecialTextNode specialTextNode)
    {
        visit(specialTextNode);
    }

    @Override
    public void visit(StrongNode strongNode)
    {
        List<Block> children = new ArrayList<Block>();
        FormatBlock block = new FormatBlock(children, Format.BOLD);

        visitChildren(strongNode, block);
    }

    @Override
    public void visit(EmphNode emphNode)
    {
        List<Block> children = new ArrayList<Block>();
        FormatBlock block = new FormatBlock(children, Format.ITALIC);

        visitChildren(emphNode, block);
    }

    @Override
    public void visit(BulletListNode bulletListNode)
    {
        List<Block> children = new ArrayList<Block>();
        BulletedListBlock block = new BulletedListBlock(children, Collections.EMPTY_MAP);

        visitChildren(bulletListNode, block);
    }

    @Override
    public void visit(ListItemNode listItemNode)
    {
        ListItemBlock listItemBlock = new ListItemBlock(new ArrayList<Block>());
        currentBlock.addChild(listItemBlock);
        Block originalCurrentBlock = currentBlock;

        for (Node node : listItemNode.getChildren()) {
            currentBlock = listItemBlock;
            node.accept(this);
        }

        currentBlock = originalCurrentBlock;
    }

    @Override
    public void visit(CodeNode codeNode)
    {
        VerbatimBlock block = new VerbatimBlock(codeNode.getText(), true);

        currentBlock.addChild(block);
    }

    @Override
    public void visit(VerbatimNode verbatimNode)
    {
        Block block;

        if (verbatimNode.getType().length() == 0) {
            String text = StringUtils.removeEnd(verbatimNode.getText(), "\n");
            block = new VerbatimBlock(text, false);
        } else {
            block = new MacroBlock("code", Collections.singletonMap("language", verbatimNode.getType()),
                    verbatimNode.getText(), false);
        }

        currentBlock.addChild(block);
    }

    @Override
    public void visit(HeaderNode headerNode)
    {
        // Step 1: Generate a unique id for the Heading block
        XDOM xdom = this.converterProvider.get().buildBlocks(new SuperNode(headerNode.getChildren()));
        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        this.plainTextBlockRenderer.render(xdom, wikiPrinter);
        String uniqueId = idGenerator.generateUniqueId("H", wikiPrinter.toString());

        // Step 2: Create Section and Heading Blocks
        List<Block> childrenBlocks = new ArrayList<Block>();
        SectionBlock sectionBlock = new SectionBlock(childrenBlocks);
        List<Block> headerChildrenBlocks = new ArrayList<Block>();
        HeaderBlock headerBlock =
            new HeaderBlock(headerChildrenBlocks, HeaderLevel.parseInt(headerNode.getLevel()), uniqueId);
        sectionBlock.addChild(headerBlock);
        currentBlock.addChild(sectionBlock);

        currentBlock = headerBlock;
        for (Node node : headerNode.getChildren()) {
            node.accept(this);
        }

        currentBlock = sectionBlock;
    }

    @Override
    public void visit(AbbreviationNode abbreviationNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(AutoLinkNode autoLinkNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(BlockQuoteNode blockQuoteNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(DefinitionListNode definitionListNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(DefinitionNode definitionNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(DefinitionTermNode definitionTermNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(ExpImageNode expImageNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(ExpLinkNode expLinkNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(HtmlBlockNode htmlBlockNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(InlineHtmlNode inlineHtmlNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(MailLinkNode mailLinkNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(OrderedListNode orderedListNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(QuotedNode quotedNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(ReferenceNode referenceNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(RefImageNode refImageNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(RefLinkNode refLinkNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(SimpleNode simpleNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableBodyNode tableBodyNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableCellNode tableCellNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableColumnNode tableColumnNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableHeaderNode tableHeaderNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableNode tableNode)
    {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public void visit(TableRowNode tableRowNode)
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

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

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ast.*;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.block.*;
import org.xwiki.rendering.block.match.BlockMatcher;
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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <a href="https://github.com/sirthias/pegdown">Pegdown</a> visitor 
 * to transform the Pegdown AST into an XDOM representation.
 *
 * @version $Id$
 * @since 4.4-milestone-1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultPegdownToXDOMConverter implements Visitor, PegdownToXDOMConverter {

    @Inject
    @Named("plain/1.0")
    private Parser plainTextParser;

    private IdGenerator idGenerator = new IdGenerator();

    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainTextBlockRenderer;

    @Inject
    private Provider<PegdownToXDOMConverter> converterProvider;

    /**
     * List of the blocks we'll be building when visiting the Pegdown tree.
     */
    private List<Block> blocks = new ArrayList<Block>();

    /**
     *
     */
    private XDOM rootBlock = new XDOM(blocks, new MetaData(Collections.<String, Object> singletonMap(MetaData.SYNTAX,
            Syntax.MARKDOWN_1_0)));

    /**
     * The current block that will contain the current XDOM node
     */
    private Block currentBlock = rootBlock;

    private ListItemBlock previousListItemBlock;

    /**
     * @return Return the XDOM root block
     */
    @Override
    public XDOM buildBlocks(SuperNode superNode) {
        superNode.accept(this);
        return rootBlock;
    }

    @Override
    public void visit(RootNode rootNode) {
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
     * @param node
     */
    protected void visitChildren(Node node) {
        for (Node child : node.getChildren()) {
            child.accept(this);
        }
    }

    protected void visitChildren(Node node, Block block) {
        currentBlock.addChild(block);
        currentBlock = block;

        visitChildren(node);

        currentBlock = currentBlock.getParent();
    }

    @Override
    public void visit(Node node) {
        // not called for top-level class nodes
    }

    @Override
    public void visit(ParaNode paraNode) {
        ArrayList<Block> paraChildren = new ArrayList<Block>();
        ParagraphBlock paragraphBlock = new ParagraphBlock(paraChildren);

        visitChildren(paraNode, paragraphBlock);
    }

    @Override
    public void visit(TextNode textNode) {
        XDOM xdom;
        try {
            xdom = plainTextParser.parse(new StringReader(textNode.getText()));
        } catch (ParseException e) {
            throw new RuntimeException(String.format("Problem parsing text [%s]", textNode.getText()), e);
        }

        currentBlock.addChildren(xdom.getFirstBlock(new ClassBlockMatcher(ParagraphBlock.class), Block.Axes.CHILD).getChildren());
    }

    @Override
    public void visit(SpecialTextNode specialTextNode) {
        visit(specialTextNode);
    }

    @Override
    public void visit(StrongNode strongNode) {
        ArrayList<Block> children = new ArrayList<Block>();
        FormatBlock block = new FormatBlock(children, Format.BOLD);

        visitChildren(strongNode, block);
    }

    @Override
    public void visit(EmphNode emphNode) {
        ArrayList<Block> children = new ArrayList<Block>();
        FormatBlock block = new FormatBlock(children, Format.ITALIC);

        visitChildren(emphNode, block);
    }

    @Override
    public void visit(BulletListNode bulletListNode) {
        ArrayList<Block> children = new ArrayList<Block>();
        BulletedListBlock block = new BulletedListBlock(children, Collections.EMPTY_MAP);

        visitChildren(bulletListNode, block);
    }

    @Override
    public void visit(ListItemNode listItemNode) {
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
    public void visit(CodeNode codeNode) {
        VerbatimBlock block = new VerbatimBlock(codeNode.getText(), true);

        currentBlock.addChild(block);
    }

    @Override
    public void visit(VerbatimNode verbatimNode) {
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
    public void visit(HeaderNode headerNode) {
        ArrayList<Block> childrenBlocks = new ArrayList<Block>();

        SectionBlock sectionBlock = new SectionBlock(childrenBlocks);

        XDOM xdom = this.converterProvider.get().buildBlocks(new SuperNode(headerNode.getChildren()));

        WikiPrinter wikiPrinter = new DefaultWikiPrinter();
        this.plainTextBlockRenderer.render(xdom, wikiPrinter);
        String uniqueId = idGenerator.generateUniqueId("H", wikiPrinter.toString());

        ArrayList<Block> headerChildrenBlocks = new ArrayList<Block>();
        HeaderBlock headerBlock = new HeaderBlock(headerChildrenBlocks, HeaderLevel.parseInt(headerNode.getLevel()), uniqueId);
        sectionBlock.addChild(headerBlock);

        currentBlock.addChild(sectionBlock);

        currentBlock = headerBlock;

        for (Node node: headerNode.getChildren()) {
            node.accept(this);
        }

        currentBlock = sectionBlock;
    }



    @Override
    public void visit(AbbreviationNode abbreviationNode) {
    }

    @Override
    public void visit(AutoLinkNode autoLinkNode) {
    }

    @Override
    public void visit(BlockQuoteNode blockQuoteNode) {
    }

    @Override
    public void visit(DefinitionListNode definitionListNode) {
    }

    @Override
    public void visit(DefinitionNode definitionNode) {
    }

    @Override
    public void visit(DefinitionTermNode definitionTermNode) {
    }

    @Override
    public void visit(ExpImageNode expImageNode) {
    }

    @Override
    public void visit(ExpLinkNode expLinkNode) {
    }

    @Override
    public void visit(HtmlBlockNode htmlBlockNode) {
    }

    @Override
    public void visit(InlineHtmlNode inlineHtmlNode) {
    }

    @Override
    public void visit(MailLinkNode mailLinkNode) {
    }

    @Override
    public void visit(OrderedListNode orderedListNode) {
    }

    @Override
    public void visit(QuotedNode quotedNode) {
    }

    @Override
    public void visit(ReferenceNode referenceNode) {
    }

    @Override
    public void visit(RefImageNode refImageNode) {
    }

    @Override
    public void visit(RefLinkNode refLinkNode) {
    }

    @Override
    public void visit(SimpleNode simpleNode) {
    }


    @Override
    public void visit(TableBodyNode tableBodyNode) {
    }

    @Override
    public void visit(TableCellNode tableCellNode) {
    }

    @Override
    public void visit(TableColumnNode tableColumnNode) {
    }

    @Override
    public void visit(TableHeaderNode tableHeaderNode) {
    }

    @Override
    public void visit(TableNode tableNode) {
    }

    @Override
    public void visit(TableRowNode tableRowNode) {
    }

    @Override
    public void visit(WikiLinkNode wikiLinkNode) {
    }

    @Override
    public void visit(SuperNode superNode) {
        visitChildren(superNode);
    }
}

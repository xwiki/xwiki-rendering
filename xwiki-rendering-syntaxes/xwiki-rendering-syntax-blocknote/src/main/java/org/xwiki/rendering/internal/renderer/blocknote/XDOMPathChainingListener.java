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
package org.xwiki.rendering.internal.renderer.blocknote;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.DefinitionDescriptionBlock;
import org.xwiki.rendering.block.DefinitionListBlock;
import org.xwiki.rendering.block.DefinitionTermBlock;
import org.xwiki.rendering.block.EmptyLinesBlock;
import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.block.FigureCaptionBlock;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.HorizontalLineBlock;
import org.xwiki.rendering.block.IdBlock;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.NewLineBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.QuotationBlock;
import org.xwiki.rendering.block.QuotationLineBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.SpecialSymbolBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Records the path of XDOM blocks from the root to the current block being rendered.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class XDOMPathChainingListener extends AbstractChainingListener
{
    private final Deque<Block> path = new LinkedList<>();

    /**
     * Creates a new instance that uses the given listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public XDOMPathChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
    }

    /**
     * @return the path of XDOM blocks from the root to the current block being rendered
     */
    public Deque<Block> getXDOMPath()
    {
        return this.path;
    }

    //
    // Events
    //

    @Override
    public void beginDocument(MetaData metadata)
    {
        this.path.push(new XDOM(List.of(), metadata));
        super.beginDocument(metadata);
    }

    @Override
    public void endDocument(MetaData metadata)
    {
        super.endDocument(metadata);
        this.path.pop();
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        this.path.push(new SectionBlock(List.of(), parameters));
        super.beginSection(parameters);
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        super.endSection(parameters);
        this.path.pop();
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.path.push(new HeaderBlock(List.of(), level, parameters, id));
        super.beginHeader(level, id, parameters);
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        super.endHeader(level, id, parameters);
        this.path.pop();
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.path.push(new ParagraphBlock(List.of(), parameters));
        super.beginParagraph(parameters);
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        super.endParagraph(parameters);
        this.path.pop();
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        this.path.push(new GroupBlock(List.of(), parameters));
        super.beginGroup(parameters);
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        super.endGroup(parameters);
        this.path.pop();
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        switch (type) {
            case BULLETED:
                this.path.push(new BulletedListBlock(List.of(), parameters));
                break;
            case NUMBERED:
                this.path.push(new NumberedListBlock(List.of(), parameters));
                break;
            default:
                throw new IllegalStateException("Unsupported list type [%s].".formatted(type));
        }
        super.beginList(type, parameters);
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        super.endList(type, parameters);
        this.path.pop();
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        this.path.push(new ListItemBlock(List.of(), parameters));
        super.beginListItem(parameters);
    }

    @Override
    public void beginListItem()
    {
        this.path.push(new ListItemBlock(List.of(), Map.of()));
        super.beginListItem();
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        super.endListItem(parameters);
        this.path.pop();
    }

    @Override
    public void endListItem()
    {
        super.endListItem();
        this.path.pop();
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        this.path.push(new DefinitionListBlock(List.of(), parameters));
        super.beginDefinitionList(parameters);
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        super.endDefinitionList(parameters);
        this.path.pop();
    }

    @Override
    public void beginDefinitionTerm()
    {
        this.path.push(new DefinitionTermBlock(List.of()));
        super.beginDefinitionTerm();
    }

    @Override
    public void endDefinitionTerm()
    {
        super.endDefinitionTerm();
        this.path.pop();
    }

    @Override
    public void beginDefinitionDescription()
    {
        this.path.push(new DefinitionDescriptionBlock(List.of()));
        super.beginDefinitionDescription();
    }

    @Override
    public void endDefinitionDescription()
    {
        super.endDefinitionDescription();
        this.path.pop();
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        this.path.push(new QuotationBlock(List.of(), parameters));
        super.beginQuotation(parameters);
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        super.endQuotation(parameters);
        this.path.pop();
    }

    @Override
    public void beginQuotationLine()
    {
        this.path.push(new QuotationLineBlock(List.of()));
        super.beginQuotationLine();
    }

    @Override
    public void endQuotationLine()
    {
        super.endQuotationLine();
        this.path.pop();
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        this.path.push(new MetaDataBlock(List.of(), metadata));
        super.beginMetaData(metadata);
    }

    @Override
    public void endMetaData(MetaData metadata)
    {
        super.endMetaData(metadata);
        this.path.pop();
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        this.path.push(new MacroMarkerBlock(name, parameters, content, List.of(), isInline));
        super.beginMacroMarker(name, parameters, content, isInline);
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        super.endMacroMarker(name, parameters, content, isInline);
        this.path.pop();
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        this.path.push(new FigureBlock(List.of(), parameters));
        super.beginFigure(parameters);
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        super.endFigure(parameters);
        this.path.pop();
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        this.path.push(new FigureCaptionBlock(List.of(), parameters));
        super.beginFigureCaption(parameters);
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        super.endFigureCaption(parameters);
        this.path.pop();
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        this.path.push(new FormatBlock(List.of(), format, parameters));
        super.beginFormat(format, parameters);
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        super.endFormat(format, parameters);
        this.path.pop();
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.path.push(new LinkBlock(List.of(), reference, freestanding, parameters));
        super.beginLink(reference, freestanding, parameters);
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        super.endLink(reference, freestanding, parameters);
        this.path.pop();
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        this.path.push(new TableBlock(List.of(), parameters));
        super.beginTable(parameters);
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        super.endTable(parameters);
        this.path.pop();
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        this.path.push(new TableRowBlock(List.of(), parameters));
        super.beginTableRow(parameters);
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        super.endTableRow(parameters);
        this.path.pop();
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.path.push(new TableHeadCellBlock(List.of(), parameters));
        super.beginTableHeadCell(parameters);
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        super.endTableHeadCell(parameters);
        this.path.pop();
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        this.path.push(new TableCellBlock(List.of(), parameters));
        super.beginTableCell(parameters);
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        super.endTableCell(parameters);
        this.path.pop();
    }

    @Override
    public void onEmptyLines(int count)
    {
        this.path.push(new EmptyLinesBlock(count));
        super.onEmptyLines(count);
        this.path.pop();
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.path.push(new HorizontalLineBlock(parameters));
        super.onHorizontalLine(parameters);
        this.path.pop();
    }

    @Override
    public void onId(String name)
    {
        this.path.push(new IdBlock(name));
        super.onId(name);
        this.path.pop();
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.path.push(new ImageBlock(reference, freestanding, parameters));
        super.onImage(reference, freestanding, parameters);
        this.path.pop();
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        this.path.push(new ImageBlock(reference, freestanding, id, parameters));
        super.onImage(reference, freestanding, id, parameters);
        this.path.pop();
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        this.path.push(new MacroBlock(id, parameters, content, inline));
        super.onMacro(id, parameters, content, inline);
        this.path.pop();
    }

    @Override
    public void onNewLine()
    {
        this.path.push(new NewLineBlock());
        super.onNewLine();
        this.path.pop();
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        this.path.push(new RawBlock(text, syntax));
        super.onRawText(text, syntax);
        this.path.pop();
    }

    @Override
    public void onSpace()
    {
        this.path.push(new SpaceBlock());
        super.onSpace();
        this.path.pop();
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.path.push(new SpecialSymbolBlock(symbol));
        super.onSpecialSymbol(symbol);
        this.path.pop();
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        this.path.push(new VerbatimBlock(content, parameters, inline));
        super.onVerbatim(content, inline, parameters);
        this.path.pop();
    }

    @Override
    public void onWord(String word)
    {
        this.path.push(new WordBlock(word));
        super.onWord(word);
        this.path.pop();
    }
}

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
package org.xwiki.rendering.internal.parser;

import java.util.Map;

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
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Produce a {@link XDOM} based on events.
 *
 * @version $Id$
 * @since 2.1M1
 */
public class XDOMGeneratorListener implements Listener
{
    private XDOMBuilder builder = new XDOMBuilder();

    /**
     * @return the generated {@link XDOM}.
     */
    public XDOM getXDOM()
    {
        return this.builder.getXDOM();
    }

    @Override
    public void beginDefinitionDescription()
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginDefinitionTerm()
    {
        this.builder.startBlockList();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginDocument(MetaData metadata)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginListItem()
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginQuotationLine()
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.builder.startBlockList();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        this.builder.startBlockList();
    }

    @Override
    public void endDefinitionDescription()
    {
        this.builder.addBlock(new DefinitionDescriptionBlock(this.builder.endBlockList()));
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        this.builder.addBlock(new DefinitionListBlock(this.builder.endBlockList(),
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endDefinitionTerm()
    {
        this.builder.addBlock(new DefinitionTermBlock(this.builder.endBlockList()));
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metadata)
    {
        this.builder.addBlock(new XDOM(this.builder.endBlockList(), metadata));
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        this.builder.addBlock(new FigureBlock(this.builder.endBlockList(), parameters));
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        this.builder.addBlock(new FigureCaptionBlock(this.builder.endBlockList(), parameters));
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        this.builder.addBlock(new FormatBlock(this.builder.endBlockList(), format != null ? format : Format.NONE,
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        this.builder.addBlock(
            new GroupBlock(this.builder.endBlockList(), parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.builder.addBlock(new HeaderBlock(this.builder.endBlockList(), level,
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS, id));
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        if (type == ListType.BULLETED) {
            this.builder.addBlock(new BulletedListBlock(this.builder.endBlockList(),
                parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
        } else {
            this.builder.addBlock(new NumberedListBlock(this.builder.endBlockList(),
                parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
        }
    }

    @Override
    public void endListItem()
    {
        this.builder.addBlock(new ListItemBlock(this.builder.endBlockList()));
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        this.builder.addBlock(new ListItemBlock(this.builder.endBlockList(), parameters));
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.builder
            .addBlock(new MacroMarkerBlock(name, macroParameters != null ? macroParameters : Listener.EMPTY_PARAMETERS,
                content, this.builder.endBlockList(), isInline));
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        this.builder.addBlock(new ParagraphBlock(this.builder.endBlockList(),
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        this.builder.addBlock(new QuotationBlock(this.builder.endBlockList(),
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endQuotationLine()
    {
        this.builder.addBlock(new QuotationLineBlock(this.builder.endBlockList()));
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        this.builder.addBlock(
            new SectionBlock(this.builder.endBlockList(), parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        this.builder.addBlock(
            new TableBlock(this.builder.endBlockList(), parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        this.builder.addBlock(new TableCellBlock(this.builder.endBlockList(),
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        this.builder.addBlock(new TableHeadCellBlock(this.builder.endBlockList(),
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        this.builder.addBlock(new TableRowBlock(this.builder.endBlockList(),
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.builder.addBlock(new LinkBlock(this.builder.endBlockList(), reference, freestanding,
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.0M2
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        this.builder.addBlock(new MetaDataBlock(this.builder.endBlockList(), metadata));
    }

    @Override
    public void onEmptyLines(int count)
    {
        this.builder.addBlock(new EmptyLinesBlock(count));
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.builder.addBlock(new HorizontalLineBlock(parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void onId(String name)
    {
        this.builder.addBlock(new IdBlock(name));
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        this.builder
            .addBlock(new MacroBlock(id, parameters != null ? parameters : Listener.EMPTY_PARAMETERS, content, inline));
    }

    @Override
    public void onNewLine()
    {
        this.builder.addBlock(new NewLineBlock());
    }

    @Override
    public void onRawText(String content, Syntax syntax)
    {
        this.builder.addBlock(new RawBlock(content, syntax));
    }

    @Override
    public void onSpace()
    {
        this.builder.addBlock(new SpaceBlock());
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.builder.addBlock(new SpecialSymbolBlock(symbol));
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        this.builder
            .addBlock(new VerbatimBlock(content, parameters != null ? parameters : Listener.EMPTY_PARAMETERS, inline));
    }

    @Override
    public void onWord(String word)
    {
        this.builder.addBlock(new WordBlock(word));
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        this.builder.addBlock(
            new ImageBlock(reference, freestanding, parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        this.builder.addBlock(
            new ImageBlock(reference, freestanding, id, parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }
}

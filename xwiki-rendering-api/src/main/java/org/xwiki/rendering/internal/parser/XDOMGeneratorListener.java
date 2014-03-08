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
        return builder.getXDOM();
    }

    @Override
    public void beginDefinitionDescription()
    {
        builder.startBlockList();
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginDefinitionTerm()
    {
        builder.startBlockList();
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void beginDocument(MetaData metaData)
    {
        builder.startBlockList();
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginListItem()
    {
        builder.startBlockList();
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        builder.startBlockList();
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginQuotationLine()
    {
        builder.startBlockList();
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    @Override
    public void beginLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        builder.startBlockList();
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        builder.startBlockList();
    }

    @Override
    public void endDefinitionDescription()
    {
        builder.addBlock(new DefinitionDescriptionBlock(builder.endBlockList()));
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        builder.addBlock(new DefinitionListBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endDefinitionTerm()
    {
        builder.addBlock(new DefinitionTermBlock(builder.endBlockList()));
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metaData)
    {
        builder.addBlock(new XDOM(builder.endBlockList(), metaData));
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        builder.addBlock(new FormatBlock(builder.endBlockList(), format != null ? format : Format.NONE,
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        builder.addBlock(new GroupBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        builder.addBlock(new HeaderBlock(builder.endBlockList(), level, parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS, id));
    }

    @Override
    public void endList(ListType listType, Map<String, String> parameters)
    {
        if (listType == ListType.BULLETED) {
            builder.addBlock(new BulletedListBlock(builder.endBlockList(), parameters != null ? parameters
                : Listener.EMPTY_PARAMETERS));
        } else {
            builder.addBlock(new NumberedListBlock(builder.endBlockList(), parameters != null ? parameters
                : Listener.EMPTY_PARAMETERS));
        }
    }

    @Override
    public void endListItem()
    {
        builder.addBlock(new ListItemBlock(builder.endBlockList()));
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        builder.addBlock(new MacroMarkerBlock(name, macroParameters != null ? macroParameters
            : Listener.EMPTY_PARAMETERS, content, builder.endBlockList(), isInline));
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        builder.addBlock(new ParagraphBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        builder.addBlock(new QuotationBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endQuotationLine()
    {
        builder.addBlock(new QuotationLineBlock(builder.endBlockList()));
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        builder.addBlock(new SectionBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        builder.addBlock(new TableBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        builder.addBlock(new TableCellBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        builder.addBlock(new TableHeadCellBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        builder.addBlock(new TableRowBlock(builder.endBlockList(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        builder.addBlock(new LinkBlock(builder.endBlockList(), reference, isFreeStandingURI, parameters != null
            ? parameters : Listener.EMPTY_PARAMETERS));
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void endMetaData(MetaData metadata)
    {
        builder.addBlock(new MetaDataBlock(builder.endBlockList(), metadata));
    }

    @Override
    public void onEmptyLines(int count)
    {
        builder.addBlock(new EmptyLinesBlock(count));
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        builder.addBlock(new HorizontalLineBlock(parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void onId(String name)
    {
        builder.addBlock(new IdBlock(name));
    }

    @Override
    public void onMacro(String id, Map<String, String> macroParameters, String content, boolean isInline)
    {
        builder.addBlock(new MacroBlock(id, macroParameters != null ? macroParameters : Listener.EMPTY_PARAMETERS,
            content, isInline));
    }

    @Override
    public void onNewLine()
    {
        builder.addBlock(new NewLineBlock());
    }

    @Override
    public void onRawText(String rawContent, Syntax syntax)
    {
        builder.addBlock(new RawBlock(rawContent, syntax));
    }

    @Override
    public void onSpace()
    {
        builder.addBlock(new SpaceBlock());
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        builder.addBlock(new SpecialSymbolBlock(symbol));
    }

    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        builder.addBlock(new VerbatimBlock(protectedString, parameters != null ? parameters : Listener.EMPTY_PARAMETERS,
            isInline));
    }

    @Override
    public void onWord(String word)
    {
        builder.addBlock(new WordBlock(word));
    }

    @Override
    public void onImage(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        builder.addBlock(new ImageBlock(reference, isFreeStandingURI, parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }
}

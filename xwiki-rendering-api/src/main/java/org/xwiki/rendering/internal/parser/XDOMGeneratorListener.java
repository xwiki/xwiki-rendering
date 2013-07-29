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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.xwiki.rendering.block.AbstractBlock;
import org.xwiki.rendering.block.Block;
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
    private Stack<Block> stack = new Stack<Block>();

    private final MarkerBlock marker = new MarkerBlock();

    private static class MarkerBlock extends AbstractBlock
    {
        @Override
        public void traverse(Listener listener)
        {
            // Nothing to do since this block is only used as a marker.
        }
    }

    public XDOM getXDOM()
    {
        List<Block> blocks = generateListFromStack();

        // support even events without begin/endDocument for partial content
        if (!blocks.isEmpty() && blocks.get(0) instanceof XDOM) {
            return (XDOM) blocks.get(0);
        } else {
            return new XDOM(blocks);
        }
    }

    private List<Block> generateListFromStack()
    {
        List<Block> blocks = new ArrayList<Block>();
        while (!this.stack.empty()) {
            if (this.stack.peek() != this.marker) {
                blocks.add(this.stack.pop());
            } else {
                this.stack.pop();
                break;
            }
        }
        Collections.reverse(blocks);
        return blocks;
    }

    @Override
    public void beginDefinitionDescription()
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginDefinitionTerm()
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void beginDocument(MetaData metaData)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginListItem()
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginQuotationLine()
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginSection(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void beginLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        this.stack.push(this.marker);
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void beginMetaData(MetaData metadata)
    {
        this.stack.push(this.marker);
    }

    @Override
    public void endDefinitionDescription()
    {
        this.stack.push(new DefinitionDescriptionBlock(generateListFromStack()));
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        this.stack.push(new DefinitionListBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endDefinitionTerm()
    {
        this.stack.push(new DefinitionTermBlock(generateListFromStack()));
    }

    /**
     * {@inheritDoc}
     * 
     * @since 3.0M2
     */
    @Override
    public void endDocument(MetaData metaData)
    {
        this.stack.push(new XDOM(generateListFromStack(), metaData));
    }

    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        this.stack.push(new FormatBlock(generateListFromStack(), format != null ? format : Format.NONE,
            parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endGroup(Map<String, String> parameters)
    {
        this.stack.push(new GroupBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        this.stack.push(new HeaderBlock(generateListFromStack(), level, parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS, id));
    }

    @Override
    public void endList(ListType listType, Map<String, String> parameters)
    {
        if (listType == ListType.BULLETED) {
            this.stack.push(new BulletedListBlock(generateListFromStack(), parameters != null ? parameters
                : Listener.EMPTY_PARAMETERS));
        } else {
            this.stack.push(new NumberedListBlock(generateListFromStack(), parameters != null ? parameters
                : Listener.EMPTY_PARAMETERS));
        }
    }

    @Override
    public void endListItem()
    {
        this.stack.push(new ListItemBlock(generateListFromStack()));
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.stack.push(new MacroMarkerBlock(name, macroParameters != null ? macroParameters
            : Listener.EMPTY_PARAMETERS, content, generateListFromStack(), isInline));
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        this.stack.push(new ParagraphBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        this.stack.push(new QuotationBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endQuotationLine()
    {
        this.stack.push(new QuotationLineBlock(generateListFromStack()));
    }

    @Override
    public void endSection(Map<String, String> parameters)
    {
        this.stack.push(new SectionBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTable(Map<String, String> parameters)
    {
        this.stack.push(new TableBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        this.stack.push(new TableCellBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        this.stack.push(new TableHeadCellBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        this.stack.push(new TableRowBlock(generateListFromStack(), parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void endLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        this.stack.push(new LinkBlock(generateListFromStack(), reference, isFreeStandingURI, parameters != null
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
        this.stack.push(new MetaDataBlock(generateListFromStack(), metadata));
    }

    @Override
    public void onEmptyLines(int count)
    {
        this.stack.push(new EmptyLinesBlock(count));
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        this.stack.push(new HorizontalLineBlock(parameters != null ? parameters : Listener.EMPTY_PARAMETERS));
    }

    @Override
    public void onId(String name)
    {
        this.stack.push(new IdBlock(name));
    }

    @Override
    public void onMacro(String id, Map<String, String> macroParameters, String content, boolean isInline)
    {
        this.stack.push(new MacroBlock(id, macroParameters != null ? macroParameters : Listener.EMPTY_PARAMETERS,
            content, isInline));
    }

    @Override
    public void onNewLine()
    {
        this.stack.push(new NewLineBlock());
    }

    @Override
    public void onRawText(String rawContent, Syntax syntax)
    {
        this.stack.push(new RawBlock(rawContent, syntax));
    }

    @Override
    public void onSpace()
    {
        this.stack.push(new SpaceBlock());
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        this.stack.push(new SpecialSymbolBlock(symbol));
    }

    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        this.stack.push(new VerbatimBlock(protectedString, parameters != null ? parameters : Listener.EMPTY_PARAMETERS,
            isInline));
    }

    @Override
    public void onWord(String word)
    {
        this.stack.push(new WordBlock(word));
    }

    @Override
    public void onImage(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        this.stack.push(new ImageBlock(reference, isFreeStandingURI, parameters != null ? parameters
            : Listener.EMPTY_PARAMETERS));
    }
}

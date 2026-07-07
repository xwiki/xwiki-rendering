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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.DefinitionListBlock;
import org.xwiki.rendering.block.ListBLock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.QuotationBlock;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PARAMETERS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractListBlockParser.LIST_TYPE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.BulletListBlockParser.BULLETED_LIST_ITEM;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CheckListBlockParser.CHECKED_PARAMETER;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CheckListBlockParser.CHECKED_PROPERTY;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CheckListBlockParser.CHECK_LIST_ITEM;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.CheckListBlockParser.CHECK_LIST_TYPE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.DefinitionListBlockParser.DEFINITION_LIST_ITEM;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.DefinitionListBlockParser.TERM;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.NumberedListBlockParser.NUMBERED_LIST_ITEM;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.NumberedListBlockParser.START;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.QuoteBlockParser.QUOTE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ToggleListBlockParser.TOGGLE_LIST_ITEM;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ToggleListBlockParser.TOGGLE_LIST_TYPE;

/**
 * Renders lists to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.6.0RC1
 */
public class ListChainingListener extends AbstractChainingListener
{
    private final Context context;

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public ListChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        // Nothing to do here because in BlockNote syntax the list items are not wrapped in a list block.
    }

    @Override
    public void endList(ListType type, Map<String, String> parameters)
    {
        // Nothing to do here.
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        Map<String, String> listItemParameters =
            mergeListParameters(ListBLock.class, this.context.getBlockState()::getListItemIndex);
        ObjectNode listItem = this.context.getBlockNoteState().beginBlock(getListItemType(), true, true, true, true);
        ObjectNode listItemProperties = (ObjectNode) listItem.path(PROPS);

        ObjectNode unknownParameters = (ObjectNode) listItemProperties.path(PARAMETERS);
        unknownParameters.remove(List.of(START, CHECKED_PARAMETER, LIST_TYPE));

        // XWiki syntax doesn't support parameters for list items so the start number is kept as a list parameter.
        // BlockNote doesn't have a list block. It has only list items, which is why we need to copy the start number
        // from the list to the first item (unless the value is specified explicitly on the list item, which could
        // happen if the input syntax is not XWiki 2.1).
        if (parameters.containsKey(START)
            || (listItemParameters.containsKey(START) && this.context.getBlockState().getListItemIndex() == 0)) {
            listItemProperties.put(START, Integer.parseInt(listItemParameters.get(START)));
        }

        // Each list item can be checked or unchecked, but they are grouped by value in XDOM because XWiki syntax
        // doesn't support parameters on list items. We need to copy the value from the list (group) to each list item
        // (when ungrouping).
        if (listItemParameters.containsKey(CHECKED_PARAMETER)) {
            listItemProperties.put(CHECKED_PROPERTY, Boolean.valueOf(listItemParameters.get(CHECKED_PARAMETER)));
        }
    }

    private <T extends Block> Map<String, String> mergeListParameters(Class<T> clasz, IntSupplier indexSupplier)
    {
        // Merge the list item parameters with the list parameters.
        Map<String, String> listItemParameters = new LinkedHashMap<>();
        this.context.getXDOMPath().stream().filter(clasz::isInstance).map(clasz::cast).findFirst()
            .ifPresent(listBlock -> listItemParameters.putAll(listBlock.getParameters()));
        listItemParameters.putAll(this.context.getParameters());

        if (indexSupplier.getAsInt() == 0) {
            // BlockNote doesn't have a list block so we add the list parameters to the first list item.
            this.context.getXDOMPath().peek().setParameters(listItemParameters);
        }

        return listItemParameters;
    }

    private String getListItemType()
    {
        @SuppressWarnings("null")
        ListBLock listBlock = this.context.getXDOMPath().stream().filter(ListBLock.class::isInstance)
            .map(ListBLock.class::cast).findFirst()
            .orElseThrow(() -> new IllegalStateException("Encountered a list item outside of a list block."));
        String listType = listBlock.getParameter(LIST_TYPE);
        if (TOGGLE_LIST_TYPE.equals(listType)) {
            return TOGGLE_LIST_ITEM;
        } else if (CHECK_LIST_TYPE.equals(listType)) {
            return CHECK_LIST_ITEM;
        } else if (listBlock instanceof NumberedListBlock) {
            return NUMBERED_LIST_ITEM;
        } else {
            return BULLETED_LIST_ITEM;
        }
    }

    @Override
    public void endListItem(Map<String, String> parameters)
    {
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        // Nothing to do here because in BlockNote syntax the list items are not wrapped in a list block.
    }

    @Override
    public void beginDefinitionTerm()
    {
        ObjectNode dt = beginDefinitionListItem();
        ((ObjectNode) dt.path(PROPS)).put(TERM, true);
    }

    private ObjectNode beginDefinitionListItem()
    {
        mergeListParameters(DefinitionListBlock.class, this.context.getBlockState()::getDefinitionListItemIndex);
        return this.context.getBlockNoteState().beginBlock(DEFINITION_LIST_ITEM, true, true, true, true);
    }

    @Override
    public void endDefinitionTerm()
    {
        endListItem(EMPTY_PARAMETERS);
    }

    @Override
    public void beginDefinitionDescription()
    {
        beginDefinitionListItem();
    }

    @Override
    public void endDefinitionDescription()
    {
        endListItem(EMPTY_PARAMETERS);
    }

    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        // Nothing to do here.
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        // Nothing to do here because we map each quotation line to a separate quote block in BlockNote syntax.
    }

    @Override
    public void beginQuotationLine()
    {
        mergeListParameters(QuotationBlock.class, this.context.getBlockState()::getQuotationLineIndex);
        this.context.getBlockNoteState().beginBlock(QUOTE, true, true, true, true);
    }

    @Override
    public void endQuotationLine()
    {
        this.context.getBlockNoteState().endBlock();
    }

    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        // Nothing to do here.
    }
}

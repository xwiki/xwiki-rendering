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
package org.xwiki.rendering.internal.renderer.plain;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Print only plain text information. For example it remove anything which need a specific syntax a simple plain text
 * editor can't support like the style, link, image, etc. This renderer is mainly used to generate a simple as possible
 * label like in a TOC.
 *
 * @version $Id$
 * @since 1.9M1
 */
public class PlainTextChainingRenderer extends AbstractChainingPrintRenderer
{
    /**
     * New Line character.
     */
    private static final String NL = "\n";

    /**
     * True if no empty line has been printed.
     */
    private boolean isFirstElementRendered;

    /**
     * Generate link label.
     */
    private LinkLabelGenerator linkLabelGenerator;

    /**
     * The plain text renderer supports when no link label generator is set.
     *
     * @param listenerChain the listener chain
     */
    public PlainTextChainingRenderer(ListenerChain listenerChain)
    {
        this(null, listenerChain);
    }

    /**
     * @param linkLabelGenerator the link label generator
     * @param listenerChain the listener chain
     */
    public PlainTextChainingRenderer(LinkLabelGenerator linkLabelGenerator, ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);

        this.linkLabelGenerator = linkLabelGenerator;
    }

    // State

    /**
     * @return the {@link BlockStateChainingListener} from the listeners chain
     */
    private BlockStateChainingListener getBlockState()
    {
        return (BlockStateChainingListener) getListenerChain().getListener(BlockStateChainingListener.class);
    }

    /**
     * @return the {@link EmptyBlockChainingListener} from the listeners chain
     */
    protected EmptyBlockChainingListener getEmptyBlockState()
    {
        return (EmptyBlockChainingListener) getListenerChain().getListener(EmptyBlockChainingListener.class);
    }

    // Events

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        printEmptyLine();
    }

    @Override
    public void onNewLine()
    {
        getPrinter().print(NL);
    }

    @Override
    public void endLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        if (getEmptyBlockState().isCurrentContainerBlockEmpty()) {
            ResourceType resourceType = reference.getType();
            if ((ResourceType.DOCUMENT.equals(resourceType) || ResourceType.SPACE.equals(resourceType))
                && this.linkLabelGenerator != null) {
                getPrinter().print(this.linkLabelGenerator.generate(reference));
            } else {
                getPrinter().print(reference.getReference());
            }
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        printEmptyLine();
    }

    @Override
    public void onWord(String word)
    {
        getPrinter().print(word);
    }

    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        if (getBlockState().getListDepth() == 1) {
            printEmptyLine();
        } else {
            getPrinter().print(NL);
        }
    }

    @Override
    public void beginListItem()
    {
        if (getBlockState().getListItemIndex() > 0) {
            getPrinter().print(NL);
        }

        // TODO: maybe add some syntax here like a - or not
    }

    @Override
    public void onSpace()
    {
        getPrinter().print(" ");
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        getPrinter().print(String.valueOf(symbol));
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        printEmptyLine();
        getPrinter().print("----");
    }

    @Override
    public void onEmptyLines(int count)
    {
        getPrinter().print(StringUtils.repeat(NL, count));
    }

    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        getPrinter().print(protectedString);
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
            getPrinter().print(NL);
        }
    }

    @Override
    public void beginDefinitionTerm()
    {
        if (getBlockState().getDefinitionListItemIndex() > 0) {
            getPrinter().print(NL);
        }
    }

    @Override
    public void beginDefinitionDescription()
    {
        if (getBlockState().getDefinitionListItemIndex() > 0) {
            getPrinter().print(NL);
        }
    }

    @Override
    public void beginQuotationLine()
    {
        if (getBlockState().getQuotationLineIndex() > 0) {
            getPrinter().print(NL);
        } else {
            printEmptyLine();
        }
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        printEmptyLine();
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        if (getBlockState().getCellCol() > 0) {
            getPrinter().print("\t");
        }
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        beginTableCell(parameters);
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        if (getBlockState().getCellRow() > 0) {
            getPrinter().print(NL);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        // TODO: maybe something could be done here
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        if (syntax == null || Syntax.PLAIN_1_0.equals(syntax)) {
            getPrinter().print(text);
        }
    }

    /**
     * Add an empty line to the printer.
     */
    private void printEmptyLine()
    {
        if (this.isFirstElementRendered) {
            getPrinter().print(NL + NL);
        } else {
            this.isFirstElementRendered = true;
        }
    }
}

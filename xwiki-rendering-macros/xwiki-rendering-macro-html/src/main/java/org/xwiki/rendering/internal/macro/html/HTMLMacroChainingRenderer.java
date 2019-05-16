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
package org.xwiki.rendering.internal.macro.html;

import java.util.Map;

import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Define some custom behaviours for the different HTMLMacro renderers: this wrapper takes as parameter the right syntax
 * renderer and delegate to it most behaviours. Only some of them are rewritten.
 *
 * @version $Id$
 * @since 11.4RC1
 */
public class HTMLMacroChainingRenderer extends WrappingListener implements ChainingListener, PrintRenderer
{
    private AbstractChainingPrintRenderer printRenderer;

    /**
     * @param printRenderer the right syntax renderer to be called.
     */
    public HTMLMacroChainingRenderer(AbstractChainingPrintRenderer printRenderer)
    {
        this.printRenderer = printRenderer;
        this.setWrappedListener(printRenderer);
    }

    /**
     * @return true if the current event is generated from a transformation.
     */
    private boolean isInGeneratedBlock()
    {
        // Since we're already inside the HTML macro, we check for a depth of 2 (macro inside of macro).
        return getBlockState().getMacroDepth() > 1;
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print(String.valueOf(symbol));
        } else {
            super.onSpecialSymbol(symbol);
        }
    }

    @Override
    public void onWord(String word)
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print(word);
        } else {
            super.onWord(word);
        }
    }

    @Override
    public void onNewLine()
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print("\n");
        } else {
            super.onNewLine();
        }
    }

    @Override
    public void onSpace()
    {
        if (!isInGeneratedBlock()) {
            getPrinter().print(" ");
        } else {
            super.onSpace();
        }
    }

    @Override
    public void onEmptyLines(int count)
    {
        if (!isInGeneratedBlock()) {
            // Don't print anything.
        } else {
            super.onEmptyLines(count);
        }
    }

    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        if (!isInGeneratedBlock()) {
            // Don't print anything.
        } else {
            super.beginParagraph(parameters);
        }
    }

    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        if (!isInGeneratedBlock()) {
            // Don't print anything.
        } else {
            super.endParagraph(parameters);
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        // Don't print anything since we are already in the html macro.
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Don't print anything since we are already in the html macro.
    }

    @Override
    public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        // Don't print anything since we are already in the html macro.
    }

    protected BlockStateChainingListener getBlockState()
    {
        return (BlockStateChainingListener) getListenerChain()
            .getListener(HTMLMacroBlockStateChainingListener.class);
    }

    @Override
    public ListenerChain getListenerChain()
    {
        return this.printRenderer.getListenerChain();
    }

    @Override
    public WikiPrinter getPrinter()
    {
        return this.printRenderer.getPrinter();
    }

    @Override
    public void setPrinter(WikiPrinter printer)
    {
        this.printRenderer.setPrinter(printer);
    }
}

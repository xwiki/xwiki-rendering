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
package org.xwiki.rendering.internal.renderer.xwiki20;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.LookaheadChainingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Chaining listener that detects if a figure just wraps a single image.
 *
 * @version $Id$
 * @since 14.1RC1
 */
public class StackingFigureContentChainingListener extends LookaheadChainingListener
{
    private static final String CLASS_PARAMETER = "class";

    private boolean isStacking;

    private int numImages;

    private int captionDepth;

    private int figureDepth;

    private boolean cleanImageFigure;

    private ResourceReference imageReference;

    private Map<String, String> imageParameters;

    /**
     * @param listenerChain the listener chain to save
     */
    public StackingFigureContentChainingListener(ListenerChain listenerChain)
    {
        super(listenerChain, Integer.MAX_VALUE);
        setListenerChain(listenerChain);
        this.isStacking = true;
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        super.beginFigureCaption(parameters);

        ++this.captionDepth;

        if (this.captionDepth > 1) {
            stopStacking(false);
        }
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        super.endFigureCaption(parameters);

        --this.captionDepth;
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        super.onImage(reference, freestanding, parameters);

        internalOnImage(reference, freestanding, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        super.onImage(reference, freestanding, id, parameters);

        internalOnImage(reference, freestanding, parameters);
    }

    private void internalOnImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // ignore images in captions
        if (this.captionDepth == 0) {
            ++this.numImages;

            // freestanding images cannot have a caption
            if (this.numImages > 1 || freestanding) {
                this.stopStacking(false);
            } else {
                this.imageReference = reference;
                this.imageParameters = parameters;
            }
        }
    }

    // Events not supported as nested events in a clean figure.
    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        super.beginFigure(parameters);

        ++this.figureDepth;

        // Nested figures and figures without class="image" are not supported.
        if (this.figureDepth > 1 || !parameters.containsKey(CLASS_PARAMETER)
            || !Arrays.asList(StringUtils.split(parameters.get(CLASS_PARAMETER))).contains("image"))
        {
            stopStacking(false);
        }
    }

    @Override
    public void beginTable(Map<String, String> parameters)
    {
        super.beginTable(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        super.beginTableRow(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        super.beginTableCell(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        super.beginTableHeadCell(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        super.beginDefinitionList(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginDefinitionTerm()
    {
        super.beginDefinitionTerm();

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginDefinitionDescription()
    {
        super.beginDefinitionDescription();

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginList(ListType type, Map<String, String> parameters)
    {
        super.beginList(type, parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginListItem()
    {
        super.beginListItem();

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginListItem(Map<String, String> parameters)
    {
        super.beginListItem(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        super.beginHeader(level, id, parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        super.beginQuotation(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginQuotationLine()
    {
        super.beginQuotationLine();

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        super.beginLink(reference, freestanding, parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline)
    {
        super.beginMacroMarker(name, parameters, content, isInline);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void beginMetaData(MetaData metadata)
    {
        super.beginMetaData(metadata);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onHorizontalLine(Map<String, String> parameters)
    {
        super.onHorizontalLine(parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onWord(String word)
    {
        super.onWord(word);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onMacro(String id, Map<String, String> parameters, String content, boolean inline)
    {
        super.onMacro(id, parameters, content, inline);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onRawText(String text, Syntax syntax)
    {
        super.onRawText(text, syntax);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onSpecialSymbol(char symbol)
    {
        super.onSpecialSymbol(symbol);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onVerbatim(String content, boolean inline, Map<String, String> parameters)
    {
        super.onVerbatim(content, inline, parameters);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void onId(String name)
    {
        super.onId(name);

        if (this.captionDepth == 0) {
            stopStacking(false);
        }
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        super.endFigure(parameters);

        if (this.numImages == 1) {
            stopStacking(true);
        }
    }

    /**
     * @return If the figure is a clean image, i.e., just contains a single image and potentially a caption.
     */
    public boolean isCleanImageFigure()
    {
        return this.cleanImageFigure;
    }

    /**
     * @return The parameters of the (single) image contained in the clean figure.
     */
    public Map<String, String> getImageParameters()
    {
        return this.imageParameters;
    }

    /**
     * @return The reference of the (single) image contained in the clean figure.
     */
    public ResourceReference getImageReference()
    {
        return this.imageReference;
    }

    /**
     * Stop stacking events and move them back to the {@link LookaheadChainingListener} to replay them.
     */
    protected void stopStacking(boolean isCleanImageFigure)
    {
        if (this.isStacking) {
            this.isStacking = false;

            this.cleanImageFigure = isCleanImageFigure;

            // Stop stacking in this listener
            setLookaheadDepth(0);

            // Flush all stacked events BUT flush them in the Lookahead Listener at the beginning of the stack, in order
            // to replay them and thus not break the ordering of them since there are begin/end methods in
            // XWikiSyntaxChainingRenderer that will check for the next event (e.g. onNewLine()).
            LookaheadChainingListener listener =
                (LookaheadChainingListener) getListenerChain().getListener(LookaheadChainingListener.class);
            QueueListener previousEvents = getPreviousEvents();

            listener.transferStart(previousEvents);
        }
    }
}

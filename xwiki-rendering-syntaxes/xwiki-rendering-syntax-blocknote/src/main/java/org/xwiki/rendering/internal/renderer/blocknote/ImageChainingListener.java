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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.xwiki.rendering.block.FigureBlock;
import org.xwiki.rendering.internal.parser.blocknote.blocks.ImageBlockParser;
import org.xwiki.rendering.listener.chaining.AbstractChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.reference.ResourceReference;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PARAMETERS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.PROPS;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractBlockParser.TEXT_ALIGNMENT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractEmbedBlockParser.ALT;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractEmbedBlockParser.CAPTION;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractEmbedBlockParser.NAME;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractEmbedBlockParser.PREVIEW_WIDTH;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractEmbedBlockParser.URL;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.AbstractEmbedBlockParser.WIDTH;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ImageBlockParser.IMAGE;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ImageBlockParser.IMAGE_ALIGNMENT_PARAMETER;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.ImageBlockParser.IMAGE_LABEL_PARAMETER;
import static org.xwiki.rendering.internal.parser.blocknote.blocks.LinkBlockParser.FREE_STANDING;

/**
 * Renders images to BlockNote JSON format.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
public class ImageChainingListener extends AbstractChainingListener
{
    /**
     * The mapping between XWiki Rendering image alignment styles and BlockNote image alignments. We simply reverse the
     * mapping used by the image block parser.
     */
    @SuppressWarnings("null")
    private static final Map<String, String> IMAGE_ALIGNMENT =
        Collections.unmodifiableMap(ImageBlockParser.IMAGE_ALIGNMENT.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (a, b) -> b, LinkedHashMap::new)));

    private final Context context;

    /**
     * Creates a new instance using the provided listener chain.
     * 
     * @param listenerChain the listener chain
     */
    public ImageChainingListener(ListenerChain listenerChain)
    {
        setListenerChain(listenerChain);
        this.context = new Context(listenerChain);
    }

    @Override
    public void beginFigure(Map<String, String> parameters)
    {
        // Nothing to do here (we only render the image).
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        onImage(reference, freestanding, null, parameters);
    }

    @Override
    public void onImage(ResourceReference reference, boolean freestanding, String id, Map<String, String> parameters)
    {
        if (this.context.getTextState().isPlainTextRendering()) {
            return;
        }

        Map<String, String> imageParameters = new LinkedHashMap<>();
        this.context.getXDOMPath().stream().filter(FigureBlock.class::isInstance).map(FigureBlock.class::cast)
            .findFirst().ifPresent(figureBlock -> imageParameters.putAll(figureBlock.getParameters()));
        imageParameters.putAll(parameters);

        ObjectNode image = this.context.getBlockNoteState().beginBlock(IMAGE, true, false, false, false);
        ObjectNode imageProperties = (ObjectNode) image.path(PROPS);

        ObjectNode unknownParameters = (ObjectNode) imageProperties.path(PARAMETERS);
        unknownParameters.remove(List.of(IMAGE_ALIGNMENT_PARAMETER, IMAGE_LABEL_PARAMETER, ALT, WIDTH));

        if (imageParameters.containsKey(IMAGE_ALIGNMENT_PARAMETER)) {
            String alignment = imageParameters.get(IMAGE_ALIGNMENT_PARAMETER);
            alignment = IMAGE_ALIGNMENT.getOrDefault(alignment, alignment);
            imageProperties.put(TEXT_ALIGNMENT, alignment);
        }

        if (imageParameters.containsKey(ALT)) {
            imageProperties.put(NAME, imageParameters.get(ALT));
        }

        imageProperties.set(URL, this.context.getBlockNoteState().toJSON(reference));

        if (imageParameters.containsKey(WIDTH)) {
            imageProperties.put(PREVIEW_WIDTH, Integer.parseInt(imageParameters.get(WIDTH)));
        }

        if (this.context.getXDOMPath().stream().filter(FigureBlock.class::isInstance).findFirst().isEmpty()) {
            if (imageParameters.containsKey(IMAGE_LABEL_PARAMETER)) {
                // Inline image with caption.
                imageProperties.put(CAPTION, imageParameters.get(IMAGE_LABEL_PARAMETER));
            }

            // No figure caption, so we can add the image right away.
            this.context.getBlockNoteState().endBlock();
        }

        if (freestanding) {
            imageProperties.put(FREE_STANDING, freestanding);
        }
    }

    @Override
    public void beginFigureCaption(Map<String, String> parameters)
    {
        this.context.getTextState().beginPlainTextRendering();
    }

    @Override
    public void endFigureCaption(Map<String, String> parameters)
    {
        String figureCaption = this.context.getTextState().endPlainTextRendering();
        this.context.getXDOMPath().stream().filter(FigureBlock.class::isInstance).map(FigureBlock.class::cast)
            .findFirst().ifPresent(figureBlock -> figureBlock.setAttribute(CAPTION, figureCaption));
    }

    @Override
    public void endFigure(Map<String, String> parameters)
    {
        if (!this.context.getTextState().isPlainTextRendering()) {
            ObjectNode image = (ObjectNode) this.context.getBlockNoteState().getBlockNotePath().peek();
            ObjectNode imageProperties = (ObjectNode) image.path(PROPS);
            this.context.getXDOMPath().stream().filter(FigureBlock.class::isInstance).map(FigureBlock.class::cast)
                .findFirst()
                .ifPresent(figureBlock -> imageProperties.put(CAPTION, (String) figureBlock.getAttribute(CAPTION)));
        }

        this.context.getBlockNoteState().endBlock();
    }
}

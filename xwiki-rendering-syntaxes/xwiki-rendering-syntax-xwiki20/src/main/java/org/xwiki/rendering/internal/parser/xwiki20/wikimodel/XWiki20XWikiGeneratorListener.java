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
package org.xwiki.rendering.internal.parser.xwiki20.wikimodel;

import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.WrappingListener;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * WikiModel listener bridge for the XWiki Syntax 2.0.
 *
 * @version $Id$
 * @since 14.10.13
 * @since 15.5RC1
 */
public class XWiki20XWikiGeneratorListener extends DefaultXWikiGeneratorListener
{
    private static final String CLASS_PARAMETER = "class";

    private static final String IMAGE_CLASS = "image";

    private static final String IMAGE_LABEL_PARAMETER = "data-xwiki-image-label";

    private final StreamParser imageLabelParser;

    /**
     * @param parser the parser to use to parse link labels
     * @param listener the XWiki listener to which to forward WikiModel events
     * @param linkReferenceParser the parser to parse link references
     * @param imageReferenceParser the parser to parse image references
     * @param plainRendererFactory used to generate header ids
     * @param idGenerator used to generate header ids
     * @param syntax the syntax of the parsed source
     */
    public XWiki20XWikiGeneratorListener(StreamParser parser, Listener listener,
        ResourceReferenceParser linkReferenceParser,
        ResourceReferenceParser imageReferenceParser,
        PrintRendererFactory plainRendererFactory,
        IdGenerator idGenerator, Syntax syntax)
    {
        super(parser, listener, linkReferenceParser, imageReferenceParser, plainRendererFactory, idGenerator, syntax);
        this.imageLabelParser = parser;
    }

    @Override
    public void onImage(WikiReference reference)
    {
        Map<String, String> parameters = convertParameters(reference.getParameters());

        // Store the label in the parameters so that it can be used in the caption of a figure. It cannot be stored
        // in an instance variable of this object as the parser inside a link is different from the one outside it.
        String label = reference.getLabel();
        if (label != null) {
            if (parameters.isEmpty()) {
                // Empty parameters aren't modifiable.
                parameters = new LinkedHashMap<>();
            }
            parameters.put(IMAGE_LABEL_PARAMETER, label);
        }

        onImage(reference.getLink(), false, parameters);
    }

    @Override
    public void beginParagraph(WikiParameters params)
    {
        // Collect events to see if we have a standalone image.
        pushListener(new QueueListener());
    }

    @Override
    public void endParagraph(WikiParameters params)
    {
        flushFormat();

        QueueListener queue = (QueueListener) getListener();
        popListener();

        Map<String, String> paragraphParameters = convertParameters(params);

        // If the only content of the paragraph is an image with a label, potentially wrapped in a link, convert to a
        // figure with the parsed label as caption.

        QueueListener.Event imageEvent = null;

        if (queue.size() == 1 && queue.getFirst().eventType == EventType.ON_IMAGE) {
            imageEvent = queue.getFirst();
        } else if (isLinkWrappedImage(queue)) {
            imageEvent = queue.get(1);
        }

        if (imageEvent != null && imageEvent.eventParameters[3] instanceof Map<?, ?>
            && ((Map<?, ?>) imageEvent.eventParameters[3]).containsKey(IMAGE_LABEL_PARAMETER))
        {
            Map<?, ?> imageParameters = (Map<?, ?>) imageEvent.eventParameters[3];
            String label = (String) imageParameters.get(IMAGE_LABEL_PARAMETER);

            // Remove the label from the image parameters.
            if (imageParameters.size() == 1) {
                imageEvent.eventParameters[3] = Listener.EMPTY_PARAMETERS;
            } else {
                imageParameters.remove(IMAGE_LABEL_PARAMETER);
            }

            Map<String, String> figureParameters = new LinkedHashMap<>(paragraphParameters);
            figureParameters.merge(CLASS_PARAMETER, IMAGE_CLASS,
                (oldValue, newValue) -> {
                    if (Arrays.asList(StringUtils.split(oldValue)).contains(newValue)) {
                        return oldValue;
                    } else {
                        return oldValue + " " + newValue;
                    }
                });

            // If this should be changed to produce more than just an image directly inside the caption,
            // CaptionedImageParseListenerProvider for xwiki-platform needs to be adapted.
            getListener().beginFigure(figureParameters);
            queue.consumeEvents(getListener());
            getListener().beginFigureCaption(Listener.EMPTY_PARAMETERS);
            try {
                // Render the caption ignoring begin/endDocument events.
                WrappingListener wrapper = new WrappingListener()
                {
                    @Override
                    public void beginDocument(MetaData metadata)
                    {
                        // ignore.
                    }

                    @Override
                    public void endDocument(MetaData metadata)
                    {
                        // ignore.
                    }
                };
                wrapper.setWrappedListener(getListener());
                this.imageLabelParser.parse(new StringReader(label), wrapper, getIdGenerator());
            } catch (ParseException e) {
                // TODO what should we do here ?
            }
            getListener().endFigureCaption(Listener.EMPTY_PARAMETERS);
            getListener().endFigure(figureParameters);
        } else {
            getListener().beginParagraph(paragraphParameters);
            queue.consumeEvents(getListener());
            getListener().endParagraph(paragraphParameters);
        }
    }

    private static boolean isLinkWrappedImage(QueueListener queue)
    {
        return queue.size() == 3 && queue.getFirst().eventType == EventType.BEGIN_LINK
            && queue.get(1).eventType == EventType.ON_IMAGE && queue.getLast().eventType == EventType.END_LINK;
    }
}

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
package org.xwiki.rendering.internal.parser.xhtml.wikimodel;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.listener.InlineFilterListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.IdGenerator;
import org.xwiki.rendering.wikimodel.WikiFormat;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.WikiReference;

/**
 * WikiModel listener bridge for the XHTML Syntax.
 *
 * @version $Id$
 * @since 2.5RC1
 */
public class XHTMLXWikiGeneratorListener extends DefaultXWikiGeneratorListener
{
    /**
     * Defines the class to use when an element represents a MetaData block.
     *
     * @since 10.9
     */
    public static final String METADATA_CONTAINER_CLASS = "xwiki-metadata-container";

    /**
     * Defines the prefix to use for attribute which contain a metadata.
     *
     * @since 10.9
     */
    public static final String METADATA_ATTRIBUTE_PREFIX = "data-xwiki-";

    private static final String CLASS_ATTRIBUTE = "class";

    /**
     * @param parser the parser to use to parse link labels
     * @param listener the XWiki listener to which to forward WikiModel events
     * @param linkReferenceParser the parser to parse link references
     * @param imageReferenceParser the parser to parse image references
     * @param plainRendererFactory used to generate header ids
     * @param idGenerator used to generate header ids
     * @param syntax the syntax of the parsed source
     * @since 3.0M3
     */
    public XHTMLXWikiGeneratorListener(StreamParser parser, Listener listener,
        ResourceReferenceParser linkReferenceParser, ResourceReferenceParser imageReferenceParser,
        PrintRendererFactory plainRendererFactory, IdGenerator idGenerator, Syntax syntax)
    {
        super(parser, listener, linkReferenceParser, imageReferenceParser, plainRendererFactory, idGenerator, syntax);
    }

    @Override
    public void onReference(WikiReference reference)
    {
        // We only support XWikiWikiReference as the XHTML parser never passes anything else to onReference.

        if (!(reference instanceof XWikiWikiReference)) {
            throw new IllegalArgumentException("Expected XWikiWikiReference but got another type!");
        }

        XWikiWikiReference xwikiReference = (XWikiWikiReference) reference;
        ResourceReference resourceReference = xwikiReference.getReference();
        boolean isFreeStanding = xwikiReference.isFreeStanding();
        Block labelXDOM = xwikiReference.getLabelXDOM();

        flushFormat();

        // Consider query string and anchor as ResourceReference parameters and the rest as generic parameters
        Pair<Map<String, String>, Map<String, String>> parameters =
            convertAndSeparateParameters(reference.getParameters());

        resourceReference.setParameters(parameters.getLeft());

        getListener().beginLink(resourceReference, isFreeStanding, parameters.getRight());

        if (labelXDOM != null) {
            InlineFilterListener inlineFilterListener = new InlineFilterListener();
            inlineFilterListener.setWrappedListener(getListener());
            labelXDOM.traverse(inlineFilterListener);
        }

        getListener().endLink(resourceReference, isFreeStanding, parameters.getRight());
    }

    @Override
    public void onImage(WikiReference reference)
    {
        // We need to handle 2 cases:
        // - when the passed reference is an instance of XWikiWikiReference, i.e. when a XHTML comment defining a XWiki
        // image has been specified
        // - when the passed reference is not an instance of XWikiWikiReference which will happen if there's no special
        // XHTML comment defining a XWiki image
        if (!(reference instanceof XWikiWikiReference)) {
            super.onImage(reference);
        } else {
            XWikiWikiReference xwikiReference = (XWikiWikiReference) reference;
            ResourceReference resourceReference = xwikiReference.getReference();

            flushFormat();

            onImage(resourceReference, xwikiReference.isFreeStanding(),
                convertParameters(xwikiReference.getParameters()));
        }
    }

    static boolean isMetaDataElement(WikiParameters parameters)
    {
        return parameters.getParameter(CLASS_ATTRIBUTE) != null
            && METADATA_CONTAINER_CLASS.equals(parameters.getParameter(CLASS_ATTRIBUTE).getValue());
    }

    static MetaData createMetaData(WikiParameters parameters)
    {
        MetaData metaData = new MetaData();

        int prefixSize = METADATA_ATTRIBUTE_PREFIX.length();
        for (WikiParameter parameter : parameters) {
            if (parameter.getKey().startsWith(METADATA_ATTRIBUTE_PREFIX)) {
                String metaDataKey = parameter.getKey().substring(prefixSize).toLowerCase();
                metaData.addMetaData(metaDataKey, parameter.getValue());
            }
        }

        return metaData;
    }

    private static WikiParameters cleanParametersFromMetadata(WikiParameters parameters)
    {
        WikiParameters wikiParameters = new WikiParameters();

        for (WikiParameter parameter : parameters) {
            boolean acceptParameter = !(parameter.getKey().startsWith(METADATA_ATTRIBUTE_PREFIX)
                || (
                parameter.getKey().equals(CLASS_ATTRIBUTE) && parameter.getValue().equals(METADATA_CONTAINER_CLASS)
            ));
            if (acceptParameter) {
                wikiParameters = wikiParameters.addParameter(parameter.getKey(), parameter.getValue());
            }
        }

        return wikiParameters;
    }

    @Override
    protected void beginGroup(WikiParameters parameters)
    {
        if (isMetaDataElement(parameters)) {
            MetaData metaData = createMetaData(parameters);
            getListener().beginMetaData(metaData);

            WikiParameters cleanParameters = cleanParametersFromMetadata(parameters);
            if (cleanParameters.getSize() > 0) {
                super.beginGroup(cleanParameters);
            }
        } else {
            super.beginGroup(parameters);
        }
    }

    @Override
    protected void endGroup(WikiParameters parameters)
    {
        if (isMetaDataElement(parameters)) {
            MetaData metaData = createMetaData(parameters);
            getListener().endMetaData(metaData);

            WikiParameters cleanParameters = cleanParametersFromMetadata(parameters);
            if (cleanParameters.getSize() > 0) {
                super.endGroup(cleanParameters);
            }
        } else {
            super.endGroup(parameters);
        }
    }

    @Override
    public void beginFormat(WikiFormat format)
    {
        WikiParameters wikiParameters = new WikiParameters(format.getParams());

        if (isMetaDataElement(wikiParameters)) {
            getListener().beginMetaData(createMetaData(wikiParameters));
            WikiParameters cleanParameters = cleanParametersFromMetadata(wikiParameters);
            if (cleanParameters.getSize() > 0 || !format.getStyles().isEmpty()) {
                WikiFormat newFormat = format;
                if (wikiParameters.getSize() != cleanParameters.getSize()) {
                    newFormat = format.setParameters(cleanParameters.toList());
                }

                super.beginFormat(newFormat);
            }
        } else {
            super.beginFormat(format);
        }
    }

    @Override
    public void endFormat(WikiFormat format)
    {
        WikiParameters wikiParameters = new WikiParameters(format.getParams());

        if (isMetaDataElement(wikiParameters)) {
            getListener().endMetaData(createMetaData(wikiParameters));

            WikiParameters cleanParameters = cleanParametersFromMetadata(wikiParameters);
            if (cleanParameters.getSize() > 0 || !format.getStyles().isEmpty()) {
                WikiFormat newFormat = format;
                if (wikiParameters.getSize() != cleanParameters.getSize()) {
                    newFormat = format.setParameters(cleanParameters.toList());
                }

                super.endFormat(newFormat);
            }
        } else {
            super.endFormat(format);
        }
    }
}

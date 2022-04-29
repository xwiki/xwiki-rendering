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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.xwiki.rendering.internal.parser.wikimodel.DefaultXWikiGeneratorListener;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;
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

    /**
     * URL matching pattern.
     */
    private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z0-9+.-]*://");

    /**
     * Prefix for mailto-links.
     */
    private static final String MAILTO_PREFIX = "mailto:";

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
        // We need to handle 2 cases:
        // - when the passed reference is an instance of XWikiWikiReference, i.e. when a XHTML comment defining a XWiki
        // link has been specified and the XHTML parser has recognized it and thus is passing a typed reference to us.
        // - when the passed reference is not an instance of XWikiWikiReference which will happen if there's no special
        // XHTML comment defining a XWiki link. In this case, we need to figure out what how to consider the passed
        // reference.

        ResourceReference resourceReference;
        boolean isFreeStanding;
        if (!(reference instanceof XWikiWikiReference)) {
            resourceReference = computeResourceReference(reference.getLink());
            isFreeStanding = false;
        } else {
            XWikiWikiReference xwikiReference = (XWikiWikiReference) reference;
            resourceReference = xwikiReference.getReference();
            isFreeStanding = xwikiReference.isFreeStanding();

            flushFormat();
        }

        // Consider query string and anchor as ResourceReference parameters and the rest as generic parameters
        Pair<Map<String, String>, Map<String, String>> parameters =
            convertAndSeparateParameters(reference.getParameters());

        resourceReference.setParameters(parameters.getLeft());
        onReference(resourceReference, reference.getLabel(), isFreeStanding, parameters.getRight(), false);
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

    /**
     * Recognize the passed reference and figure out what type of link it should be:
     * <ul>
     *   <li>UC1: the reference points to a valid URL, we return a reference of type "url",
     *       e.g. {@code http://server/path/reference#anchor}</li>
     *   <li>UC2: the reference is a mailto: link, we return a reference of type "mailto",
     *       e.g., {@code mailto:user@example.com}</li>
     *   <li>UC3: the reference is not a valid URL, we return a reference of type "path",
     *       e.g. {@code path/reference#anchor}</li>
     * </ul>
     *
     * @param rawReference the full reference (e.g. "/some/path/something#other")
     * @return the properly typed {@link ResourceReference} matching the use cases
     */
    private ResourceReference computeResourceReference(String rawReference)
    {
        ResourceReference reference;

        // Do we have a valid URL?
        Matcher matcher = URL_SCHEME_PATTERN.matcher(rawReference);
        if (matcher.lookingAt()) {
            // We have UC1
            reference = new ResourceReference(rawReference, ResourceType.URL);
        } else if (rawReference.startsWith(MAILTO_PREFIX)) {
            // We have UC2
            reference = new ResourceReference(rawReference.substring(MAILTO_PREFIX.length()), ResourceType.MAILTO);
        } else {
            // We have UC3
            reference = new ResourceReference(rawReference, ResourceType.PATH);
        }

        return reference;
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

    @Override
    protected Map<String, String> convertParameters(WikiParameters params)
    {
        return maybeRemoveAttributePrefix(super.convertParameters(params));
    }

    @Override
    protected Pair<Map<String, String>, Map<String, String>> convertAndSeparateParameters(WikiParameters params)
    {
        Pair<Map<String, String>, Map<String, String>> result = super.convertAndSeparateParameters(params);
        Map<String, String> genericParameters = result.getRight();

        if (!genericParameters.isEmpty()) {
            genericParameters = maybeRemoveAttributePrefix(genericParameters);
        }

        return new ImmutablePair<>(result.getLeft(), genericParameters);
    }

    private Map<String, String> maybeRemoveAttributePrefix(Map<String, String> attributes)
    {
        Map<String, String> result;

        if (!attributes.isEmpty()) {
            result = new LinkedHashMap<>();

            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String newKey;
                if (entry.getKey().startsWith(XHTMLWikiPrinter.TRANSLATED_ATTRIBUTE_PREFIX)) {
                    newKey = entry.getKey().substring(XHTMLWikiPrinter.TRANSLATED_ATTRIBUTE_PREFIX.length());
                } else {
                    newKey = entry.getKey();
                }

                result.put(newKey, entry.getValue());
            }
        } else {
            result = attributes;
        }

        return result;
    }
}

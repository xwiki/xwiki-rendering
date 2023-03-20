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
package org.xwiki.rendering.internal.renderer.xhtml.link;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.rendering.renderer.reference.link.URITitleGenerator;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Handle XHTML rendering for links to documents.
 *
 * @version $Id$
 * @since 2.5M2
 */
@Component(hints = {"doc", "page"})
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DocumentXHTMLLinkTypeRenderer extends AbstractXHTMLLinkTypeRenderer implements Initializable
{
    /**
     * The class attribute 'wikilink'.
     */
    private static final String WIKILINK = "wikilink";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXHTMLLinkTypeRenderer.class);

    /**
     * Used to generate the link targeting a local document.
     */
    private WikiModel wikiModel;

    /**
     * Used to generate a link label.
     */
    @Inject
    private LinkLabelGenerator linkLabelGenerator;

    /**
     * Used to generate a link title.
     */
    @Inject
    private URITitleGenerator defaultTitleGenerator;


    @Override
    public void initialize() throws InitializationException
    {
        // Try to find a WikiModel implementation and set it if it can be found. If not it means we're in
        // non wiki mode (i.e. no attachment in wiki documents and no links to documents for example).
        try {
            this.wikiModel = this.componentManager.getInstance(WikiModel.class);
        } catch (ComponentLookupException e) {
            // There's no WikiModel implementation available. this.wikiModel stays null.
        }
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        if (this.wikiModel == null) {
            super.beginLink(reference, freestanding, parameters);
        } else {
            beginInternalLink(reference, freestanding, parameters);
        }
    }

    @Override
    protected String computeLabel(ResourceReference reference)
    {
        return this.linkLabelGenerator.generate(reference);
    }

    /**
     * Implementation for computing a document link title when no title has been specified.
     * Looks for a component implementing URITitleGenerator with a role hint matching the reference scheme.
     * @param reference the reference of the link for which to compute the label
     * @return the computed title
     * @since 15.2RC1
     */
    private String computeCreateTitle(ResourceReference reference)
    {
        URITitleGenerator titleGenerator = this.defaultTitleGenerator;
        String title;
        if (this.componentManager.hasComponent(URITitleGenerator.class, reference.getType().getScheme())) {
            try {
                titleGenerator = this.componentManager.getInstance(URITitleGenerator.class,
                 reference.getType().getScheme());
            } catch (Exception e) {
                LOGGER.error("Error while loading component for generating URI title: [{}]",
                    ExceptionUtils.getRootCauseMessage(e));
                LOGGER.debug("Full stack trace: ", e);
            }
        }
        title = titleGenerator.generateCreateTitle(reference);
        return title;
    }

    @Override
    protected void beginLinkExtraAttributes(ResourceReference reference, Map<String, String> spanAttributes,
        Map<String, String> anchorAttributes)
    {
        if (StringUtils.isEmpty(reference.getReference())) {
            renderAutoLink(reference, spanAttributes, anchorAttributes);
        } else {
            anchorAttributes.put(XHTMLLinkRenderer.HREF, reference.getReference());
        }
    }

    /**
     * Start of an internal link.
     *
     * @param reference the reference to the link
     * @param freestanding if true then the link is a free standing URI directly in the text
     * @param parameters a generic list of parameters. Example: style="background-color: blue"
     */
    private void beginInternalLink(ResourceReference reference, boolean freestanding,
        Map<String, String> parameters)
    {
        Map<String, String> spanAttributes = new LinkedHashMap<String, String>();
        Map<String, String> anchorAttributes = new LinkedHashMap<String, String>();

        // Add all parameters to the A attributes
        anchorAttributes.putAll(parameters);

        if (StringUtils.isEmpty(reference.getReference())) {
            spanAttributes.put(CLASS, WIKILINK);
            renderAutoLink(reference, spanAttributes, anchorAttributes);
        } else if (this.wikiModel.isDocumentAvailable(reference)) {
            spanAttributes.put(CLASS, WIKILINK);
            anchorAttributes.put(XHTMLLinkRenderer.HREF, this.wikiModel.getDocumentViewURL(reference));
        } else {
            // The wiki document doesn't exist
            spanAttributes.put(CLASS, "wikicreatelink");
            spanAttributes.put(TITLE, computeCreateTitle(reference));
            anchorAttributes.put(XHTMLLinkRenderer.HREF, this.wikiModel.getDocumentEditURL(reference));
        }

        getXHTMLWikiPrinter().printXMLStartElement(SPAN, spanAttributes);
        getXHTMLWikiPrinter().printXMLStartElement(XHTMLLinkRenderer.ANCHOR, anchorAttributes);
    }

    /**
     * @param reference the reference to the link
     * @param spanAttributes the span element where to put the class
     * @param aAttributes the anchor element where to put the reference
     */
    private void renderAutoLink(ResourceReference reference, Map<String, String> spanAttributes,
        Map<String, String> aAttributes)
    {
        spanAttributes.put(CLASS, WIKILINK);

        StringBuilder buffer = new StringBuilder();
        String queryString = reference.getParameter(DocumentResourceReference.QUERY_STRING);
        if (queryString != null) {
            buffer.append('?');
            buffer.append(queryString);
        }
        buffer.append('#');
        String anchor = reference.getParameter(DocumentResourceReference.ANCHOR);
        if (anchor != null) {
            buffer.append(anchor);
        }

        aAttributes.put(XHTMLLinkRenderer.HREF, buffer.toString());
    }

    @Override
    protected boolean isExternalLink(ResourceReference reference)
    {
        return false;
    }
}

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
package org.xwiki.rendering.internal.renderer.html5.link;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.internal.renderer.html5.link.HTML5LinkRenderer;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.reference.link.LinkLabelGenerator;
import org.xwiki.rendering.wiki.WikiModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handle XHTML rendering for links to documents.
 *
 * @version $Id$
 * @since 4.4M1
 */
@Component
@Named("doc")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DocumentHTML5LinkTypeRenderer extends AbstractHTML5LinkTypeRenderer implements Initializable
{
    /**
     * The class attribute 'wikilink'.
     */
    private static final String WIKILINK = "wikilink";

    /**
     * Used to generate the link targeting a local document.
     */
    private WikiModel wikiModel;

    /**
     * Used to generate a link label.
     */
    @Inject
    private LinkLabelGenerator linkLabelGenerator;

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
    public void beginLink(ResourceReference reference, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        if (this.wikiModel == null) {
            super.beginLink(reference, isFreeStandingURI, parameters);
        } else {
            beginInternalLink(reference, isFreeStandingURI, parameters);
        }
    }

    @Override
    protected String computeLabel(ResourceReference reference)
    {
        return this.linkLabelGenerator.generate(reference);
    }

    @Override
    protected void beginLinkExtraAttributes(ResourceReference reference, Map<String, String> spanAttributes,
        Map<String, String> anchorAttributes)
    {
        if (StringUtils.isEmpty(reference.getReference())) {
            renderAutoLink(reference, spanAttributes, anchorAttributes);
        } else {
            anchorAttributes.put(HTML5LinkRenderer.HREF, reference.getReference());
        }
    }

    /**
     * Start of an internal link.
     *
     * @param reference the reference to the link
     * @param isFreeStandingURI if true then the link is a free standing URI directly in the text
     * @param parameters a generic list of parameters. Example: style="background-color: blue"
     */
    private void beginInternalLink(ResourceReference reference, boolean isFreeStandingURI,
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
            anchorAttributes.put(HTML5LinkRenderer.HREF, this.wikiModel.getDocumentViewURL(reference));
        } else {
            // The wiki document doesn't exist
            spanAttributes.put(CLASS, "wikicreatelink");
            anchorAttributes.put(HTML5LinkRenderer.HREF, this.wikiModel.getDocumentEditURL(reference));
        }

        getXHTMLWikiPrinter().printXMLStartElement(SPAN, spanAttributes);
        getXHTMLWikiPrinter().printXMLStartElement(HTML5LinkRenderer.ANCHOR, anchorAttributes);
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

        aAttributes.put(org.xwiki.rendering.internal.renderer.html5.link.HTML5LinkRenderer.HREF, buffer.toString());
    }

    @Override
    protected boolean isExternalLink(ResourceReference reference)
    {
        return false;
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;
import org.xwiki.rendering.renderer.reference.link.URILabelGenerator;

/**
 * Common code for XHTML Link Type Renderer implementations.
 *
 * @version $Id$
 * @since 2.5M2
 */
public abstract class AbstractXHTMLLinkTypeRenderer implements XHTMLLinkTypeRenderer
{
    /**
     * The XHTML element <code>class</code> parameter.
     */
    protected static final String CLASS = "class";

    /**
     * The name of the XHTML format element.
     */
    protected static final String SPAN = "span";

    private static final String NOOPENER = "noopener";

    private static final String NOREFERRER = "noreferrer";

    private static final String REL = "rel";

    /**
     * Used to look for {@link org.xwiki.rendering.renderer.reference.link.URILabelGenerator} component implementations
     * when computing labels.
     */
    @Inject
    protected ComponentManager componentManager;

    /**
     * The XHTML printer to use to output links as XHTML.
     */
    private XHTMLWikiPrinter xhtmlPrinter;

    /**
     * @see #setHasLabel(boolean)
     */
    private boolean labelExist;

    /**
     * @return See {@link #setHasLabel(boolean)}
     */
    protected boolean hasLabel()
    {
        return this.labelExist;
    }

    @Override
    public void setHasLabel(boolean hasLabel)
    {
        this.labelExist = hasLabel;
    }

    @Override
    public void setXHTMLWikiPrinter(XHTMLWikiPrinter printer)
    {
        this.xhtmlPrinter = printer;
    }

    @Override
    public XHTMLWikiPrinter getXHTMLWikiPrinter()
    {
        return this.xhtmlPrinter;
    }

    /**
     * Hook called when rendering the beginning of a link to allow implementation classes to augment the passed span and
     * anchor attributes as they see fit.
     *
     * @param reference the reference of the link being rendered
     * @param spanAttributes the HTML attributes for the SPAN HTML element added around the ANCHOR HTML element
     * @param anchorAttributes the HTML attributes for the ANCHOR element
     */
    protected abstract void beginLinkExtraAttributes(ResourceReference reference, Map<String, String> spanAttributes,
        Map<String, String> anchorAttributes);

    /**
     * Default implementation for computing a link label when no label has been specified. Can be overwritten by
     * implementations to provide a different algorithm.
     *
     * @param reference the reference of the link for which to compute the label
     * @return the computed label
     */
    protected String computeLabel(ResourceReference reference)
    {
        // Look for a component implementing URILabelGenerator with a role hint matching the link scheme.
        // If not found then use the full reference as the label.
        // If there's no scheme separator then use the full reference as the label. Note that this can happen
        // when we're not in wiki mode (since all links are considered URIs when not in wiki mode).
        String label;
        try {
            URILabelGenerator uriLabelGenerator =
                this.componentManager.getInstance(URILabelGenerator.class, reference.getType().getScheme());
            label = uriLabelGenerator.generateLabel(reference);
        } catch (ComponentLookupException e) {
            label = reference.getReference();
        }
        return label;
    }

    @Override
    public void beginLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        Map<String, String> spanAttributes = new LinkedHashMap<String, String>();
        Map<String, String> anchorAttributes = new LinkedHashMap<String, String>();

        // Add all parameters to the A attributes
        anchorAttributes.putAll(parameters);

        if (isExternalLink(reference)) {
            spanAttributes.put(CLASS, "wikiexternallink");
            handleTargetAttribute(anchorAttributes);
        } else {
            spanAttributes.put(CLASS, "wikiinternallink");
        }
        if (freestanding) {
            anchorAttributes.put(CLASS, addAttributeValue(anchorAttributes.get(CLASS), "wikimodel-freestanding"));
        }

        beginLinkExtraAttributes(reference, spanAttributes, anchorAttributes);

        getXHTMLWikiPrinter().printXMLStartElement(SPAN, spanAttributes);
        getXHTMLWikiPrinter().printXMLStartElement(XHTMLLinkRenderer.ANCHOR, anchorAttributes);
    }

    /**
     * When a link is open in an other window or in an other frame, the loaded page has some restricted access to the
     * parent window. Among other things, it has the ability to redirect it to an other page, which can lead to
     * dangerous phishing attacks.
     *
     * See: https://mathiasbynens.github.io/rel-noopener/ or https://dev.to/phishing or
     * https://jira.xwiki.org/browse/XRENDERING-462
     *
     * To avoid this vulnerability, we automatically add the "noopener" value to the "rel" attribute of the anchor.
     * But because Firefox does not handle it, we also need to add the "noreferer" value.
     *
     * @param anchorAttributes the anchor attributes (that going to be changed)
     * @since 7.4.5
     * @since 8.2.2
     * @since 8.3M2
     */
    private void handleTargetAttribute(Map<String, String> anchorAttributes)
    {
        String target = anchorAttributes.get("target");

        // Target can have these values:
        //
        // "_blank" which opens the link in a new window
        // "_self" which opens the link in the same window (default)
        // "_top" and "_parent" which control the top or the parent window of some frame
        // "frame-name" (it could be anything) which opens the link in the frame called "frame-name".
        //
        // "_self", "_top" and "_parent" are the only safe values. So we need to handle any other value...
        if (StringUtils.isNotBlank(target)
                && !"_self".equals(target) && !"_parent".equals(target) && !"_top".equals(target)) {
            List<String> relAttributes = new ArrayList<>();

            // Parse the current values
            String relAttribute = anchorAttributes.get(REL);
            if (relAttribute != null) {
                relAttributes.addAll(Arrays.asList(relAttribute.split(" ")));
            }

            // Add the "noopener" attribute
            if (!relAttributes.contains(NOOPENER)) {
                relAttributes.add(NOOPENER);
            }

            // Add the "noreferrer" attribute
            if (!relAttributes.contains(NOREFERRER)) {
                relAttributes.add(NOREFERRER);
            }

            // Serialize the attributes
            if (!relAttributes.isEmpty()) {
                anchorAttributes.put(REL, String.join(" ", relAttributes));
            }
        }
    }

    @Override
    public void endLink(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        // If there was no link content then generate it based on the passed reference
        if (!hasLabel()) {
            getXHTMLWikiPrinter().printXMLStartElement(SPAN, new String[][] { { CLASS, "wikigeneratedlinkcontent" } });
            getXHTMLWikiPrinter().printXML(computeLabel(reference));
            getXHTMLWikiPrinter().printXMLEndElement(SPAN);
        }

        getXHTMLWikiPrinter().printXMLEndElement(XHTMLLinkRenderer.ANCHOR);
        getXHTMLWikiPrinter().printXMLEndElement(SPAN);
    }

    /**
     * Add an attribute value to an existing attribute. This is useful for example for adding a value to an HTML CLASS
     * attribute.
     *
     * @param currentValue the current value of the attribute (can be null)
     * @param valueToAdd the value to add
     * @return the current value augmented by the value to add
     */
    private String addAttributeValue(String currentValue, String valueToAdd)
    {
        String newValue;
        if (currentValue == null || currentValue.length() == 0) {
            newValue = "";
        } else {
            newValue = currentValue + " ";
        }
        return newValue + valueToAdd;
    }

    /**
     * Check if this link is internal or external to the application or rendered document. For example, when running the
     * rendering engine inside a wiki, links to other wiki documents are considered to be internal, while links to HTTP
     * URLs are considered external. Another example, when rendering standalone documents, references to other parts of
     * the document are internal, while URLs are external references. Subclasses should override this method to use
     * their own decision process.
     *
     * @param reference the reference used by this link, in case the status of the link depends on the particular
     *            resource being referenced
     * @return {@code true} if the referenced resource is external to the containing application or the rendered
     *         document, or if the notion of "internal references" doesn't even make sense, {@code false} otherwise
     */
    protected boolean isExternalLink(ResourceReference reference)
    {
        return true;
    }
}

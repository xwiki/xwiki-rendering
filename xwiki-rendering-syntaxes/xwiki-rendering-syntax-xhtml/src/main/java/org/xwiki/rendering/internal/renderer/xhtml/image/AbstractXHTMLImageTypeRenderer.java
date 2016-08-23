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
package org.xwiki.rendering.internal.renderer.xhtml.image;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;
import org.xwiki.rendering.renderer.reference.link.URILabelGenerator;

/**
 * Common code for XHTML Image Type Renderer implementations.
 *
 * @version $Id$
 * @since 5.4RC1
 */
public abstract class AbstractXHTMLImageTypeRenderer implements XHTMLImageTypeRenderer
{
    /**
     * The XHTML element <code>class</code> attribute.
     */
    protected static final String CLASS = "class";

    /**
     * The name of the XHTML format element.
     */
    protected static final String SPAN = "span";

    /**
     * The name of the XHTML teletype element.
     */
    protected static final String TT = "tt";

    @Inject
    protected ComponentManager componentManager;

    /**
     * @see #setXHTMLWikiPrinter(XHTMLWikiPrinter)
     */
    private XHTMLWikiPrinter xhtmlPrinter;

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
     * {@inheritDoc}
     *
     * @see XHTMLImageRenderer#onImage(org.xwiki.rendering.listener.reference.ResourceReference , boolean,
     *      java.util.Map)
     * @since 2.5RC1
     */
    @Override
    public void onImage(ResourceReference reference, boolean freestanding, Map<String, String> parameters)
    {
        Map<String, String> attributes = new LinkedHashMap<String, String>();

        try {
            // First we need to compute the image SRC attribute value.
            String imageSrcAttributeValue = getImageSrcAttributeValue(reference, parameters);

            // Then add it as an attribute of the IMG element.
            attributes.put(XHTMLImageRenderer.SRC, imageSrcAttributeValue);

            // Add the class if we're on a freestanding uri
            if (freestanding) {
                attributes.put(CLASS, "wikimodel-freestanding");
            }

            // Add the other parameters as attributes
            attributes.putAll(parameters);

            // If no ALT attribute has been specified, add it since the XHTML specifications makes it mandatory.
            if (!parameters.containsKey(XHTMLImageRenderer.ALTERNATE)) {
                attributes.put(XHTMLImageRenderer.ALTERNATE, computeAltAttributeValue(reference));
            }

            // And generate the XHTML IMG element.
            getXHTMLWikiPrinter().printXMLElement(XHTMLImageRenderer.IMG, attributes);
        } catch (Throwable e) {
            // Error title
            getXHTMLWikiPrinter().printXMLStartElement(SPAN, new String[][] { { CLASS, "xwikirenderingerror" } });
            getXHTMLWikiPrinter().printXML(e.getMessage());
            getXHTMLWikiPrinter().printXMLEndElement(SPAN);

            // Error details
            getXHTMLWikiPrinter().printXMLStartElement(SPAN,
                new String[][] { { CLASS, "xwikirenderingerrordescription hidden" } });
            getXHTMLWikiPrinter().printXMLStartElement(TT, new String[][] { { CLASS, "wikimodel-verbatim" } });
            getXHTMLWikiPrinter().printXML(ExceptionUtils.getStackTrace(e));
            getXHTMLWikiPrinter().printXMLEndElement(TT);
            getXHTMLWikiPrinter().printXMLEndElement(SPAN);
        }
    }

    protected abstract String getImageSrcAttributeValue(ResourceReference reference, Map<String, String> parameters);

    private String computeAltAttributeValue(ResourceReference reference)
    {
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
}

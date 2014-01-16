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

import java.util.Map;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Handle XHTML rendering for images located in attachments.
 *
 * @version $Id$
 * @since 5.4RC1
 */
@Component
@Named("attach")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class AttachmentXHTMLImageTypeRenderer extends AbstractXHTMLImageTypeRenderer implements Initializable
{
    /**
     * Use to resolve local image URL when the image is attached to a document.
     */
    private WikiModel wikiModel;

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
    protected String getImageSrcAttributeValue(ResourceReference reference, Map<String, String> parameters)
    {
        String imageSrcAttributeValue;
        if (this.wikiModel != null) {
            imageSrcAttributeValue = this.wikiModel.getImageURL(reference, parameters);
        } else {
            imageSrcAttributeValue = String.format("%s:%s", reference.getType().getScheme(), reference.getReference());
        }
        return imageSrcAttributeValue;
    }
}

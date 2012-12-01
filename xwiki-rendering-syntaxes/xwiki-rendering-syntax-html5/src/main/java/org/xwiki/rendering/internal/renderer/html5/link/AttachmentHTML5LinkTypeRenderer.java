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

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.rendering.internal.renderer.xhtml.link.HTML5LinkRenderer;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.renderer.reference.ResourceReferenceTypeSerializer;
import org.xwiki.rendering.wiki.WikiModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * Handle XHTML rendering for links to attachments.
 *
 * @version $Id$
 * @since 2.5M2
 */
@Component
@Named("attach")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class AttachmentHTML5LinkTypeRenderer extends AbstractHTML5LinkTypeRenderer implements Initializable
{
    /**
     * Used to serialize the attachment link to XWiki Syntax 2.0 when we're not inside a wiki.
     * We choose the XWiki Syntax 2.0 arbitrarily. Normally the user should never use a link to an attachment when
     * not inside a wiki. 
     */
    @Inject
    @Named("xwiki/2.0")
    private ResourceReferenceTypeSerializer defaultResourceReferenceTypeSerializer;

    /**
     * Used to generate the link targeting a local document.
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
    protected void beginLinkExtraAttributes(ResourceReference reference, Map<String, String> spanAttributes,
        Map<String, String> anchorAttributes)
    {
        if (this.wikiModel != null) {
            anchorAttributes.put(HTML5LinkRenderer.HREF, this.wikiModel.getLinkURL(reference));
            spanAttributes.put(CLASS, "wikiattachmentlink");
        } else {
            anchorAttributes.put(org.xwiki.rendering.internal.renderer.xhtml.link.HTML5LinkRenderer.HREF, this.defaultResourceReferenceTypeSerializer.serialize(
                reference));
            spanAttributes.put(CLASS, "wikiattachmentlink wikiexternallink");
        }
    }

    @Override
    protected boolean isExternalLink(ResourceReference reference)
    {
        return false;
    }
}

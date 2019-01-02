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
package org.xwiki.rendering.test;

import java.util.Map;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Mock WikiModel implementation for integration tests.
 *
 * @version $Id$
 * @since 2.0M1
 */
public class MockWikiModel implements WikiModel
{
    /**
     * Question Mark symbol.
     */
    private static final String QM = "?";

    /**
     * Create and return a descriptor for this component.
     *
     * @return the descriptor of the component.
     */
    public static ComponentDescriptor<WikiModel> getComponentDescriptor()
    {
        DefaultComponentDescriptor<WikiModel> componentDescriptor = new DefaultComponentDescriptor<WikiModel>();

        componentDescriptor.setRoleType(WikiModel.class);
        componentDescriptor.setImplementation(MockWikiModel.class);

        return componentDescriptor;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.5RC1
     */
    @Override
    public String getLinkURL(ResourceReference linkReference)
    {
        String queryString = linkReference.getParameter(AttachmentResourceReference.QUERY_STRING);
        return "attachmenturl" + (queryString != null ? QM + queryString : "");
    }

    @Override
    public String getImageURL(ResourceReference imageReference, Map<String, String> parameters)
    {
        String queryString = imageReference.getParameter(AttachmentResourceReference.QUERY_STRING);
        return "imageurl" + (queryString != null ? QM + queryString : "");
    }

    @Override
    public String getDocumentEditURL(ResourceReference documentReference)
    {
        return "editurl";
    }

    @Override
    public String getDocumentViewURL(ResourceReference documentReference)
    {
        String queryString = documentReference.getParameter(DocumentResourceReference.QUERY_STRING);
        String anchor = documentReference.getParameter(DocumentResourceReference.ANCHOR);
        return "viewurl" + (queryString != null ? QM + queryString : "") + (anchor != null ? "#" + anchor : "");
    }

    @Override
    public boolean isDocumentAvailable(ResourceReference documentReference)
    {
        ResourceType resourceType = documentReference.getType();
        String resourceValue = documentReference.getReference();

        if (ResourceType.DOCUMENT.equals(resourceType)) {
            return "Space.ExistingPage".equals(resourceValue);
        } else if (ResourceType.SPACE.equals(resourceType)) {
            return "ExistingSpace".equals(resourceValue);
        } else if (ResourceType.PAGE.equals(resourceType)) {
            return "ExistingPage".equals(resourceValue);
        }

        return false;
    }
}

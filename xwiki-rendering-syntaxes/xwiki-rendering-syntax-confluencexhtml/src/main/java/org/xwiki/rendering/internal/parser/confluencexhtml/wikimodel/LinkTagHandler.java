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
package org.xwiki.rendering.internal.parser.confluencexhtml.wikimodel;

import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler.TagStack.TagContext;

/**
 * Handles links.
 * <p>
 * Example:
 * <p>
 * {@code
 * <ac:link ac:anchor="anchor">
 *   <ri:page ri:content-title="Page" ri:space-key="SPACE" />
 *   <ac:plain-text-link-body><![CDATA[label]] ></ac:plain-text-link-body>
 * </ac:link>
 * <ac:link ac:anchor="anchor">
 *   <ri:attachment ri:filename="file.png">
 *     <ri:page ri:content-title="xhtml" ri:space-key="SPACE" />
 *   </ri:attachment>
 *   <ac:plain-text-link-body><![CDATA[image1234.png]]></ac:plain-text-link-body>
 * </ac:link>
 * <ac:link ac:anchor="anchor">
 *   <ri:space ri:space-key="ds" />
 * </ac:link>
 * <ac:link ac:anchor="anchor">
 *   <ri:user ri:username="admin" />
 * </ac:link>
 * }
 *
 * @version $Id$
 * @since 5.3M2
 */
public class LinkTagHandler extends TagHandler
{
    public LinkTagHandler()
    {
        super(false, false, false);
    }

    @Override
    protected void begin(TagContext context)
    {
        ConfluenceLinkWikiReference link = new ConfluenceLinkWikiReference();

        WikiParameter anchorParameter = context.getParams().getParameter("ac:anchor");

        if (anchorParameter != null) {
            link.setAnchor(anchorParameter.getValue());
        }

        context.getTagStack().pushStackParameter("confluence-container", link);
    }

    @Override
    protected void end(TagContext context)
    {
        ConfluenceLinkWikiReference link =
            (ConfluenceLinkWikiReference) context.getTagStack().popStackParameter("confluence-container");

        context.getScannerContext().onReference(link);
    }
}

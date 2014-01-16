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

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.listener.ImageListener;
import org.xwiki.rendering.renderer.printer.XHTMLWikiPrinter;

/**
 * Renders a type of image (URL, attachment, Data URI, etc) in XHTML. Components implementing this
 * interface implement the rendering logic only for a single image type and must have a role hint value
 * equal to the {@link org.xwiki.rendering.listener.reference.ResourceType} name (eg "url" for URL image, "attach"
 * for attachment image, "data" for Data URI image, etc).
 *
 * Implementations must handle both cases when rendering a link:
 * <ul>
 *   <li>when inside a wiki (ie when an implementation of {@link org.xwiki.rendering.wiki.WikiModel} is provided.</li>
 *   <li>when outside of a wiki. In this case image attachments are ignored and rendered as is as direct SRC values.
 *   In other words only external or self-contained (as with Data URI) images are meaningful.</li>
 * </ul>
 *
 * @version $Id$
 * @since 5.4RC1
 */
@Role
public interface XHTMLImageTypeRenderer extends ImageListener
{
    /**
     * @param printer the XHTML printer to use to output links as XHTML
     */
    void setXHTMLWikiPrinter(XHTMLWikiPrinter printer);

    /**
     * @return the XHTML printer to use to output links as XHTML
     */
    XHTMLWikiPrinter getXHTMLWikiPrinter();
}

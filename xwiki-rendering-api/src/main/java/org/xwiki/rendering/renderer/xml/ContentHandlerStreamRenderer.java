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
package org.xwiki.rendering.renderer.xml;

import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.renderer.Renderer;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Convert rendering events into SAX events.
 *
 * @version $Id$
 * @since 3.3M1
 */
@Role
public interface ContentHandlerStreamRenderer extends Renderer
{
    /**
     * @return the syntax the parser is implementing
     */
    Syntax getSyntax();

    /**
     * @return the object to send SAX events to
     */
    ContentHandler getContentHandler();

    /**
     * @param contentHandler the object to send SAX events to
     */
    void setContentHandler(ContentHandler contentHandler);
}

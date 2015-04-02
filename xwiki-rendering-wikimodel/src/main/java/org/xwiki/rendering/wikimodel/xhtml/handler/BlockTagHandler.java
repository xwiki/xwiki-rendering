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

package org.xwiki.rendering.wikimodel.xhtml.handler;

import org.xwiki.rendering.wikimodel.xhtml.impl.XhtmlHandler;

/**
 * @version $Id$
 * @since 7.0RC1, 6.4.4
 */
public class BlockTagHandler extends TagHandler
{
    private String documentClass = "wikimodel-document";

    public BlockTagHandler()
    {
        super(true, false, true);
    }

    public BlockTagHandler(String documentClass)
    {
        super(true, false, true);
        this.documentClass = documentClass;
    }

    @Override
    public boolean isBlockHandler(XhtmlHandler.TagStack.TagContext context)
    {
        return false;
    }

    public String getDocumentClass()
    {
        return documentClass;
    }

    @Override
    protected void begin(XhtmlHandler.TagStack.TagContext context)
    {
        beginDocument(context, context.getParams());
    }

    @Override
    protected void end(XhtmlHandler.TagStack.TagContext context)
    {
        endDocument(context);
    }
}

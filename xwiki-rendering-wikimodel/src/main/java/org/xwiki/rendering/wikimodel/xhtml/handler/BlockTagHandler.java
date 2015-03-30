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

import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * @version $Id$
 * @since 7.0RC1
 */
public class BlockTagHandler extends TagHandler
{
    public BlockTagHandler()
    {
        super(true, false, true);
    }

    @Override
    public boolean isBlockHandler(TagContext context)
    {
        return false;
    }

    /**
     * @return the class used to indicate the division block is an embedded
     *         document. Note that use a method instead of a static private
     *         String field so that user code can override the class name.
     */
    protected String getDocumentClass()
    {
        return "wikimodel-document";
    }

    @Override
    protected void begin(TagContext context)
    {
        beginDocument(context, context.getParams());
    }

    @Override
    protected void end(TagContext context)
    {
        endDocument(context);
    }
}

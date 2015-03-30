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
 * @since 4.0M1
 */
public class ListTagHandler extends TagHandler
{
    public ListTagHandler()
    {
        super(false);
    }

    @Override
    public boolean isBlockHandler(TagContext context)
    {
        TagContext parent = context.getParent();
        // A new list is considered a block element only if the parent is not a
        // list item since nested lists
        // are not new block elements
        return !(parent.isTag("li") || parent.isTag("dd") || parent.isTag("dt"));
    }

    @Override
    protected void begin(TagContext context)
    {
        sendEmptyLines(context);
        // We only send a new list event if we're not already inside a list.
        context.getScannerContext().beginList(context.getParams());
    }

    @Override
    protected void end(TagContext context)
    {
        // We only need to close the list if we're on the last list item.
        // Note that we need to close the list explicitely and not wait for the
        // next element to close it
        // since the next element could be an implicit paragraph.
        // For example: <html><ul><li>item</li></ul>a</html>
        if (context.getTagStack().isEndOfList()) {
            context.getScannerContext().endList();
        }
    }
}

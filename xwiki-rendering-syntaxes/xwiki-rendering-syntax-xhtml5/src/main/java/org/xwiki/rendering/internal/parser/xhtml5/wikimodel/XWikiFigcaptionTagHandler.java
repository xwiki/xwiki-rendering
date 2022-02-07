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
package org.xwiki.rendering.internal.parser.xhtml5.wikimodel;

import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiWikiModelHandler;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * Handle the XHTML5 &lt;figcaption&gt; tag.
 *
 * @since 14.1RC1
 * @version $Id$
 */
public class XWikiFigcaptionTagHandler extends TagHandler implements XWikiWikiModelHandler
{
    /**
     * Create a new figcaption-tag handler.
     */
    public XWikiFigcaptionTagHandler()
    {
        super(true);
    }

    @Override
    public boolean isBlockHandler(TagContext context)
    {
        return true;
    }

    @Override
    protected void begin(TagContext context)
    {
        sendEmptyLines(context);

        context.getScannerContext().beginFigureCaption(context.getParams());

        // Stack context parameters since we enter in a new document
        context.getTagStack().pushStackParameters();

        context.getTagStack().unsetInsideBlockElement();
    }

    @Override
    protected void end(TagContext context)
    {
        context.getTagStack().popStackParameters();

        sendEmptyLines(context);

        context.getScannerContext().endFigureCaption();
    }
}

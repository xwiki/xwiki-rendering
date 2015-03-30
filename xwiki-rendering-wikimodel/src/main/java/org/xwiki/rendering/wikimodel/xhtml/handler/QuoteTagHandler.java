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
public class QuoteTagHandler extends TagHandler
{
    public QuoteTagHandler()
    {
        super(true);
    }

    @Override
    public boolean isBlockHandler(TagContext context)
    {
        // A new blockquote is considered a block element only if the parent is
        // not a blockquote item since blockquotes
        // are not new block elements
        return !(context.getParent().isTag("blockquote"));
    }

    @Override
    protected void begin(TagContext context)
    {
        int quoteDepth = context.getTagStack().getQuoteDepth();
        if (quoteDepth == 0) {
            context.getScannerContext().beginQuot(context.getParams());
        }
        quoteDepth++;
        context.getScannerContext().beginQuotLine(quoteDepth);
        context.getTagStack().setQuoteDepth(quoteDepth);
    }

    @Override
    protected void end(TagContext context)
    {
        int quoteDepth = context.getTagStack().getQuoteDepth();
        quoteDepth--;
        if (quoteDepth < 0) {
            quoteDepth = 0;
        }
        context.getScannerContext().endQuotLine();
        if (quoteDepth == 0) {
            context.getScannerContext().endQuot();
        }
        context.getTagStack().setQuoteDepth(quoteDepth);
    }
}

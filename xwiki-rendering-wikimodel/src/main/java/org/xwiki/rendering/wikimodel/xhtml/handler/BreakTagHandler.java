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

import java.util.Iterator;

import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class BreakTagHandler extends TagHandler
{
    public BreakTagHandler()
    {
        super(false);
    }

    @Override
    protected void begin(TagContext context)
    {
        // If we're inside a quotation line then a BR must close the current
        // quotation line and start a new quotation line.
        int quoteDepth = context.getTagStack().getQuoteDepth();
        if (quoteDepth > 0) {
            context.getScannerContext().beginQuotLine(quoteDepth);

            // duplicate span on several quotation lines
            Iterator it = context.getTagStack().getStackParameterIterator(AbstractFormatTagHandler.FORMATPARAMETERS);
            if (it != null) {
                while (it.hasNext()) {
                    WikiParameters spanParameters = (WikiParameters) it.next();
                    context.getScannerContext().beginFormat(spanParameters);
                }
            }
        } else {
            context.getScannerContext().onNewLine();
        }
    }
}

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

import java.util.Arrays;

import org.xwiki.rendering.wikimodel.IWemConstants;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.impl.WikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class TeletypeTagHandler extends AbstractFormatTagHandler
{
    // There are 2 possible output for <tt>:
    // * If there a class="wikimodel-verbatim" specified then we emit a
    // onVerbatimInline() event
    // * If there no class or a class with another value then we emit a
    // Monospace Format event.

    public TeletypeTagHandler()
    {
        super(IWemConstants.MONO);
    }

    @Override
    protected void begin(TagContext context)
    {
        WikiParameter param = context.getParams().getParameter("class");
        if ((param != null)
            && Arrays.asList(param.getValue().split(" ")).contains(
            "wikimodel-verbatim"))
        {
            beginVerbatim(context);
        } else {
            super.begin(context);
        }
    }

    @Override
    protected void end(TagContext context)
    {
        WikiParameter param = context.getParams().getParameter("class");
        if ((param != null)
            && Arrays.asList(param.getValue().split(" ")).contains(
            "wikimodel-verbatim"))
        {
            endVerbatim(context);
        } else {
            super.end(context);
        }
    }

    private void beginVerbatim(TagContext context)
    {
        // filter content of the <tt> element
        context.getTagStack().pushScannerContext(
            new WikiScannerContext(new PreserverListener()));
        context.getScannerContext().beginDocument();
    }

    private void endVerbatim(TagContext context)
    {
        context.getScannerContext().endDocument();
        PreserverListener preserverListener = (PreserverListener) context
            .getTagStack().popScannerContext().getfListener();

        context.getScannerContext().onVerbatim(preserverListener.toString(),
            true);
    }
}
